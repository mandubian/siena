/**
 * 
 */
package siena.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import siena.BaseQuery;
import siena.PersistenceManager;
import siena.Query;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class BaseListQuery<T> implements ListQuery4PM<T>{
	transient private static final long serialVersionUID = -1417704952199421178L;

	transient protected ProxyList<T> elements;
	
	transient protected ProxyQuery<T> query;
	transient protected boolean isSync = true;
	
	public BaseListQuery(PersistenceManager pm, Class<T> clazz) {
		query = new ProxyQuery<T>(this, pm, clazz);
		elements = new ProxyList<T>(new ArrayList<T>());
	}

	public boolean isSync() {
		return isSync;
	}

	public ListQuery4PM<T> setSync(boolean isSync){
		this.isSync = isSync;
		
		return this;
	}

	public List<T> asList() {
		if(isSync){
			return elements;
		}else {
			return query.fetch();
		}
	}

	public Query<T> asQuery() {
		return query;
	}
	
	public List<T> asList2Remove() {
		return elements.elements2Remove;
	}

	protected class ProxyQuery<V> extends BaseQuery<V>{
		private static final long serialVersionUID = 3622188538479070257L;
		BaseListQuery<V> lq;
		
		public ProxyQuery(BaseListQuery<V> lq, PersistenceManager pm, Class<V> clazz) {
			super(pm, clazz);
			this.lq = lq;
		}

		@Override
		public List<V> fetch() {
			if(isSync){
				return lq.elements;
			}else {
				lq.elements = new ProxyList(super.fetch());
				lq.setSync(true);
				return lq.elements;
			}
		}	
		
		@Override
		public List<V> fetch(int limit) {
			if(isSync){
				return lq.elements;
			}else {
				// doesn't consider it's synchronized but doesn't change state
				//lq.setSync(false);
				return super.fetch(limit);
			}
		}	
		
		@Override
		public List<V> fetch(int limit, Object offset) {
			if(isSync){
				return lq.elements;
			}else {
				// doesn't consider it's synchronized but doesn't change state
				//lq.setSync(false);
				return super.fetch(limit, offset);
			}
		}	
		
		@Override
		public List<V> fetchKeys() {
			// does use the already fetched object (yet it contains more than the key in general)
			if(isSync){
				return lq.elements;
			}else {
				// doesn't consider it's synchronized but doesn't change state
				//lq.setSync(false);
				return super.fetchKeys();
			}
		}	
		
		@Override
		public List<V> fetchKeys(int limit) {
			// does use the already fetched object (yet it contains more than the key in general)
			if(isSync){
				return lq.elements;
			}else {
				// doesn't consider it's synchronized but doesn't change state
				//lq.setSync(false);
				return super.fetchKeys(limit);
			}
		}	
		
		@Override
		public List<V> fetchKeys(int limit, Object offset) {
			// does use the already fetched object (yet it contains more than the key in general)
			if(isSync){
				return lq.elements;
			}else {
				// doesn't consider it's synchronized but doesn't change state
				//lq.setSync(false);
				return super.fetchKeys(limit, offset);
			}
		}	
		
		@Override
		public int delete() {
			// forces the sync to false;
			lq.setSync(false);
			return super.delete();			
		}

		@Override
		public V get() {
			if(isSync){
				return lq.elements.get(0);
			}
			else {
				return super.get();
			}
		}

		@Override
		public int count() {
			if(isSync){
				return lq.elements.size();
			}
			else {
				return super.count();
			}
		}

	}

	protected class ProxyList<V> implements List<V>{
		protected List<V> elements;
		protected List<V> elements2Remove;

		public ProxyList(List<V> elements){
			this.elements = elements;
			this.elements2Remove = new ArrayList<V>();
		}
		
		@Override
		public boolean add(V e) {
			return elements.add(e);
		}

		@Override
		public void add(int index, V element) {
			elements.add(index, element);
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			return elements.addAll(c);
		}

		@Override
		public boolean addAll(int index, Collection<? extends V> c) {
			return addAll(index, c);
		}

		@Override
		public void clear() {
			elements.clear();
		}

		@Override
		public boolean contains(Object o) {
			return elements.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return elements.containsAll(c);
		}

		@Override
		public V get(int index) {
			return elements.get(index);
		}

		@Override
		public int indexOf(Object o) {
			return elements.indexOf(o);
		}

		@Override
		public boolean isEmpty() {
			return elements.isEmpty();
		}

		@Override
		public Iterator<V> iterator() {
			return elements.iterator();
		}

		@Override
		public int lastIndexOf(Object o) {
			return elements.lastIndexOf(o);
		}

		@Override
		public ListIterator<V> listIterator() {
			return elements.listIterator();
		}

		@Override
		public ListIterator<V> listIterator(int index) {
			return elements.listIterator(index);
		}

		@Override
		public boolean remove(Object o) {
			elements2Remove.add((V)o);
			return elements.remove(o);
		}

		@Override
		public V remove(int index) {
			V o = elements.remove(index);
			elements2Remove.add(o);
			return o;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return elements.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return elements.retainAll(c);
		}

		@Override
		public V set(int index, V element) {
			return elements.set(index, element);
		}

		@Override
		public int size() {
			return elements.size();
		}

		@Override
		public List<V> subList(int fromIndex, int toIndex) {
			return elements.subList(fromIndex, toIndex);
		}

		@Override
		public Object[] toArray() {
			return elements.toArray();
		}

		@Override
		public <Z> Z[] toArray(Z[] a) {
			return elements.toArray(a);
		}
		
	}

}
