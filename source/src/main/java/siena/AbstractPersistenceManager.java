package siena;

import java.util.List;

public abstract class AbstractPersistenceManager implements PersistenceManager {

	public <T> Query<T> createQuery(Class<T> clazz) {
		return new BaseQuery<T>(this, clazz);
	}

	public <T> T get(Query<T> query) {
		List<T> list = fetch(query, 1);
		if(list.isEmpty()) { return null; }
		return list.get(0);
	}

	public <T> int count(Query<T> query, int limit) {
		return fetch(query, limit).size();
	}

	@Override
	public <T> int count(Query<T> query, int limit, Object offset) {
		return fetch(query, limit, offset).size();
	}

	
}
