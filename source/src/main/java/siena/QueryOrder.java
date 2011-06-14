package siena;

import java.lang.reflect.Field;

import siena.embed.EmbeddedMap;
@EmbeddedMap
public class QueryOrder {
	
	public Field field;
	public boolean ascending;
	// for joined sort field
	public Field parentField;
	
	public QueryOrder() {
		
	}
	
	public QueryOrder(Field field, boolean ascending) {
		this.field = field;
		this.ascending = ascending;
	}

	public QueryOrder(Field field, boolean ascending, Field parentField) {
		this.field = field;
		this.ascending = ascending;
		this.parentField = parentField;
	}

	public String toString() {
		return "field:"+this.field!=null?this.field.getName():"null"
			+" - ascending:"+this.ascending
			+"parentField:"+this.parentField!=null?this.parentField.getName():"null";
	}
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryOrder l = (QueryOrder)obj;
		return 
			(this.field == null?l.field==null:this.field.equals(l.field))
			&& this.ascending==l.ascending 
			&& (this.parentField == null?l.parentField==null:this.parentField.equals(l.parentField)) ;
	}
}
