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
package siena.core.async;

import java.util.List;
import java.util.Map;

import siena.Model;
import siena.PersistenceManagerFactory;
import siena.Query;
import siena.QueryAggregated;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.core.batch.BatchAsync;
import siena.core.options.QueryOption;

/**
 * This is a technical class to bring asynchronous functions to Models
 * but this class shouldn't be inherited 
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class ModelAsync {

	transient private PersistenceManagerAsync persistenceManagerAsync;
	transient private Model model;

	public ModelAsync(Model model) {
		this.model = model;
		persistenceManagerAsync = model.getPersistenceManager().async();
	}

	public SienaFuture<Void> get() {
		return getPersistenceManager().get(model);
	}

	public SienaFuture<Void> delete() {
		return getPersistenceManager().delete(model);
	}

	public SienaFuture<Void> insert() {
		return getPersistenceManager().insert(model);
	}

	public SienaFuture<Void> update() {
		return getPersistenceManager().update(model);
	}

	public final PersistenceManagerAsync getPersistenceManager() {
		if(persistenceManagerAsync == null) {
			persistenceManagerAsync = PersistenceManagerFactory.getPersistenceManager(model.getClass()).async();
		}
		return persistenceManagerAsync;
	}

	public static <R> QueryAsync<R> all(Class<R> clazz) {
		return PersistenceManagerFactory.getPersistenceManager(clazz).createQuery(clazz).async();
	}
	
	public static <R> BatchAsync<R> batch(Class<R> clazz) {
		return PersistenceManagerFactory.getPersistenceManager(clazz).createBatch(clazz).async();
	}

	public boolean equals(Object that) {
		if(this == that) { return true; }
		if(that == null || that.getClass() != this.getClass()) { return false; }

		ModelAsync masync = (ModelAsync)that;
		
		return this.model.equals(masync.model);
	}

	public int hashCode() {
		return model.hashCode();
	}

	class ProxyQueryAsync<T> implements QueryAsync<T> {
		private static final long serialVersionUID = -1820063783201503668L;

		private String filter;
		private Class<T> clazz;
		private ModelAsync obj;

		public ProxyQueryAsync(Class<T> clazz, String filter, ModelAsync obj) {
			this.filter = filter;
			this.clazz = clazz;
			this.obj = obj;
		}

		private QueryAsync<T> createQuery() {
			return getPersistenceManager().createQuery(clazz).filter(filter, obj);
		}

		public QueryAsync<T> filter(String fieldName, Object value) {
			return createQuery().filter(fieldName, value);
		}

		public QueryAsync<T> order(String fieldName) {
			return createQuery().order(fieldName);
		}

		public QueryAsync<T> join(String field, String... sortFields) {
			return createQuery().join(field, sortFields);
		}

		public QueryAsync<T> aggregated(Object aggregator, String field) {
			return createQuery().aggregated(aggregator, field);
		}

		public QueryAsync<T> search(String match, String... fields) {
			return createQuery().search(match, fields);
		}

		public QueryAsync<T> search(String match, QueryOption opt,
				String... fields) {
			return createQuery().search(match, opt, fields);
		}

		public SienaFuture<T> get() {
			return createQuery().get();
		}

		public SienaFuture<T> getByKey(Object key) {
			return createQuery().getByKey(key);
		}

		public SienaFuture<Integer> delete() {
			return createQuery().delete();			
		}

		public SienaFuture<Integer> update(Map<String, ?> fieldValues) {
			return createQuery().update(fieldValues);
		}

		public SienaFuture<Integer> count() {
			return createQuery().count();
		}

		public SienaFuture<List<T>> fetch() {
			return createQuery().fetch();
		}

		public SienaFuture<List<T>> fetch(int limit) {
			return createQuery().fetch();
		}

		public SienaFuture<List<T>> fetch(int limit, Object offset) {
			return createQuery().fetch(limit, offset);
		}

		public SienaFuture<List<T>> fetchKeys() {
			return createQuery().fetchKeys();
		}

		public SienaFuture<List<T>> fetchKeys(int limit) {
			return createQuery().fetchKeys(limit);
		}

		public SienaFuture<List<T>> fetchKeys(int limit, Object offset) {
			return createQuery().fetchKeys(limit, offset);
		}

		public SienaFuture<Iterable<T>> iter() {
			return createQuery().iter();
		}

		public SienaFuture<Iterable<T>> iter(int limit) {
			return createQuery().iter(limit);
		}

		public SienaFuture<Iterable<T>> iter(int limit, Object offset) {
			return createQuery().iter(limit, offset);
		}

		public SienaFuture<Iterable<T>> iterPerPage(int limit) {
			return createQuery().iterPerPage(limit);
		}

		public Class<T> getQueriedClass() {
			return clazz;
		}

		public SienaFuture<Object> raw(String request) {
			return createQuery().raw(request);
		}

		public QueryAsync<T> paginate(int size) {
			return createQuery().paginate(size);
		}

		public QueryAsync<T> nextPage() {
			return createQuery().nextPage();
		}

		public QueryAsync<T> previousPage() {
			return createQuery().previousPage();
		}


		public QueryAsync<T> customize(QueryOption... options) {
			return createQuery().customize(options);
		}

		public QueryOption option(int option) {
			return createQuery().option(option);
		}

		public Map<Integer, QueryOption> options() {
			return createQuery().options();
		}

		public QueryAsync<T> stateful() {
			return createQuery().stateful();
		}
		@Override
		public QueryAsync<T> stateless() {
			return createQuery().stateless();
		}
		public QueryAsync<T> release() {
			return createQuery().release();
		}

		@Override
		public QueryAsync<T> resetData() {
			return createQuery().resetData();
		}

		public String dump() {
			return createQuery().dump();
		}

		public QueryAsync<T> restore(String dump) {
			return createQuery().restore(dump);
		}

		public Query<T> sync() {
			return createQuery().sync();
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

		public QueryAsync<T> clone() {
			return createQuery().clone();
		}

		public PersistenceManagerAsync getPersistenceManager() {
			return obj.getPersistenceManager();
		}

		public QueryAsync<T> limit(int limit) {
			return createQuery().limit(limit);
		}

		public QueryAsync<T> offset(Object offset) {
			return createQuery().offset(offset);
		}


	}

}
