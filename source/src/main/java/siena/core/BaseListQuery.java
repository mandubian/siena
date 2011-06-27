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
public class BaseListQuery<T> extends BaseQuery<T> implements ListQuery4PM<T>{
	transient private static final long serialVersionUID = -1417704952199421178L;

	transient protected List<T> elements = new ArrayList<T>();
	transient protected boolean isSync = true;
	
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
		if(!isSync){
			return fetch();
		}
		return elements;
	}

	public boolean isSync() {
		return isSync;
	}

	public ListQuery4PM<T> setSync(boolean isSync){
		this.isSync = isSync;
		
		return this;
	}
	
	@Override
	public List<T> fetch() {
		elements = super.fetch();
		isSync = true;
		return elements;
	}

	@Override
	public List<T> fetch(int limit) {
		elements = super.fetch(limit);
		isSync = true;
		return elements;
	}
	
	@Override
	public List<T> fetch(int limit, Object offset) {
		elements = super.fetch(limit, offset);
		isSync = true;
		return elements;
	}

	@Override
	public List<T> fetchKeys() {
		elements = super.fetchKeys();
		isSync = true;
		return elements;
	}

	@Override
	public List<T> fetchKeys(int limit) {
		elements = super.fetchKeys(limit);
		isSync = true;
		return elements;
	}

	@Override
	public List<T> fetchKeys(int limit, Object offset) {
		elements = super.fetchKeys(limit, offset);
		isSync = true;
		return elements;
	}
	
	@Override
	public Iterable<T> iter() {
		// TODO Auto-generated method stub
		return super.iter();
	}

	@Override
	public Iterable<T> iter(int limit) {
		// TODO Auto-generated method stub
		return super.iter(limit);
	}

	@Override
	public Iterable<T> iter(int limit, Object offset) {
		// TODO Auto-generated method stub
		return super.iter(limit, offset);
	}
	
	@Override
	public T get() {
		// TODO Auto-generated method stub
		return super.get();
	}



	@Override
	public Iterable<T> iterPerPage(int pageSize) {
		// TODO Auto-generated method stub
		return super.iterPerPage(pageSize);
	}

	@Override
	public T getByKey(Object key) {
		// TODO Auto-generated method stub
		return super.getByKey(key);
	}

}
