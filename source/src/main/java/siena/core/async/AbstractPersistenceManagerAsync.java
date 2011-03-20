package siena.core.async;

import java.util.List;

import siena.BaseQueryData;
import siena.core.batch.BaseBatchAsync;
import siena.core.batch.BatchAsync;
import siena.core.options.QueryOption;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionState;

public abstract class AbstractPersistenceManagerAsync implements PersistenceManagerAsync {

	public <T> QueryAsync<T> createQuery(Class<T> clazz) {
		return new BaseQueryAsync<T>(this, clazz);
	}
	
	public <T> QueryAsync<T> createQuery(BaseQueryData<T> data) {
		return new BaseQueryAsync<T>(this, data);
	}

	public <T> BatchAsync<T> createBatch(Class<T> clazz) {
		return new BaseBatchAsync<T>(this, clazz);
	}

	public <T> SienaFuture<T> get(QueryAsync<T> query) {
		final SienaFuture<List<T>> future = fetch(query, 1);
		
		return new SienaFuture<T>(){
			public T get() {
				List<T> list = future.get();
				if(list.isEmpty()) { return null; }
				return list.get(0);
			}
		};
		
	}

	public <T> void release(QueryAsync<T> query) {
		QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		QueryOption reuse = query.option(QueryOptionState.ID);
		
		// resets offset
		if(offset.isActive()) 
			offset.offset=0;
		// disables reusable and cludge
		if(reuse.isActive()){
			reuse.passivate();
		}
	}

}
