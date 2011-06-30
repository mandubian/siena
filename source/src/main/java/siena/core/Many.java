package siena.core;

import java.util.List;

import siena.Query;


/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 * A query encapsulating a list of elements.
 * it extends Query and Iterable but not Collection because collection provides 
 * to many functions with names to near from Query... It would be misleading
 *
 * asList manages all the elements as a list (useful to build the list)
 * asQuery accesses the elements through a query (useful mainly in read mode)
 * isSync() tells if the listquery is synchronized with the DB content. When created, it will be considered as synchronized by default
 * @param <T>
 */
public interface Many<T>  {
	SyncList<T> asList();
	Query<T> asQuery();
}
