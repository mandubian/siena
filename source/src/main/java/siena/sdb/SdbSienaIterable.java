package siena.sdb;

import java.lang.reflect.Field;
import java.util.Iterator;

import siena.ClassInfo;
import siena.Query;
import siena.Util;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;

import com.amazonaws.services.simpledb.model.Item;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 * 
 * A Siena Iterable<Model> encapsulating a GAE Iterable<Entity> with its Iterator<Model>...
 */
public class SdbSienaIterable<Model> implements Iterable<Model> {
	Iterable<Item> items;
	Query<Model> query;

	SdbSienaIterable(Iterable<Item> items, Query<Model> query) {
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

		SdbSienaIterator(Query<T> query, Iterable<Item> items) {
			this.query = query;
			this.id = ClassInfo.getIdField(query.getQueriedClass());
			this.it = items.iterator();
			
			// if paginating and 0 results then no more data else resets noMoreDataAfter
			QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
			QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);

			// if has offset, advances in the iterator
			QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
			if(off!=null && off.offset != 0){
				for(int i=0; i<off.offset; i++){
					if(it.hasNext()){
						it.next();
					}
				}
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
			return it.hasNext();
		}

		public T next() {
			Item item = it.next();
			Class<T> clazz = query.getQueriedClass();
			QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);

			T obj = Util.createObjectInstance(clazz);
			
			switch(fetchType.fetchType){
			case KEYS_ONLY:
				SdbMappingUtils.fillModelKeysOnly(item, clazz, ClassInfo.getClassInfo(clazz), obj);
			case NORMAL:
			default:
				SdbMappingUtils.fillModel(item, clazz, ClassInfo.getClassInfo(clazz), obj);
			}			
			return obj;
		}

		public void remove() {
			it.remove();
		}

	}

}