package siena;

import java.lang.reflect.Field;

public class QueryOrder {
	
	public Field field;
	public boolean ascending;
	// for joined sort field
	public Field parentField;
	
	public QueryOrder(Field field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}

	public QueryOrder(Field field, boolean ascending, Field parentField) {
		this.field = field;
		this.ascending = ascending;
		this.parentField = parentField;
	}

}
