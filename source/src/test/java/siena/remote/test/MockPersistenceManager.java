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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.BaseQueryData;
import siena.PersistenceManager;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.core.ListQuery;
import siena.core.async.PersistenceManagerAsync;
import siena.core.async.QueryAsync;
import siena.core.batch.Batch;
import siena.core.options.QueryOption;

public class MockPersistenceManager implements PersistenceManager {
	
	public String action;
	public Object object;
	public MockQuery<?> lastQuery;
	
	public void initModel(Object obj) {
	}

	public void beginTransaction(int isolationLevel) {
	}
	
	public void beginTransaction() {
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
		public Iterable<T> iter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<T> iter(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<T> iter(int limit, Object offset) {
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
		public List<QueryFilterSearch> getSearches() {
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

		@Override
		public List<QueryJoin> getJoins() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> join(String field, String... sortFields) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> paginate(int size) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> customize(QueryOption... options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> search(String match, String... fields) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> search(String match, QueryOption opt, String... fields) {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public QueryOption option(int option) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<Integer, QueryOption> options() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> stateful() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> release() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int update(Map<String, ?> fieldValues) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Query<T> nextPage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> previousPage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> stateless() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> resetData() {
			// TODO Auto-generated method stub
			return null;
		}

		

		@Override
		public String dump(QueryOption... options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void dump(OutputStream os, QueryOption... options) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Query<T> restore(String dump, QueryOption... options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> restore(InputStream dump, QueryOption... options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryAsync<T> async() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PersistenceManager getPersistenceManager() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> limit(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> offset(Object offset) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<T> iterPerPage(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T getByKey(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> copy() {
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
	public <T> Iterable<T> iter(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] supportedOperators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void release(Query<T> query) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public <T> Query<T> createQuery(BaseQueryData<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Batch<T> createBatch(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return null;
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
	public int insert(Object... objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insert(Iterable<?> objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Iterable<?> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int get(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int get(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int update(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int update(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> void paginate(Query<T> query) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public <T> Iterable<T> iterPerPage(Query<T> query, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <T> T getByKey(Class<T> clazz, Object key) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void save(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int save(Object... objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int save(Iterable<?> objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> ListQuery<T> createListQuery(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}


}