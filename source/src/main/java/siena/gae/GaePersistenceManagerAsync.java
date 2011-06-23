/*
 * @author mandubian <pascal.voitot@mandubian.org>
 */
package siena.gae;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.commons.lang.NotImplementedException;

import siena.ClassInfo;
import siena.PersistenceManager;
import siena.SienaException;
import siena.Util;
import siena.core.async.AbstractPersistenceManagerAsync;
import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;
import siena.core.async.SienaFutureContainer;
import siena.core.async.SienaFutureMock;
import siena.core.async.SienaFutureWrapper;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

public class GaePersistenceManagerAsync extends AbstractPersistenceManagerAsync {

	private AsyncDatastoreService ds;
	private PersistenceManager syncPm;
	/*
	 * properties are not used but keeps it in case of...
	 */
	private Properties props;
	
	public static final String DB = "GAE_ASYNC";

	public void init(Properties p) {
		ds = DatastoreServiceFactory.getAsyncDatastoreService();
		props = p;
	}
	
	public void init(Properties p, PersistenceManager syncPm) {
		this.syncPm = syncPm;
		ds = DatastoreServiceFactory.getAsyncDatastoreService();
		props = p;
	}
	
	public PersistenceManager sync() {
		if(syncPm==null){
			syncPm = new GaePersistenceManager();
			syncPm.init(props);
		}
		return syncPm;
	}

	public SienaFuture<Void> insert(final Object obj) {
		final Class<?> clazz = obj.getClass();
		final ClassInfo info = ClassInfo.getClassInfo(clazz);
		final Field idField = info.getIdField();
		
		Entity entity = GaeMappingUtils.createEntityInstance(idField, info, obj);
		GaeMappingUtils.fillEntity(obj, entity);
		Future<Key> future = ds.put(entity);
		
		Future<Void> wrapped = new SienaFutureWrapper<Key, Void>(future) {
             @Override
             protected Void wrap(Key generatedKey) throws Exception
             {
            	 GaeMappingUtils.setIdFromKey(idField, obj, generatedKey);  
            	 return null;
             }
		};
		
		return new SienaFutureContainer<Void>(wrapped);
	}

	public SienaFuture<Void> delete(Object obj) {
		return new SienaFutureContainer<Void>(ds.delete(GaeMappingUtils.getKey(obj)));
	}
	
	public SienaFuture<Void> get(final Object obj) {
		final Key key = GaeMappingUtils.getKey(obj);
		Future<Entity> future = ds.get(key);
		
		Future<Void> wrapped = new SienaFutureWrapper<Entity, Void>(future) {
            @Override
            protected Void wrap(Entity entity) throws Exception
            {
            	GaeMappingUtils.fillModel(obj, entity);
            	return null;
            }
		};
		
		return new SienaFutureContainer<Void>(wrapped);
	}
	
	public SienaFuture<Void> beginTransaction(int isolationLevel) {
		Future<Transaction> future = ds.beginTransaction();
		
		Future<Void> wrapped = new SienaFutureWrapper<Transaction, Void>(future) {
            @Override
            protected Void wrap(Transaction transaction) throws Exception
            {
            	return null;
            }
		};
		
		return new SienaFutureContainer<Void>(wrapped);
	}
	
	public SienaFuture<Void> beginTransaction() {
		Future<Transaction> future = ds.beginTransaction();
		
		Future<Void> wrapped = new SienaFutureWrapper<Transaction, Void>(future) {
            @Override
            protected Void wrap(Transaction transaction) throws Exception
            {
            	return null;
            }
		};
		
		return new SienaFutureContainer<Void>(wrapped);
	}

	public SienaFuture<Void> closeConnection() {
		// does nothing
		return null;
	}

	public SienaFuture<Void> commitTransaction() {
		Transaction txn = ds.getCurrentTransaction();
		return new SienaFutureContainer<Void>(txn.commitAsync());
	}

	public SienaFuture<Void> rollbackTransaction() {
		Transaction txn = ds.getCurrentTransaction();
		return new SienaFutureContainer<Void>(txn.rollbackAsync());
	}

	public SienaFuture<Void> update(Object obj) {
		Entity entity = new Entity(GaeMappingUtils.getKey(obj));
		GaeMappingUtils.fillEntity(obj, entity);
		Future<Key> future = ds.put(entity);
		
		Future<Void> wrapped = new SienaFutureWrapper<Key, Void>(future) {
            @Override
            protected Void wrap(Key key) throws Exception
            {
            	return null;
            }
		};
		
		return new SienaFutureContainer<Void>(wrapped);
	}


	protected AsyncDatastoreService getDatastoreService() {
		return ds;
	}

	

	private <T> PreparedQuery prepare(QueryAsync<T> query) {
		Class<?> clazz = query.getQueriedClass();
		com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query(
				ClassInfo.getClassInfo(clazz).tableName);

		return ds.prepare(GaeQueryUtils.addFiltersOrders(query, q));
	}

	private <T> PreparedQuery prepareKeysOnly(QueryAsync<T> query) {
		Class<?> clazz = query.getQueriedClass();
		com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query(
				ClassInfo.getClassInfo(clazz).tableName);

		return ds.prepare(GaeQueryUtils.addFiltersOrders(query, q).setKeysOnly());
	}

	protected <T> T mapJoins(QueryAsync<T> query, T model) {
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
					Future<Map<Key, Entity>> entities = ds.get(fieldMap.get(field));
					// gets the future here because we need it!
					entityMap.put(field, entities.get());
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
	
	protected <T> List<T> mapJoins(QueryAsync<T> query, List<T> models) {
		try {
			// join queries
			Map<Field, ArrayList<Key>> fieldMap = GaeQueryUtils.buildJoinFieldKeysMap(query);
			
			// creates the list of joined entity keys to extract 
			for (final T model : models) {
				for(Field field: fieldMap.keySet()){
                    Object objVal = Util.readField(model, field);
                    // our object is not linked to another object...so it doesn't have any key
                    if(objVal == null) {
                        continue;
                    }

                    Key key = GaeMappingUtils.getKey(objVal);
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
					Future<Map<Key, Entity>> entities = ds.get(fieldMap.get(field));
					// gets the future here because we need it so we wait for it
					entityMap.put(field, entities.get());
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
					Object objVal = Util.readField(model, field);
                    // our object is not linked to another object...so it doesn't have any key
                    if(objVal == null) {
                        continue;
                    }
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
	
	protected <T> T map(QueryAsync<T> query, Entity entity) {
		Class<?> clazz = query.getQueriedClass();
		@SuppressWarnings("unchecked")
		T result = (T)GaeMappingUtils.mapEntity(entity, clazz);
		
		// join management
		if(!query.getJoins().isEmpty() || ClassInfo.getClassInfo(clazz).joinFields.size() != 0)
			return mapJoins(query, result);
		
		return result;
	}
	
	protected <T> List<T> map(QueryAsync<T> query, Iterable<Entity> entities) {
		Class<?> clazz = query.getQueriedClass();
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) GaeMappingUtils.mapEntities(entities, clazz);
		
		// join management
		if(!query.getJoins().isEmpty() || ClassInfo.getClassInfo(clazz).joinFields.size() != 0)
			return mapJoins(query, result);
		
		return result;
	}
	
	protected <T> List<T> map(QueryAsync<T> query, QueryResultList<Entity> entities) {
		Class<?> clazz = query.getQueriedClass();
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) GaeMappingUtils.mapEntities(entities, clazz);
		
		// join management
		if(!query.getJoins().isEmpty() || ClassInfo.getClassInfo(clazz).joinFields.size() != 0)
			return mapJoins(query, result);
		
		return result;
	}

	
	private <T> SienaFuture<List<T>> doFetchList(QueryAsync<T> query, int limit, int offset) {
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		if(gaeCtx==null){
			gaeCtx = new QueryOptionGaeContext();
			query.customize(gaeCtx);
		}
		
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			// no pagination but pageOption active
			if(pag.isActive()){
				// if local limit is set, it overrides the pageOption.pageSize
				if(limit!=Integer.MAX_VALUE){
					gaeCtx.realPageSize = limit;
					fetchOptions.limit(gaeCtx.realPageSize);
					// pageOption is passivated to be sure it is not reused
					pag.passivate();
				}
				// using pageOption.pageSize
				else {
					gaeCtx.realPageSize = pag.pageSize;
					fetchOptions.limit(gaeCtx.realPageSize);
					// passivates the pageOption in stateless mode not to keep anything between 2 requests
					if(state.isStateless()){
						pag.passivate();
					}						
				}
			}
			else {
				if(limit != Integer.MAX_VALUE){
					gaeCtx.realPageSize = limit;
					fetchOptions.limit(gaeCtx.realPageSize);
				}
			}
		}else {
			// paginating so use the pagesize and don't passivate pageOption
			// local limit is not taken into account
			gaeCtx.realPageSize = pag.pageSize;
			fetchOptions.limit(gaeCtx.realPageSize);
		}

		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		// if local offset has been set, uses it
		if(offset!=0){
			off.activate();
			off.offset = offset;
		}
						
		// if previousPage has detected there is no more data, simply returns an empty list
		if(gaeCtx.noMoreDataBefore){
			return new SienaFutureMock<List<T>>(new ArrayList<T>());
		}
		
		if(state.isStateless()) {
			if(pag.isPaginating()){
				if(off.isActive()){
					gaeCtx.realOffset+=off.offset;
					fetchOptions.offset(gaeCtx.realOffset);
					off.passivate();
				}else {
					fetchOptions.offset(gaeCtx.realOffset);
				}
			}else {
				// if stateless and not paginating, resets the realoffset to 0
				gaeCtx.realOffset = 0;
				if(off.isActive()){
					gaeCtx.realOffset=off.offset;
					fetchOptions.offset(gaeCtx.realOffset);
					off.passivate();
				}
			}
			
			switch(fetchType.fetchType){
			case KEYS_ONLY:
				{
					// uses iterable as it is the only async request for prepared query for the time being
					Iterable<Entity> entities = prepareKeysOnly(query).asIterable(fetchOptions);
					return new GaeSienaFutureListMapper<T>(this, entities, query, GaeSienaFutureListMapper.MapType.KEYS_ONLY);
				}
			case NORMAL:
			default:
				{
					// uses iterable as it is the only async request for prepared query for the time being
					Iterable<Entity> entities = prepare(query).asIterable(fetchOptions);
					return new GaeSienaFutureListMapper<T>(this, entities, query);
				}
			}
			
		}else {
			if(off.isActive()){
				// by default, we add the offset but it can be added with the realoffset 
				// in case of cursor desactivated
				fetchOptions.offset(off.offset);
				gaeCtx.realOffset+=off.offset;
				off.passivate();
			}
			
			// manages cursor limitations for IN and != operators by using offset	
			if(!gaeCtx.isActive()){
				// cursor not yet created
				switch(fetchType.fetchType){
				case KEYS_ONLY:
					{
						PreparedQuery pq = prepareKeysOnly(query);
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							//if(offset.isActive()){
							//	fetchOptions.offset(offset.offset);
							//}
							fetchOptions.offset(gaeCtx.realOffset);							
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
						}else{
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addAndMoveCursor(entities.getCursor().toWebSafeString());
							}
							// keeps track of the offset anyway if not paginating
							gaeCtx.realOffset+=entities.size();
						}											
						
						return new GaeSienaFutureListMapper<T>(
								this, entities, query, GaeSienaFutureListMapper.MapType.KEYS_ONLY);
					}
				case NORMAL:
				default:
					{
						PreparedQuery pq = prepare(query);
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							//if(offset.isActive()){
							//	fetchOptions.offset(offset.offset);
							//}
							fetchOptions.offset(gaeCtx.realOffset);							
						}
						// we can't use real asynchronous function with cursors
						// so the page is extracted at once and wrapped into a SienaFuture
						QueryResultList<Entity> entities = pq.asQueryResultList(fetchOptions);
						
						// activates the GaeCtx now that it is initialised
						gaeCtx.activate();
						// sets the current cursor (in stateful mode, cursor is always kept for further use)
						//if(gaeCtx.useCursor){
						if(pag.isPaginating()){
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addCursor(cursor.toWebSafeString());
							}
						}else{
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addAndMoveCursor(entities.getCursor().toWebSafeString());
							}
							// keeps track of the offset anyway if not paginating
							gaeCtx.realOffset+=entities.size();
						}
						//}
						
						return new GaeSienaFutureListMapper<T>(this, entities, query);
					}
				}
				
			}else {
				switch(fetchType.fetchType){
				case KEYS_ONLY:
					{
						// we prepare the query each time
						PreparedQuery pq = prepareKeysOnly(query);
						QueryResultList<Entity> entities;
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							//if(offset.isActive()){
							//	fetchOptions.offset(offset.offset);
							//}
							fetchOptions.offset(gaeCtx.realOffset);
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
						//if(gaeCtx.useCursor){
						if(pag.isPaginating()){
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addCursor(cursor.toWebSafeString());
							}
						}else{
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addAndMoveCursor(entities.getCursor().toWebSafeString());
							}
							// keeps track of the offset anyway if not paginating
							gaeCtx.realOffset+=entities.size();
						}
						//}
						
						return new GaeSienaFutureListMapper<T>(
								this, entities, query, GaeSienaFutureListMapper.MapType.KEYS_ONLY);
					}
				case NORMAL:
				default:
					{
						PreparedQuery pq = prepare(query);
						QueryResultList<Entity> entities;
						if(!gaeCtx.useCursor){
							// then uses offset (in case of IN or != operators)
							//if(offset.isActive()){
							//	fetchOptions.offset(offset.offset);
							//}
							fetchOptions.offset(gaeCtx.realOffset);
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
						//if(gaeCtx.useCursor){
						if(pag.isPaginating()){
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addCursor(cursor.toWebSafeString());
							}
						}else{
							Cursor cursor = entities.getCursor();
							if(cursor!=null){
								gaeCtx.addAndMoveCursor(entities.getCursor().toWebSafeString());
							}
							// keeps track of the offset anyway
							gaeCtx.realOffset+=entities.size();
						}
						//}
						
						return new GaeSienaFutureListMapper<T>(this, entities, query);
					}
				}
			}

		}
	}
	
	
	private <T> SienaFuture<Iterable<T>> doFetchIterable(QueryAsync<T> query, int limit, int offset) 
	{
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);
				
		if(gaeCtx==null){
			gaeCtx = new QueryOptionGaeContext();
			query.customize(gaeCtx);
		}

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			// no pagination but pageOption active
			if(pag.isActive()){
				// if local limit is set, it overrides the pageOption.pageSize
				if(limit!=Integer.MAX_VALUE){
					gaeCtx.realPageSize = limit;
					fetchOptions.limit(gaeCtx.realPageSize);
					// pageOption is passivated to be sure it is not reused
					pag.passivate();
				}
				// using pageOption.pageSize
				else {
					gaeCtx.realPageSize = pag.pageSize;
					fetchOptions.limit(gaeCtx.realPageSize);
					// passivates the pageOption in stateless mode not to keep anything between 2 requests
					if(state.isStateless()){
						pag.passivate();
					}						
				}
			}
			else {
				if(limit != Integer.MAX_VALUE){
					gaeCtx.realPageSize = limit;
					fetchOptions.limit(gaeCtx.realPageSize);
				}
			}
		}else {
			// paginating so use the pagesize and don't passivate pageOption
			// local limit is not taken into account
			gaeCtx.realPageSize = pag.pageSize;
			fetchOptions.limit(gaeCtx.realPageSize);
		}

		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		// if local offset has been set, uses it
		if(offset!=0){
			off.activate();
			off.offset = offset;
		}
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(gaeCtx.noMoreDataBefore){
			return new SienaFutureMock<Iterable<T>>(new ArrayList<T>());
		}
		
		if(state.isStateless()) {
			if(pag.isPaginating()){			
				if(off.isActive()){
					gaeCtx.realOffset+=off.offset;
					fetchOptions.offset(gaeCtx.realOffset);
					off.passivate();
				}else {
					fetchOptions.offset(gaeCtx.realOffset);
				}
			}else {
								
				// if stateless and not paginating, resets the realoffset to 0
				gaeCtx.realOffset = off.offset;
				if(off.isActive()){
					fetchOptions.offset(gaeCtx.realOffset);
					off.passivate();
				}
			}
			
			switch(fetchType.fetchType){
			case ITER:
			default:
				{
					// uses iterable as it is the only async request for prepared query for the time being
					Iterable<Entity> entities = prepare(query).asIterable(fetchOptions);
					return new GaeSienaFutureIterableMapper<T>(this, entities, query);
				}
			}
			
		}else {
			if(off.isActive()){
				// by default, we add the offset but it can be added with the realoffset 
				// in case of cursor desactivated
				fetchOptions.offset(off.offset);
				gaeCtx.realOffset+=off.offset;
				off.passivate();
			}
			// manages cursor limitations for IN and != operators		
			if(!gaeCtx.isActive()){
				// cursor not yet created
				switch(fetchType.fetchType){
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
							
							return new GaeSienaFutureIterableMapper<T>(this, entities, query);
						}else {
							// if not paginating, we simply use the queryresultiterable and moves the current cursor
							// while iterating
							QueryResultIterable<Entity> entities = pq.asQueryResultIterable(fetchOptions);
							// activates the GaeCtx now that it is initialised
							gaeCtx.activate();
							return new GaeSienaFutureIterableMapperWithCursor<T>(this, entities, query);
						}
						
					}
				}
				
			}else {
				switch(fetchType.fetchType){
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
								//if(offset.isActive()){
								//	fetchOptions.offset(offset.offset);
								//}
								fetchOptions.offset(gaeCtx.realOffset);
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
							return new GaeSienaFutureIterableMapper<T>(this, entities, query);
						}else {
							// if not paginating, we simply use the queryresultiterable and moves the current cursor
							// while iterating
							QueryResultIterable<Entity> entities;
							if(!gaeCtx.useCursor){
								// then uses offset (in case of IN or != operators)
								//if(offset.isActive()){
								//	fetchOptions.offset(offset.offset);
								//}
								fetchOptions.offset(gaeCtx.realOffset);								
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
							return new GaeSienaFutureIterableMapperWithCursor<T>(this, entities, query);
						}
					}
				}
			}

		}
	}
	
	public <T> SienaFuture<Integer> count(QueryAsync<T> query) {
		 int nb = prepare(query)
				.countEntities(FetchOptions.Builder.withDefaults());
		 
		 return new SienaFutureMock<Integer>(nb);
	}

	public <T> SienaFuture<Integer> delete(QueryAsync<T> query) {
		final ArrayList<Key> keys = new ArrayList<Key>();

		for (final Entity entity : prepareKeysOnly(query).asIterable(
				FetchOptions.Builder.withDefaults())) {
			keys.add(entity.getKey());
		}

		Future<Void> future = ds.delete(keys);
		Future<Integer> wrapped = new SienaFutureWrapper<Void, Integer>(future) {
            @Override
            protected Integer wrap(Void v) throws Exception
            {
            	return keys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}

	
	public <T> SienaFuture<List<T>> fetch(QueryAsync<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.NORMAL;
		return doFetchList(query, Integer.MAX_VALUE, 0);
	}

	public <T> SienaFuture<List<T>> fetch(QueryAsync<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.NORMAL;
		return doFetchList(query, limit, 0);
	}

	public <T> SienaFuture<List<T>> fetch(QueryAsync<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.NORMAL;
		return doFetchList(query, limit, (Integer)offset);
	}
	
	public <T> SienaFuture<List<T>> fetchKeys(QueryAsync<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;

		return doFetchList(query, Integer.MAX_VALUE, 0);
	}

	public <T> SienaFuture<List<T>> fetchKeys(QueryAsync<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;

		return doFetchList(query, limit, 0);
	}

	public <T> SienaFuture<List<T>>  fetchKeys(QueryAsync<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;

		return doFetchList(query, limit, (Integer)offset);
	}

	public <T> SienaFuture<Iterable<T>> iter(QueryAsync<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;

		return doFetchIterable(query, Integer.MAX_VALUE, 0);
	}

	public <T> SienaFuture<Iterable<T>> iter(QueryAsync<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;

		return doFetchIterable(query, limit, 0);
	}

	public <T> SienaFuture<Iterable<T>> iter(QueryAsync<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;

		return doFetchIterable(query, limit, (Integer)offset);
	}

	public <T> SienaFuture<Iterable<T>> iterPerPage(QueryAsync<T> query,
			int pageSize) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER_PER_PAGE;
		return new SienaIterableAsyncPerPageWrapper<T>(query, pageSize);
	}

	public <T> void release(QueryAsync<T> query) {
		super.release(query);
		GaeQueryUtils.release(query);
	}
	
	public <T> void paginate(QueryAsync<T> query) {
		GaeQueryUtils.paginate(query);
	}
	
	public <T> void nextPage(QueryAsync<T> query) {
		GaeQueryUtils.nextPage(query);
	}
	
	public <T> void previousPage(QueryAsync<T> query) {
		GaeQueryUtils.previousPage(query);
	}

	public SienaFuture<Integer> insert(final Object... objects) {
		List<Entity> entities = new ArrayList<Entity>(objects.length);
		for(int i=0; i<objects.length;i++){
			Class<?> clazz = objects[i].getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			Entity entity = GaeMappingUtils.createEntityInstance(idField, info, objects[i]);
			GaeMappingUtils.fillEntity(objects[i], entity);
			entities.add(entity);
		}
				
		Future<List<Key>> future = ds.put(entities);
		
		Future<Integer> wrapped = new SienaFutureWrapper<List<Key>, Integer>(future) {
             @Override
             protected Integer wrap(List<Key> generatedKeys) throws Exception
             {
            	 int i=0;
            	 for(Object obj:objects){
            		 Class<?> clazz = obj.getClass();
            		 ClassInfo info = ClassInfo.getClassInfo(clazz);
            		 Field idField = info.getIdField();
            		 GaeMappingUtils.setIdFromKey(idField, obj, generatedKeys.get(i++));
            	 }
            	 return generatedKeys.size();
             }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}

	public SienaFuture<Integer> insert(final Iterable<?> objects) {
		List<Entity> entities = new ArrayList<Entity>();
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			Entity entity = GaeMappingUtils.createEntityInstance(idField, info, obj);
			GaeMappingUtils.fillEntity(obj, entity);
			entities.add(entity);
		}		
		
		Future<List<Key>> future = ds.put(entities);
		
		Future<Integer> wrapped = new SienaFutureWrapper<List<Key>, Integer>(future) {
             @Override
             protected Integer wrap(List<Key> generatedKeys) throws Exception
             {
            	 int i=0;
            	 for(Object obj:objects){
            		 Class<?> clazz = obj.getClass();
            		 ClassInfo info = ClassInfo.getClassInfo(clazz);
            		 Field idField = info.getIdField();
            		 GaeMappingUtils.setIdFromKey(idField, obj, generatedKeys.get(i++));
            	 }
            	 return generatedKeys.size();
             }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}

	public SienaFuture<Integer> delete(final Object... models) {
		final List<Key> keys = new ArrayList<Key>();
		for(Object obj:models){
			keys.add(GaeMappingUtils.getKey(obj));
		}
		
		Future<Void> future = ds.delete(keys);
		
		
		Future<Integer> wrapped = new SienaFutureWrapper<Void, Integer>(future) {
            @Override
            protected Integer wrap(Void v) throws Exception
            {
            	return keys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}


	public SienaFuture<Integer> delete(final Iterable<?> models) {
		final List<Key> keys = new ArrayList<Key>();
		for(Object obj:models){
			keys.add(GaeMappingUtils.getKey(obj));
		}
		
		Future<Void> future = ds.delete(keys);
		
		Future<Integer> wrapped = new SienaFutureWrapper<Void, Integer>(future) {
            @Override
            protected Integer wrap(Void v) throws Exception
            {
            	return keys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}


	public <T> SienaFuture<Integer> deleteByKeys(Class<T> clazz, Object... keys) {
		final List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(GaeMappingUtils.makeKey(clazz, key));
		}
		
		Future<Void> future = ds.delete(gaeKeys);
		
		Future<Integer> wrapped = new SienaFutureWrapper<Void, Integer>(future) {
            @Override
            protected Integer wrap(Void v) throws Exception
            {
            	return gaeKeys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}

	public <T> SienaFuture<Integer> deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		final List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(GaeMappingUtils.makeKey(clazz, key));
		}
		
		Future<Void> future = ds.delete(gaeKeys);
		
		Future<Integer> wrapped = new SienaFutureWrapper<Void, Integer>(future) {
            @Override
            protected Integer wrap(Void v) throws Exception
            {
            	return gaeKeys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}
	
	
	public SienaFuture<Integer> get(final Object... objects) {
		List<Key> keys = new ArrayList<Key>();
		for(Object obj:objects){
			keys.add(GaeMappingUtils.getKey(obj));
		}
		
		Future<Map<Key, Entity>> future = ds.get(keys);
		
		Future<Integer> wrapped = new SienaFutureWrapper<Map<Key, Entity>, Integer>(future) {
            @Override
            protected Integer wrap(Map<Key, Entity> entityMap) throws Exception
            {
            	for(Object obj:objects){
        			GaeMappingUtils.fillModel(obj, entityMap.get(GaeMappingUtils.getKey(obj)));
        		}
        		
        		return entityMap.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}

	public <T> SienaFuture<Integer> get(final Iterable<T> objects) {
		List<Key> keys = new ArrayList<Key>();
		for(Object obj:objects){
			keys.add(GaeMappingUtils.getKey(obj));
		}
		
		Future<Map<Key, Entity>> future = ds.get(keys);
		
		Future<Integer> wrapped = new SienaFutureWrapper<Map<Key, Entity>, Integer>(future) {
            @Override
            protected Integer wrap(Map<Key, Entity> entityMap) throws Exception
            {
            	for(Object obj:objects){
        			GaeMappingUtils.fillModel(obj, entityMap.get(GaeMappingUtils.getKey(obj)));
        		}
        		
        		return entityMap.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}
	
	public <T> SienaFuture<T> getByKey(final Class<T> clazz, final Object key) {
		Key gkey = GaeMappingUtils.makeKey(clazz, key);
		try {
			Future<Entity> future = ds.get(gkey);
			
			Future<T> wrapped = new SienaFutureWrapper<Entity, T>(future) {
	            @Override
	            protected T wrap(Entity entity) throws Exception
	            {
	    			T obj = Util.createObjectInstance(clazz);
	            	GaeMappingUtils.fillModelAndKey(obj, entity);
	            	return obj;
	            }
			};
			
			return new SienaFutureContainer<T>(wrapped);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	public <T> SienaFuture<List<T>> getByKeys(final Class<T> clazz, final Object... keys) {
		List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(GaeMappingUtils.makeKey(clazz, key));
		}
		
		Future<Map<Key, Entity>> future = ds.get(gaeKeys);
		
		Future<List<T>> wrapped = new SienaFutureWrapper<Map<Key, Entity>, List<T>>(future) {
            @Override
            protected List<T> wrap(Map<Key, Entity> entityMap) throws Exception
            {
            	List<T> models = new ArrayList<T>(entityMap.size());
            	for(Object key:keys){
        			models.add(GaeMappingUtils.mapEntity(entityMap.get(GaeMappingUtils.makeKey(clazz, key)), clazz));
        		}
        		
        		return models;
            }
		};
		
		return new SienaFutureContainer<List<T>>(wrapped);
	}

	public <T> SienaFuture<List<T>> getByKeys(final Class<T> clazz, final Iterable<?> keys) {
		List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(GaeMappingUtils.makeKey(clazz, key));
		}
		
		Future<Map<Key, Entity>> future = ds.get(gaeKeys);
		
		Future<List<T>> wrapped = new SienaFutureWrapper<Map<Key, Entity>, List<T>>(future) {
            @Override
            protected List<T> wrap(Map<Key, Entity> entityMap) throws Exception
            {
            	List<T> models = new ArrayList<T>(entityMap.size());
            	for(Object key:keys){
        			models.add(GaeMappingUtils.mapEntity(entityMap.get(GaeMappingUtils.makeKey(clazz, key)), clazz));
        		}
        		
        		return models;
            }
		};
		
		return new SienaFutureContainer<List<T>>(wrapped);
	}


	@Override
	public SienaFuture<Integer> update(Object... objects) {
		//throw new NotImplementedException("update not implemented for GAE yet");
		List<Entity> entities = new ArrayList<Entity>();
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			Entity entity = GaeMappingUtils.createEntityInstanceForUpdate(idField, info, obj);
			GaeMappingUtils.fillEntity(obj, entity);
			entities.add(entity);
		}
				
		Future<List<Key>> future = ds.put(entities);
		
		Future<Integer> wrapped = new SienaFutureWrapper<List<Key>, Integer>(future) {
            @Override
            protected Integer wrap(List<Key> keys) throws Exception
            {
            	return keys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}

	@Override
	public <T> SienaFuture<Integer> update(Iterable<T> objects) {
		//throw new NotImplementedException("update not implemented for GAE yet");
		List<Entity> entities = new ArrayList<Entity>();
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			Entity entity = GaeMappingUtils.createEntityInstanceForUpdate(idField, info, obj);
			GaeMappingUtils.fillEntity(obj, entity);
			entities.add(entity);
		}
				
		Future<List<Key>> future = ds.put(entities);
		
		Future<Integer> wrapped = new SienaFutureWrapper<List<Key>, Integer>(future) {
            @Override
            protected Integer wrap(List<Key> keys) throws Exception
            {
            	return keys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);	}

	@Override
	public <T> SienaFuture<Integer> update(QueryAsync<T> query,
			Map<String, ?> fieldValues) {
		throw new NotImplementedException("update not implemented for GAE yet");

	}

	public SienaFuture<Void> save(final Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		final Field idField = info.getIdField();
		
		final Entity entity;
		final Object idVal = Util.readField(obj, idField);
		// id with null value means insert
		if(idVal == null){
			entity = GaeMappingUtils.createEntityInstance(idField, info, obj);
		}
		// id with not null value means update
		else{
			entity = GaeMappingUtils.createEntityInstanceForUpdate(idField, info, obj);			
		}
		
		GaeMappingUtils.fillEntity(obj, entity);
		Future<Key> future = ds.put(entity);
		
		Future<Void> wrapped = new SienaFutureWrapper<Key, Void>(future) {
            @Override
            protected Void wrap(Key generatedKey) throws Exception
            {
            	if(idVal == null){
        			GaeMappingUtils.setIdFromKey(idField, obj, entity.getKey());
        		}
            	return null;
            }
		};
		
		return new SienaFutureContainer<Void>(wrapped);
	}

	public SienaFuture<Integer> save(final Object... objects) {
		List<Entity> entities = new ArrayList<Entity>();
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			
			Entity entity;
			Object idVal = Util.readField(obj, idField);
			// id with null value means insert
			if(idVal == null){
				entity = GaeMappingUtils.createEntityInstance(idField, info, obj);
			}
			// id with not null value means update
			else{
				entity = GaeMappingUtils.createEntityInstanceForUpdate(idField, info, obj);			
			}
			
			GaeMappingUtils.fillEntity(obj, entity);
			entities.add(entity);			
		}
		
		Future<List<Key>> future = ds.put(entities);
		
		Future<Integer> wrapped = new SienaFutureWrapper<List<Key>, Integer>(future) {
            @Override
            protected Integer wrap(List<Key> keys) throws Exception
            {
            	int i=0;
        		for(Object obj:objects){
        			Class<?> clazz = obj.getClass();
        			ClassInfo info = ClassInfo.getClassInfo(clazz);
        			Field idField = info.getIdField();
        			Object idVal = Util.readField(obj, idField);
        			if(idVal == null){
        				GaeMappingUtils.setIdFromKey(idField, obj, keys.get(i++));
        			}
        		}
        		return keys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}

	public SienaFuture<Integer> save(final Iterable<?> objects) {
		List<Entity> entities = new ArrayList<Entity>();
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			
			Entity entity;
			Object idVal = Util.readField(obj, idField);
			// id with null value means insert
			if(idVal == null){
				entity = GaeMappingUtils.createEntityInstance(idField, info, obj);
			}
			// id with not null value means update
			else{
				entity = GaeMappingUtils.createEntityInstanceForUpdate(idField, info, obj);			
			}
			
			GaeMappingUtils.fillEntity(obj, entity);
			entities.add(entity);			
		}
		
		Future<List<Key>> future = ds.put(entities);
		
		Future<Integer> wrapped = new SienaFutureWrapper<List<Key>, Integer>(future) {
            @Override
            protected Integer wrap(List<Key> keys) throws Exception
            {
            	int i=0;
        		for(Object obj:objects){
        			Class<?> clazz = obj.getClass();
        			ClassInfo info = ClassInfo.getClassInfo(clazz);
        			Field idField = info.getIdField();
        			Object idVal = Util.readField(obj, idField);
        			if(idVal == null){
        				GaeMappingUtils.setIdFromKey(idField, obj, keys.get(i++));
        			}
        		}
        		return keys.size();
            }
		};
		
		return new SienaFutureContainer<Integer>(wrapped);
	}

	private static String[] supportedOperators;

	static {
		supportedOperators = GaeQueryUtils.operators.keySet().toArray(new String[0]);
	}	

	@Override
	public String[] supportedOperators() {
		return supportedOperators;
	}


}
