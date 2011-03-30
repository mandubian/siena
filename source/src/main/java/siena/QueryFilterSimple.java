package siena;

import java.lang.reflect.Field;

import siena.embed.EmbeddedMap;

public class QueryFilterSimple extends QueryFilter {
	
	public Field field;
	public String operator;
	public Object value;
	
	public QueryFilterSimple(Field field, String operator, Object value) {
		this.field = field;
		this.operator = operator;
		this.value = value;
	}
	
}
