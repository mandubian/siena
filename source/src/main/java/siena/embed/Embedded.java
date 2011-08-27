package siena.embed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Embedded {
	public enum Mode {
		SERIALIZE_JSON,
		SERIALIZE_JAVA,
		NATIVE
	}
	
	Mode mode() default Mode.SERIALIZE_JSON;
}
