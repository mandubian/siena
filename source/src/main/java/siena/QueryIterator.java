package siena;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;


public class QueryIterator<T> implements Iterator<T> {
	
	private Query<T> query;
	private int max;
	private List<T> current;
	private int index;
	private String field;
	private T last;

	public QueryIterator(Query<T> query, int max, String field) {
		this.query = query.copy().order(field);
		this.max = max;
		this.index = 0;
		this.field = field;
		current = this.query.fetch(max);
		this.last = null;
	}

	public boolean hasNext() {
		try {
			if(current == null) {
				Field f = last.getClass().getDeclaredField(field);
				f.setAccessible(true);
				current = query.copy().filter(field+">", f.get(last)).fetch(max);
			}
		} catch(Exception e) {
			throw new SienaException(e);
		}
		return !current.isEmpty();
	}

	public T next() {
		T next = current.get(index);
		index++;
		if(index == current.size()) {
			index = 0;
			last = next;
			current = null;
		}
		return next;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}