/**
 * 
 */
package siena.logging;

import java.util.logging.Logger;

/**
 * Factory for siena logger.
 * 
 * @author jsanca
 * 
 */
public class SienaLoggerFactory {

	/**
	 * Get the siena logger.
	 * @param logName String.
	 * @return SienaLogger.
	 */
	public static SienaLogger getLogger(String logName) {

		SienaLogger logger = null;

		// TODO: read the properties and config the specific logger.
		logger = new SienaLoggerJDKLoggingWrapper(Logger.getLogger(logName));

		return logger;
	} // getLogger.

	/**
	 * Get the siena logger.
	 * @param clazz Class
	 * @return SienaLogger.
	 */
	@SuppressWarnings("unchecked")
	public static SienaLogger getLogger(Class clazz) {

		return getLogger(clazz.getName());
	} // getLogger.
} // E:O:F:SienaLoggerFactory.
