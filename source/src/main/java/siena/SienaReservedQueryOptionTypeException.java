package siena;

/**
 * @author mandubian
 *
 */
public class SienaReservedQueryOptionTypeException extends RuntimeException {
	private static final long serialVersionUID = -5078174427412462781L;

	public SienaReservedQueryOptionTypeException(int type, int max) {
		super("Siena Exception: QueryOption type "+type+" should be more than "+max
				+" as those types are reserved");
	}

	public SienaReservedQueryOptionTypeException(int type, int max, String message, Throwable cause) {
		super("Siena Exception: QueryOption type "+type+" should be more than "+max
				+" as those types are reserved"
				+ message, cause);

	}

	public SienaReservedQueryOptionTypeException(int type, int max, String message) {
		super("Siena Exception: QueryOption type "+type+" should be more than "+max
				+" as those types are reserved"
				+ message);
	}

	public SienaReservedQueryOptionTypeException(int type, int max, Throwable cause) {
		super("Siena Exception: QueryOption type "+type+" should be more than "+max
				+" as those types are reserved", cause);
	}

	
}
