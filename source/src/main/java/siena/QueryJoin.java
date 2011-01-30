package siena;

import java.lang.reflect.Field;

public class QueryJoin {
	
	public Field field;
	public String[] sortFields;
	
	public QueryJoin(Field field, String... sortFields) {
		this.field = field;
		this.sortFields = sortFields;
	}
	
}
