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
 * elements() is an accessor to the elements of the aggregator.
 * isSync() tells if the listquery is synchronized with the DB content. When created, it will be considered as synchronized by default
 * @param <T>
 */
public interface ListQuery<T> extends Iterable<T>, Query<T> {
	List<T> elements();
	boolean isSync();
}
