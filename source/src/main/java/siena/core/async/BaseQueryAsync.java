/**
 * 
 */
package siena.core.async;

import java.util.List;
import java.util.Map;

import siena.BaseQueryData;
import siena.Query;
import siena.core.options.QueryOption;

/**
 * The base implementation of QueryAsync<T> where T is the model being queried (not necessarily inheriting siena.Model)
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 */
public class BaseQueryAsync<T> extends BaseQueryData<T> implements QueryAsync<T> {
	private static final long serialVersionUID = -7039977099068819124L;

	private PersistenceManagerAsync pm;
	
	public BaseQueryAsync(PersistenceManagerAsync pm, Class<T> clazz) {
		super(clazz);
		this.pm = pm;
	}
	
	public BaseQueryAsync(BaseQueryAsync<T> query) {
		super(query);
		this.pm = query.pm;
	}
	
	public BaseQueryAsync(PersistenceManagerAsync pm, BaseQueryData<T> data) {
		super(data);
		this.pm = pm;
	}
	
	public PersistenceManagerAsync getPersistenceManager(){
		return pm;
	}
	
	public QueryAsync<T> filter(String fieldName, Object value) {
		addFilter(fieldName, value, pm.supportedOperators());		
		return this;
	}

	public QueryAsync<T> order(String fieldName) {
		addOrder(fieldName);		
		return this;
	}

	public QueryAsync<T> join(String fieldName, String... sortFields) {
		addJoin(fieldName, sortFields);
		return this;
	}
	
	public QueryAsync<T> aggregated(Object aggregator, String fieldName) {
		addAggregated(aggregator, fieldName);
		return this;
	}

	public QueryAsync<T> search(String match, String... fields) {
		addSearch(match, fields);
		return this;
	}

	public QueryAsync<T> search(String match, QueryOption opt, String... fields) {
		addSearch(match, opt, fields);
		return this;
	}

	public SienaFuture<T> get() {
		return pm.get(this);
	}

	public SienaFuture<T> getByKey(Object key) {
		return pm.getByKey(clazz, key);
	}

	public SienaFuture<Integer> delete() {
		return pm.delete(this);
	}

	public SienaFuture<Integer> update(Map<String, ?> fieldValues) {
		return pm.update(this, fieldValues); 
	}

	public SienaFuture<Integer> count() {
		return pm.count(this);
	}

	public SienaFuture<List<T>> fetch() {
		return pm.fetch(this);
	}

	public SienaFuture<List<T>> fetch(int limit) {
		return pm.fetch(this, limit);
	}

	public SienaFuture<List<T>> fetch(int limit, Object offset) {
		return pm.fetch(this, limit, offset);
	}

	public SienaFuture<List<T>> fetchKeys() {
		return pm.fetchKeys(this);
	}

	public SienaFuture<List<T>> fetchKeys(int limit) {
		return pm.fetchKeys(this, limit);
	}

	public SienaFuture<List<T>> fetchKeys(int limit, Object offset) {
		return pm.fetchKeys(this, limit, offset);
	}

	public SienaFuture<Iterable<T>> iter() {
		return pm.iter(this);
	}

	public SienaFuture<Iterable<T>> iter(int limit) {
		return pm.iter(this, limit);
	}

	public SienaFuture<Iterable<T>> iter(int limit, Object offset) {
		return pm.iter(this, limit, offset);
	}

	public SienaFuture<Iterable<T>> iterPerPage(int limit) {
		return pm.iterPerPage(this, limit);
	}

	public SienaFuture<Object> raw(String request) {
		// TODO
		return null;
	}

	public QueryAsync<T> limit(int limit) {
		optionLimit(limit);
		return this;
	}

	public QueryAsync<T> offset(Object offset) {
		optionOffset((Integer)offset);
		return this;
	}

	public QueryAsync<T> paginate(int pageSize) {
		optionPaginate(pageSize);
		pm.paginate(this);
		return this;
	}

	public QueryAsync<T> nextPage() {
		pm.nextPage(this);
		return this;
	}

	public QueryAsync<T> previousPage() {
		pm.previousPage(this);
		return this;
	}

	public QueryAsync<T> customize(QueryOption... options) {
		addOptions(options);
		return this;
	}

	public QueryAsync<T> stateful() {
		optionStateful();
		return this;
	}

	@Override
	public QueryAsync<T> stateless() {
		// when going to stateless mode, we reset the options to default values 
		resetOptions();
		optionStateless();
		return this;
	}

	public QueryAsync<T> release() {
		pm.release(this);
		return this;
	}

	@Override
	public QueryAsync<T> resetData() {
		super.reset();
		pm.release(this);
		return this;
	}

	@Override
	public String dump() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryAsync<T> restore(String dump) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query<T> sync() {
		return pm.sync().createQuery(this);
	}

	public QueryAsync<T> clone(){
		// TODO code a real deep clone function
		return new BaseQueryAsync<T>(this);
	}
}
