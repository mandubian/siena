package siena.gae;

import java.lang.reflect.Field;
import java.util.Iterator;

import siena.ClassInfo;
import siena.Query;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 * 
 * A Siena Iterable<Model> encapsulating a GAE Iterable<Entity> with its Iterator in asynchronous mode<Model>...
 */
public class GaeSienaIterableWithCursor<Model> implements Iterable<Model> {
	QueryResultIterable<Entity> gaeIterable;
	Query<Model> query;
	GaePersistenceManager pm;
	
	GaeSienaIterableWithCursor(GaePersistenceManager pm, QueryResultIterable<Entity> gaeIterable, Query<Model> query) {
		this.gaeIterable = gaeIterable;
		this.query = query;
		this.pm = pm;
	}

	public Iterator<Model> iterator() {
		return new SienaGaeIteratorWithCursor<Model>(query, gaeIterable);
	}

	public class SienaGaeIteratorWithCursor<T> implements Iterator<T> {
		Field id;
		Query<T> query;
		QueryResultIterator<Entity> gaeIterator;

		SienaGaeIteratorWithCursor(Query<T> query, QueryResultIterable<Entity> gaeIterable) {
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
			QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
			// overrides current cursor with the current iterator cursor
			// sets the current cursor (in stateful mode, cursor is always kept for further use)
			Cursor cursor = gaeIterator.getCursor();
			if(cursor!=null) {
				gaeCtx.setCurrentCursor(cursor.toWebSafeString());
			}
			
			// keeps track of the offset anyway if not paginating
			QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
			offset.offset++;

			T obj = pm.map(query, entity);
			return obj;
		}

		public void remove() {
			gaeIterator.remove();
		}

	}

}