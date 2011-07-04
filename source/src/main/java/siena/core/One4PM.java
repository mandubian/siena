package siena.core;



/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 * One is just a lazy loader
 * 
 * @param <T>
 */
public interface One4PM<T> extends One<T>  {
	One4PM<T> setSync(boolean isSync);
	One4PM<T> setModified(boolean isModified);
	boolean isModified();
	T getPrev();

	One4PM<T> aggregationMode(Object aggregator, String fieldName);
	One4PM<T> relationMode(Object owner, String fieldName);
}
