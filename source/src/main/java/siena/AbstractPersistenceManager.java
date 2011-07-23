package siena;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import siena.core.BaseMany;
import siena.core.BaseOne;
import siena.core.Many4PM;
import siena.core.One4PM;
import siena.core.SienaIterablePerPage;
import siena.core.batch.BaseBatch;
import siena.core.batch.Batch;
import siena.core.options.PmOption;
import siena.core.options.QueryOption;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionState;

public abstract class AbstractPersistenceManager implements PersistenceManager {
	protected ThreadLocal<Map<Integer, PmOption>> options = new ThreadLocal<Map<Integer, PmOption>>();
	
	public <T> Query<T> createQuery(Class<T> clazz) {
		return new BaseQuery<T>(this, clazz);
	}
	
	public <T> Query<T> createQuery(BaseQueryData<T> data) {
		return new BaseQuery<T>(this, data);
	}

	public <T> Batch<T> createBatch(Class<T> clazz) {
		return new BaseBatch<T>(this, clazz);
	}

	public PersistenceManager option(PmOption opt) {
		Map<Integer, PmOption> map = options.get(); 
		if(map == null){
			map = new HashMap<Integer, PmOption>();
			options.set(map);
		}
		map.put(opt.type, opt);
		
		return this;
	}
	
	public PmOption option(int type){
		Map<Integer, PmOption> map = options.get(); 
		if(map == null){
			return null;
		}
		return map.get(type);
	}
	
	public Map<Integer, PmOption> options() {
		return options.get();
	}

	public void resetOptions() {
		options.remove();
	}

	public <T> Many4PM<T> createMany(Class<T> clazz) {
		return new BaseMany<T>(this, clazz);
	}
	
	public <T> One4PM<T> createOne(Class<T> clazz) {
		return new BaseOne<T>(this, clazz);
	}	
	
	public <T> T get(Query<T> query) {
		List<T> list = fetch(query, 1);
		if(list.isEmpty()) { return null; }
		return list.get(0);
	}

	public <T> void release(Query<T> query) {
		QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		QueryOption state = query.option(QueryOptionState.ID);
		
		// resets offset
		if(offset.isActive()) 
			offset.offset=0;
		// disables reusable and cludge
		if(state.isActive()){
			state.passivate();
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
