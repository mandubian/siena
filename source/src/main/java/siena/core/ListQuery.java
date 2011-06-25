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
 * elements() is an acccessor to the elements of the aggregator.
 * @param <T>
 */
public interface ListQuery<T> extends Iterable<T>, Query<T> {
	List<T> elements();
}
