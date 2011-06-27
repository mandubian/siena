/*
 * Copyright 2008 Alberto Gimeno <gimenete at gmail.com>
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
package siena;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import siena.core.Aggregated;
import siena.core.InheritFilter;
import siena.core.ListQuery;
import siena.core.ListQuery4PM;
import siena.core.async.ModelAsync;
import siena.core.async.QueryAsync;
import siena.core.batch.Batch;
import siena.core.options.QueryOption;
import siena.core.options.QueryOptionState;

/**
 * This is the base abstract class to extend your domain classes.
 * It's strongly recommended to implement the static method "all". Example:
 *
 * For example:
 * 
 * <code>
 * public static Query&lt;YourClass&gt; all() {
 *		return Model.all(YourClass.class);
 *	}
 * </code>
 * 
 * @author gimenete
 * @author mandubian
 *
 */
public abstract class Model {

	transient private PersistenceManager persistenceManager;

	public Model() {
		init();
	}

	public void get() {
		getPersistenceManager().get(this);
	}

	public void delete() {
		getPersistenceManager().delete(this);
	}

	public void insert() {
		getPersistenceManager().insert(this);
	}

	public void update() {
		getPersistenceManager().update(this);
	}
	
	public void save() {
		getPersistenceManager().save(this);
	}

	public final PersistenceManager getPersistenceManager() {
		if(persistenceManager == null) {
			persistenceManager = PersistenceManagerFactory.getPersistenceManager(getClass());
		}
		return persistenceManager;
	}

	public static <R> Query<R> all(Class<R> clazz) {
		return PersistenceManagerFactory.getPersistenceManager(clazz).createQuery(clazz);
	}
	
	public static <R> Batch<R> batch(Class<R> clazz) {
		return PersistenceManagerFactory.getPersistenceManager(clazz).createBatch(clazz);
	}

	public static <R> R getByKey(Class<R> clazz, Object key) {
		return PersistenceManagerFactory.getPersistenceManager(clazz).getByKey(clazz, key);
	}
	
	public ModelAsync async() {
		return new ModelAsync(this);
	}
	
	public boolean equals(Object that) {
		if(this == that) { return true; }
		if(that == null || that.getClass() != this.getClass()) { return false; }

		List<Field> keys = ClassInfo.getClassInfo(getClass()).keys;
		for (Field field : keys) {
			field.setAccessible(true);
			try {
				Object a = field.get(this);
				Object b = field.get(that);
				if(a == null ? b != null : !a.equals(b))
					{ return false; }
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return true;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;

		List<Field> keys = ClassInfo.getClassInfo(getClass()).keys;
		for (Field field : keys) {
			field.setAccessible(true);
			try {
				Object value = field.get(this);
				result = prime * result + ((value == null) ? 0 : value.hashCode());
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init() {
		// initialize Query<T> types
		Class<?> clazz = getClass();
		
		// Takes into account superclass fields for inheritance!!!!
		List<Class<?>> classH = new ArrayList<Class<?>>();
		Class<?> cl = clazz;
		Set<String> removedFields = new HashSet<String>();
        while (cl!=null) {
        	classH.add(0, cl);
        	// add exceptFields
        	InheritFilter iFilter = cl.getAnnotation(InheritFilter.class);
        	if(iFilter != null){
        		String[] efs = iFilter.removedFields();
	        	for(String ef:efs){
	        		removedFields.add(ef);
	        	}
        	}
        	cl = cl.getSuperclass();        	
        }
        
        for(Class<?> c: classH) {		
			for (Field field : c.getDeclaredFields()) {
				if(removedFields.contains(field.getName())) continue;	
				if(field.getType() == Query.class) { 
					Filter filter = field.getAnnotation(Filter.class);
					if(filter == null) {
						throw new SienaException("Found Query<T> field without @Filter annotation at "
								+c.getName()+"."+field.getName());
					}
		
					ParameterizedType pt = (ParameterizedType) field.getGenericType();
					cl = (Class<?>) pt.getActualTypeArguments()[0];
		
					try {
						field.set(this, new ProxyQuery(cl, filter.value(), this));
					} catch (Exception e) {
						throw new SienaException(e);
					}
					
				}else if(field.getType() == ListQuery.class){
					ParameterizedType pt = (ParameterizedType) field.getGenericType();
					cl = (Class<?>) pt.getActualTypeArguments()[0];
					
					Aggregated agg = field.getAnnotation(Aggregated.class);
					Filter filter = field.getAnnotation(Filter.class);
					if(agg!=null && filter!=null){
						throw new SienaException("Found ListQuery<T> field "
								+ c.getName()+"."+field.getName() 
								+ "with @Filter + @Aggregated: this is not authorized");
					}
					if(agg != null){
						try {
							field.set(this, new ProxyListQuery(cl, this, ProxyMode.AGGREGATION, field));
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}else if(filter != null){
						try {
							field.set(this, new ProxyQuery(cl, filter.value(), this));
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}
				}
			}
        }
	}

	class ProxyQuery<T> implements Query<T> {
		private static final long serialVersionUID = -7726081283511624780L;

		private String filter;
		private Class<T> clazz;
		private Model obj;
		private Query<T> query;
		
		public ProxyQuery(Class<T> clazz, String filter, Model obj) {
			this.filter = filter;
			this.clazz = clazz;
			this.obj = obj;
		}

		private Query<T> createQuery() {
			//return getPersistenceManager().createQuery(clazz).filter(filter, obj);
			
			// initializes once the query and reuses it
			// it is not initialized in the constructor because the persistencemanager might not be
			// initialized correctly with the Model
			if(this.query == null){
				this.query = obj.getPersistenceManager().createQuery(clazz);				
			}
			else if(((QueryOptionState)this.query.option(QueryOptionState.ID)).isStateless())
				this.query.release();
			return this.query.filter(filter, obj);
		}

		public int count() {
			return createQuery().count();
		}

		@Deprecated
		public int count(int limit) {
			return createQuery().count(limit);
		}

		@Deprecated
		public int count(int limit, Object offset) {
			return createQuery().count(limit, offset);
		}

		public List<T> fetch() {
			return createQuery().fetch();
		}

		public List<T> fetch(int limit) {
			return createQuery().fetch(limit);
		}

		public List<T> fetch(int limit, Object offset) {
			return createQuery().fetch(limit, offset);
		}

		public Query<T> filter(String fieldName, Object value) {
			return createQuery().filter(fieldName, value);
		}

		public Query<T> order(String fieldName) {
			return createQuery().order(fieldName);
		}

		@Deprecated
		public Query<T> search(String match, boolean inBooleanMode, String index) {
			return createQuery().search(match, inBooleanMode, index);
		}
		
		public Query<T> join(String field, String... sortFields) {
			return createQuery().join(field, sortFields);
		}

		public Query<T> aggregated(Object aggregator, String fieldName) {
			return createQuery().aggregated(aggregator, fieldName);
		}

		public T get() {
			return createQuery().get();
		}

		public Iterable<T> iter() {
			return createQuery().iter();
		}
		
		public Iterable<T> iter(int limit) {
			return createQuery().iter(limit);
		}
		
		public Iterable<T> iter(int limit, Object offset) {
			return createQuery().iter(limit, offset);
		}
		
		public Iterable<T> iterPerPage(int limit) {
			return createQuery().iterPerPage(limit);
		}	
		
		public ProxyQuery<T> copy() {
			return new ProxyQuery<T>(clazz, filter, obj);
		}

		@Deprecated
		public Object nextOffset() {
			return createQuery().nextOffset();
		}

		public int delete() {
			return createQuery().delete();
		}

		public List<T> fetchKeys() {
			return createQuery().fetchKeys();
		}

		public List<T> fetchKeys(int limit) {
			return createQuery().fetchKeys(limit);
		}

		public List<T> fetchKeys(int limit, Object offset) {
			return createQuery().fetchKeys(limit, offset);
		}

		public List<QueryFilter> getFilters() {
			return createQuery().getFilters();
		}

		public List<QueryOrder> getOrders() {
			return createQuery().getOrders();
		}

		public List<QueryFilterSearch> getSearches() {
			return createQuery().getSearches();
		}

		public List<QueryJoin> getJoins() {
			return createQuery().getJoins();
		}

		public List<QueryAggregated> getAggregatees() {
			return createQuery().getAggregatees();
		}

		@Deprecated
		public void setNextOffset(Object nextOffset) {
			createQuery().setNextOffset(nextOffset);
		}

		public Class<T> getQueriedClass() {
			return clazz;
		}

		public Query<T> paginate(int limit) {
			return createQuery().paginate(limit);
		}

		public Query<T> limit(int limit) {
			return createQuery().limit(limit);
		}

		public Query<T> offset(Object offset) {
			return createQuery().offset(offset);
		}

		public Query<T> customize(QueryOption... options) {
			return createQuery().customize(options);
		}

		public QueryOption option(int option) {
			return createQuery().option(option);
		}

		public Map<Integer, QueryOption> options() {
			return createQuery().options();
		}

		public Query<T> stateful() {
			return createQuery().stateful();
		}

		public Query<T> stateless() {
			return createQuery().stateless();
		}

		public Query<T> release() {
			return createQuery().release();
		}

		public Query<T> resetData() {
			return createQuery().resetData();
		}

		public Query<T> search(String match, String... fields) {
			return createQuery().search(match, fields);
		}

		public Query<T> search(String match, QueryOption opt, String... fields) {
			return createQuery().search(match, opt, fields);
		}

		public int update(Map<String, ?> fieldValues) {
			return createQuery().update(fieldValues);
		}

		public Query<T> nextPage() {
			return createQuery().nextPage();
		}

		public Query<T> previousPage() {
			return createQuery().previousPage();
		}

		public String dump() {
			return createQuery().dump();
		}

		public Query<T> restore(String dump) {
			return createQuery().restore(dump);
		}

		public QueryAsync<T> async() {
			return createQuery().async();
		}

		public T getByKey(Object key) {
			return createQuery().getByKey(key);
		}

		public PersistenceManager getPersistenceManager() {
			return obj.getPersistenceManager();
		}

		public String dump(QueryOption... options) {
			return createQuery().dump(options);
		}

		public void dump(OutputStream os, QueryOption... options) {
			createQuery().dump(os, options);
		}

		public Query<T> restore(String dump, QueryOption... options) {
			return createQuery().restore(dump, options);
		}

		public Query<T> restore(InputStream dump, QueryOption... options) {
			return createQuery().restore(dump, options);
		}

		
	}

	enum ProxyMode {
		FILTER,
		AGGREGATION
	}
	
	class ProxyListQuery<T> implements ListQuery4PM<T> {
		private static final long serialVersionUID = -4540064249546783019L;
		
		private Class<T> 		clazz;
		private Model 			obj;
		private ListQuery<T> 	listQuery;
		private ProxyMode 		mode;
		private Field			field;	

		public ProxyListQuery(Class<T> clazz, Model obj, ProxyMode mode, Field field) {
			this.clazz = clazz;
			this.obj = obj;
			this.mode = mode;
			this.field = field;
		}

		private Query<T> createQuery() {
			if(this.listQuery == null){
				this.listQuery = obj.getPersistenceManager().createListQuery(clazz);				
			}
			else if(((QueryOptionState)this.listQuery.option(QueryOptionState.ID)).isStateless())
				this.listQuery.release();
			if(mode == ProxyMode.AGGREGATION){
				return this.listQuery.aggregated(obj, field.getName());
			}
			
			return this.listQuery;
		}
		
		public Iterator<T> iterator() {
			return ((ListQuery<T>)createQuery()).iterator();
		}

		public PersistenceManager getPersistenceManager() {
			return obj.getPersistenceManager();
		}

		public List<T> elements() {
			return ((ListQuery<T>)createQuery()).elements();
		}

		public boolean isSync() {
			return ((ListQuery<T>)createQuery()).isSync();
		}

		public ListQuery4PM<T> setSync(boolean isSync) {
			return ((ListQuery4PM<T>)createQuery()).setSync(isSync);
		}

		public int count() {
			return createQuery().count();
		}

		@Deprecated
		public int count(int limit) {
			return createQuery().count(limit);
		}

		@Deprecated
		public int count(int limit, Object offset) {
			return createQuery().count(limit, offset);
		}

		public List<T> fetch() {
			return createQuery().fetch();
		}

		public List<T> fetch(int limit) {
			return createQuery().fetch(limit);
		}

		public List<T> fetch(int limit, Object offset) {
			return createQuery().fetch(limit, offset);
		}

		public Query<T> filter(String fieldName, Object value) {
			return createQuery().filter(fieldName, value);
		}

		public Query<T> order(String fieldName) {
			return createQuery().order(fieldName);
		}

		@Deprecated
		public Query<T> search(String match, boolean inBooleanMode, String index) {
			return createQuery().search(match, inBooleanMode, index);
		}
		
		public Query<T> join(String field, String... sortFields) {
			return createQuery().join(field, sortFields);
		}

		public Query<T> aggregated(Object aggregator, String fieldName) {
			return createQuery().aggregated(aggregator, fieldName);
		}

		public T get() {
			return createQuery().get();
		}

		public Iterable<T> iter() {
			return createQuery().iter();
		}
		
		public Iterable<T> iter(int limit) {
			return createQuery().iter(limit);
		}
		
		public Iterable<T> iter(int limit, Object offset) {
			return createQuery().iter(limit, offset);
		}
		
		public Iterable<T> iterPerPage(int limit) {
			return createQuery().iterPerPage(limit);
		}	
		
		public ProxyListQuery<T> copy() {
			return new ProxyListQuery<T>(clazz, obj, mode, field);
		}

		@Deprecated
		public Object nextOffset() {
			return createQuery().nextOffset();
		}

		public int delete() {
			return createQuery().delete();
		}

		public List<T> fetchKeys() {
			return createQuery().fetchKeys();
		}

		public List<T> fetchKeys(int limit) {
			return createQuery().fetchKeys(limit);
		}

		public List<T> fetchKeys(int limit, Object offset) {
			return createQuery().fetchKeys(limit, offset);
		}

		public List<QueryFilter> getFilters() {
			return createQuery().getFilters();
		}

		public List<QueryOrder> getOrders() {
			return createQuery().getOrders();
		}

		public List<QueryFilterSearch> getSearches() {
			return createQuery().getSearches();
		}

		public List<QueryJoin> getJoins() {
			return createQuery().getJoins();
		}

		public List<QueryAggregated> getAggregatees() {
			return createQuery().getAggregatees();
		}

		@Deprecated
		public void setNextOffset(Object nextOffset) {
			createQuery().setNextOffset(nextOffset);
		}

		public Class<T> getQueriedClass() {
			return clazz;
		}

		public Query<T> paginate(int limit) {
			return createQuery().paginate(limit);
		}

		public Query<T> limit(int limit) {
			return createQuery().limit(limit);
		}

		public Query<T> offset(Object offset) {
			return createQuery().offset(offset);
		}

		public Query<T> customize(QueryOption... options) {
			return createQuery().customize(options);
		}

		public QueryOption option(int option) {
			return createQuery().option(option);
		}

		public Map<Integer, QueryOption> options() {
			return createQuery().options();
		}

		public Query<T> stateful() {
			return createQuery().stateful();
		}

		public Query<T> stateless() {
			return createQuery().stateless();
		}

		public Query<T> release() {
			return createQuery().release();
		}

		public Query<T> resetData() {
			return createQuery().resetData();
		}

		public Query<T> search(String match, String... fields) {
			return createQuery().search(match, fields);
		}

		public Query<T> search(String match, QueryOption opt, String... fields) {
			return createQuery().search(match, opt, fields);
		}

		public int update(Map<String, ?> fieldValues) {
			return createQuery().update(fieldValues);
		}

		public Query<T> nextPage() {
			return createQuery().nextPage();
		}

		public Query<T> previousPage() {
			return createQuery().previousPage();
		}

		public String dump() {
			return createQuery().dump();
		}

		public Query<T> restore(String dump) {
			return createQuery().restore(dump);
		}

		public QueryAsync<T> async() {
			return createQuery().async();
		}

		public T getByKey(Object key) {
			return createQuery().getByKey(key);
		}

		public String dump(QueryOption... options) {
			return createQuery().dump(options);
		}

		public void dump(OutputStream os, QueryOption... options) {
			createQuery().dump(os, options);
		}

		public Query<T> restore(String dump, QueryOption... options) {
			return createQuery().restore(dump, options);
		}

		public Query<T> restore(InputStream dump, QueryOption... options) {
			return createQuery().restore(dump, options);
		}

		
	}
	
}
