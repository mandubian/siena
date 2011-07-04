/**
 * 
 */
package siena.core;

import siena.PersistenceManager;
import siena.Query;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class BaseOne<T> implements One4PM<T>{

	transient protected PersistenceManager pm;
	transient protected Class<T> clazz;
	
	protected RelationMode mode;

	transient protected Query<T> query;
	transient protected T obj;
	transient protected boolean isSync = true;
	transient protected boolean isModified = false;
	transient protected T prevObj;

	public BaseOne(PersistenceManager pm, Class<T> clazz){
		this.pm = pm;
		this.clazz = clazz;
		this.query = pm.createQuery(clazz);
	}

	public BaseOne(PersistenceManager pm, Class<T> clazz, RelationMode mode, Object obj, String fieldName) {
		this.pm = pm;
		this.clazz = clazz;
		this.mode = mode;
		switch(mode){
		case AGGREGATION:
			this.query = pm.createQuery(clazz).aggregated(obj, fieldName);
			break;
		case RELATION:
			this.query = pm.createQuery(clazz).filter(fieldName, obj);
			break;
		}
		query = pm.createQuery(clazz);		
	}
	
	public T get() {
		sync();
		return obj;
	}

	public void set(T obj) {
		this.prevObj = this.obj;
		this.obj = obj;
		isModified = true;
	}

	public One<T> sync() {
		if(!isSync){
			return forceSync();
		}
		return this; 
	}

	public One<T> forceSync() {
		obj = query.get();
		isSync = true;
		isModified = false;
		return this;
	}

	public One4PM<T> setSync(boolean isSync) {
		this.isSync = isSync;
		return this;
	}

	public boolean isModified() {
		return isModified;
	}

	public One4PM<T> setModified(boolean isModified) {
		this.isModified = isModified;
		return this;
	}

	public T getPrev() {
		return this.prevObj;
	}

	public One4PM<T> aggregationMode(Object aggregator, String fieldName) {
		this.mode = RelationMode.AGGREGATION;
		this.query.release().aggregated(aggregator, fieldName);
		return this;
	}

	public One4PM<T> relationMode(Object owner, String fieldName) {
		this.mode = RelationMode.RELATION;
		this.query.release().filter(fieldName, owner);
		return this;
	}
}
