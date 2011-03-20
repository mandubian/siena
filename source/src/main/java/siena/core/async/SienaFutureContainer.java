package siena.core.async;

import java.util.concurrent.Future;

import siena.SienaException;

/**
 * Directly inspired by Result class from Objectify project 
 * @see http://code.google.com/p/objectify-appengine/source/browse/trunk/src/com/googlecode/objectify/Result.java
 * it encapsulates {@code java.util.concurrent.Future} and mimics get() function without requiring to catch 
 * any checked Exceptions such as ExecutionException. 
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 */
public class SienaFutureContainer<T> implements SienaFuture<T> {
	Future<T> future;

	public SienaFutureContainer(java.util.concurrent.Future<T> future){
		this.future = future;
	}
	
	public T get() {
		try {
			return future.get();
		} catch (Exception e) {
			// FIXME
			// here it might wrap several exceptions with ExecutionException.
			// needs to unwrap it???
			throw new SienaException(e);
		}
	}

	public java.util.concurrent.Future<T> getFuture() {
		return future;
	}
        
}
