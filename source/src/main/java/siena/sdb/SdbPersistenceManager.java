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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Query;
import siena.QueryJoin;
import siena.SienaException;
import siena.Util;
import siena.core.async.PersistenceManagerAsync;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;
import siena.gae.GaeMappingUtils;
import siena.gae.GaeQueryUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class SdbPersistenceManager extends AbstractPersistenceManager {
	public static final String DB = "SDB";
	private static final String[] supportedOperators = { "<", ">", ">=", "<=", "!=", "=", "LIKE", "NOT LIKE", "IN" };

    public final static PmOptionSdbReadConsistency CONSISTENT_READ = new PmOptionSdbReadConsistency(true);
    public final static PmOptionSdbReadConsistency NOT_CONSISTENT_READ = new PmOptionSdbReadConsistency(false);
	
    public final static int MAX_ITEMS_PER_CALL = 25;
    public final static int MAX_ATTR_PER_SELECT = 20;

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
				
				int len = doList.size()> MAX_ITEMS_PER_CALL ? MAX_ITEMS_PER_CALL: doList.size();
				for(int i=0; i < doList.size(); i += len){
					sdb.batchPutAttributes(
							new BatchPutAttributesRequest(
									domain, 
									doList.subList(i, i+len)));			

				}
				
			}
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
		return nb;
	}
	
	public void get(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(obj.getClass());
		
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
			
			// join management
			if(!info.joinFields.isEmpty()){
				mapJoins(obj);
			}
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}
	
	public int get(Object... models) {
		return get(Arrays.asList(models));
	}

	protected <T> int rawGet(Iterable<T> models) {
		StringBuffer domainBuf = new StringBuffer();
		SelectRequest req = SdbMappingUtils.buildBatchGetQuery(models, prefix, domainBuf);
		req.setConsistentRead(isReadConsistent());
		try {	
			checkDomain(domainBuf.toString());
			SelectResult res = sdb.select(req);
			int nb = SdbMappingUtils.mapSelectResult(res, models);
			
			// join management
			// gets class
			Class<?> clazz = null;
			for(T obj: models){
				if(clazz == null){
					clazz = obj.getClass();
					break;
				}
			}
			if(!ClassInfo.getClassInfo(clazz).joinFields.isEmpty()){
				mapJoins(models);
			}
			
			return nb;
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}

	
	public <T> int get(Iterable<T> models) {
		Iterator<T> it = models.iterator();
		
		int total = 0;
		while(it.hasNext()){
			List<T> subList = new ArrayList<T>();
			for(int i=0; i < MAX_ATTR_PER_SELECT && it.hasNext();i++){
				subList.add(it.next());
			}
			if(!subList.isEmpty()){
				total += rawGet(subList);
			}
		}
		
		return total;
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
				
				int len = doList.size()> MAX_ITEMS_PER_CALL ? MAX_ITEMS_PER_CALL: doList.size();
				for(int i=0; i < doList.size(); i += len){
					sdb.batchPutAttributes(
							new BatchPutAttributesRequest(
									domain, 
									doList.subList(i, i+len)));			
				}
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
				int len = doList.size() > MAX_ITEMS_PER_CALL ? MAX_ITEMS_PER_CALL: doList.size();
				for(int i=0; i < doList.size(); i += len){
					sdb.batchDeleteAttributes(
							new BatchDeleteAttributesRequest(
									domain, 
									doList.subList(i, i + len)));			

				}
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
			
			// join management
			if(!ClassInfo.getClassInfo(clazz).joinFields.isEmpty()){
				mapJoins(obj);
			}
			
			return obj;
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}

	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		return getByKeys(clazz, Arrays.asList(keys));
	}
	
	protected <T> List<T> rawGetByKeys(Class<T> clazz, Iterable<?> keys) {
		try {	
			StringBuffer domainBuf = new StringBuffer();

			SelectRequest req = SdbMappingUtils.buildBatchGetQueryByKeys(clazz, keys, prefix, domainBuf);
			checkDomain(domainBuf.toString());
			req.setConsistentRead(isReadConsistent());
			SelectResult res = sdb.select(req);
			List<T> models = new ArrayList<T>(); 
				
			SdbMappingUtils.mapSelectResultToListOrderedFromKeys(res, models, clazz, keys);
			
			// join management
			if(!ClassInfo.getClassInfo(clazz).joinFields.isEmpty()){
				mapJoins(models);
			}
			
			return models;
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
	}
	
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		Iterator<?> it = keys.iterator();
		
		List<T> list = new ArrayList<T>();
		while(it.hasNext()){
			List<Object> subList = new ArrayList<Object>();
			for(int i=0; i < MAX_ATTR_PER_SELECT && it.hasNext();i++){
				subList.add(it.next());
			}
			if(!subList.isEmpty()){
				list.addAll(rawGetByKeys(clazz, subList));
			}
		}
		
		return list;
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
		List<T> models = new ArrayList<T>(); 
		doFetchList(query, Integer.MAX_VALUE, 0, models, 0);
		return models;
	}
	
	public <T> List<T> fetch(Query<T> query, int limit) {
		List<T> models = new ArrayList<T>(); 
		doFetchList(query, limit, 0, models, 0);
		return models;
	}

	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		List<T> models = new ArrayList<T>(); 
		doFetchList(query, limit, (Integer)offset, models, 0);
		return models;
	}

	public <T> List<T> fetchKeys(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;
		List<T> models = new ArrayList<T>(); 
		doFetchList(query, Integer.MAX_VALUE, 0, models, 0);		
		return models;
	}

	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;
		List<T> models = new ArrayList<T>(); 
		doFetchList(query, limit, 0, models, 0);
		return models;
	}

	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;

		List<T> models = new ArrayList<T>(); 
		doFetchList(query, limit, (Integer)offset, models, 0);
		return models;
	}
	
	public <T> Iterable<T> iter(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		return doFetchIterable(query, Integer.MAX_VALUE, 0, false);
	}

	public <T> Iterable<T> iter(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		return doFetchIterable(query, limit, 0, false);
	}

	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		return doFetchIterable(query, limit, (Integer)offset, false);
	}

	
	public <T> int delete(Query<T> query) {
		List<T> l = fetchKeys(query);
		
		return delete(l);
	}
	
	protected <T> void postMapping(Query<T> query){
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);

		// desactivates paging & offset in stateless mode
		if(state.isStateless() && !pag.isPaginating()){
			pag.passivate();
			pag.pageSize = 0;
			sdbCtx.resetAll();
		}
		// offset if not kept as it is never reused as is even in stateful
		// mode. the stateful mode only keeps the realOffset/pagination alive
		off.passivate();
		off.offset = 0;
	}
	
	protected <T> void continueFetchNextToken(Query<T> query, List<T> results, int depth){
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		
		// desactivates offset not to use if fetching more items from next token
		if(state.isStateless()){
			off.passivate();
		}
		
		if(!pag.isActive()){
			if(state.isStateless()){
				// retrieves next token
				if(sdbCtx.nextToken()!=null){
					doFetchList(query, Integer.MAX_VALUE, 0, results, depth+1);
				}
			}else {
				if(sdbCtx.currentToken()!=null){
					// desactivates offset because we don't to go on using offset while going to next tokens
					boolean b = off.isActive();
					off.passivate();
					doFetchList(query, Integer.MAX_VALUE, 0, results, depth+1);
					// reactivate it if it was activated
					if(b) off.activate();
				}
			}
		}
	}
	
	protected <T> void postFetch(Query<T> query, SelectResult res) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		
		if(sdbCtx.realPageSize == 0){
			sdbCtx.realPageSize = res.getItems().size();
		}
		
		String token = null;
		// sets the current cursor (in stateful mode, cursor is always kept for further use)
		if(pag.isPaginating()){
			token = res.getNextToken();
			if(token!=null){
				sdbCtx.addToken(token, sdbCtx.realOffset + sdbCtx.realPageSize + off.offset);
			}
			// if paginating and 0 results then no more data else resets noMoreDataAfter
			if(res.getItems().size()==0){
				sdbCtx.noMoreDataAfter = true;
			} else {
				sdbCtx.noMoreDataAfter = false;
			}
		}else{
			if(state.isStateless()){
				// in stateless, doesn't follow real offset & stays where it is
				sdbCtx.realOffset = off.offset;
				
				token = res.getNextToken();
				if(token!=null){
					sdbCtx.addToken(token, /*sdbCtx.realOffset +*/ sdbCtx.realPageSize);
				}
			}else {
				// follows the real offset in stateful mode
				sdbCtx.realOffset += sdbCtx.realPageSize /*+ off.offset*/;
				
				token = res.getNextToken();
				if(token!=null){
					sdbCtx.addAndMoveToken(token, sdbCtx.realOffset);
				}else {
					// forces to go to next token to invalidate current one
					// if there are no token after, currentToken will be null
					sdbCtx.nextToken();
				}
			}
			
			
		}
	}
	
	protected <T> void preFetch(Query<T> query, int limit, int offset, boolean recursing){
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);

		if(sdbCtx==null){
			sdbCtx = new QueryOptionSdbContext();
			query.customize(sdbCtx);
		}
		
		if(!pag.isPaginating()){
			if(state.isStateless()){
				// if not empty, it means we are recursing on tokens
				sdbCtx.reset(recursing);				
			}
			// no pagination but pageOption active
			if(pag.isActive()){				
				// if local limit is set, it overrides the pageOption.pageSize
				if(limit!=Integer.MAX_VALUE){
					sdbCtx.realPageSize = limit;				
					// DONT DO THAT BECAUSE IT PREVENTS GOING TO NEXT TOKENS USING PAGE SIZE
					// pageOption is passivated to be sure it is not reused
					//pag.passivate();
				}
				// using pageOption.pageSize
				else {
					sdbCtx.realPageSize = pag.pageSize;
					// DONT DO THAT BECAUSE IT PREVENTS GOING TO NEXT TOKENS USING PAGE SIZE
					// passivates the pageOption in stateless mode not to keep anything between 2 requests
					//if(state.isStateless()){
					//	pag.passivate();
					//}						
				}
			}
			else {
				if(limit != Integer.MAX_VALUE){
					sdbCtx.realPageSize = limit;
					// activates paging (but not pagination)
					pag.activate();
				}else {
					sdbCtx.realPageSize = 0;
				}
			}
		}else {
			// paginating so use the pagesize and don't passivate pageOption
			// local limit is not taken into account
			sdbCtx.realPageSize = pag.pageSize;
		}
		
		// if local offset has been set, uses it
		if(offset!=0){
			off.activate();
			off.offset = offset;
		}
	}
	
	
	protected final int MAX_DEPTH = 25;
	
	protected <T> void doFetchList(Query<T> query, int limit, int offset, List<T> resList, int depth) {
		if(depth >= MAX_DEPTH){
			throw new SienaException("Reached maximum depth of recursion when retrieving more data ("+MAX_DEPTH+")");
		}
			
		preFetch(query, limit, offset, !resList.isEmpty());
		
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(sdbCtx.noMoreDataBefore || sdbCtx.noMoreDataAfter){
			return;
		}
						
		// manages cursor limitations for IN and != operators with offsets
		if(!sdbCtx.isActive()){
			StringBuffer domainBuf = new StringBuffer();
			SelectRequest req = SdbMappingUtils.buildQuery(query, prefix, domainBuf);
			req.setConsistentRead(isReadConsistent());
			checkDomain(domainBuf.toString());
			SelectResult res = sdb.select(req);
			
			// activates the SdbCtx now that it is really initialised
			sdbCtx.activate();
									
			postFetch(query, res);
			
			// cursor not yet created
			switch(fetchType.fetchType){
			case KEYS_ONLY:
				if(off.isActive()){
					SdbMappingUtils.mapSelectResultToListKeysOnly(res, resList, query.getQueriedClass(), off.offset);
				}else {
					SdbMappingUtils.mapSelectResultToListKeysOnly(res, resList, query.getQueriedClass());
				}				
				break;
			case NORMAL:
			default:
				if(off.isActive()){
					SdbMappingUtils.mapSelectResultToList(res, resList, query.getQueriedClass(), off.offset);
				}else {
					SdbMappingUtils.mapSelectResultToList(res, resList, query.getQueriedClass());
				}
				// join management
				if(!query.getJoins().isEmpty() 
						|| !ClassInfo.getClassInfo(query.getQueriedClass()).joinFields.isEmpty())
					mapJoins(query, resList);
			}
			
			continueFetchNextToken(query, resList, depth);
			postMapping(query);
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
				
			postFetch(query, res);
			
			switch(fetchType.fetchType){
			case KEYS_ONLY: 
				if(off.isActive()){
					SdbMappingUtils.mapSelectResultToListKeysOnly(res, resList, query.getQueriedClass(), off.offset);
				}else {
					SdbMappingUtils.mapSelectResultToListKeysOnly(res, resList, query.getQueriedClass());
				}
				break;
			case NORMAL:
			default:
				if(off.isActive()){
					SdbMappingUtils.mapSelectResultToList(res, resList, query.getQueriedClass(), off.offset);
				} else {
					SdbMappingUtils.mapSelectResultToList(res, resList, query.getQueriedClass());
				}
				// join management
				if(!query.getJoins().isEmpty() 
						|| !ClassInfo.getClassInfo(query.getQueriedClass()).joinFields.isEmpty())
					mapJoins(query, resList);
			}
			
			continueFetchNextToken(query, resList, depth);
			postMapping(query);
		}
	}
	
	protected <T> Iterable<T> doFetchIterable(Query<T> query, int limit, int offset, boolean recursing) {
		preFetch(query, limit, offset, recursing);
		
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(sdbCtx.noMoreDataBefore || sdbCtx.noMoreDataAfter){
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
			
			postFetch(query, res);		
			
			return new SdbSienaIterable<T>(this, res.getItems(), query);
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
									
			postFetch(query, res);
			
			return new SdbSienaIterable<T>(this, res.getItems(), query);
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
		return deleteByKeys(clazz, Arrays.asList(keys));
	}


	@Override
	public <T> void nextPage(Query<T> query) {
		SdbMappingUtils.nextPage(query);
	}

	@Override
	public <T> void previousPage(Query<T> query) {
		SdbMappingUtils.previousPage(query);
	}

	@Override
	public <T> void paginate(Query<T> query) {
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);

		if(sdbCtx==null){
			sdbCtx = new QueryOptionSdbContext();
			query.customize(sdbCtx);
		}
		
		// in stateless, resetting pagination resets everything in the context
		if(state.isStateless()){
			sdbCtx.resetAll();			
		}
		
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		off.passivate();
		off.offset = 0;
	}
	
	@Override
	public <T> PersistenceManagerAsync async() {
		// TODO Auto-generated method stub
		return null;
	}





	

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		List<DeletableItem> doList = new ArrayList<DeletableItem>(); 
		int nb = 0;
		String domain = SdbMappingUtils.getDomainName(clazz, prefix);
		for(Object key: keys){
			doList.add(SdbMappingUtils.createDeletableItemFromKey(clazz, key));			
			nb++;
		}
		try {
			checkDomain(domain);			
			int len = doList.size() > MAX_ITEMS_PER_CALL ? MAX_ITEMS_PER_CALL: doList.size();
			for(int i=0; i < doList.size(); i += len){
				sdb.batchDeleteAttributes(
					new BatchDeleteAttributesRequest(
						domain, 
						doList.subList(i, i + len)));			
			}
		}catch(AmazonClientException ex){
			throw new SienaException(ex);
		}
		return nb;
	}



	protected <T> T mapJoins(T model) {
		// join queries
		Map<Class<?>, Map<String, Object>> classMap = new HashMap<Class<?>, Map<String, Object>>();
		
		// sorts by class and itemName
		for(Field field: ClassInfo.getClassInfo(model.getClass()).joinFields)
		{
			Map<String, Object> strMap = classMap.get(field.getType());
			if(strMap == null){
				strMap = new HashMap<String, Object>();
				classMap.put(field.getType(), strMap);
			}
			
			String itemName = SdbMappingUtils.toString(Util.readField(model, field));
			strMap.put(itemName, null);
		}
		
		for(Class<?> clazz: classMap.keySet()){
			List<?> objs = this.getByKeys(clazz, classMap.get(clazz).keySet());
			Map<String, Object> strMap = classMap.get(clazz);
			for(Object obj:objs){
				String itemName = SdbMappingUtils.getItemName(clazz, obj);
				strMap.put(itemName, obj);
			}
		}
					
		for(Field field: ClassInfo.getClassInfo(model.getClass()).joinFields){			
			String itemName = SdbMappingUtils.toString(Util.readField(model, field));
			Util.setField(model, field, classMap.get(field.getType()).get(itemName));
		}

		return model;	
	}
	
	protected <T> void mapJoins(Iterable<T> models) {
		
		// join queries
		Map<Class<?>, Map<String, Object>> classMap = new HashMap<Class<?>, Map<String, Object>>();
		
		for(T model: models){
			// sorts by class and itemName
			for(Field field: ClassInfo.getClassInfo(model.getClass()).joinFields)
			{
				Map<String, Object> strMap = classMap.get(field.getType());
				if(strMap == null){
					strMap = new HashMap<String, Object>();
					classMap.put(field.getType(), strMap);
				}
				
				String itemName = SdbMappingUtils.toString(Util.readField(model, field));
				strMap.put(itemName, null);
			}
		}
		
		for(Class<?> clazz: classMap.keySet()){
			List<?> objs = this.getByKeys(clazz, classMap.get(clazz).keySet());
			Map<String, Object> strMap = classMap.get(clazz);
			for(Object obj:objs){
				String itemName = SdbMappingUtils.getItemName(clazz, obj);
				strMap.put(itemName, obj);
			}
		}
					
		for(T model: models){
			for(Field field: ClassInfo.getClassInfo(model.getClass()).joinFields){			
				String itemName = SdbMappingUtils.toString(Util.readField(model, field));
				Util.setField(model, field, classMap.get(field.getType()).get(itemName));
			}
		}
	}
	
	protected <T> T mapJoins(Query<T> query, T model) {
		List<QueryJoin> joins = query.getJoins();
		
		// join queries
		Map<Class<?>, Map<String, Object>> classMap = new HashMap<Class<?>, Map<String, Object>>();
		
		// sorts by class and itemName
		// joins in query
		for (QueryJoin join : joins)
		{
			Field field = join.field;
			Map<String, Object> strMap = classMap.get(field.getType());
			if(strMap == null){
				strMap = new HashMap<String, Object>();
				classMap.put(field.getType(), strMap);
			}
			
			String itemName = SdbMappingUtils.toString(Util.readField(model, field));
			strMap.put(itemName, null);
		}
		
		// join annotations
		for(Field field: 
			ClassInfo.getClassInfo(query.getQueriedClass()).joinFields)
		{
			Map<String, Object> strMap = classMap.get(field.getType());
			if(strMap == null){
				strMap = new HashMap<String, Object>();
				classMap.put(field.getType(), strMap);
			}
			
			String itemName = SdbMappingUtils.toString(Util.readField(model, field));
			strMap.put(itemName, null);
		}
		
		for(Class<?> clazz: classMap.keySet()){
			List<?> objs = this.getByKeys(clazz, classMap.get(clazz).keySet());
			Map<String, Object> strMap = classMap.get(clazz);
			for(Object obj:objs){
				String itemName = SdbMappingUtils.getItemName(clazz, obj);
				strMap.put(itemName, obj);
			}
		}
					
		for(Field field: ClassInfo.getClassInfo(model.getClass()).joinFields){			
			String itemName = SdbMappingUtils.toString(Util.readField(model, field));
			Util.setField(model, field, classMap.get(field.getType()).get(itemName));
		}

		return model;	
	}
	
	protected <T> List<T> mapJoins(Query<T> query, List<T> models) {
		List<QueryJoin> joins = query.getJoins();
		
		// join queries
		Map<Class<?>, Map<String, Object>> classMap = new HashMap<Class<?>, Map<String, Object>>();
		
		// sorts by class and itemName
		// joins in query
		for (final T model : models) {
			for (QueryJoin join : joins)
			{
				Field field = join.field;
				Map<String, Object> strMap = classMap.get(field.getType());
				if(strMap == null){
					strMap = new HashMap<String, Object>();
					classMap.put(field.getType(), strMap);
				}
				
				String itemName = SdbMappingUtils.toString(Util.readField(model, field));
				strMap.put(itemName, null);
			}
			
			// join annotations
			for(Field field: 
				ClassInfo.getClassInfo(query.getQueriedClass()).joinFields)
			{
				Map<String, Object> strMap = classMap.get(field.getType());
				if(strMap == null){
					strMap = new HashMap<String, Object>();
					classMap.put(field.getType(), strMap);
				}
				
				String itemName = SdbMappingUtils.toString(Util.readField(model, field));
				strMap.put(itemName, null);
			}
		}
		
		for(Class<?> clazz: classMap.keySet()){
			List<?> objs = this.getByKeys(clazz, classMap.get(clazz).keySet());
			Map<String, Object> strMap = classMap.get(clazz);
			for(Object obj:objs){
				String itemName = SdbMappingUtils.getItemName(clazz, obj);
				strMap.put(itemName, obj);
			}
		}
					
		for(T model: models){
			for (QueryJoin join : joins){
				Field field = join.field;
				String itemName = SdbMappingUtils.toString(Util.readField(model, field));
				Util.setField(model, field, classMap.get(field.getType()).get(itemName));
			}
			for(Field field: ClassInfo.getClassInfo(model.getClass()).joinFields){			
				String itemName = SdbMappingUtils.toString(Util.readField(model, field));
				Util.setField(model, field, classMap.get(field.getType()).get(itemName));
			}
		}

		return models;
	}

		
}
