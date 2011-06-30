package siena.core;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.BaseQueryData;
import siena.ClassInfo;
import siena.PersistenceManager;
import siena.Query;
import siena.core.async.PersistenceManagerAsync;
import siena.core.batch.Batch;
import siena.core.lifecycle.LifeCyclePhase;
import siena.core.lifecycle.LifeCycleUtils;

public class PersistenceManagerLifeCycleWrapper implements PersistenceManager{
	PersistenceManager pm;
	
	public PersistenceManagerLifeCycleWrapper(PersistenceManager pm){
		this.pm = pm;
	}

	@Override
	public void init(Properties p) {
		pm.init(p);
	}

	@Override
	public <T> Query<T> createQuery(Class<T> clazz) {
		return pm.createQuery(clazz);
	}

	@Override
	public <T> Query<T> createQuery(BaseQueryData<T> query) {
		return pm.createQuery(query);
	}

	@Override
	public <T> Batch<T> createBatch(Class<T> clazz) {
		return pm.createBatch(clazz);
	}
	
	@Override
	public <T> Many4PM<T> createMany(Class<T> clazz) {
			return pm.createMany(clazz);
	}
	
	@Override
	public void get(Object obj) {
		ClassInfo ci = ClassInfo.getClassInfo(obj.getClass());
		LifeCycleUtils.executeMethods(LifeCyclePhase.PRE_FETCH, ci, obj);
		pm.get(obj);
		LifeCycleUtils.executeMethods(LifeCyclePhase.POST_FETCH, ci, obj);
	}

	@Override
	public void insert(Object obj) {
		ClassInfo ci = ClassInfo.getClassInfo(obj.getClass());
		LifeCycleUtils.executeMethods(LifeCyclePhase.PRE_INSERT, ci, obj);
		pm.insert(obj);
		LifeCycleUtils.executeMethods(LifeCyclePhase.POST_INSERT, ci, obj);
	}

	@Override
	public void delete(Object obj) {
		ClassInfo ci = ClassInfo.getClassInfo(obj.getClass());
		LifeCycleUtils.executeMethods(LifeCyclePhase.PRE_DELETE, ci, obj);
		pm.delete(obj);
		LifeCycleUtils.executeMethods(LifeCyclePhase.POST_DELETE, ci, obj);
	}

	@Override
	public void update(Object obj) {
		ClassInfo ci = ClassInfo.getClassInfo(obj.getClass());
		LifeCycleUtils.executeMethods(LifeCyclePhase.PRE_UPDATE, ci, obj);
		pm.update(obj);
		LifeCycleUtils.executeMethods(LifeCyclePhase.POST_UPDATE, ci, obj);
	}

	@Override
	public void save(Object obj) {
		ClassInfo ci = ClassInfo.getClassInfo(obj.getClass());
		LifeCycleUtils.executeMethods(LifeCyclePhase.PRE_SAVE, ci, obj);
		pm.save(obj);
		LifeCycleUtils.executeMethods(LifeCyclePhase.POST_SAVE, ci, obj);
	}

	@Override
	public <T> T get(Query<T> query) {
		ClassInfo ci = ClassInfo.getClassInfo(query.getQueriedClass());
		T obj = pm.get(query);
		LifeCycleUtils.executeMethods(LifeCyclePhase.POST_FETCH, ci, obj);
		return obj;
	}

	@Override
	public <T> int delete(Query<T> query) {
		return pm.delete(query);
	}

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		return pm.update(query, fieldValues);
	}

	@Override
	public <T> int count(Query<T> query) {
		return pm.count(query);
	}

	@Override
	public <T> List<T> fetch(Query<T> query) {
		return pm.fetch(query);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		return pm.fetch(query, limit);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		return pm.fetch(query, limit, offset);
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		return pm.fetchKeys(query);
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		return pm.fetchKeys(query, limit);
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		return pm.fetchKeys(query, limit, offset);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		return pm.iter(query);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		return pm.iter(query, limit);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		return pm.iter(query, limit, offset);
	}

	@Override
	public <T> Iterable<T> iterPerPage(Query<T> query, int pageSize) {
		return pm.iterPerPage(query, pageSize);
	}

	@Override
	public int save(Object... objects) {
		return pm.save(objects);
	}

	@Override
	public int save(Iterable<?> objects) {
		return pm.save(objects);
	}

	@Override
	public int insert(Object... objects) {
		return pm.insert(objects);
	}

	@Override
	public int insert(Iterable<?> objects) {
		return pm.insert(objects);
	}

	@Override
	public int delete(Object... models) {
		return pm.delete(models);
	}

	@Override
	public int delete(Iterable<?> models) {
		return pm.delete(models);
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Object... keys) {
		return pm.deleteByKeys(clazz, keys);
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		return pm.deleteByKeys(clazz, keys);
	}

	@Override
	public int get(Object... models) {
		return pm.get(models);
	}

	@Override
	public <T> int get(Iterable<T> models) {
		return pm.get(models);
	}

	@Override
	public <T> T getByKey(Class<T> clazz, Object key) {
		return pm.getByKey(clazz, key);
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		return pm.getByKeys(clazz, keys);
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		return pm.getByKeys(clazz, keys);
	}

	@Override
	public <T> int update(Object... models) {
		return pm.update(models);
	}

	@Override
	public <T> int update(Iterable<T> models) {
		return pm.update(models);
	}

	@Override
	public void beginTransaction(int isolationLevel) {
		pm.beginTransaction(isolationLevel);
	}

	@Override
	public void beginTransaction() {
		pm.beginTransaction();
	}
	
	@Override
	public void commitTransaction() {
		pm.commitTransaction();
	}

	@Override
	public void rollbackTransaction() {
		pm.rollbackTransaction();
	}

	@Override
	public void closeConnection() {
		pm.closeConnection();
	}
	
	@Override
	public <T> void release(Query<T> query) {
		pm.release(query);
	}

	@Override
	public <T> void paginate(Query<T> query) {
		pm.paginate(query);
	}

	@Override
	public <T> void nextPage(Query<T> query) {
		pm.nextPage(query);
	}

	@Override
	public <T> void previousPage(Query<T> query) {
		pm.previousPage(query);
	}

	@Override
	public <T> PersistenceManagerAsync async() {
		return pm.async();
	}

	@Override
	public String[] supportedOperators() {
		return pm.supportedOperators();
	}

	@Deprecated
	@Override
	public <T> int count(Query<T> query, int limit) {
		return pm.count(query, limit);
	}

	@Deprecated
	@Override	
	public <T> int count(Query<T> query, int limit, Object offset) {
		return pm.count(query, limit, offset);
	}
	
	
}
