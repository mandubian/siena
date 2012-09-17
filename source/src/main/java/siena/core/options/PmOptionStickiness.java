package siena.core.options;

public enum PmOptionStickiness
{
    /**
     * option is set globally (for the PM instance)
     */
	STICKY,
	
	/**
	 * option is set only for the thread context
	 */
	NOT_STICKY
}