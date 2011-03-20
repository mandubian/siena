package siena.gae;

import java.lang.reflect.Field;
import java.util.Iterator;

import siena.ClassInfo;
import siena.Query;
import siena.core.options.QueryOptionPage;

import com.google.appengine.api.datastore.Entity;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 * 
 * A Siena Iterable<Model> encapsulating a GAE Iterable<Entity> with its Iterator<Model>...
 */
public class GaeSienaIterable<Model> implements Iterable<Model> {
	Iterable<Entity> gaeIterable;
	Query<Model> query;
	GaePersistenceManager pm;

	GaeSienaIterable(GaePersistenceManager pm, Iterable<Entity> gaeIterable, Query<Model> query) {
		this.gaeIterable = gaeIterable;
		this.query = query;
		this.pm = pm;
	}

	public Iterator<Model> iterator() {
		return new SienaGaeIterator<Model>(query, gaeIterable);
	}

	public class SienaGaeIterator<T> implements Iterator<T> {
		Field id;
		Query<T> query;
		Iterator<Entity> gaeIterator;

		SienaGaeIterator(Query<T> query, Iterable<Entity> gaeIterable) {
			this.query = query;
			this.id = ClassInfo.getIdField(query.getQueriedClass());
			this.gaeIterator = gaeIterable.iterator();
			
			// if paginating and 0 results then no more data else resets noMoreDataAfter
			QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
			QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
			if(pag.isPaginating()){
				if(!gaeIterator.hasNext()){
					gaeCtx.noMoreDataAfter = true;
				}else {
					gaeCtx.noMoreDataAfter = false;
				}
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