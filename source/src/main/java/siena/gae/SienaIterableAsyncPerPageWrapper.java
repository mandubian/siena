package siena.gae;

import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;

/**
 * @author mandubian
 * 
 *         A Siena Iterable<Model> encapsulating a iteration per page
 *         its Iterator<Model>...
 */
public class SienaIterableAsyncPerPageWrapper<T> implements SienaFuture<Iterable<T>> {
    
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
    SienaFuture<Iterable<T>> iterable;
    
	public SienaIterableAsyncPerPageWrapper(QueryAsync<T> query, int pageSize) {
		this.query = query;
		this.pageSize = pageSize;
		
		this.iterable = query.paginate(this.pageSize).iter();
	}

	public Iterable<T> get() {
		return new SienaIterableAsyncPerPage<T>(query, pageSize, iterable.get());
	}

}