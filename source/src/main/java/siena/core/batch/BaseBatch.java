package siena.core.batch;

import java.util.List;

import siena.PersistenceManager;


public class BaseBatch<T> implements Batch<T> {
	
	private Class<T> clazz;
	private PersistenceManager pm;

	public BaseBatch(PersistenceManager pm, Class<T> clazz) {
		this.clazz = clazz;
		this.pm = pm;
	}
	
	public BaseBatch(BaseBatch<T> batch) {
		this.clazz = batch.clazz;
		this.pm = batch.pm;
	}
	
	public int insert(T... models){
		return pm.insert(models);
	}

	public int insert(Iterable<T> models){
		return pm.insert(models);
	}

	public int delete(T... models){
		return pm.delete(models);
	}

	public int delete(Iterable<T> models){
		return pm.delete(models);
	}
	
	public int deleteByKeys(Object... keys){
		return pm.delete(clazz, keys);
	}

	public int deleteByKeys(Iterable<?> keys){
		return pm.delete(clazz, keys);
	}

	public int update(Object... models) {
		return pm.update(models);
	}

	public int update(Iterable<T> models) {
		return pm.update(models);
	}
	
	public int get(T... models) {
		return pm.get(models);
	}

	public int get(Iterable<T> models) {
		return pm.get(models);
	}

	public List<T> getByKeys(Object... keys) {
		return pm.getByKeys(clazz, keys);
	}

	public List<T> getByKeys(Iterable<?> keys) {
		return pm.getByKeys(clazz, keys);
	}

	public BatchAsync<T> async() {
		return pm.async().createBatch(clazz);
	}
}
