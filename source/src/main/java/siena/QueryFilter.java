package siena;

import java.lang.reflect.Field;

public class QueryFilter {
	
	public Field field;
	public String operator;
	public Object value;
	
	public QueryFilter(Field field, String operator, Object value) {
		this.field = field;
		this.operator = operator;
		this.value = value;
	}
	
}
