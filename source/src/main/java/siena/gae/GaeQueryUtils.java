package siena.gae;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import siena.ClassInfo;
import siena.Id;
import siena.QueryAggregated;
import siena.QueryData;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryFilterSimple;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.Util;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

public class GaeQueryUtils {
	public static final Map<String, FilterOperator> operators = new HashMap<String, FilterOperator>() {
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

	
	public static <T> com.google.appengine.api.datastore.Query 
			addFiltersOrders(
					QueryData<T> query, 
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
					query.option(QueryOptionOffset.ID).activate();					
				}
				
				if (value != null && ClassInfo.isModel(value.getClass())) {
					Key key = GaeMappingUtils.getKey(value);
					q.addFilter(propertyName, op, key);
				} else {
					if (ClassInfo.isId(f)) {
						Id id = f.getAnnotation(Id.class);
						switch(id.value()) {
						case NONE:
							if(value != null){
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
							}
							break;
						case AUTO_INCREMENT:
							if(value != null){
								if(!Collection.class.isAssignableFrom(value.getClass())){
									Key key; 
									Class<?> type = f.getType();
	
									if(Long.TYPE == type || Long.class.isAssignableFrom(type)){
										key = KeyFactory.createKey(
												q.getKind(),
												(Long)value);
									} else {
										key = KeyFactory.createKey(
												q.getKind(),
												value.toString());									
									}
									
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
							}
							break;
						case UUID:
							if(value != null) {
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
					throw new SienaException("Search not possible for several fields in GAE: only one field");
				try {
					Field field = Util.getField(clazz, qf.fields[0]);
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

	public static void addSearchFilterBeginsWith(com.google.appengine.api.datastore.Query q, Field field, String match) 
	{
		String[] columns = ClassInfo.getColumnNames(field);
		if(columns.length>1)
			throw new SienaException("Search not possible for multi-column fields in GAE: only one field with one column");
		q.addFilter(columns[0], FilterOperator.GREATER_THAN_OR_EQUAL, match);
		q.addFilter(columns[0], FilterOperator.LESS_THAN, match + "\ufffd");
	}

	public static void addSearchFilterEquals(com.google.appengine.api.datastore.Query q, Field field, String match) 
	{
		String[] columns = ClassInfo.getColumnNames(field);
		if(columns.length>1)
			throw new SienaException("Search not possible for multi-column fields in GAE: only one field with one column");
		q.addFilter(columns[0], FilterOperator.EQUAL, match);
	}


	public static void addSearchFilterIn(com.google.appengine.api.datastore.Query q, Field field, List<String> matches) 
	{
		String[] columns = ClassInfo.getColumnNames(field);
		if(columns.length>1)
			throw new SienaException("Search not possible for multi-column fields in GAE: only one field with one column");
		q.addFilter(columns[0], FilterOperator.IN, matches);
	}

	public static <T> Map<Field, ArrayList<Key>> buildJoinFieldKeysMap(QueryData<T> query){
		List<QueryJoin> joins = query.getJoins();
		
		// join queries
		Map<Field, ArrayList<Key>> fieldMap = new HashMap<Field, ArrayList<Key>>();
		for (QueryJoin join : joins) {
			Field field = join.field;
			if (!ClassInfo.isModel(field.getType())){
				throw new SienaRestrictedApiException(GaePersistenceManager.DB, "join", "Join not possible: Field "+field.getName()+" is not a relation field");
			}
			else if(join.sortFields!=null && join.sortFields.length!=0)
				throw new SienaRestrictedApiException(GaePersistenceManager.DB, "join", "Join not allowed with sort fields");
			fieldMap.put(field, new ArrayList<Key>());
		}
		
		// join annotations
		for(Field field: 
			ClassInfo.getClassInfo(query.getQueriedClass()).joinFields)
		{
			fieldMap.put(field, new ArrayList<Key>());
		}
		
		return fieldMap;
	}
	
	public static <T> Map<Field, ArrayList<Key>> buildJoinFieldKeysMap(T model){
		// join queries
		Map<Field, ArrayList<Key>> fieldMap = new HashMap<Field, ArrayList<Key>>();
				
		// join annotations
		for(Field field: 
			ClassInfo.getClassInfo(model.getClass()).joinFields)
		{
			fieldMap.put(field, new ArrayList<Key>());
		}
		
		return fieldMap;
	}

	public static <T> void paginate(QueryData<T> query) {
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		if(gaeCtx==null){
			gaeCtx = new QueryOptionGaeContext();
			query.options().put(gaeCtx.type, gaeCtx);
		}
		
		// resets the realoffset to 0 if stateless
		if(state.isStateless()){
			gaeCtx.realOffset = 0;
		}
	}
	
	public static <T> void nextPage(QueryData<T> query) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		if(gaeCtx==null){
			gaeCtx = new QueryOptionGaeContext();
			query.options().put(gaeCtx.type, gaeCtx);
		}
		
		// if no more data after, doesn't try to go after
		if(gaeCtx.noMoreDataAfter){
			return;
		}
		
		// if no more data before, removes flag to be able and stay there
		if(gaeCtx.noMoreDataBefore){
			gaeCtx.noMoreDataBefore = false;
			return;
		}
		
		if(pag.isPaginating()){
			gaeCtx.realPageSize = pag.pageSize;
			if(state.isStateless()) {
				//QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
				//if(offset.isActive()){
				gaeCtx.realOffset+=pag.pageSize;
				//}
			}			
			else {
				if(!gaeCtx.isActive()){
					QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
					if(!gaeCtx.useCursor){
						// then uses offset (in case of IN or != operators)
						//if(offset.isActive()){
						gaeCtx.realOffset+=pag.pageSize;
						//}
					}
					// if the cursor is used, just passivates the offset
					else {
						offset.passivate();
						// keeps track of the offset anyway
						gaeCtx.realOffset+=pag.pageSize;
					}
				}else {
					QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
					// if there is a next cursor, we prefer using it because it can mean it was a 
					// cursor added by a previousPage call which tries to go backward the first page
					if(!gaeCtx.useCursor && !gaeCtx.hasNextCursor()){
						// then uses offset (in case of IN or != operators)
						//if(offset.isActive()){
						gaeCtx.realOffset+=pag.pageSize;
						//}
					}else{
						// forces cursor to be sure it is used
						gaeCtx.useCursor = true;
						String cursor = gaeCtx.nextCursor();
						// if the cursor is null, it means we are back to the first page so we reactivate the offset
						gaeCtx.realOffset+=pag.pageSize;
						if(cursor==null){
							offset.activate();
						}else {
							offset.passivate();
						}
					}
				}
			}
		}else {
			// throws exception because it's impossible to reuse nextPage when paginating has been interrupted, the cases are too many
			throw new SienaException("Can't use nextPage after pagination has been interrupted...");
		}
	}

	public static <T> void previousPage(QueryData<T> query) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		if(gaeCtx==null){
			gaeCtx = new QueryOptionGaeContext();
			query.options().put(gaeCtx.type, gaeCtx);
		}
		
		// if no more data before, doesn't try to go before
		if(gaeCtx.noMoreDataBefore){
			return;
		}
		
		// if no more data after, removes flag to be able to go before
		if(gaeCtx.noMoreDataAfter){
			gaeCtx.noMoreDataAfter = false;
		}
		
		if(pag.isPaginating()){
			gaeCtx.realPageSize = pag.pageSize;
			if(state.isStateless()) {
				//QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
				//if(offset.isActive()){
					if(gaeCtx.realOffset>=pag.pageSize) {
						gaeCtx.realOffset-=pag.pageSize;
					}
					else {
						gaeCtx.realOffset = 0;
						gaeCtx.noMoreDataBefore = true;
					}
				//}
			}			
			else {
				if(!gaeCtx.isActive()){
					if(!gaeCtx.useCursor){
						// then uses offset (in case of IN or != operators)
						//QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
						//if(offset.isActive()){
							if(gaeCtx.realOffset>=pag.pageSize) {
								gaeCtx.realOffset-=pag.pageSize;
							}
							else {
								gaeCtx.realOffset = 0;
								gaeCtx.noMoreDataBefore = true;
							}
						//}
					}
					// if the cursor is active, verifies this is not the first page 
					// with the offset (active or passive) and sets noMoreData in this case
					else {
						//QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
						if(gaeCtx.realOffset==0) {
							gaeCtx.noMoreDataBefore = true;
						}
					}
					
				}else {
					QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
					if(!gaeCtx.useCursor){
						// then uses offset (in case of IN or != operators)
						//if(offset.isActive()){
							if(gaeCtx.realOffset>=pag.pageSize) {
								gaeCtx.realOffset-=pag.pageSize;
							}
							// passivates offset and computes the page before because we are at the first page
							else{
								offset.passivate();
								gaeCtx.noMoreDataBefore = true;								
								previousPage(query);
							}
						//}
					}else{
						String cursor = gaeCtx.previousCursor();
						// if the cursor is null, it means we are back to the first page 
						// so we reactivate the offset and deactivate the useCursor 
						// and recall the previousPage with the offset mechanism 
						if(cursor==null){
							offset.activate();
							gaeCtx.useCursor = false;
							previousPage(query);
						}else {
							offset.passivate();
							gaeCtx.useCursor = true;
							if(gaeCtx.realOffset>=pag.pageSize) {
								gaeCtx.realOffset-=pag.pageSize;
							}
							// passivates offset and computes the page before because we are at the first page
							else{
								gaeCtx.noMoreDataBefore = true;								
								previousPage(query);
							}
							//previousPage(query);
						}
						
					}
				}
			}
		} else {
			// throws exception because it's impossible to reuse nextPage when paginating has been interrupted, the cases are too many
			throw new SienaException("Can't use nextPage after pagination has been interrupted...");
		}
	}
	
	public static <T> void release(QueryData<T> query) {
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);

		if(gaeCtx != null){
			gaeCtx.cursors.clear();
			gaeCtx.passivate();
		}
	}
/*	
	public static <T> List<T> mapKeysOnly(QueryData<T> query, Iterable<Entity> entities) {
		Class<?> clazz = query.getQueriedClass();
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) GaeMappingUtils.mapEntitiesKeysOnly(entities, clazz);
		return result;
	}
	
	public static <T> List<T> mapKeysOnly(QueryData<T> query, QueryResultList<Entity> entities) {
		Class<?> clazz = query.getQueriedClass();
		
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) GaeMappingUtils.mapEntitiesKeysOnly(entities, clazz);
		return result;
	}
	*/
}
