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
 *   
 *   @author mandubian <pascal.voitot@mandubian.org>
 */
package siena.gae;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Query;
import siena.SienaException;
import siena.core.async.PersistenceManagerAsync;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

public class GaePersistenceManager extends AbstractPersistenceManager {

	private DatastoreService ds;
	private PersistenceManagerAsync asyncPm;
	/*
	 * properties are not used but keeps it in case of...
	 */
	private Properties props;
	
	public static final String DB = "GAE";

	public void init(Properties p) {
		ds = DatastoreServiceFactory.getDatastoreService();
		props = p;
	}

	public <T> PersistenceManagerAsync async() {
		if(asyncPm==null){
			asyncPm = new GaePersistenceManagerAsync();
			asyncPm.init(props);
		}
		return asyncPm;		
	}

	
	public void beginTransaction(int isolationLevel) {
		ds.beginTransaction();
	}

	public void closeConnection() {
		// does nothing
	}

	public void commitTransaction() {
		Transaction txn = ds.getCurrentTransaction();
		txn.commit();
	}

	public void rollbackTransaction() {
		Transaction txn = ds.getCurrentTransaction();
		txn.rollback();
	}
	
	public void delete(Object obj) {
		ds.delete(GaeMappingUtils.getKey(obj));
	}

	public void get(Object obj) {
		Key key = GaeMappingUtils.getKey(obj);
		try {
			Entity entity = ds.get(key);
			GaeMappingUtils.fillModel(obj, entity);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}


	public void insert(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		Field idField = info.getIdField();
		Entity entity = GaeMappingUtils.createEntityInstance(idField, info, obj);
		GaeMappingUtils.fillEntity(obj, entity);
		ds.put(entity);
		GaeMappingUtils.setKey(idField, obj, entity.getKey());
	}




	public void update(Object obj) {
		try {
			Entity entity = new Entity(GaeMappingUtils.getKey(obj));
			GaeMappingUtils.fillEntity(obj, entity);
			ds.put(entity);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	protected DatastoreService getDatastoreService() {
		return ds;
	}



	
	private <T> PreparedQuery prepare(Query<T> query) {
		Class<?> clazz = query.getQueriedClass();
		com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query(
				ClassInfo.getClassInfo(clazz).tableName);

		return ds.prepare(GaeQueryUtils.addFiltersOrders(query, q));
	}

	private <T> PreparedQuery prepareKeysOnly(Query<T> query) {
		Class<?> clazz = query.getQueriedClass();
		com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query(
				ClassInfo.getClassInfo(clazz).tableName);

		return ds.prepare(GaeQueryUtils.addFiltersOrders(query, q).setKeysOnly());
	}

	
	protected <T> T mapJoins(Query<T> query, T model) {
		try {
			// join queries
			Map<Field, ArrayList<Key>> fieldMap = GaeQueryUtils.buildJoinFieldKeysMap(query);
			
			// creates the list of joined entity keys to extract 
			for(Field field: fieldMap.keySet()){
				Key key = GaeMappingUtils.getKey(field.get(model));
				List<Key> keys = fieldMap.get(field);
				if(!keys.contains(key))
					keys.add(key);
			}
			
			Map<Field, Map<Key, Entity>> entityMap = 
				new HashMap<Field, Map<Key, Entity>>();

			try {
				// retrieves all joined entities per field
				for(Field field: fieldMap.keySet()){
					Map<Key, Entity> entities = ds.get(fieldMap.get(field));
					entityMap.put(field, entities);
				}
			}catch(Exception ex){
				throw new SienaException(ex);
			}
			// associates linked models to their models
			// linkedModels is just a map to contain entities already mapped
			Map<Key, Object> linkedModels = new HashMap<Key, Object>();
			Object linkedObj;
			Entity entity; 
			
			for(Field field: fieldMap.keySet()){
				Object objVal = field.get(model);
				Key key = GaeMappingUtils.getKey(objVal);
				linkedObj = linkedModels.get(key);
				if(linkedObj==null){
					entity = entityMap.get(field).get(key);
					linkedObj = objVal;
					GaeMappingUtils.fillModel(linkedObj, entity);
					linkedModels.put(key, linkedObj);
				}
			
				field.set(model, linkedObj);				
			}

			return model;
		} catch(IllegalAccessException ex){
			throw new SienaException(ex);
		}		
	}
	
	protected <T> List<T> mapJoins(Query<T> query, List<T> models) {
		try {
			// join queries
			Map<Field, ArrayList<Key>> fieldMap = GaeQueryUtils.buildJoinFieldKeysMap(query);
			
			// creates the list of joined entity keys to extract 
			for (final T model : models) {
				for(Field field: fieldMap.keySet()){
					Key key = GaeMappingUtils.getKey(field.get(model));
					List<Key> keys = fieldMap.get(field);
					if(!keys.contains(key))
						keys.add(key);
				}
			}
			
			Map<Field, Map<Key, Entity>> entityMap = 
				new HashMap<Field, Map<Key, Entity>>();

			try {
				// retrieves all joined entities per field
				for(Field field: fieldMap.keySet()){
					Map<Key, Entity> entities = ds.get(fieldMap.get(field));
					// gets the future here because we need it so we wait for it
					entityMap.put(field, entities);
				}
			}catch(Exception ex){
				throw new SienaException(ex);
			}
			// associates linked models to their models
			// linkedModels is just a map to contain entities already mapped
			Map<Key, Object> linkedModels = new HashMap<Key, Object>();
			Object linkedObj;
			Entity entity; 
			
			for (final T model : models) {
				for(Field field: fieldMap.keySet()){
					Object objVal = field.get(model);
					Key key = GaeMappingUtils.getKey(objVal);
					linkedObj = linkedModels.get(key);
					if(linkedObj==null){
						entity = entityMap.get(field).get(key);
						linkedObj = objVal;
						GaeMappingUtils.fillModel(linkedObj, entity);
						linkedModels.put(key, linkedObj);
					}
				
					field.set(model, linkedObj);				
				}
			}
			return models;
		} catch(IllegalAccessException ex){
			throw new SienaException(ex);
		}		
	}
	
	/*protected <T> List<T> mapJoins(Query<T> query, List<T> models) {
		try {
			List<QueryJoin> joins = query.getJoins();
			
			// join queries
			Map<Field, ArrayList<Key>> fieldMap = new HashMap<Field, ArrayList<Key>>();
			for (QueryJoin join : joins) {
				Field field = join.field;
				if (!ClassInfo.isModel(field.getType())){
					throw new SienaRestrictedApiException(DB, "join", "Join not possible: Field "+field.getName()+" is not a relation field");
				}
				else if(join.sortFields!=null && join.sortFields.length!=0)
					throw new SienaRestrictedApiException(DB, "join", "Join not allowed with sort fields");
				fieldMap.put(field, new ArrayList<Key>());
			}
			
			// join annotations
			for(Field field: 
				ClassInfo.getClassInfo(query.getQueriedClass()).joinFields)
			{
				fieldMap.put(field, new ArrayList<Key>());
			}
			
			// creates the list of joined entity keys to extract 
			for (final T model : models) {
				for(Field field: fieldMap.keySet()){
					Key key = GaeMappingUtils.getKey(field.get(model));
					List<Key> keys = fieldMap.get(field);
					if(!keys.contains(key))
						keys.add(key);
				}
			}
			
			Map<Field, Map<Key, Entity>> entityMap = 
				new HashMap<Field, Map<Key, Entity>>();

			// retrieves all joined entities per field
			for(Field field: fieldMap.keySet()){
				Map<Key, Entity> entities = ds.get(fieldMap.get(field));
				entityMap.put(field, entities);
			}
			
			// associates linked models to their models
			// linkedModels is just a map to contain entities already mapped
			Map<Key, Object> linkedModels = new HashMap<Key, Object>();
			Object linkedObj;
			Entity entity; 
			
			for (final T model : models) {
				for(Field field: fieldMap.keySet()){
					Object objVal = field.get(model);
					Key key = GaeMappingUtils.getKey(objVal);
					linkedObj = linkedModels.get(key);
					if(linkedObj==null){
						entity = entityMap.get(field).get(key);
						linkedObj = objVal;
						GaeMappingUtils.fillModel(linkedObj, entity);
						linkedModels.put(key, linkedObj);
					}
				
					field.set(model, linkedObj);				
				}
			}
			return models;
		} catch(IllegalAccessException ex){
			throw new SienaException(ex);
		}		
	}*/
	
	protected <T> T map(Query<T> query, Entity entity) {
		Class<?> clazz = query.getQueriedClass();
		@SuppressWarnings("unchecked")
		T result = (T)GaeMappingUtils.mapEntity(entity, clazz);
		
		// join management
		if(!query.getJoins().isEmpty() || ClassInfo.getClassInfo(clazz).joinFields.size() != 0)
			return mapJoins(query, result);
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> map(Query<T> query, List<Entity> entities) {
		Class<?> clazz = query.getQueriedClass();
		List<T> result = (List<T>) GaeMappingUtils.mapEntities(entities, clazz);
		
		// join management
		if(!query.getJoins().isEmpty() || ClassInfo.getClassInfo(clazz).joinFields.size() != 0)
			return mapJoins(query, result);
		
		return result;
	}


	/*private <T> Iterable<T> doFetch(Query<T> query) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		QueryOptionState reuse = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		if(gaeCtx==null){
			gaeCtx = new QueryOptionGaeContext();
			query.customize(gaeCtx);
		}
		
		// TODO manage pagination + offset
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		if(pag.isActive()) {
			fetchOptions.limit(pag.pageSize);
		}
		// set offset only when no in REUSE mode because it would disturb the cursor
		if(offset.isActive() && !reuse.isActive()){
			fetchOptions.offset(offset.offset);
		}
		
		if(!reuse.isActive()) {
			switch(fetchType.type){
			case KEYS_ONLY:
				{
					List<Entity> results = prepareKeysOnly(query).asList(fetchOptions);
					//updates offset
					if(offset.isActive()){
						offset.offset+=results.size();
					}
					return map(query, 0, results);
				}
			case ITER:
				{
					Iterable<Entity> results = prepare(query).asIterable(fetchOptions);
					//updates offset
					if(offset.isActive()){
						offset.offset+=pag.pageSize;
					}
					return new GaeSienaIterable<T>(results, query.getQueriedClass());
				}
			case NORMAL:
			default:
				{
					List<Entity> results = prepare(query).asList(fetchOptions);
					//updates offset
					if(offset.isActive()){
						offset.offset+=results.size();
					}
					return map(query, 0, results);
				}
			}
			
		}else {
			// TODO manage cursor limitations for IN and != operators		
			if(!gaeCtx.isActive()){
				// cursor not yet created
				switch(fetchType.type){
				case KEYS_ONLY:
					{
						PreparedQuery pq =prepareKeysOnly(query);
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
						}
						QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
						// saves the cursor websafe string
						gaeCtx.activate();
						if(gaeCtx.useCursor){
							gaeCtx.cursor = results.getCursor().toWebSafeString();
						}else {
							// uses offset
							offset.offset+=results.size();
						}
						gaeCtx.query = pq;
						return map(query, 0, results);
					}
				case ITER:
					{
						PreparedQuery pq =prepare(query);
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
						}
						QueryResultIterable<Entity> results = pq.asQueryResultIterable(fetchOptions);
						gaeCtx.activate();
						if(gaeCtx.useCursor){
							gaeCtx.cursor = results.iterator().getCursor().toWebSafeString();
						}else {
							// uses offset
							offset.offset+=pag.pageSize;
						}
						gaeCtx.query = pq;
						return new GaeSienaIterable<T>(results, query.getQueriedClass());
					}
				case NORMAL:
				default:
					{
						PreparedQuery pq =prepare(query);
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
						}
						QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
						// saves the cursor websafe string
						gaeCtx.activate();
						if(gaeCtx.useCursor){
							gaeCtx.cursor = results.getCursor().toWebSafeString();
						}else {
							// uses offset
							offset.offset+=results.size();
						}
						gaeCtx.query = pq;
						return map(query, 0, results);
					}
				}
				
			}else {
				switch(fetchType.type){
				case KEYS_ONLY:
					{
						PreparedQuery pq = gaeCtx.query;
						QueryResultList<Entity> results;
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
							results = pq.asQueryResultList(fetchOptions);
						}else {
							results = pq.asQueryResultList(fetchOptions.startCursor(Cursor.fromWebSafeString(gaeCtx.cursor)));
						}
						// saves the cursor websafe string
						if(gaeCtx.useCursor){
							gaeCtx.cursor = results.getCursor().toWebSafeString();
						}else {
							// uses offset
							offset.offset+=results.size();
						}
						return map(query, 0, results);
					}
				case ITER:
					{
						PreparedQuery pq = gaeCtx.query;
						QueryResultIterable<Entity> results;
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
							results = pq.asQueryResultIterable(fetchOptions);
						}else {
							results = pq.asQueryResultIterable(fetchOptions.startCursor(Cursor.fromWebSafeString(gaeCtx.cursor)));
						}
						if(gaeCtx.useCursor){
							gaeCtx.cursor = results.iterator().getCursor().toWebSafeString();
						}else {
							// uses offset
							offset.offset+=pag.pageSize;
						}
						return new GaeSienaIterable<T>(results, query.getQueriedClass());
					}
				case NORMAL:
				default:
					{
						PreparedQuery pq = gaeCtx.query;
						QueryResultList<Entity> results;
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
							results = pq.asQueryResultList(fetchOptions);
						}else {
							results = pq.asQueryResultList(fetchOptions.startCursor(Cursor.fromWebSafeString(gaeCtx.cursor)));
						}
						// saves the cursor websafe string
						if(gaeCtx.useCursor){
							gaeCtx.cursor = results.getCursor().toWebSafeString();
						}else {
							// uses offset
							offset.offset+=results.size();
						}
						return map(query, 0, results);
					}
				}
			}

		}
	}*/
	
	private <T> List<T> doFetchList(Query<T> query) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		if(gaeCtx==null){
			gaeCtx = new QueryOptionGaeContext();
			query.customize(gaeCtx);
		}
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(gaeCtx.noMoreDataBefore){
			return new ArrayList<T>();
		}
		
		// TODO manage pagination + offset
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		if(pag.isActive()) {
			fetchOptions.limit(pag.pageSize);
		}
		// set offset only when no in STATEFUL mode because it would disturb the cursor
		if(offset.isActive()){
			fetchOptions.offset(offset.offset);
		}
		
		if(state.isStateless()) {
			switch(fetchType.type){
			case KEYS_ONLY:
				{
					// uses iterable as it is the only async request for prepared query for the time being
					List<Entity> entities = prepareKeysOnly(query).asList(fetchOptions);
					// if paginating and 0 results then no more data else resets noMoreDataAfter
					if(pag.isPaginating()){
						if(entities.size() == 0){
							gaeCtx.noMoreDataAfter = true;
						}
						else {
							gaeCtx.noMoreDataAfter = false;
						}
					}
					return GaeMappingUtils.mapEntitiesKeysOnly(entities, query.getQueriedClass());
				}
			case NORMAL:
			default:
				{
					// uses iterable as it is the only async request for prepared query for the time being
					List<Entity> entities = prepare(query).asList(fetchOptions);
					// if paginating and 0 results then no more data else resets noMoreDataAfter
					if(pag.isPaginating()){
						if(entities.size() == 0){
							gaeCtx.noMoreDataAfter = true;
						}
						else {
							gaeCtx.noMoreDataAfter = false;
						}
					}
					return map(query, entities);
				}
			}
			
		}else {
			// TODO manage cursor limitations for IN and != operators		
			if(!gaeCtx.isActive()){
				// cursor not yet created
				switch(fetchType.type){
				case KEYS_ONLY:
					{
						PreparedQuery pq = prepareKeysOnly(query);
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
						}
						
						// we can't use real asynchronous function with cursors
						// so the page is extracted at once and wrapped into a SienaFuture
						QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);

						// activates the GaeCtx now that it is initialised
						gaeCtx.activate();
						// sets the current cursor (in stateful mode, cursor is always kept for further use)
						if(pag.isPaginating()){
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addCursor(cursor.toWebSafeString());
							}
							
							// if paginating and 0 results then no more data else resets noMoreDataAfter
							if(entities.size()==0){
								gaeCtx.noMoreDataAfter = true;
							} else {
								gaeCtx.noMoreDataAfter = false;
							}
						}else{
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addAndMoveCursor(entities.getCursor().toWebSafeString());
							}
							// keeps track of the offset anyway if not paginating
							offset.offset+=entities.size();
						}											
						
						return GaeMappingUtils.mapEntitiesKeysOnly(entities, query.getQueriedClass());
					}
				case NORMAL:
				default:
					{
						PreparedQuery pq = prepare(query);
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
						}
						// we can't use real asynchronous function with cursors
						// so the page is extracted at once and wrapped into a SienaFuture
						QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
						
						// activates the GaeCtx now that it is initialised
						gaeCtx.activate();
						// sets the current cursor (in stateful mode, cursor is always kept for further use)
						if(pag.isPaginating()){
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addCursor(cursor.toWebSafeString());
							}
							// if paginating and 0 results then no more data else resets noMoreDataAfter
							if(entities.size()==0){
								gaeCtx.noMoreDataAfter = true;
							} else {
								gaeCtx.noMoreDataAfter = false;
							}
						}else{
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addAndMoveCursor(entities.getCursor().toWebSafeString());
							}
							// keeps track of the offset anyway if not paginating
							offset.offset+=entities.size();
						}
						
						return map(query, entities);
					}
				}
				
			}else {
				switch(fetchType.type){
				case KEYS_ONLY:
					{
						// we prepare the query each time
						PreparedQuery pq = prepareKeysOnly(query);
						QueryResultList<Entity> entities;
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
							// we can't use real asynchronous function with cursors
							// so the page is extracted at once and wrapped into a SienaFuture
							entities = pq.asQueryResultList(fetchOptions);
						}else {
							// we can't use real asynchronous function with cursors
							// so the page is extracted at once and wrapped into a SienaFuture
							String cursor = gaeCtx.currentCursor();
							if(cursor!=null){
								entities = pq.asQueryResultList(
									fetchOptions.startCursor(Cursor.fromWebSafeString(cursor)));
							}
							else {
								entities = pq.asQueryResultList(fetchOptions);
							}
						}
						
						// sets the current cursor (in stateful mode, cursor is always kept for further use)
						if(pag.isPaginating()){
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addCursor(cursor.toWebSafeString());
							}
							// if paginating and 0 results then no more data else resets noMoreDataAfter
							if(entities.size()==0){
								gaeCtx.noMoreDataAfter = true;
							} else {
								gaeCtx.noMoreDataAfter = false;
							}
						}else{
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addAndMoveCursor(entities.getCursor().toWebSafeString());
							}
							// keeps track of the offset anyway if not paginating
							offset.offset+=entities.size();
						}
						//}
						
						return GaeMappingUtils.mapEntitiesKeysOnly(entities, query.getQueriedClass());
					}
				case NORMAL:
				default:
					{
						PreparedQuery pq = prepare(query);
						QueryResultList<Entity> entities;
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							if(offset.isActive()){
								fetchOptions.offset(offset.offset);
							}
							// we can't use real asynchronous function with cursors
							// so the page is extracted at once and wrapped into a SienaFuture
							entities = pq.asQueryResultList(fetchOptions);
						}else {
							// we can't use real asynchronous function with cursors
							// so the page is extracted at once and wrapped into a SienaFuture
							String cursor = gaeCtx.currentCursor();
							if(cursor!=null){
								entities = pq.asQueryResultList(
									fetchOptions.startCursor(Cursor.fromWebSafeString(gaeCtx.currentCursor())));
							}else {
								entities = pq.asQueryResultList(fetchOptions);
							}
						}
						
						// sets the current cursor (in stateful mode, cursor is always kept for further use)
						if(pag.isPaginating()){
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addCursor(cursor.toWebSafeString());
							}
							// if paginating and 0 results then no more data else resets noMoreDataAfter
							if(entities.size()==0){
								gaeCtx.noMoreDataAfter = true;
							} else {
								gaeCtx.noMoreDataAfter = false;
							}
						}else{
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addAndMoveCursor(entities.getCursor().toWebSafeString());
							}
							// keeps track of the offset anyway
							offset.offset+=entities.size();
						}
						
						return map(query, entities);
					}
				}
			}
		}
	}
	
	
	private <T> Iterable<T> doFetchIterable(Query<T> query) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		if(gaeCtx==null){
			gaeCtx = new QueryOptionGaeContext();
			query.customize(gaeCtx);
		}
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(gaeCtx.noMoreDataBefore){
			return new ArrayList<T>();
		}
		
		// TODO manage pagination + offset
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		if(pag.isActive()) {
			fetchOptions.limit(pag.pageSize);
		}
		if(offset.isActive()){
			fetchOptions.offset(offset.offset);
		}
		
		if(state.isStateless()) {
			switch(fetchType.type){
			case ITER:
			default:
				{
					// uses iterable as it is the only async request for prepared query for the time being
					Iterable<Entity> entities = prepare(query).asIterable(fetchOptions);
					return new GaeSienaIterable<T>(this, entities, query);
				}
			}
			
		}else {
			// TODO manage cursor limitations for IN and != operators		
			if(!gaeCtx.isActive()){
				// cursor not yet created
				switch(fetchType.type){
				case ITER:
				default:
					{
						PreparedQuery pq = prepare(query);
						
						if(pag.isPaginating()){
							// in case of pagination, we need to allow asynchronous calls such as:
							// QueryAsync<MyClass> query = pm.createQuery(MyClass).paginate(5).stateful().order("name");
							// SienaFuture<Iterable<MyClass>> future1 = query.iter();
							// SienaFuture<Iterable<MyClass>> future2 = query.nextPage().iter();
							// Iterable<MyClass> it = future1.get().iterator();
							// while(it.hasNext()) { // do it }
							// it = future2.get().iterator();
							// while(it.hasNext()) { // do it }
							
							// so we can't use the asQueryResultIterable as the cursor is not moved to the end of the current page
							// but moved at each call of iterable.iterator().next()
							// thus we use the List in this case to be able to move directly to the next page with cursors
							QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);

							// activates the GaeCtx now that it is initialised
							gaeCtx.activate();
							// sets the current cursor (in stateful mode, cursor is always kept for further use)
							//if(gaeCtx.useCursor){
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addCursor(cursor.toWebSafeString());
							}
							//}
							return new GaeSienaIterable<T>(this, entities, query);
						}else {
							// if not paginating, we simply use the queryresultiterable and moves the current cursor
							// while iterating
							QueryResultIterable<Entity> entities = pq.asQueryResultIterable(fetchOptions);
							// activates the GaeCtx now that it is initialised
							gaeCtx.activate();
							return new GaeSienaIterableWithCursor<T>(this, entities, query);
						}
						
					}
				}
				
			}else {
				switch(fetchType.type){
				case ITER:
				default:
					{
						PreparedQuery pq = prepare(query);
						if(pag.isPaginating()){
							// in case of pagination, we need to allow asynchronous calls such as:
							// QueryAsync<MyClass> query = pm.createQuery(MyClass).paginate(5).stateful().order("name");
							// SienaFuture<Iterable<MyClass>> future1 = query.iter();
							// SienaFuture<Iterable<MyClass>> future2 = query.nextPage().iter();
							// Iterable<MyClass> it = future1.get().iterator();
							// while(it.hasNext()) { // do it }
							// it = future2.get().iterator();
							// while(it.hasNext()) { // do it }
							
							// so we can't use the asQueryResultIterable as the cursor is not moved to the end of the current page
							// but moved at each call of iterable.iterator().next()
							// thus we use the List in this case to be able to move directly to the next page with cursors
							QueryResultList<Entity> entities;
							if(!gaeCtx.useCursor){
								// then uses offset (in case of IN or != operators)
								if(offset.isActive()){
									fetchOptions.offset(offset.offset);
								}
								entities = pq.asQueryResultList(fetchOptions);
							}else {
								String cursor = gaeCtx.currentCursor();
								if(cursor!=null){
									entities = pq.asQueryResultList(
										fetchOptions.startCursor(Cursor.fromWebSafeString(cursor)));
								}else {
									entities = pq.asQueryResultList(fetchOptions);
								}
								
								// sets the current cursor (in stateful mode, cursor is always kept for further use)
								//if(gaeCtx.useCursor){
								gaeCtx.addCursor(entities.getCursor().toWebSafeString());
								//}
							}
							return new GaeSienaIterable<T>(this, entities, query);
						}else {
							// if not paginating, we simply use the queryresultiterable and moves the current cursor
							// while iterating
							QueryResultIterable<Entity> entities;
							if(!gaeCtx.useCursor){
								// then uses offset (in case of IN or != operators)
								if(offset.isActive()){
									fetchOptions.offset(offset.offset);
								}
								entities = pq.asQueryResultIterable(fetchOptions);
							}else {
								String cursor = gaeCtx.currentCursor();
								if(cursor!=null){
									entities = pq.asQueryResultIterable(
										fetchOptions.startCursor(Cursor.fromWebSafeString(gaeCtx.currentCursor())));
								}else {
									entities = pq.asQueryResultIterable(fetchOptions);	
								}
							}
							return new GaeSienaIterableWithCursor<T>(this, entities, query);
						}
					}
				}
			}

		}
	}
	
	public <T> List<T> fetch(Query<T> query) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			pag.passivate();
		}
		return (List<T>)doFetchList(query);
	}

	public <T> List<T> fetch(Query<T> query, int limit) {
		((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		return (List<T>)doFetchList(query);
	}

	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;
		return (List<T>)doFetchList(query);
	}

	public <T> int count(Query<T> query) {
		return prepare(query)
				.countEntities(FetchOptions.Builder.withDefaults());
	}

	public <T> int delete(Query<T> query) {
		final ArrayList<Key> keys = new ArrayList<Key>();

		for (final Entity entity : prepareKeysOnly(query).asIterable(
				FetchOptions.Builder.withDefaults())) {
			keys.add(entity.getKey());
		}

		ds.delete(keys);

		return keys.size();
	}

	public <T> List<T> fetchKeys(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.KEYS_ONLY;
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			pag.passivate();
		}

		return (List<T>)doFetchList(query);
	}

	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.KEYS_ONLY;
		((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;

		return (List<T>)doFetchList(query);
	}

	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.KEYS_ONLY;
		((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;

		return (List<T>)doFetchList(query);
	}

	public <T> Iterable<T> iter(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.ITER;
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			pag.passivate();
		}

		return doFetchIterable(query);
	}

	public <T> Iterable<T> iter(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.ITER;
		((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;

		return doFetchIterable(query);
	}

	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.ITER;
		((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;

		return doFetchIterable(query);
	}


	public <T> void release(Query<T> query) {
		super.release(query);
		GaeQueryUtils.release(query);
	}
	
	public <T> void nextPage(Query<T> query) {
		GaeQueryUtils.nextPage(query);
	}

	public <T> void previousPage(Query<T> query) {
		GaeQueryUtils.previousPage(query);
	}

	public int insert(Object... objects) {
		List<Entity> entities = new ArrayList<Entity>(objects.length);
		for(int i=0; i<objects.length;i++){
			Class<?> clazz = objects[i].getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			Entity entity = GaeMappingUtils.createEntityInstance(idField, info, objects[i]);
			GaeMappingUtils.fillEntity(objects[i], entity);
			entities.add(entity);
		}
				
		List<Key> generatedKeys =  ds.put(entities);
		
		int i=0;
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			GaeMappingUtils.setKey(idField, obj, generatedKeys.get(i++));
		}
		
		return generatedKeys.size();
	}

	public int insert(Iterable<?> objects) {
		List<Entity> entities = new ArrayList<Entity>();
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			Entity entity = GaeMappingUtils.createEntityInstance(idField, info, obj);
			GaeMappingUtils.fillEntity(obj, entity);
			entities.add(entity);
		}
				
		List<Key> generatedKeys = ds.put(entities);
		
		int i=0;
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			GaeMappingUtils.setKey(idField, obj, generatedKeys.get(i++));
		}
		return generatedKeys.size();

	}

	public int delete(Object... models) {
		List<Key> keys = new ArrayList<Key>();
		for(Object obj:models){
			keys.add(GaeMappingUtils.getKey(obj));
		}
		
		ds.delete(keys);
		
		return keys.size();
	}


	public int delete(Iterable<?> models) {
		List<Key> keys = new ArrayList<Key>();
		for(Object obj:models){
			keys.add(GaeMappingUtils.getKey(obj));
		}
		
		ds.delete(keys);
		
		return keys.size();
	}


	public <T> int deleteByKeys(Class<T> clazz, Object... keys) {
		List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(GaeMappingUtils.makeKey(clazz, key));
		}
		
		ds.delete(gaeKeys);
		
		return gaeKeys.size();
	}

	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(GaeMappingUtils.makeKey(clazz, key));
		}
		
		ds.delete(gaeKeys);

		return gaeKeys.size();
	}


	public int get(Object... objects) {
		List<Key> keys = new ArrayList<Key>();
		for(Object obj:objects){
			keys.add(GaeMappingUtils.getKey(obj));
		}
		
		Map<Key, Entity> entityMap = ds.get(keys);
		
		for(Object obj:objects){
			GaeMappingUtils.fillModel(obj, entityMap.get(GaeMappingUtils.getKey(obj)));
		}
		
		return entityMap.size();
	}

	public <T> int get(Iterable<T> objects) {
		List<Key> keys = new ArrayList<Key>();
		for(Object obj:objects){
			keys.add(GaeMappingUtils.getKey(obj));
		}
		
		Map<Key, Entity> entityMap = ds.get(keys);
		
		for(Object obj:objects){
			GaeMappingUtils.fillModel(obj, entityMap.get(GaeMappingUtils.getKey(obj)));
		}
		
		return entityMap.size();
	}

	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(GaeMappingUtils.makeKey(clazz, key));
		}
		
		Map<Key, Entity> entityMap = ds.get(gaeKeys);
		List<T> models = new ArrayList<T>(entityMap.size());
		
		for(Object key:keys){
			models.add(GaeMappingUtils.mapEntity(entityMap.get(GaeMappingUtils.makeKey(clazz, key)), clazz));
		}
		
		return models;
	}

	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(GaeMappingUtils.makeKey(clazz, key));
		}
		
		Map<Key, Entity> entityMap = ds.get(gaeKeys);
		List<T> models = new ArrayList<T>(entityMap.size());
		for(Object key:keys){
			models.add(GaeMappingUtils.mapEntity(entityMap.get(GaeMappingUtils.makeKey(clazz, key)), clazz));
		}
		
		return models;
	}


	@Override
	public <T> int update(Object... models) {
		throw new NotImplementedException("update not implemented for GAE yet");
	}

	@Override
	public <T> int update(Iterable<T> models) {
		throw new NotImplementedException("update not implemented for GAE yet");
	}

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		throw new NotImplementedException("update not implemented for GAE yet");
	}


	private static String[] supportedOperators;

	static {
		supportedOperators = GaeQueryUtils.operators.keySet().toArray(new String[0]);
	}	

	public String[] supportedOperators() {
		return supportedOperators;
	}


}
