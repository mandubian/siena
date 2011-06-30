package siena.core;

import java.lang.reflect.Field;
import java.util.List;



/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 * an extension interface being used by the persistencemanager to set the synchronization state
 * 
 * @param <T>
 */
public interface Many4PM<T> extends Many<T> {
	Many4PM<T> setSync(boolean isSync);
	List<T> asList2Remove();
	Many4PM<T> aggregationMode(Object aggregator, Field field);
	Many4PM<T> associationMode();
}
