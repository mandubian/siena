/**
 * 
 */
package siena.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import siena.BaseQuery;
import siena.BaseQueryData;
import siena.PersistenceManager;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class BaseListQuery<T> extends BaseQuery<T> implements ListQuery<T>{
	transient private static final long serialVersionUID = -1417704952199421178L;

	transient protected List<T> elements = new ArrayList<T>();
	
	public BaseListQuery(PersistenceManager pm, Class<T> clazz) {
		super(pm, clazz);
	}
	
	public BaseListQuery(BaseQuery<T> query) {
		super(query);
	}

	public BaseListQuery(PersistenceManager pm, BaseQueryData<T> data) {
		super(pm, data);
	}
	
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	public List<T> elements() {
		return elements;
	}

}
