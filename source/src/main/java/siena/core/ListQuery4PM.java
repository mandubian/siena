package siena.core;



/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 * an extension interface being used by the persistencemanager to set the synchronization state
 * 
 * @param <T>
 */
public interface ListQuery4PM<T> extends ListQuery<T> {
	ListQuery4PM<T> setSync(boolean isSync);
}
