package siena.core;

import java.util.Iterator;

import siena.Query;

/**
 * @author mandubian
 * 
 *         A Siena Iterable<Model> encapsulating a iteration per page
 *         its Iterator<Model>...
 */
public class SienaIterablePerPage<T> implements Iterable<T> {
    
    /**
     * The wrapped <code>Query</code>.
     */
    private Query<T> query;
        
    /**
     * The pageSize
     */
    private int pageSize;
    
    /**
     * The wrapped <code>Iterable</code>.
     */
	Iterable<T> iterable;
    
	public SienaIterablePerPage(Query<T> query, int pageSize) {
		this.query = query;
		this.pageSize = pageSize;
		
		this.iterable = query.paginate(this.pageSize).iter();
	}

	public Iterator<T> iterator() {
		return new SienaIteratorPerPage(iterable.iterator());
	}

	// only constructs the iterator with Class<V> in order to transmit the generic type T
	public class SienaIteratorPerPage implements Iterator<T> {
		private Iterator<T> iterator;
		private boolean hasNext = false;
		
		SienaIteratorPerPage(Iterator<T> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			if(iterator.hasNext()) {
				hasNext = true;
			}
			else {
				iterable = query.nextPage().iter();
				this.iterator = iterable.iterator();
				
				hasNext = this.iterator.hasNext();
			}
			return hasNext;
		}

		@Override
		public T next() {
			if(hasNext || iterator.hasNext()){
				return iterator.next();
			}else {
				iterable = query.nextPage().iter();
				this.iterator = iterable.iterator();
				
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