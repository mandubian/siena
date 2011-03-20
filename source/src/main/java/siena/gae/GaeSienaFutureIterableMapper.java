package siena.gae;

import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;

import com.google.appengine.api.datastore.Entity;

public class GaeSienaFutureIterableMapper<T> implements SienaFuture<Iterable<T>>{
	Iterable<Entity> entities;
	QueryAsync<T> query;
	GaePersistenceManagerAsync pm;
	
	public GaeSienaFutureIterableMapper(
			GaePersistenceManagerAsync pm, Iterable<Entity> entities, QueryAsync<T> query){
		this.entities = entities;
		this.query = query;
		this.pm = pm;
	}
	
	public Iterable<T> get() {	
		return new GaeSienaIterableAsync<T>(pm, entities, query);
	}

}
