package siena;

import java.lang.reflect.Field;

public class QueryOrder {
	
	public Field field;
	public boolean ascending;
	
	public QueryOrder(Field field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}
	
}
