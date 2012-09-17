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
import siena.core.options.PmOptionStickiness;
import siena.core.options.QueryOption;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionState;

public abstract class AbstractPersistenceManager implements PersistenceManager {
	protected ThreadLocal<Map<Integer, PmOption>> options = new ThreadLocal<Map<Integer, PmOption>>();
	protected Map<Integer, PmOption> optionsSticky = new HashMap<Integer, PmOption>();
	
	public <T> Query<T> createQuery(Class<T> clazz) {
		return new BaseQuery<T>(this, clazz);
	}
	
	public <T> Query<T> createQuery(BaseQueryData<T> data) {
		return new BaseQuery<T>(this, data);
	}

	public <T> Batch<T> createBatch(Class<T> clazz) {
		return new BaseBatch<T>(this, clazz);
	}

	public PersistenceManager option(PmOption opt)
	{
		return option(opt, PmOptionStickiness.STICKY);
	}
	
    public PersistenceManager option(PmOption opt, PmOptionStickiness stick) {
        Map<Integer, PmOption> map = optionsSticky;
        
        if (PmOptionStickiness.NOT_STICKY.equals(stick))
        {
		    map = options.get(); 
		    if(map == null){
			    map = new HashMap<Integer, PmOption>();
			    options.set(map);
		     }
		}
		map.put(opt.type, opt);
		
		return this;
	}
	
	public PmOption option(int type){
		return options().get(type);
	}
	
	public Map<Integer, PmOption> options() {
		Map<Integer, PmOption> res = new HashMap<Integer, PmOption>();
		res.putAll(optionsSticky);
		if (options.get() != null)
		{
			res.putAll(options.get());
		}
		return res;
	}

	public void resetOptions(PmOptionStickiness stick) {
		if (PmOptionStickiness.NOT_STICKY.equals(stick))
        {
        	options.remove();
        }
        else
        {
            optionsSticky.clear();
        }
	}

	public void resetOptions()
	{
		resetOptions(PmOptionStickiness.STICKY);
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
