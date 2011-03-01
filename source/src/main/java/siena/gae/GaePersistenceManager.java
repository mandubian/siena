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
package siena.gae;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Id;
import siena.Json;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryFilterSimple;
import siena.QueryJoin;
import siena.QueryOptionFetchType;
import siena.QueryOptionOffset;
import siena.QueryOptionPaginate;
import siena.QueryOptionReuse;
import siena.QueryOrder;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.Util;
import siena.embed.Embedded;
import siena.embed.JsonSerializer;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;

public class GaePersistenceManager extends AbstractPersistenceManager {

	private DatastoreService ds;
	
	public static final String DB = "GAE";

	public void beginTransaction(int isolationLevel) {
	}

	public void closeConnection() {
	}

	public void commitTransaction() {
	}

	public void delete(Object obj) {
		ds.delete(getKey(obj));
	}

	public void get(Object obj) {
		Key key = getKey(obj);
		try {
			Entity entity = ds.get(key);
			fillModel(obj, entity);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	public void init(Properties p) {
		ds = DatastoreServiceFactory.getDatastoreService();
	}

	public void insert(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		Field idField = info.getIdField();
		Entity entity = createEntityInstance(idField, info, obj);
		fillEntity(obj, entity);
		ds.put(entity);
		setKey(idField, obj, entity.getKey());
	}

	protected static Entity createEntityInstance(Field idField, ClassInfo info, Object obj){
		Entity entity = null;
		Id id = idField.getAnnotation(Id.class);
		if(id != null){
			switch(id.value()) {
			case NONE:
				Object idVal = null;
				try {
					idVal = readField(obj, idField);
				}catch(Exception ex){
					throw new SienaException("Id Field " + idField.getName() + " access error", ex);
				}
				if(idVal == null)
					throw new SienaException("Id Field " + idField.getName() + " value null");
				String keyVal = Util.toString(idField, idVal);				
				entity = new Entity(info.tableName, keyVal);
				break;
			case AUTO_INCREMENT:
				entity = new Entity(info.tableName);
				break;
			case UUID:
				entity = new Entity(info.tableName, UUID.randomUUID().toString());
				break;
			default:
				throw new SienaRestrictedApiException("DB", "createEntityInstance", "Id Generator "+id.value()+ " not supported");
			}
		}
		else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		
		return entity;
	}
	
	protected static void setKey(Field idField, Object obj, Key key) {
		Id id = idField.getAnnotation(Id.class);
		if(id != null){
			switch(id.value()) {
			case NONE:
				idField.setAccessible(true);
				Object val = null;
				if (idField.getType().isAssignableFrom(String.class))
					val = key.getName();
				else if (idField.getType().isAssignableFrom(Long.class))
					val = Long.parseLong((String) key.getName());
				else
					throw new SienaRestrictedApiException("DB", "setKey", "Id Type "+idField.getType()+ " not supported");
					
				try {
					idField.set(obj, val);
				}catch(Exception ex){
					throw new SienaException("Field " + idField.getName() + " access error", ex);
				}
				break;
			case AUTO_INCREMENT:
				// Long value means key.getId()
				try {
					idField.setAccessible(true);
					idField.set(obj, key.getId());
				}catch(Exception ex){
					throw new SienaException("Field " + idField.getName() + " access error", ex);
				}
				break;
			case UUID:
				try {
					idField.setAccessible(true);
					idField.set(obj, key.getName());					
				}catch(Exception ex){
					throw new SienaException("Field " + idField.getName() + " access error", ex);
				}
				break;
			default:
				throw new SienaException("Id Generator "+id.value()+ " not supported");
			}
		}
		else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
	}

	public void rollbackTransaction() {
	}

	public void update(Object obj) {
		try {
			Entity entity = new Entity(getKey(obj));
			fillEntity(obj, entity);
			ds.put(entity);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	protected static Key getKey(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		try {
			Field idField = info.getIdField();
			Object value = readField(obj, idField);
			
			if(idField.isAnnotationPresent(Id.class)){
				Id id = idField.getAnnotation(Id.class);
				switch(id.value()) {
				case NONE:
					// long or string goes toString
					return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							value.toString());
				case AUTO_INCREMENT:
					if (value instanceof String)
						value = Long.parseLong((String) value);
					return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							(Long)value);
				case UUID:
					return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							value.toString());
				default:
					throw new SienaException("Id Generator "+id.value()+ " not supported");
				}
			}
			else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	protected static Key makeKey(Class<?> clazz, Object value) {
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		try {
			Field idField = info.getIdField();
			
			if(idField.isAnnotationPresent(Id.class)){
				Id id = idField.getAnnotation(Id.class);
				switch(id.value()) {
				case NONE:
					// long or string goes toString
					return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							value.toString());
				case AUTO_INCREMENT:
					if (value instanceof String)
						value = Long.parseLong((String) value);
					return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							(Long)value);
				case UUID:
					return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							value.toString());
				default:
					throw new SienaException("Id Generator "+id.value()+ " not supported");
				}
			}
			else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	private static Object readField(Object object, Field field) {
		field.setAccessible(true);
		try {
			return field.get(object);
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			field.setAccessible(false);
		}
	}

	protected static void fillEntity(Object obj, Entity entity) {
		Class<?> clazz = obj.getClass();

		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			String property = ClassInfo.getColumnNames(field)[0];
			Object value = readField(obj, field);
			Class<?> fieldClass = field.getType();
			if (ClassInfo.isModel(fieldClass)) {
				if (value == null) {
					entity.setProperty(property, null);
				} else {
					Key key = getKey(value);
					entity.setProperty(property, key);
				}
			} else {
				if (value != null) {
					if (field.getType() == Json.class) {
						value = value.toString();
					} else if (value instanceof String) {
						String s = (String) value;
						if (s.length() > 500)
							value = new Text(s);
					} else if (value instanceof byte[]) {
						byte[] arr = (byte[]) value;
						// GAE Blob doesn't accept more than 1MB
						if (arr.length < 1000000)
							value = new Blob(arr);
						else
							value = new Blob(Arrays.copyOf(arr, 1000000));
					}
					else if (field.getAnnotation(Embedded.class) != null) {
						value = JsonSerializer.serialize(value).toString();
						String s = (String) value;
						if (s.length() > 500)
							value = new Text(s);
					}
					// enum is after embedded because an enum can be embedded
					// don't know if anyone will use it but it will work :)
					else if (Enum.class.isAssignableFrom(field.getType())) {
						value = value.toString();
					} 
				}
				Unindexed ui = field.getAnnotation(Unindexed.class);
				if (ui == null) {
					entity.setProperty(property, value);
				} else {
					entity.setUnindexedProperty(property, value);
				}
			}
		}
	}

	protected static void fillModel(Object obj, Entity entity) {
		Class<?> clazz = obj.getClass();

		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			field.setAccessible(true);
			String property = ClassInfo.getColumnNames(field)[0];
			try {
				Class<?> fieldClass = field.getType();
				if (ClassInfo.isModel(fieldClass)) {
					Key key = (Key) entity.getProperty(property);
					if (key != null) {
						Object value = fieldClass.newInstance();
						Field id = ClassInfo.getIdField(fieldClass);
						setKey(id, value, key);
						field.set(obj, value);
					}
				} else {
					setFromObject(obj, field, entity.getProperty(property));
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
	}

	protected static void setFromObject(Object object, Field f, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		if(value instanceof Text)
			value = ((Text) value).getValue();
		else if(value instanceof Blob && f.getType() == byte[].class) {
			value = ((Blob) value).getBytes();
		}
		Util.setFromObject(object, f, value);
	}

	protected DatastoreService getDatastoreService() {
		return ds;
	}

	protected static <T> List<T> mapEntities(List<Entity> entities,
			Class<T> clazz) {
		Field id = ClassInfo.getIdField(clazz);
		List<T> list = new ArrayList<T>(entities.size());
		for (Entity entity : entities) {
			T obj;
			try {
				obj = clazz.newInstance();
				fillModel(obj, entity);
				list.add(obj);
				setKey(id, obj, entity.getKey());
			} catch (SienaException e) {
				throw e;
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return list;
	}

	protected static <T> List<T> mapEntitiesKeysOnly(List<Entity> entities,
			Class<T> clazz) {
		Field id = ClassInfo.getIdField(clazz);
		List<T> list = new ArrayList<T>(entities.size());
		for (Entity entity : entities) {
			T obj;
			try {
				obj = clazz.newInstance();
				list.add(obj);
				setKey(id, obj, entity.getKey());
			} catch (SienaException e) {
				throw e;
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return list;
	}
	
	protected static <T> com.google.appengine.api.datastore.Query 
			addFiltersOrders(
					Query<T> query, 
					com.google.appengine.api.datastore.Query q) 
	{
		List<QueryFilter> filters = query.getFilters();
		for (QueryFilter filter : filters) {
			if(QueryFilterSimple.class.isAssignableFrom(filter.getClass())){
				QueryFilterSimple qf = (QueryFilterSimple)filter;
				Field f = qf.field;
				String propertyName = ClassInfo.getColumnNames(f)[0];
				Object value = qf.value;
				FilterOperator op = operators.get(qf.operator);
				
				// IN and NOT_EQUAL doesn't allow to use cursors
				if(op == FilterOperator.IN || op == FilterOperator.NOT_EQUAL){
					QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
					gaeCtx.useCursor = false;
				}
				
				if (value != null && ClassInfo.isModel(value.getClass())) {
					Key key = getKey(value);
					q.addFilter(propertyName, op, key);
				} else {
					if (ClassInfo.isId(f)) {
						Id id = f.getAnnotation(Id.class);
						switch(id.value()) {
						case NONE:
							if(!Collection.class.isAssignableFrom(value.getClass())){
								// long or string goes toString
								Key key = KeyFactory.createKey(
										q.getKind(),
										value.toString());
								q.addFilter(Entity.KEY_RESERVED_PROPERTY, op, key);
							}else {
								List<Key> keys = new ArrayList<Key>();
								for(Object val: (Collection<?>)value) {
									keys.add(KeyFactory.createKey(q.getKind(), val.toString()));
								}
								q.addFilter(Entity.KEY_RESERVED_PROPERTY, op, keys);
							}
							break;
						case AUTO_INCREMENT:
							if(!Collection.class.isAssignableFrom(value.getClass())){
								if (value instanceof String)
									value = Long.parseLong((String) value);
								Key key = KeyFactory.createKey(
										q.getKind(),
										(Long)value);
								q.addFilter(Entity.KEY_RESERVED_PROPERTY, op, key);
							}else {
								List<Key> keys = new ArrayList<Key>();
								for(Object val: (Collection<?>)value) {
									if (value instanceof String)
										val = Long.parseLong((String) val);
									keys.add(KeyFactory.createKey(q.getKind(), (Long)val));
								}
								q.addFilter(Entity.KEY_RESERVED_PROPERTY, op, keys);
							}
							break;
						case UUID:
							if(!Collection.class.isAssignableFrom(value.getClass())){
								// long or string goes toString
								Key key = KeyFactory.createKey(
										q.getKind(),
										value.toString());
								q.addFilter(Entity.KEY_RESERVED_PROPERTY, op, key);
							}else {
								List<Key> keys = new ArrayList<Key>();
								for(Object val: (Collection<?>)value) {
									keys.add(KeyFactory.createKey(q.getKind(), val.toString()));
								}
								q.addFilter(Entity.KEY_RESERVED_PROPERTY, op, keys);
							}
							break;
						default:
							throw new SienaException("Id Generator "+id.value()+ " not supported");
						}

					} else if (Enum.class.isAssignableFrom(f.getType())) {
						value = value.toString();
						q.addFilter(propertyName, op, value);
					} else {
						q.addFilter(propertyName, op, value);
					}
				}
			}else if(QueryFilterSearch.class.isAssignableFrom(filter.getClass())){
				Class<T> clazz = query.getQueriedClass();
				QueryFilterSearch qf = (QueryFilterSearch)filter;
				if(qf.fields.length>1)
					throw new SienaRestrictedApiException(DB, "addFiltersOrders", "Search not possible for several fields in GAE: only one field");
				try {
					Field field = clazz.getDeclaredField(qf.fields[0]);
					if(field.isAnnotationPresent(Unindexed.class)){
						throw new SienaException("Cannot search the @Unindexed field "+field.getName());
					}
					
					// cuts match into words
					String[] words = qf.match.split("\\s");
					
					// if several words, then only OR operator represented by IN GAE
					Pattern pNormal = Pattern.compile("[^\\*](\\w+)[^\\*]");
					if(words.length>1){
						for(String word:words){
							if(!pNormal.matcher(word).matches()){
								throw new SienaException("Cannot do a multiwords search with the * operator");
							}
						}
						List<String> wordList = new ArrayList<String>();
						Collections.addAll(wordList, words);
						addSearchFilterIn(q, field, wordList);
					}else {
						// searches for pattern such as "alpha*" or "*alpha" or "alpha"
						Pattern pStart = Pattern.compile("(\\w+)\\*");

						String word = words[0];
						Matcher matcher = pStart.matcher(word);
						if(matcher.matches()){
							String realWord = matcher.group(1);
							addSearchFilterBeginsWith(q, field, realWord);
							continue;
						}
						
						matcher = pNormal.matcher(word);
						if(matcher.matches()){
							addSearchFilterEquals(q, field, word);
							continue;
						}
						
						Pattern pEnd = Pattern.compile("\\*(\\w+)");
						matcher = pEnd.matcher(word);
						if(matcher.matches()){
							throw new SienaException("Cannot do a \"*word\" search in GAE");
						} 						
					}					
				}catch(Exception e){
					throw new SienaException(e);
				}
				break;
			}
		}

		List<QueryOrder> orders = query.getOrders();
		for (QueryOrder order : orders) {
			Field f = order.field;
			if (ClassInfo.isId(f)) {
				q.addSort(Entity.KEY_RESERVED_PROPERTY,
						order.ascending ? SortDirection.ASCENDING
								: SortDirection.DESCENDING);
			} else {
				q.addSort(ClassInfo.getColumnNames(f)[0],
						order.ascending ? SortDirection.ASCENDING
								: SortDirection.DESCENDING);
			}
		}

		return q;
	}
	
	private static void addSearchFilterBeginsWith(com.google.appengine.api.datastore.Query q, Field field, String match) 
	{
		String[] columns = ClassInfo.getColumnNames(field);
		if(columns.length>1)
			throw new SienaException("Search not possible for multi-column fields in GAE: only one field with one column");
		q.addFilter(columns[0], FilterOperator.GREATER_THAN_OR_EQUAL, match);
		q.addFilter(columns[0], FilterOperator.LESS_THAN, match + "\ufffd");
	}
	
	private static void addSearchFilterEquals(com.google.appengine.api.datastore.Query q, Field field, String match) 
	{
		String[] columns = ClassInfo.getColumnNames(field);
		if(columns.length>1)
			throw new SienaException("Search not possible for multi-column fields in GAE: only one field with one column");
		q.addFilter(columns[0], FilterOperator.EQUAL, match);
	}

	
	private static void addSearchFilterIn(com.google.appengine.api.datastore.Query q, Field field, List<String> matches) 
	{
		String[] columns = ClassInfo.getColumnNames(field);
		if(columns.length>1)
			throw new SienaException("Search not possible for multi-column fields in GAE: only one field with one column");
		q.addFilter(columns[0], FilterOperator.IN, matches);
	}
	
	private <T> PreparedQuery prepare(Query<T> query) {
		Class<?> clazz = query.getQueriedClass();
		com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query(
				ClassInfo.getClassInfo(clazz).tableName);

		return ds.prepare(addFiltersOrders(query, q));
	}

	private <T> PreparedQuery prepareKeysOnly(Query<T> query) {
		Class<?> clazz = query.getQueriedClass();
		com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query(
				ClassInfo.getClassInfo(clazz).tableName);

		return ds.prepare(addFiltersOrders(query, q).setKeysOnly());
	}

	
	protected <T> List<T> mapJoins(Query<T> query, List<T> models) {
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
					Key key = getKey(field.get(model));
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
					Key key = getKey(objVal);
					linkedObj = linkedModels.get(key);
					if(linkedObj==null){
						entity = entityMap.get(field).get(key);
						linkedObj = objVal;
						fillModel(linkedObj, entity);
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
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> map(Query<T> query, int offset,
			List<Entity> entities) {
		Class<?> clazz = query.getQueriedClass();
		List<T> result = (List<T>) mapEntities(entities, clazz);
		//query.setNextOffset(offset + result.size());
		
		// join management
		if(!query.getJoins().isEmpty() || ClassInfo.getClassInfo(clazz).joinFields.size() != 0)
			return mapJoins(query, result);
		
		return result;
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> mapKeysOnly(Query<T> query, int offset,
			List<Entity> entities) {
		Class<?> clazz = query.getQueriedClass();
		List<T> result = (List<T>) mapEntitiesKeysOnly(entities, clazz);
		//query.setNextOffset(offset + result.size());
		return result;
	}

	private <T> Iterable<T> doFetch(Query<T> query) {
		QueryOptionPaginate pag = (QueryOptionPaginate)query.option(QueryOptionPaginate.ID);
		QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		QueryOptionReuse reuse = (QueryOptionReuse)query.option(QueryOptionReuse.ID);
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
					return new SienaGaeIterable<T>(results, query.getQueriedClass());
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
						return new SienaGaeIterable<T>(results, query.getQueriedClass());
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
						return new SienaGaeIterable<T>(results, query.getQueriedClass());
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
	}
	
	@Override
	public <T> List<T> fetch(Query<T> query) {
		return (List<T>)doFetch(query);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		((QueryOptionPaginate)query.option(QueryOptionPaginate.ID).activate()).pageSize=limit;
		return (List<T>)doFetch(query);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		((QueryOptionPaginate)query.option(QueryOptionPaginate.ID).activate()).pageSize=limit;
		((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;
		return (List<T>)doFetch(query);
	}

	@Override
	public <T> int count(Query<T> query) {
		return prepare(query)
				.countEntities(FetchOptions.Builder.withDefaults());
	}

	@Override
	public <T> int delete(Query<T> query) {
		final ArrayList<Key> keys = new ArrayList<Key>();

		for (final Entity entity : prepareKeysOnly(query).asIterable(
				FetchOptions.Builder.withDefaults())) {
			keys.add(entity.getKey());
		}

		ds.delete(keys);

		return keys.size();
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.KEYS_ONLY;

		return (List<T>)doFetch(query);
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.KEYS_ONLY;
		((QueryOptionPaginate)query.option(QueryOptionPaginate.ID).activate()).pageSize=limit;

		return (List<T>)doFetch(query);
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.KEYS_ONLY;
		((QueryOptionPaginate)query.option(QueryOptionPaginate.ID).activate()).pageSize=limit;
		((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;

		return (List<T>)doFetch(query);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.ITER;

		return (Iterable<T>)doFetch(query);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.ITER;
		((QueryOptionPaginate)query.option(QueryOptionPaginate.ID).activate()).pageSize=limit;

		return (Iterable<T>)doFetch(query);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).type=QueryOptionFetchType.Type.ITER;
		((QueryOptionPaginate)query.option(QueryOptionPaginate.ID).activate()).pageSize=limit;
		((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;

		return (Iterable<T>)doFetch(query);
	}


	@Override
	public <T> void release(Query<T> query) {
		super.release(query);
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);

		if(gaeCtx != null){
			gaeCtx.cursor = null;
			gaeCtx.passivate();
		}
	}
	
	@Override
	public void insert(Object... objects) {
		List<Entity> entities = new ArrayList<Entity>(objects.length);
		for(int i=0; i<objects.length;i++){
			Class<?> clazz = objects[i].getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			Entity entity = createEntityInstance(idField, info, objects[i]);
			fillEntity(objects[i], entity);
			entities.add(entity);
		}
				
		ds.put(entities);
	}

	@Override
	public void insert(Iterable<?> objects) {
		List<Entity> entities = new ArrayList<Entity>();
		for(Object obj:objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			Field idField = info.getIdField();
			Entity entity = createEntityInstance(idField, info, obj);
			fillEntity(obj, entity);
			entities.add(entity);
		}
				
		ds.put(entities);
	}

	@Override
	public void delete(Object... models) {
		List<Key> keys = new ArrayList<Key>();
		for(Object obj:models){
			keys.add(getKey(obj));
		}
		
		ds.delete(keys);
	}


	@Override
	public void delete(Iterable<?> models) {
		List<Key> keys = new ArrayList<Key>();
		for(Object obj:models){
			keys.add(getKey(obj));
		}
		
		ds.delete(keys);
	}


	@Override
	public <T> void deleteByKeys(Class<T> clazz, Object... keys) {
		List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(makeKey(clazz, key));
		}
		
		ds.delete(gaeKeys);
	}

	@Override
	public <T> void deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		List<Key> gaeKeys = new ArrayList<Key>();
		for(Object key:keys){
			gaeKeys.add(makeKey(clazz, key));
		}
		
		ds.delete(gaeKeys);
	}

	@Override
	public void update(Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		
	}

	private static final Map<String, FilterOperator> operators = new HashMap<String, FilterOperator>() {
		private static final long serialVersionUID = 1L;
		{
			put("=", FilterOperator.EQUAL);
			put("!=", FilterOperator.NOT_EQUAL);
			put("<", FilterOperator.LESS_THAN);
			put(">", FilterOperator.GREATER_THAN);
			put("<=", FilterOperator.LESS_THAN_OR_EQUAL);
			put(">=", FilterOperator.GREATER_THAN_OR_EQUAL);
			put(" IN", FilterOperator.IN);
		}
	};

	private static String[] supportedOperators;

	static {
		supportedOperators = operators.keySet().toArray(new String[0]);
	}

	@Override
	public String[] supportedOperators() {
		return supportedOperators;
	}

	/**
	 * @author mandubian
	 * 
	 *         A Siena Iterable<Model> encapsulating a GAE Iterable<Entity> with
	 *         its Iterator<Model>...
	 */
	public static class SienaGaeIterable<Model> implements Iterable<Model> {
		Iterable<Entity> gaeIterable;
		Class<Model> clazz;

		SienaGaeIterable(Iterable<Entity> gaeIterable, Class<Model> clazz) {
			this.gaeIterable = gaeIterable;
			this.clazz = clazz;
		}

		@Override
		public Iterator<Model> iterator() {
			return new SienaGaeIterator<Model>(gaeIterable.iterator(), clazz);
		}

		public class SienaGaeIterator<T> implements Iterator<T> {
			Iterator<Entity> gaeIterator;
			Class<T> clazz;
			Field id;

			SienaGaeIterator(Iterator<Entity> gaeIterator, Class<T> clazz) {
				this.gaeIterator = gaeIterator;
				this.clazz = clazz;
				this.id = ClassInfo.getIdField(clazz);
			}

			@Override
			public boolean hasNext() {
				return gaeIterator.hasNext();
			}

			@Override
			public T next() {
				T obj;

				try {
					obj = clazz.newInstance();
					Entity entity = gaeIterator.next();
					fillModel(obj, entity);
					setKey(id, obj, entity.getKey());

					return obj;
				} catch (IllegalAccessException e) {
					throw new SienaException(e);
				} catch (InstantiationException e) {
					throw new SienaException(e);
				} 
			}

			@Override
			public void remove() {
				gaeIterator.remove();
			}

		}

	}

}
