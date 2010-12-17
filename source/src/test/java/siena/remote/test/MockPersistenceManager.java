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
package siena.remote.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import siena.PersistenceManager;
import siena.Query;
import siena.QueryFilter;
import siena.QueryOrder;
import siena.QuerySearch;

public class MockPersistenceManager implements PersistenceManager {
	
	public String action;
	public Object object;
	public MockQuery<?> lastQuery;
	
	public void initModel(Object obj) {
	}

	public void beginTransaction(int isolationLevel) {
	}

	public void closeConnection() {
	}

	public void commitTransaction() {
	}

	public <T> Query<T> createQuery(Class<T> clazz) {
		MockQuery<T> query = new MockQuery<T>();
		lastQuery = query;
		return query;
	}

	public void delete(Object obj) {
		action = "delete";
		object = obj;
	}

	public void get(Object obj) {
		action = "get";
		object = obj;
	}

	public void init(Properties p) {
	}

	public void insert(Object obj) {
		action = "insert";
		object = obj;
	}

	public void rollbackTransaction() {
	}

	public void update(Object obj) {
		action = "update";
		object = obj;
	}

	class MockQuery<T> implements Query<T> {

		public int limit;
		public Object offset;
		public List<String> orders = new ArrayList<String>();
		public List<Object[]> filters = new ArrayList<Object[]>();
		
		public int count() {
			return 0;
		}

		public int count(int limit) {
			this.limit = limit;
			return 0;
		}

		public int count(int limit, Object offset) {
			this.limit = limit;
			this.offset = offset;
			return 0;
		}

		public List<T> fetch() {
			return Collections.emptyList();
		}

		public List<T> fetch(int limit) {
			this.limit = limit;
			return Collections.emptyList();
		}

		public List<T> fetch(int limit, Object offset) {
			this.limit = limit;
			this.offset = offset;
			return Collections.emptyList();
		}

		public Query<T> filter(String fieldName, Object value) {
			filters.add(new Object[]{ fieldName, value });
			return this;
		}

		public T get() {
			return null;
		}

		public Iterable<T> iter(String field, int max) {
			return null;
		}

		public Query<T> order(String fieldName) {
			orders.add(fieldName);
			return this;
		}

		public Query<T> search(String match, boolean inBooleanMode,
				String... fieldNames) {
			return null;
		}
		
		public Query<T> clone() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Object nextOffset() {
			return null;
		}

		@Override
		public int delete() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<T> fetchKeys() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<T> fetchKeys(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<T> fetchKeys(int limit, Object offset) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<QueryFilter> getFilters() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<QueryOrder> getOrders() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<QuerySearch> getSearches() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> search(String match, boolean inBooleanMode, String index) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setNextOffset(Object nextOffset) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Class<T> getQueriedClass() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	@Override
	public <T> T get(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetch(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int count(Query<T> query) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int count(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int count(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
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
	public String[] supportedOperators() {
		// TODO Auto-generated method stub
		return null;
	}

}