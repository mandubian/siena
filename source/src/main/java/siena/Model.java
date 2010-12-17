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
import java.util.Collections;
import java.util.List;

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
 *
 */
public abstract class Model {

	private PersistenceManager persistenceManager;

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

	public boolean equals(Object that) {
		if(this == that) return true;
		if(that == null || that.getClass() != this.getClass()) return false;

		List<Field> keys = ClassInfo.getClassInfo(getClass()).keys;
		for (Field field : keys) {
			field.setAccessible(true);
			try {
				Object a = field.get(this);
				Object b = field.get(that);
				if(a == null ? b != null : !a.equals(b))
					return false;
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
			if(field.getType() != Query.class) continue;

			Filter filter = field.getAnnotation(Filter.class);
			if(filter == null)
				throw new SienaException("Found Query<T> field without @Filter annotation at "
						+clazz.getName()+"."+field.getName());

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

		public int count(int limit) {
			return createQuery().count(limit);
		}

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

		public T get() {
			return createQuery().get();
		}

		public Iterable<T> iter(String field, int max) {
			return createQuery().iter(field, max);
		}

		public Query<T> order(String fieldName) {
			return createQuery().order(fieldName);
		}

		public ProxyQuery<T> clone() {
			return new ProxyQuery<T>(clazz, filter, obj);
		}

		@Override
		public Object nextOffset() {
			return null; // TODO
		}

		@Override
		public int delete() {
			return createQuery().delete();
		}

		@Override
		public List<T> fetchKeys() {
			return createQuery().fetchKeys();
		}

		@Override
		public List<T> fetchKeys(int limit) {
			return createQuery().fetchKeys(limit);
		}

		@Override
		public List<T> fetchKeys(int limit, Object offset) {
			return createQuery().fetchKeys(limit, offset);
		}

		@Override
		public List<QueryFilter> getFilters() {
			return Collections.emptyList();
		}

		@Override
		public List<QueryOrder> getOrders() {
			return Collections.emptyList();
		}

		@Override
		public List<QuerySearch> getSearches() {
			return Collections.emptyList();
		}

		@Override
		public Query<T> search(String match, boolean inBooleanMode, String index) {
			return createQuery().search(match, inBooleanMode, index);
		}

		@Override
		public void setNextOffset(Object nextOffset) {
		}

		@Override
		public Class<T> getQueriedClass() {
			return clazz;
		}
	}

}