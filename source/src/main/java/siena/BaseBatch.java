package siena;


public class BaseBatch implements Batch {
	
	private PersistenceManager pm;

	public BaseBatch(PersistenceManager pm) {
		this.pm = pm;

	}
	
	public BaseBatch(BaseBatch query) {
		this.pm = query.pm;
	}
	
	public void insert(Object... models){
		pm.insert(models);
	}

	public void insert(Iterable<?> models){
		pm.insert(models);
	}

	public void delete(Object... models){
		pm.delete(models);
	}

	public void delete(Iterable<?> models){
		pm.delete(models);
	}
	
	public void deleteByKeys(Class<?> clazz, Object... keys){
		pm.delete(clazz, keys);
	}

	public void deleteByKeys(Class<?> clazz, Iterable<?> keys){
		pm.delete(clazz, keys);
	}

	public void update(Object... models) {
		pm.update(models);
	}

	public void update(Iterable<?> models) {
		pm.update(models);
	}
		
}
