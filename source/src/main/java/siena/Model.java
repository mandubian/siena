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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import siena.core.async.ModelAsync;
import siena.core.async.QueryAsync;
import siena.core.batch.Batch;
import siena.core.options.QueryOption;

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
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if(field.getType() != Query.class) { continue; }

			Filter filter = field.getAnnotation(Filter.class);
			if(filter == null) {
				throw new SienaException("Found Query<T> field without @Filter annotation at "
						+clazz.getName()+"."+field.getName());
			}

			ParameterizedType pt = (ParameterizedType) field.getGenericType();
			Class<?> c = (Class<?>) pt.getActualTypeArguments()[0];

			try {
				field.set(this, new ProxyQuery(c, filter.value(), this));
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
	}

	class ProxyQuery<T> implements Query<T> {

		private String filter;
		private Class<T> clazz;
		private Model obj;

		public ProxyQuery(Class<T> clazz, String filter, Model obj) {
			this.filter = filter;
			this.clazz = clazz;
			this.obj = obj;
		}

		private Query<T> createQuery() {
			return getPersistenceManager().createQuery(clazz).filter(filter, obj);
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

		public ProxyQuery<T> clone() {
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

		@Deprecated
		public void setNextOffset(Object nextOffset) {
			createQuery().setNextOffset(nextOffset);
		}

		public Class<T> getQueriedClass() {
			return clazz;
		}

		public Object raw(String request) {
			return createQuery().raw(request);
		}

		public Query<T> paginate(int limit) {
			return createQuery().paginate(limit);
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

		public PersistenceManager getPersistenceManager() {
			return obj.getPersistenceManager();
		}	
		
	}

}
