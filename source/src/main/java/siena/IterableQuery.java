package siena;

import java.util.Iterator;


public class IterableQuery<T> implements Iterable<T> {
	
	private Query<T> query;
	private int max;
	private String field;
	
	public IterableQuery(Query<T> query, int max, String field) {
		this.query = query;
		this.max = max;
		this.field = field;
	}
	
	public Iterator<T> iterator() {
		return new QueryIterator<T>(query, max, field);
	}
	
}
