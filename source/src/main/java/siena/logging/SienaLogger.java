/**
 * 
 */
package siena.logging;

import java.io.Serializable;

/**
 * Basic signature for logging proposals
 * @author jsanca
 *
 */
public interface SienaLogger extends Serializable {

	/**
	 * Do severe log level.
	 * @param msg Object
	 */
	void severe (Object msg);
	
	/**
	 * Do severe log level; including throwable logging as well.
	 * @param msg Object
	 * @param throwable Throwable
	 */
	void severe (Object msg, Throwable throwable);
	
	/**
	 * Determine if the severe level is enabled.
	 * @return boolean true if is enabled.
	 */
	boolean isSevereEnabled ();
	
	/**
	 * Do warning log level.
	 * @param msg Object
	 */
	void warning (Object msg);
	
	/**
	 * Do warning log level; including throwable logging as well.
	 * @param msg Object
	 * @param throwable Throwable
	 */
	void warning (Object msg, Throwable throwable);
	

	/**
	 * Determine if the warning level is enabled.
	 * @return boolean true if is enabled.
	 */
	boolean isWarningEnabled ();
	

	/**
	 * Do info log level.
	 * @param msg Object
	 */
	void info (Object msg);
	

	/**
	 * Do info log level; including throwable logging as well.
	 * @param msg Object
	 * @param throwable Throwable
	 */
	void info (Object msg, Throwable throwable);
	

	/**
	 * Determine if the info level is enabled.
	 * @return boolean true if is enabled.
	 */
	boolean isInfoEnabled ();
} // E:O:F:SienaLogger.
