package siena.gae;

import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterable;

public class GaeSienaFutureIterableMapperWithCursor<T> implements SienaFuture<Iterable<T>>{
	QueryResultIterable<Entity> entities;
	QueryAsync<T> query;
	GaePersistenceManagerAsync pm;

	public GaeSienaFutureIterableMapperWithCursor(
			GaePersistenceManagerAsync pm, QueryResultIterable<Entity> entities, QueryAsync<T> query){
		this.entities = entities;
		this.query = query;
		this.pm = pm;
	}
	
	public Iterable<T> get() {
		return new GaeSienaIterableAsyncWithCursor<T>(pm, entities, query);
	}

}
