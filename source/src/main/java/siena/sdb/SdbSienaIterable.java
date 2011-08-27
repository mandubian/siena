package siena.sdb;

import java.lang.reflect.Field;
import java.util.Iterator;

import siena.ClassInfo;
import siena.Query;
import siena.Util;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.gae.GaePersistenceManager;

import com.amazonaws.services.simpledb.model.Item;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 * 
 * A Siena Iterable<Model> encapsulating a GAE Iterable<Entity> with its Iterator<Model>...
 */
public class SdbSienaIterable<Model> implements Iterable<Model> {
	protected Iterable<Item> items;
	protected Query<Model> query;
	protected SdbPersistenceManager pm;

	SdbSienaIterable(SdbPersistenceManager pm, Iterable<Item> items, Query<Model> query) {
		this.pm = pm;
		this.items = items;
		this.query = query;
	}

	public Iterator<Model> iterator() {
		return new SdbSienaIterator<Model>(query, items);
	}

	public class SdbSienaIterator<T> implements Iterator<T> {
		Field id;
		Query<T> query;
		Iterator<Item> it;
		int idx = 0; //used to count when in pagination

		QueryOptionPage pag;
		QueryOptionSdbContext sdbCtx;

		SdbSienaIterator(Query<T> query, Iterable<Item> items) {
			this.query = query;
			this.id = ClassInfo.getIdField(query.getQueriedClass());
			this.it = items.iterator();
			
			// if paginating and 0 results then no more data else resets noMoreDataAfter
			pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
			sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
			
			// if has offset, advances in the iterator
			QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
			if(off!=null && off.offset != 0){
				for(int i=0; i<off.offset; i++){
					if(it.hasNext()){
						it.next();
					}
				}
				
				// moves real offset if not paginating
				if(!pag.isPaginating())
					sdbCtx.realOffset += off.offset;
			}
			
			if(pag.isPaginating()){
				if(!it.hasNext()){
					sdbCtx.noMoreDataAfter = true;
				}else {
					sdbCtx.noMoreDataAfter = false;
				}
			}
			
		}

		public boolean hasNext() {
			boolean n = it.hasNext();
			if(!n){
				if(!pag.isPaginating()){
					// not paginating = we get current token as the move has already been done 
					// tries to fetch new items if has a live token
					if(sdbCtx.currentToken() != null && idx < sdbCtx.realPageSize){
						SdbSienaIterable<Model> iter = (SdbSienaIterable<Model>)pm.iter(query);
						items = iter.items;
						it = items.iterator();
						idx = 0;
						return it.hasNext();
					}
				}else {
					// paginating = if has next token & not already at the last element of the page
					// moves to next token (but not before verifying it has next token)
					if(sdbCtx.hasNextToken() && idx < pag.pageSize){
						sdbCtx.nextToken();
						SdbSienaIterable<Model> iter = (SdbSienaIterable<Model>)pm.iter(query);
						items = iter.items;
						it = items.iterator();
						idx = 0;
						
						return it.hasNext();
					}
				}
				
				return false;
			}
			return true;
		}

		public T next() {
			Item item = it.next();
			idx++;
			
			// moves realoffset if not paginating
			if(!pag.isPaginating()){
				sdbCtx.realOffset++;
			}
			
			
			Class<T> clazz = query.getQueriedClass();
			QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);

			T obj = Util.createObjectInstance(clazz);
			
			switch(fetchType.fetchType){
			case KEYS_ONLY:
				SdbMappingUtils.fillModelKeysOnly(item, clazz, ClassInfo.getClassInfo(clazz), obj);
			case NORMAL:
			default:
				SdbMappingUtils.fillModel(item, clazz, ClassInfo.getClassInfo(clazz), obj);
				
				// join management
				if(!query.getJoins().isEmpty() 
						|| !ClassInfo.getClassInfo(query.getQueriedClass()).joinFields.isEmpty())
					pm.mapJoins(query, obj);
			}			
			return obj;
		}

		public void remove() {
			it.remove();
		}

	}

}