package siena.core.async;

/**
 * Directly inspired by Result class from Objectify project 
 * @see http://code.google.com/p/objectify-appengine/source/browse/trunk/src/com/googlecode/objectify/Result.java
 * it encapsulates {@code java.util.concurrent.Future} and mimics get() function without requiring to catch 
 * any checked Exceptions such as ExecutionException. 
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 */
public interface SienaFuture<T> {
        /**
         * Waits if necessary for the computation to complete, and then retrieves its result.
         * 
         * @return the result
         */
        T get();

}
