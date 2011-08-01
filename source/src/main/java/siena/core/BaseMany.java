/**
 * 
 */
package siena.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import siena.ClassInfo;
import siena.PersistenceManager;
import siena.Query;
import siena.Util;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class BaseMany<T> implements Many4PM<T>{
	transient private static final long serialVersionUID = -1417704952199421178L;

	transient protected PersistenceManager pm;
	transient protected Class<T> clazz;
	
	protected Relation relation;
	
	transient protected ProxyList<T> list;	
	transient protected Query<T> query;
	
	public BaseMany(PersistenceManager pm, Class<T> clazz){
		this.pm = pm;
		this.clazz = clazz;
		list = new ProxyList<T>(this);
		this.query = pm.createQuery(clazz);
	}
	
	public BaseMany(PersistenceManager pm, Class<T> clazz, RelationMode mode, Object obj, String fieldName) {
		this.pm = pm;
		this.clazz = clazz;
		list = new ProxyList<T>(this);
		switch(mode){
		case AGGREGATION:
			this.relation = new Relation(mode, obj, fieldName);
			this.query = pm.createQuery(clazz).aggregated(obj, fieldName);
			break;
		case RELATION:
			this.query = pm.createQuery(clazz).filter(fieldName, obj);
			break;
		}
		query = pm.createQuery(clazz);		
	}

	public SyncList<T> asList() {
		return list.sync();
	}

	public Query<T> asQuery() {
		return query;
	}
	
	public Many4PM<T> setSync(boolean isSync) {
		list.isSync = isSync;
		return this;
	}
	
	public List<T> asList2Remove() {
		return list.elements2Remove;
	}
	
	public List<T> asList2Add() {
		return list.elements2Add;
	}

	public Many4PM<T> aggregationMode(Object aggregator, Field field) {
		if(relation == null){
			this.relation = new Relation(RelationMode.AGGREGATION, aggregator, field);
		}
		else {
			this.relation.mode = RelationMode.AGGREGATION;
			this.relation.target = aggregator;
			this.relation.discriminator = field;
		}

		this.query.release().aggregated(aggregator, ClassInfo.getSimplestColumnName(field));
		return this;
	}

	public Many4PM<T> relationMode(Object owner, Field field) {
		this.query.release().filter(ClassInfo.getSimplestColumnName(field), owner);
		return this;
	}

	protected class ProxyList<V> implements SyncList<V>{
		transient protected Many<V> many;
		transient protected List<V> elements;
		transient protected List<V> elements2Remove;
		transient protected List<V> elements2Add;

		// isSync set to true by default because when you begin you generally want to add new elements
		transient protected boolean isSync = true;

		public ProxyList(BaseMany<V> many){
			this.many = many;
			this.elements = new ArrayList<V>();
			this.elements2Remove = new ArrayList<V>();
			this.elements2Add = new ArrayList<V>();
		}
		
		public boolean add(V e) {
			if(relation != null && relation.mode == RelationMode.AGGREGATION){
				Util.setField(e, ClassInfo.getClassInfo(clazz).aggregator, relation);
			}
			elements2Add.add(e);
			return elements.add(e);
		}

		public void add(int index, V element) {
			if(relation != null && relation.mode == RelationMode.AGGREGATION){
				Util.setField(element, ClassInfo.getClassInfo(clazz).aggregator, relation);
			}
			elements2Add.add(element);
			elements.add(index, element);
		}

		public boolean addAll(Collection<? extends V> c) {
			if(relation != null && relation.mode == RelationMode.AGGREGATION){
				for(V o:c){
					Util.setField(o, ClassInfo.getClassInfo(clazz).aggregator, relation);			
				}
			}
			elements2Add.addAll(c);
			return elements.addAll(c);
		}

		public boolean addAll(int index, Collection<? extends V> c) {
			if(relation != null && relation.mode == RelationMode.AGGREGATION){
				for(V o:c){
					Util.setField(o, ClassInfo.getClassInfo(clazz).aggregator, relation);			
				}
			}
			elements2Add.addAll(c);
			return elements.addAll(index, c);
		}

		public <F extends V> boolean addAll(F... c) {
			if(relation != null && relation.mode == RelationMode.AGGREGATION){
				for(V o:c){
					Util.setField(o, ClassInfo.getClassInfo(clazz).aggregator, relation);			
				}
			}
			List<F> l = Arrays.asList(c);
			elements2Add.addAll(l);
			return elements.addAll(l);
		}

		public <F extends V> boolean addAll(int index, F... c) {
			if(relation != null && relation.mode == RelationMode.AGGREGATION){
				for(V o:c){
					Util.setField(o, ClassInfo.getClassInfo(clazz).aggregator, relation);			
				}
			}
			List<F> l = Arrays.asList(c);
			elements2Add.addAll(l);
			return elements.addAll(index, l);
		}

		public void clear() {
			elements2Add.clear();
			elements2Remove.addAll(elements);
			elements.clear();
		}

		public boolean contains(Object o) {
			return elements.contains(o);
		}

		public boolean containsAll(Collection<?> c) {
			return elements.containsAll(c);
		}

		public V get(int index) {
			return elements.get(index);
		}

		public int indexOf(Object o) {
			return elements.indexOf(o);
		}

		public boolean isEmpty() {
			return elements.isEmpty();
		}

		public Iterator<V> iterator() {
			return elements.iterator();
		}

		public int lastIndexOf(Object o) {
			return elements.lastIndexOf(o);
		}

		public ListIterator<V> listIterator() {
			return elements.listIterator();
		}

		public ListIterator<V> listIterator(int index) {
			return elements.listIterator(index);
		}

		@SuppressWarnings("unchecked")
		public boolean remove(Object o) {
			elements2Remove.add((V)o);
			return elements.remove(o);
		}

		public V remove(int index) {
			V o = elements.remove(index);
			if(relation != null && relation.mode == RelationMode.AGGREGATION){
				Util.setField(o, ClassInfo.getClassInfo(clazz).aggregator, null);
			}

			elements2Remove.add(o);
			return o;
		}

		@SuppressWarnings("unchecked")
		public boolean removeAll(Collection<?> c) {
			elements2Remove.addAll((Collection<V>)c);
			if(relation != null && relation.mode == RelationMode.AGGREGATION){
				for(Object o:c){
					Util.setField(o, ClassInfo.getClassInfo(clazz).aggregator, null);				
				}
			}
			return elements.removeAll(c);
		}

		public boolean retainAll(Collection<?> c) {
			return elements.retainAll(c);
		}

		public V set(int index, V element) {
			Util.setField(element, ClassInfo.getClassInfo(clazz).aggregator, null);
			return elements.set(index, element);
		}

		public int size() {
			return elements.size();
		}

		public List<V> subList(int fromIndex, int toIndex) {
			return elements.subList(fromIndex, toIndex);
		}

		public Object[] toArray() {
			return elements.toArray();
		}

		public <Z> Z[] toArray(Z[] a) {
			return elements.toArray(a);
		}

		public SyncList<V> sync() {
			if(!isSync){
				return forceSync();
			}
			return this; 
		}
				
		public SyncList<V> forceSync() {
			elements = many.asQuery().fetch();
			elements2Remove.clear();
			elements2Add.clear();
			isSync = true;
			return this;
		}
	}


}
