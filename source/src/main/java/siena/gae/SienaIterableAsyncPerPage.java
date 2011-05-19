package siena.gae;

import java.util.Iterator;

import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;

/**
 * @author mandubian
 * 
 *         A Siena Iterable<Model> encapsulating a iteration per page
 *         its Iterator<Model>...
 */
public class SienaIterableAsyncPerPage<T> implements Iterable<T> {
    
    /**
     * The wrapped <code>Query</code>.
     */
    private QueryAsync<T> query;
        
    /**
     * The pageSize
     */
    private int pageSize;
    
    /**
     * The wrapped <code>Iterable</code>.
     */
    Iterable<T> iterable;
    
	public SienaIterableAsyncPerPage(QueryAsync<T> query, int pageSize, Iterable<T> iterable) {
		this.query = query;
		this.pageSize = pageSize;
		
		this.iterable = iterable;
	}

	public Iterator<T> iterator() {
		return new SienaIteratorAsyncPerPage(iterable.iterator());
	}

	// only constructs the iterator with Class<V> in order to transmit the generic type T
	public class SienaIteratorAsyncPerPage implements Iterator<T> {
		private Iterator<T> iterator;
		private boolean hasNext = false;
		
		SienaIteratorAsyncPerPage(Iterator<T> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			if(iterator.hasNext()) {
				hasNext = true;
			}
			else {
				this.iterator = query.nextPage().iter().get().iterator();
				
				hasNext = this.iterator.hasNext();
			}
			return hasNext;
		}

		@Override
		public T next() {
			if(hasNext || iterator.hasNext()){
				return iterator.next();
			}else {
				this.iterator = query.nextPage().iter().get().iterator();
				
				return this.iterator.next();
			}
		}

		@Override
		public void remove() {
			// doesn't delete row because it REALLY deletes row from DB!!!
			// need to think about it
			/*try {
				
	            rs.deleteRow();
	        } catch (SQLException e) {
	        	throw new SienaException(e);
	        }*/
		}

		@Override
		protected void finalize() throws Throwable {
			query.release();
			super.finalize();
		}

	}

	@Override
	protected void finalize() throws Throwable {
		query.release();
		super.finalize();
	}


}