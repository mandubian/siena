/**
 * 
 */
package siena.core;

import java.lang.reflect.Field;

import siena.ClassInfo;
import siena.PersistenceManager;
import siena.Query;
import siena.Util;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class BaseOne<T> implements One4PM<T>{

	transient protected PersistenceManager pm;
	transient protected Class<T> clazz;
	
	protected Relation relation;

	transient protected Query<T> query;
	transient protected T target;
	transient protected boolean isSync = true;
	transient protected boolean isModified = false;
	transient protected T prevTarget;

	public BaseOne(PersistenceManager pm, Class<T> clazz){
		this.pm = pm;
		this.clazz = clazz;
		this.query = pm.createQuery(clazz);
		this.relation = new Relation();
	}

	public BaseOne(PersistenceManager pm, Class<T> clazz, RelationMode mode, Object obj, String fieldName) {
		this.pm = pm;
		this.clazz = clazz;
		this.relation = new Relation(mode, obj, fieldName);
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
		return target;
	}

	public void set(T obj) {
		this.prevTarget = this.target;

		this.target = obj;
		
		// sets relation on target object
		if(this.target != null){
			Util.setField(this.target, ClassInfo.getClassInfo(clazz).aggregator, this.relation);
		}

		// resets relation on previous object
		if(this.prevTarget != null){
			Util.setField(this.prevTarget, ClassInfo.getClassInfo(clazz).aggregator, null);
		}
		
		isModified = true;
	}

	public One<T> sync() {
		if(!isSync){
			return forceSync();
		}
		return this; 
	}

	public One<T> forceSync() {
		target = query.get();
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
		return this.prevTarget;
	}

	public One4PM<T> aggregationMode(Object aggregator, Field field) {
		this.relation.mode = RelationMode.AGGREGATION;
		this.relation.target = aggregator;
		this.relation.discriminator = field;
		
		this.query.release().aggregated(aggregator, ClassInfo.getSimplestColumnName(field));
		return this;
	}

	public One4PM<T> relationMode(Object owner, Field field) {
		this.relation.mode = RelationMode.RELATION;
		this.relation.target = owner;
		this.relation.discriminator = field;
		
		this.query.release().filter(ClassInfo.getSimplestColumnName(field), owner);
		return this;
	}
}
