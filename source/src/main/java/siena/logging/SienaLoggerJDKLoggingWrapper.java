/**
 * 
 */
package siena.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This wrapper implemented the Siena logger based in the JDK Logging.
 * @author jsanca
 *
 */
public class SienaLoggerJDKLoggingWrapper implements SienaLogger {

	private static final long serialVersionUID = -7104006291635231463L;

	private Logger logger = null;
	
	/**
	 * Constructor.
	 */
	public SienaLoggerJDKLoggingWrapper(Logger logger) {

		super ();
		this.logger = logger;
	} // SienaLoggerJDKLoggingWrapper.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#info(java.lang.Object)
	 */
	public void info(Object msg) {

		this.logger.info(msg.toString());
	} // info.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#info(java.lang.Object, java.lang.Throwable)
	 */
	public void info(Object msg, Throwable throwable) {

		this.logger.log(Level.INFO, msg.toString(), throwable);
	} // info.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#isInfoEnabled()
	 */
	public boolean isInfoEnabled() {

		return this.logger.isLoggable(Level.INFO);
	} // isInfoEnabled.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#isSevereEnabled()
	 */
	public boolean isSevereEnabled() {
		
		return this.logger.isLoggable(Level.SEVERE);
	} // isSevereEnabled.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#isWarningEnabled()
	 */
	public boolean isWarningEnabled() {

		return this.logger.isLoggable(Level.WARNING);
	} // isWarningEnabled.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#severe(java.lang.Object)
	 */
	public void severe(Object msg) {

		this.logger.severe(msg.toString());
	} // severe.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#severe(java.lang.Object, java.lang.Throwable)
	 */
	public void severe(Object msg, Throwable throwable) {

		this.logger.log(Level.SEVERE, msg.toString(), throwable);
	} // severe.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#warning(java.lang.Object)
	 */
	public void warning(Object msg) {

		this.logger.warning(msg.toString());
	} // warning.

	/* (non-Javadoc)
	 * @see siena.logging.SienaLogger#warning(java.lang.Object, java.lang.Throwable)
	 */
	public void warning(Object msg, Throwable throwable) {

		this.logger.log(Level.WARNING, msg.toString(), throwable);
	} // warning.

} // E:O:F:SienaLoggerJDKLoggingWrapper.
