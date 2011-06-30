/**
 * 
 */
package siena.core;

import java.util.Collection;
import java.util.List;

/**
 * @author mandubian
 *
 */
public interface SyncList<E> extends List<E> {
	
	SyncList<E> sync();
	SyncList<E> forceSync();
	
	public <F extends E> boolean addAll(F ...c);

	public <F extends E> boolean addAll(int index, F ...c);
}
