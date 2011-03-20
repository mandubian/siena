package siena.gae;

import java.util.List;

import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;
import siena.core.options.QueryOptionPage;

import com.google.appengine.api.datastore.Entity;

public class GaeSienaFutureListMapper<T> implements SienaFuture<List<T>>{
	Iterable<Entity> entities;
	QueryAsync<T> query;
	GaePersistenceManagerAsync pm;
	MapType mapType = MapType.ALL;
	
	public enum MapType {
		ALL,
		KEYS_ONLY
	};
	
	public GaeSienaFutureListMapper(
			GaePersistenceManagerAsync pm, Iterable<Entity> entities, QueryAsync<T> query){
		this.entities = entities;
		this.query = query;
		this.pm = pm;
	}
	
	public GaeSienaFutureListMapper(
			GaePersistenceManagerAsync pm, Iterable<Entity> entities, QueryAsync<T> query,
			MapType mapType){
		this.entities = entities;
		this.query = query;
		this.pm = pm;
		this.mapType = mapType;
	}
	
	public List<T> get() {
		List<T> results;
		switch(mapType){
		case KEYS_ONLY:
			results = GaeMappingUtils.mapEntitiesKeysOnly(entities, query.getQueriedClass());
			break;
		case ALL:
		default:
			results = pm.map(query, entities);
			break;
		}
		// if paginating and 0 results then no more data else resets noMoreDataAfter
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		if(pag.isPaginating()){
			if(results.size() == 0){
				gaeCtx.noMoreDataAfter = true;
			}else {
				gaeCtx.noMoreDataAfter = false;
			}
		}
			
		return results;
	}

}
