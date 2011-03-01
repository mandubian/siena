package siena;

import java.util.List;

public abstract class AbstractPersistenceManager implements PersistenceManager {

	public <T> Query<T> createQuery(Class<T> clazz) {
		return new BaseQuery<T>(this, clazz);
	}

	public Batch createBatch() {
		return new BaseBatch(this);
	}

	public <T> T get(Query<T> query) {
		List<T> list = fetch(query, 1);
		if(list.isEmpty()) { return null; }
		return list.get(0);
	}
	
	@Deprecated
	public <T> int count(Query<T> query, int limit) {
		return fetch(query, limit).size();
	}
	@Deprecated
	public <T> int count(Query<T> query, int limit, Object offset) {
		return fetch(query, limit, offset).size();
	}

	public <T> void release(Query<T> query) {
		QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		QueryOption reuse = query.option(QueryOptionReuse.ID);
		
		// resets offset
		if(offset.isActive()) 
			offset.offset=0;
		// disables reusable and cludge
		if(reuse.isActive()){
			reuse.passivate();
		}
	}
}
