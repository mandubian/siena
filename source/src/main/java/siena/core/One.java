package siena.core;



/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 * One is just a lazy loader
 * 
 * @param <T>
 */
public interface One<T>  {
	T get();
	void set(T obj);
	One<T> sync();
	One<T> forceSync();
}
