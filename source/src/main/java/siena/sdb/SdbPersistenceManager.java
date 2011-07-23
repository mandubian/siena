/*
 * Copyright 2009 Alberto Gimeno <gimenete at gmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package siena.sdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Query;
import siena.SienaException;
import siena.core.async.PersistenceManagerAsync;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.BatchDeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeletableItem;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

public class SdbPersistenceManager extends AbstractPersistenceManager {
	
	private static final String[] supportedOperators = { "<", ">", ">=", "<=", "=", "!=", "like", "not like", "in" };

    public final static PmOptionSdbReadConsistency CONSISTENT_READ = new PmOptionSdbReadConsistency(true);
    public final static PmOptionSdbReadConsistency NOT_CONSISTENT_READ = new PmOptionSdbReadConsistency(false);
	
	private AmazonSimpleDB sdb;
	private String prefix;
	private List<String> domains;

	public void init(Properties p) {
		String awsAccessKeyId = p.getProperty("awsAccessKeyId");
		String awsSecretAccessKey = p.getProperty("awsSecretAccessKey");
		if(awsAccessKeyId == null || awsSecretAccessKey == null)
			throw new SienaException("Both awsAccessKeyId and awsSecretAccessKey properties must be set");
		prefix = p.getProperty("prefix");
		if(prefix == null) prefix = "";
		sdb = new AmazonSimpleDBClient(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey));
	}

	public void checkDomain(String domainName) {
		if(domains == null) {
			domains = sdb.listDomains().getDomainNames();
		}
		if(!domains.contains(domainName)) {
			sdb.createDomain(new CreateDomainRequest(domainName));
			domains.add(domainName);
		}
	}
	
	public boolean isReadConsistent() {
		PmOptionSdbReadConsistency opt = (PmOptionSdbReadConsistency)option(CONSISTENT_READ.type);
		if(opt != null) {
			return opt.isConsistentRead;
		}
		return false;
	}
	
	public void insert(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		String domain = SdbMappingUtils.getDomainName(clazz, prefix);
		
		try {
			checkDomain(domain);
			sdb.putAttributes(SdbMappingUtils.createPutRequest(domain, clazz, info, obj));
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}
	
	@Override
	public int insert(Object... objects) {
		return insert(Arrays.asList(objects));
	}

	@Override
	public int insert(Iterable<?> objects) {
		Map<String, List<ReplaceableItem>> doMap = new HashMap<String, List<ReplaceableItem>>(); 
		int nb = 0;
		for(Object obj: objects){
			Class<?> clazz = obj.getClass();
			String domain = SdbMappingUtils.getDomainName(clazz, prefix);
			List<ReplaceableItem> doList = doMap.get(domain); 
			if(doList == null){
				doList = new ArrayList<ReplaceableItem>();
				doMap.put(domain, doList);
			}
			doList.add(SdbMappingUtils.createItem(obj));
			
			nb++;
		}
		
		for(String domain: doMap.keySet()){
			checkDomain(domain);			
			List<ReplaceableItem> doList = doMap.get(domain);
			sdb.batchPutAttributes(new BatchPutAttributesRequest(domain, doList));			
		}
		return nb;
	}
	
	public void get(Object obj) {
		Class<?> clazz = obj.getClass();
		
		String domain = SdbMappingUtils.getDomainName(clazz, prefix);
		try {
			checkDomain(domain);
			GetAttributesRequest req = SdbMappingUtils.createGetRequest(domain, clazz, obj);
			// sets consistent read to true when reading one single object
			req.setConsistentRead(isReadConsistent());
			GetAttributesResult res = sdb.getAttributes(req);
			if(res.getAttributes().size() == 0){
				throw new SienaException(req.getItemName()+" not found in domain"+req.getDomainName());
			}
				
			SdbMappingUtils.fillModel(req.getItemName(), res, clazz, obj);
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}
	
	public int get(Object... models) {
		return get(Arrays.asList(models));
	}

	public <T> int get(Iterable<T> models) {
		SelectRequest req = SdbMappingUtils.buildBatchGetQuery(models, prefix);
		req.setConsistentRead(isReadConsistent());
		SelectResult res = sdb.select(req);
		return SdbMappingUtils.mapSelectResult(res, models);
	}

	
	public void update(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		String domain = SdbMappingUtils.getDomainName(clazz, prefix);
		
		try {
			checkDomain(domain);
			sdb.putAttributes(SdbMappingUtils.createPutRequest(domain, clazz, info, obj));
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}
	
	public <T> int update(Object... models) {
		return update(Arrays.asList(models));
	}

	public <T> int update(Iterable<T> models) {
		Map<String, List<ReplaceableItem>> doMap = new HashMap<String, List<ReplaceableItem>>(); 
		int nb = 0;
		for(Object obj: models){
			Class<?> clazz = obj.getClass();
			String domain = SdbMappingUtils.getDomainName(clazz, prefix);
			List<ReplaceableItem> doList = doMap.get(domain); 
			if(doList == null){
				doList = new ArrayList<ReplaceableItem>();
				doMap.put(domain, doList);
			}
			doList.add(SdbMappingUtils.createItem(obj));
			
			nb++;
		}
		
		for(String domain: doMap.keySet()){
			checkDomain(domain);			
			List<ReplaceableItem> doList = doMap.get(domain);
			sdb.batchPutAttributes(new BatchPutAttributesRequest(domain, doList));			
		}
		return nb;
	}

	
	public void delete(Object obj) {
		Class<?> clazz = obj.getClass();
		
		String domain = SdbMappingUtils.getDomainName(clazz, prefix);
		
		try {
			checkDomain(domain);
			sdb.deleteAttributes(SdbMappingUtils.createDeleteRequest(domain, clazz, obj));
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}

	public int delete(Object... models) {
		return delete(Arrays.asList(models));
	}

	@Override
	public int delete(Iterable<?> models) {
		Map<String, List<DeletableItem>> doMap = new HashMap<String, List<DeletableItem>>(); 
		int nb = 0;
		for(Object obj: models){
			Class<?> clazz = obj.getClass();
			String domain = SdbMappingUtils.getDomainName(clazz, prefix);
			List<DeletableItem> doList = doMap.get(domain); 
			if(doList == null){
				doList = new ArrayList<DeletableItem>();
				doMap.put(domain, doList);
			}
			doList.add(SdbMappingUtils.createDeletableItem(obj));
			
			nb++;
		}
		
		for(String domain: doMap.keySet()){
			checkDomain(domain);			
			List<DeletableItem> doList = doMap.get(domain);
			sdb.batchDeleteAttributes(new BatchDeleteAttributesRequest(domain, doList));			
		}
		return nb;
	}
	
	/* transactions */

	public void beginTransaction(int isolationLevel) {
	}

	public void beginTransaction() {
	}
	
	public void closeConnection() {
	}

	public void commitTransaction() {
	}

	public void rollbackTransaction() {
	}
	
	


	@Override
	public <T> List<T> fetch(Query<T> query) {
		return null;
		//return query(query, "", null);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		return null;
		//return query(query, " limit "+limit, null);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		return null;
		//return query(query, " limit "+limit, offset.toString());
	}

	@Override
	public <T> int count(Query<T> query) {
//		Class<?> clazz = query.getQueriedClass();
//		String domain = getDomainName(clazz);
//		String q = buildQuery(query, "select count(*) from "+domain);
//		SelectResponse response = ws.select(q, null);
//		query.setNextOffset(response.nextToken);
//		return Integer.parseInt(response.items.get(0).attributes.get("Count").get(0));
		return 0;
	}

	@Override
	public <T> int delete(Query<T> query) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void release(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] supportedOperators() {
		return supportedOperators;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <T> void nextPage(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void previousPage(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> PersistenceManagerAsync async() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <T> void paginate(Query<T> query) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public <T> T getByKey(Class<T> clazz, Object key) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void save(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int save(Object... objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int save(Iterable<?> objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		return 0;
	}


}
