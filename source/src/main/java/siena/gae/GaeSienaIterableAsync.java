package siena.gae;

import java.lang.reflect.Field;
import java.util.Iterator;

import siena.ClassInfo;
import siena.core.async.QueryAsync;
import siena.core.options.QueryOptionPage;

import com.google.appengine.api.datastore.Entity;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 * 
 * A Siena Iterable<Model> encapsulating a GAE Iterable<Entity> with its Iterator in asynchronous mode<Model>...
 */
public class GaeSienaIterableAsync<Model> implements Iterable<Model> {
	Iterable<Entity> gaeIterable;
	QueryAsync<Model> query;
	GaePersistenceManagerAsync pm;
	
	GaeSienaIterableAsync(GaePersistenceManagerAsync pm, Iterable<Entity> gaeIterable, QueryAsync<Model> query) {
		this.gaeIterable = gaeIterable;
		this.query = query;
		this.pm = pm;
	}

	public Iterator<Model> iterator() {
		return new SienaGaeIteratorAsync<Model>(query, gaeIterable);
	}

	public class SienaGaeIteratorAsync<T> implements Iterator<T> {
		Field id;
		QueryAsync<T> query;
		Iterator<Entity> gaeIterator;

		SienaGaeIteratorAsync(QueryAsync<T> query, Iterable<Entity> gaeIterable) {
			this.query = query;
			this.id = ClassInfo.getIdField(query.getQueriedClass());
			this.gaeIterator = gaeIterable.iterator();
			
			// if paginating and 0 results then no more data
			QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
			QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
			if(pag.isPaginating() && !gaeIterator.hasNext()){
				gaeCtx.noMoreDataAfter = true;
			}
		}

		public boolean hasNext() {
			return gaeIterator.hasNext();
		}

		public T next() {
			Entity entity = gaeIterator.next();
			T obj = pm.map(query, entity);
			return obj;
		}

		public void remove() {
			gaeIterator.remove();
		}

	}

}