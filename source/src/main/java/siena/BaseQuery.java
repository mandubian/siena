package siena;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseQuery<T> implements Query<T> {
	
	private PersistenceManager pm;
	private Class<T> clazz;
	
	private List<QueryFilter> filters;

	private List<QueryOrder> orders;
	private List<QueryFilterSearch> searches;
	private List<QueryJoin> joins;

	@Deprecated
	private Object nextOffset;
	
	private Map<Integer, QueryOption> options = new HashMap<Integer, QueryOption>() {
		private static final long serialVersionUID = -7438657296637379900L;
	{
		put(QueryOptionPaginate.ID, new QueryOptionPaginate(0));
		put(QueryOptionReuse.ID, new QueryOptionReuse());
		put(QueryOptionOffset.ID, new QueryOptionOffset(0));
	}};
	
	public BaseQuery(PersistenceManager pm, Class<T> clazz) {
		this.pm = pm;
		this.clazz = clazz;
		
		filters = new ArrayList<QueryFilter>();
		orders = new ArrayList<QueryOrder>();
		searches = new ArrayList<QueryFilterSearch>();
		joins = new ArrayList<QueryJoin>();
	}
	
	public BaseQuery(BaseQuery<T> query) {
		this.pm = query.pm;
		this.clazz = query.clazz;		
		
		this.filters = new ArrayList<QueryFilter>();
		this.orders = new ArrayList<QueryOrder>();
		this.searches = new ArrayList<QueryFilterSearch>();
		this.joins = new ArrayList<QueryJoin>();
		
		Collections.copy(this.filters, query.filters);
		Collections.copy(this.orders, query.orders);
		Collections.copy(this.searches, query.searches);
		Collections.copy(this.joins, query.joins);
		
		//this.nextOffset = query.nextOffset;
	}
	
	public List<QueryFilter> getFilters() {
		return filters;
	}

	public List<QueryOrder> getOrders() {
		return orders;
	}

	public List<QueryFilterSearch> getSearches() {
		return searches;
	}

	public List<QueryJoin> getJoins() {
		return joins;
	}
	
	public Query<T> filter(String fieldName, Object value) {
		String op = "=";
		for (String s : pm.supportedOperators()) {
			if(fieldName.endsWith(s)) {
				op = s;
				fieldName = fieldName.substring(0, fieldName.length() - op.length());;
				break;
			}
		}
		fieldName = fieldName.trim();
		
		try {
			Field field = clazz.getDeclaredField(fieldName);
			filters.add(new QueryFilterSimple(field, op, value));
			return this;
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	public Query<T> order(String fieldName) {
		boolean ascending = true;
		
		if(fieldName.startsWith("-")) {
			fieldName = fieldName.substring(1);
			ascending = false;
		}
		try {
			Field field = clazz.getDeclaredField(fieldName);
			orders.add(new QueryOrder(field, ascending));
			return this;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	public Query<T> search(String match, String... fields) {
		QueryFilterSearch q = new QueryFilterSearch(match, fields);
		filters.add(q);
		searches.add(q);
		return this;
	}
	
	public Query<T> search(String match, QueryOption opt, String... fields) {
		QueryFilterSearch q = new QueryFilterSearch(match, opt, fields);
		filters.add(q);
		searches.add(q);
		return this;
	}
	
	
	@Deprecated
	public Query<T> search(String match, boolean inBooleanMode, String index) {
		//TODO implements default function for backward compat
		//searches.add(new QuerySearch(match, inBooleanMode, index));
		return this;
	}

	public Query<T> join(String fieldName, String... sortFields) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			joins.add(new QueryJoin(field, sortFields));
			// add immediately orders to keep order of orders 
			// sets joined field as parent field to manage order on the right joined table for ex
			for(String sortFieldName: sortFields){
				boolean ascending = true;
				
				if(sortFieldName.startsWith("-")) {
					sortFieldName = sortFieldName.substring(1);
					ascending = false;
				}
				try {
					Field sortField = field.getType().getField(sortFieldName);
					orders.add(new QueryOrder(sortField, ascending, field));
					return this;
				} catch(NoSuchFieldException ex){
					throw new SienaException("Join not possible: join sort field "+sortFieldName+" is not a known field of "+fieldName, ex);
				}
			}
			return this;
		} catch(Exception e) {
			throw new SienaException(e);
		}
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
	
	public Query<T> clone() {
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
		((QueryOptionPaginate)(options.get(QueryOptionPaginate.ID)).activate()).pageSize=pageSize;
		options.get(QueryOptionOffset.ID).activate();

		return this;
	}

	public Query<T> offset(int offset) {
		((QueryOptionOffset)(options.get(QueryOptionOffset.ID)).activate()).offset=offset;
		return this;
	}
	
	public Query<T> customize(QueryOption... options) {
		for(QueryOption option: options){
			this.options.put(option.type, option);
		}
		return this;
	}

	public QueryOption option(int option) {
		return options.get(option);
	}

	public Map<Integer, QueryOption> options() {
		return options;
	}

	public Query<T> reuse() {
		options.get(QueryOptionReuse.ID).activate();
		return this;
	}

	public Query<T> release() {
		pm.release(this);
		return this;
	}
		
}
