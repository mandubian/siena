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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Query;
import siena.QueryFilter;
import siena.QueryOrder;
import siena.SienaException;
import siena.core.async.PersistenceManagerAsync;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

public class SdbPersistenceManager extends AbstractPersistenceManager {
	
	private static final String[] supportedOperators = { "<", ">", ">=", "<=", "=" };
	private static long ioffset = Math.abs(0l+Integer.MIN_VALUE);

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
			req.setConsistentRead(true);
			GetAttributesResult res = sdb.getAttributes(req);
			if(res.getAttributes().size() == 0){
				throw new SienaException(req.getItemName()+" not found in domain"+req.getDomainName());
			}
				
			SdbMappingUtils.fillModel(req.getItemName(), res, clazz, obj);
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}
	
	@Override
	public int get(Object... models) {
		return get(Arrays.asList(models));
	}

	@Override
	public <T> int get(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
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
	
	@SuppressWarnings("unchecked")
	private <T> List<T> query(Query<T> query, String suffix, String nextToken) {
//		Class<?> clazz = query.getQueriedClass();
//		String domain = getDomainName(clazz);
//		String q = buildQuery(query, "select * from "+domain)+suffix;
//		SelectResponse response = ws.select(q, nextToken);
//		query.setNextOffset(response.nextToken);
//		List<Item> items = response.items;
//		List<T> result = new ArrayList<T>(items.size());
		List<T> result = new ArrayList<T>();
//		for (Item item : items) {
//			try {
//				T object = (T) clazz.newInstance();
//				fillModel(item, object);
//				result.add(object);
//			} catch (Exception e) {
//				throw new SienaException(e);
//			}
//		}
		return result;
	}
	
	private <T> String buildQuery(Query<T> query, String prefix) {
		StringBuilder q = new StringBuilder(prefix);
		
		List<QueryFilter> filters = query.getFilters();
		if(!filters.isEmpty()) {
			/*q.append(" where ");
			
			boolean first = true;
			
			for (QueryFilter filter : filters) {
				if(QueryFilterSimple.class.isAssignableFrom(filter.getClass())){
					QueryFilterSimple qf = (QueryFilterSimple)filter;
					Field f      = qf.field;
					Object value = qf.value;
					String op    = qf.operator;
					
					if(!first) {
						q.append(" and ");
					}
					first = false;
					
					String column = null;
					if(ClassInfo.isId(f)) {
						column = "itemName()";
					} else {
						column = ClassInfo.getColumnNames(f)[0];
					}
					if(value == null && op.equals("=")) {
						q.append(column+" is null");
					} else {
						String s = SdbPersistenceManager.toString(f, value);
						q.append(column+op+SimpleDB.quote(s));
					}
				}
			}*/
			
		}
		
		List<QueryOrder> orders = query.getOrders();
		if(!orders.isEmpty()) {
			QueryOrder last = orders.get(orders.size()-1);
			Field field = last.field;
			
			if(ClassInfo.isId(field)) {
				q.append("order by itemName()");
			} else {
				q.append("order by ");
				q.append(ClassInfo.getColumnNames(field)[0]);
			}
			if(!last.ascending)
				q.append(" desc");
		}
		
		return q.toString();
	}

	@Override
	public <T> List<T> fetch(Query<T> query) {
		return query(query, "", null);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		return query(query, " limit "+limit, null);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		return query(query, " limit "+limit, offset.toString());
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
	public int delete(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Iterable<?> models) {
		// TODO Auto-generated method stub
		return 0;
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
	public <T> int update(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int update(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
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
