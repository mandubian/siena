package siena;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

public class BaseQuery<T> implements Query<T> {
	
	private PersistenceManager pm;
	private Class<T> clazz;
	
	private List<QueryFilter> filters;

	private List<QueryOrder> orders;
	private List<QuerySearch> searches;
	
	private Object nextOffset;
	
	public BaseQuery(PersistenceManager pm, Class<T> clazz) {
		this.pm = pm;
		this.clazz = clazz;
		
		filters = new ArrayList<QueryFilter>();
		orders = new ArrayList<QueryOrder>();
		searches = new ArrayList<QuerySearch>();
	}
	
	public List<QueryFilter> getFilters() {
		return filters;
	}

	public List<QueryOrder> getOrders() {
		return orders;
	}

	public List<QuerySearch> getSearches() {
		return searches;
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
			filters.add(new QueryFilter(field, op, value));
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

	public Query<T> search(String match, boolean inBooleanMode, String index) {
		searches.add(new QuerySearch(match, inBooleanMode, index));
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
	
	public Object nextOffset() {
		return nextOffset;
	}
	
	public void setNextOffset(Object nextOffset) {
		this.nextOffset = nextOffset;
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
	
	public Iterable<T> iter(String field, int max) {
		throw new NotImplementedException();
	}
	
	public Query<T> clone() {
		throw new NotImplementedException();
	}
	
	public Class<T> getQueriedClass() {
		return clazz;
	}

}
