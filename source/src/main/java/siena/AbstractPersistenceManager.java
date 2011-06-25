package siena;

import java.util.List;

import siena.core.BaseListQuery;
import siena.core.ListQuery;
import siena.core.SienaIterablePerPage;
import siena.core.batch.BaseBatch;
import siena.core.batch.Batch;
import siena.core.options.QueryOption;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionState;

public abstract class AbstractPersistenceManager implements PersistenceManager {

	public <T> Query<T> createQuery(Class<T> clazz) {
		return new BaseQuery<T>(this, clazz);
	}
	
	public <T> Query<T> createQuery(BaseQueryData<T> data) {
		return new BaseQuery<T>(this, data);
	}

	public <T> Batch<T> createBatch(Class<T> clazz) {
		return new BaseBatch<T>(this, clazz);
	}

	public <T> ListQuery<T> createListQuery(Class<T> clazz) {
		return new BaseListQuery<T>(this, clazz);
	}
	
	public <T> T get(Query<T> query) {
		List<T> list = fetch(query, 1);
		if(list.isEmpty()) { return null; }
		return list.get(0);
	}

	public <T> void release(Query<T> query) {
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

	public <T> Iterable<T> iterPerPage(Query<T> query, int pageSize) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER_PER_PAGE;
		return new SienaIterablePerPage<T>(query, pageSize);
	}

	
	@Deprecated
	public <T> int count(Query<T> query, int limit) {
		return fetch(query, limit).size();
	}
	@Deprecated
	public <T> int count(Query<T> query, int limit, Object offset) {
		return fetch(query, limit, offset).size();
	}
}
