package siena;

import java.util.List;
import java.util.Map;

import siena.core.async.QueryAsync;
import siena.core.options.QueryOption;

/**
 * The base implementation of Query<T> where T is the model being queried (not necessarily inheriting siena.Model)
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 * @param <T>
 */
public class BaseQuery<T> extends BaseQueryData<T> implements Query<T> {
	
	private PersistenceManager pm;

	@Deprecated
	private Object nextOffset;

	
	public BaseQuery(PersistenceManager pm, Class<T> clazz) {
		super(clazz);
		this.pm = pm;
	}
	
	public BaseQuery(BaseQuery<T> query) {
		super(query);
		this.pm = query.pm;

		//this.nextOffset = query.nextOffset;
	}
	
	public BaseQuery(PersistenceManager pm, BaseQueryData<T> data) {
		super(data);
		this.pm = pm;
	}
	
	
	public PersistenceManager getPersistenceManager(){
		return pm;
	}
		
	public Query<T> filter(String fieldName, Object value) {
		addFilter(fieldName, value, pm.supportedOperators());		
		return this;
	}

	public Query<T> order(String fieldName) {
		addOrder(fieldName);		
		return this;
	}

	public Query<T> search(String match, String... fields) {
		addSearch(match, fields);
		return this;
	}
	
	public Query<T> search(String match, QueryOption opt, String... fields) {
		addSearch(match, opt, fields);
		return this;
	}
	
	
	@Deprecated
	public Query<T> search(String match, boolean inBooleanMode, String index) {
		//TODO implements default function for backward compat
		//searches.add(new QuerySearch(match, inBooleanMode, index));
		return this;
	}

	public Query<T> join(String fieldName, String... sortFields) {
		addJoin(fieldName, sortFields);
		return this;
	}
	
	public T get() {
		return pm.get(this);
	}

	public List<T> fetch() {
		return pm.fetch(this);
	}

	public List<T> fetch(int limit) {
		return pm.fetch(this, limit);
	}

	public List<T> fetch(int limit, Object offset) {
		return pm.fetch(this, limit, offset);
	}

	public int count() {
		return pm.count(this);
	}

	@Deprecated
	public int count(int limit) {
		return pm.count(this, limit);
	}

	@Deprecated
	public int count(int limit, Object offset) {
		return pm.count(this, limit, offset);
	}

	
	public int delete() {
		return pm.delete(this);
	}

	public List<T> fetchKeys() {
		return pm.fetchKeys(this);
	}

	public List<T> fetchKeys(int limit) {
		return pm.fetchKeys(this, limit);
	}

	public List<T> fetchKeys(int limit, Object offset) {
		return pm.fetchKeys(this, limit, offset);
	}
	
	public Iterable<T> iter() {
		return pm.iter(this);
	}
	
	public Iterable<T> iter(int limit) {
		return pm.iter(this, limit);
	}
	
	public Iterable<T> iter(int limit, Object offset) {
		return pm.iter(this, limit, offset);
	}
	
	
	public Iterable<T> iterPerPage(int pageSize) {
		return pm.iterPerPage(this, pageSize);
	}
	
	
	public Query<T> clone() {
		// TODO code a real deep clone function
		return new BaseQuery<T>(this);
	}
	
	public Class<T> getQueriedClass() {
		return clazz;
	}

	public Object raw(String request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public Object nextOffset() {
		return nextOffset;
	}
	
	@Deprecated
	public void setNextOffset(Object nextOffset) {
		this.nextOffset = nextOffset;
	}


	public Query<T> paginate(int pageSize) {
		optionPaginate(pageSize);
		pm.paginate(this);
		return this;
	}
	
	@Override
	public Query<T> limit(int limit) {
		optionLimit(limit);
		return this;
	}

	@Override
	public Query<T> offset(Object offset) {
		optionOffset((Integer)offset);
		return this;
	}

	@Override
	public Query<T> nextPage() {
		pm.nextPage(this);
		return this;
	}

	@Override
	public Query<T> previousPage() {
		pm.previousPage(this);
		return this;
	}
	
	public Query<T> customize(QueryOption... options) {
		addOptions(options);
		return this;
	}

	public Query<T> stateful() {
		optionStateful();
		return this;
	}

	@Override
	public Query<T> stateless() {
		// when going to stateless mode, we reset the options to default values 
		resetOptions();
		optionStateless();
		return this;
	}

	public Query<T> release() {
		super.reset();
		pm.release(this);
		return this;
	}


	public Query<T> resetData() {
		super.reset();
		return this;
	}

	public int update(Map<String, ?> fieldValues) {
		return pm.update(this, fieldValues);
	}

	public String dump() {
		// TODO Auto-generated method stub
		return null;
	}

	public Query<T> restore(String dump) {
		// TODO Auto-generated method stub
		return null;
	}

	public QueryAsync<T> async() {
		return pm.async().createQuery(this);
	}

	public T getByKey(Object key) {
		return pm.getByKey(this.getQueriedClass(), key);
	}
		
}
