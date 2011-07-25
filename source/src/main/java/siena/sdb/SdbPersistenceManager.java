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
import siena.SienaException;
import siena.Util;
import siena.core.async.PersistenceManagerAsync;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

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
	public static final String DB = "SDB";
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
		try {
			for(String domain: doMap.keySet()){
				checkDomain(domain);			
				List<ReplaceableItem> doList = doMap.get(domain);
				sdb.batchPutAttributes(new BatchPutAttributesRequest(domain, doList));			
			}
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
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
				throw new SienaException(req.getItemName()+" not found in domain "+req.getDomainName());
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
		StringBuffer domainBuf = new StringBuffer();
		SelectRequest req = SdbMappingUtils.buildBatchGetQuery(models, prefix, domainBuf);
		req.setConsistentRead(isReadConsistent());
		try {	
			checkDomain(domainBuf.toString());
			SelectResult res = sdb.select(req);
			return SdbMappingUtils.mapSelectResult(res, models);
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
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
		try {
			for(String domain: doMap.keySet()){
				checkDomain(domain);			
				List<ReplaceableItem> doList = doMap.get(domain);
				sdb.batchPutAttributes(new BatchPutAttributesRequest(domain, doList));			
			}
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
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
		try {
			for(String domain: doMap.keySet()){
				checkDomain(domain);			
				List<DeletableItem> doList = doMap.get(domain);
				sdb.batchDeleteAttributes(new BatchDeleteAttributesRequest(domain, doList));			
			}
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
		return nb;
	}
	
	@Override
	public void save(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		Field idField = info.getIdField();
		
		//Entity entity;
		Object idVal = Util.readField(obj, idField);
		// id with null value means insert
		if(idVal == null){
			insert(obj);
		}
		// id with not null value means update
		else{
			update(obj);
		}
	}

	@Override
	public int save(Object... objects) {
		return save(Arrays.asList(objects));
	}

	@Override
	public int save(Iterable<?> objects) {
		List<Object> entities2Insert = new ArrayList<Object>();
		List<Object> entities2Update = new ArrayList<Object>();

		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			
			Object idVal = Util.readField(obj, idField);
			// id with null value means insert
			if(idVal == null){
				entities2Insert.add(obj);
			}
			// id with not null value means update
			else{
				entities2Update.add(obj);
			}
		}
		return insert(entities2Insert) + update(entities2Update);
	}
	
	public <T> T getByKey(Class<T> clazz, Object key) {
		String domain = SdbMappingUtils.getDomainName(clazz, prefix);
		try {
			checkDomain(domain);
			GetAttributesRequest req = SdbMappingUtils.createGetRequestFromKey(domain, clazz, key);
			// sets consistent read to true when reading one single object
			req.setConsistentRead(isReadConsistent());
			GetAttributesResult res = sdb.getAttributes(req);
			if(res.getAttributes().size() == 0){
				throw new SienaException(req.getItemName()+" not found in domain "+req.getDomainName());
			}
				
			T obj = Util.createObjectInstance(clazz);

			SdbMappingUtils.fillModel(req.getItemName(), res, clazz, obj);
			
			return obj;
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}

	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		return getByKeys(clazz, Arrays.asList(keys));
	}
	
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		try {	
			StringBuffer domainBuf = new StringBuffer();

			SelectRequest req = SdbMappingUtils.buildBatchGetQueryByKeys(clazz, keys, prefix, domainBuf);
			checkDomain(domainBuf.toString());
			req.setConsistentRead(isReadConsistent());
			SelectResult res = sdb.select(req);
			return SdbMappingUtils.mapSelectResultToList(res, clazz);
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}

	public <T> int count(Query<T> query) {
		StringBuffer domainBuf = new StringBuffer();
		SelectRequest req = SdbMappingUtils.buildCountQuery(query, prefix, domainBuf);

		try {	
			checkDomain(domainBuf.toString());
			req.setConsistentRead(isReadConsistent());
			SelectResult res = sdb.select(req);
			return SdbMappingUtils.mapSelectResultToCount(res);
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}

	public <T> List<T> fetch(Query<T> query) {
		return doFetchList(query, Integer.MAX_VALUE, 0);
	}
	
	public <T> List<T> fetch(Query<T> query, int limit) {
		return doFetchList(query, limit, 0);
	}

	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		return doFetchList(query, limit, (Integer)offset);
	}

	public <T> List<T> fetchKeys(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;

		return doFetchList(query, Integer.MAX_VALUE, 0);
	}

	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;

		return doFetchList(query, limit, 0);
	}

	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;

		return doFetchList(query, limit, (Integer)offset);
	}
	
	public <T> Iterable<T> iter(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		return doFetchIterable(query, Integer.MAX_VALUE, 0);
	}

	public <T> Iterable<T> iter(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		return doFetchIterable(query, limit, 0);
	}

	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		return doFetchIterable(query, limit, (Integer)offset);
	}

	
	public <T> int delete(Query<T> query) {
		List<T> l = fetchKeys(query);
		
		return delete(l);
	}
	
	private <T> List<T> doFetchList(Query<T> query, int limit, int offset) {
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		if(sdbCtx==null){
			sdbCtx = new QueryOptionSdbContext();
			query.customize(sdbCtx);
		}
		
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);

		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			// no pagination but pageOption active
			if(pag.isActive()){
				// if local limit is set, it overrides the pageOption.pageSize
				if(limit!=Integer.MAX_VALUE){
					sdbCtx.realPageSize = limit;
					// pageOption is passivated to be sure it is not reused
					pag.passivate();
				}
				// using pageOption.pageSize
				else {
					sdbCtx.realPageSize = pag.pageSize;
					// passivates the pageOption in stateless mode not to keep anything between 2 requests
					if(state.isStateless()){
						pag.passivate();
					}						
				}
			}
			else {
				if(limit != Integer.MAX_VALUE){
					sdbCtx.realPageSize = limit;
				}
			}
		}else {
			// paginating so use the pagesize and don't passivate pageOption
			// local limit is not taken into account
			sdbCtx.realPageSize = pag.pageSize;
		}
		
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		// if local offset has been set, uses it
		if(offset!=0){
			off.activate();
			off.offset = offset;
		}
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(sdbCtx.noMoreDataBefore){
			return new ArrayList<T>();
		}
						
		// manages cursor limitations for IN and != operators with offsets
		if(!sdbCtx.isActive()){
			StringBuffer domainBuf = new StringBuffer();
			SelectRequest req = SdbMappingUtils.buildQuery(query, prefix, domainBuf);
			req.setConsistentRead(isReadConsistent());
			checkDomain(domainBuf.toString());
			SelectResult res = sdb.select(req);
			
			// activates the SdbCtx now that it is initialised
			sdbCtx.activate();
			// sets the current cursor (in stateful mode, cursor is always kept for further use)
			if(pag.isPaginating()){
				String token = res.getNextToken();
				if(token!=null){
					sdbCtx.addToken(token);
				}
				
				// if paginating and 0 results then no more data else resets noMoreDataAfter
				if(res.getItems().size()==0){
					sdbCtx.noMoreDataAfter = true;
				} else {
					sdbCtx.noMoreDataAfter = false;
				}
			}else{
				String token = res.getNextToken();
				if(token!=null){
					sdbCtx.addAndMoveToken(token);
				}
			}		
			// cursor not yet created
			switch(fetchType.fetchType){
			case KEYS_ONLY:
				if(off.isActive()){
					return SdbMappingUtils.mapSelectResultToListKeysOnly(res, query.getQueriedClass(), off.offset);
				}else {
					return SdbMappingUtils.mapSelectResultToListKeysOnly(res, query.getQueriedClass());
				}
			case NORMAL:
			default:
				if(off.isActive()){
					return SdbMappingUtils.mapSelectResultToList(res, query.getQueriedClass(), off.offset);
				}else {
					return SdbMappingUtils.mapSelectResultToList(res, query.getQueriedClass());
				}
			}			
		}
		else {
			// we prepare the query each time
			StringBuffer domainBuf = new StringBuffer();
			SelectRequest req = SdbMappingUtils.buildQuery(query, prefix, domainBuf);
			req.setConsistentRead(isReadConsistent());
			checkDomain(domainBuf.toString());
			// we can't use real asynchronous function with cursors
			// so the page is extracted at once and wrapped into a SienaFuture
			String token = sdbCtx.currentToken();
			if(token!=null){
				req.setNextToken(token);
			}
			SelectResult res = sdb.select(req);
									
			// sets the current cursor (in stateful mode, cursor is always kept for further use)
			if(pag.isPaginating()){
				token = res.getNextToken();
				if(token!=null){
					sdbCtx.addToken(token);
				}
				// if paginating and 0 results then no more data else resets noMoreDataAfter
				if(res.getItems().size()==0){
					sdbCtx.noMoreDataAfter = true;
				} else {
					sdbCtx.noMoreDataAfter = false;
				}
			}else{
				token = res.getNextToken();
				if(token!=null){
					sdbCtx.addAndMoveToken(token);
				}
			}
			
			switch(fetchType.fetchType){
			case KEYS_ONLY:
				return SdbMappingUtils.mapSelectResultToListKeysOnly(res, query.getQueriedClass());
			case NORMAL:
			default:
				return SdbMappingUtils.mapSelectResultToList(res, query.getQueriedClass());
			}
		}
	}
	
	private <T> Iterable<T> doFetchIterable(Query<T> query, int limit, int offset) {
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		if(sdbCtx==null){
			sdbCtx = new QueryOptionSdbContext();
			query.customize(sdbCtx);
		}
		
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);

		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			// no pagination but pageOption active
			if(pag.isActive()){
				// if local limit is set, it overrides the pageOption.pageSize
				if(limit!=Integer.MAX_VALUE){
					sdbCtx.realPageSize = limit;
					// pageOption is passivated to be sure it is not reused
					pag.passivate();
				}
				// using pageOption.pageSize
				else {
					sdbCtx.realPageSize = pag.pageSize;
					// passivates the pageOption in stateless mode not to keep anything between 2 requests
					if(state.isStateless()){
						pag.passivate();
					}						
				}
			}
			else {
				if(limit != Integer.MAX_VALUE){
					sdbCtx.realPageSize = limit;
				}
			}
		}else {
			// paginating so use the pagesize and don't passivate pageOption
			// local limit is not taken into account
			sdbCtx.realPageSize = pag.pageSize;
		}
		
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		// if local offset has been set, uses it
		if(offset!=0){
			off.activate();
			off.offset = offset;
		}
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(sdbCtx.noMoreDataBefore){
			return new ArrayList<T>();
		}
						
		// manages cursor limitations for IN and != operators with offsets
		if(!sdbCtx.isActive()){
			StringBuffer domainBuf = new StringBuffer();
			SelectRequest req = SdbMappingUtils.buildQuery(query, prefix, domainBuf);
			req.setConsistentRead(isReadConsistent());
			checkDomain(domainBuf.toString());
			SelectResult res = sdb.select(req);
			
			// activates the SdbCtx now that it is initialised
			sdbCtx.activate();
			// sets the current cursor (in stateful mode, cursor is always kept for further use)
			if(pag.isPaginating()){
				String token = res.getNextToken();
				if(token!=null){
					sdbCtx.addToken(token);
				}
				
				// if paginating and 0 results then no more data else resets noMoreDataAfter
				if(res.getItems().size()==0){
					sdbCtx.noMoreDataAfter = true;
				} else {
					sdbCtx.noMoreDataAfter = false;
				}
			}else{
				String token = res.getNextToken();
				if(token!=null){
					sdbCtx.addAndMoveToken(token);
				}
			}		
			
			return new SdbSienaIterable<T>(res.getItems(), query);
		}
		else {
			// we prepare the query each time
			StringBuffer domainBuf = new StringBuffer();
			SelectRequest req = SdbMappingUtils.buildQuery(query, prefix, domainBuf);
			req.setConsistentRead(isReadConsistent());
			checkDomain(domainBuf.toString());
			// we can't use real asynchronous function with cursors
			// so the page is extracted at once and wrapped into a SienaFuture
			String token = sdbCtx.currentToken();
			if(token!=null){
				req.setNextToken(token);
			}
			SelectResult res = sdb.select(req);
									
			// sets the current cursor (in stateful mode, cursor is always kept for further use)
			if(pag.isPaginating()){
				token = res.getNextToken();
				if(token!=null){
					sdbCtx.addToken(token);
				}
				// if paginating and 0 results then no more data else resets noMoreDataAfter
				if(res.getItems().size()==0){
					sdbCtx.noMoreDataAfter = true;
				} else {
					sdbCtx.noMoreDataAfter = false;
				}
			}else{
				token = res.getNextToken();
				if(token!=null){
					sdbCtx.addAndMoveToken(token);
				}
			}
			
			return new SdbSienaIterable<T>(res.getItems(), query);
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
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return 0;
	}



}
