package siena;

import java.lang.reflect.Field;

import siena.embed.JsonDeserializeAs;

public class QueryFilterSimple extends QueryFilter {
	
	public Field field;
	public String operator;
	@JsonDeserializeAs(String.class)
	public Object value;
	
	public QueryFilterSimple(){
	}
	
	public QueryFilterSimple(Field field, String operator, Object value) {
		this.field = field;
		this.operator = operator;
		this.value = value;
	}
	
	public String toString() {
		return "field:"+this.field!=null?this.field.getName():"null"+" - operator:"+this.operator
				+" - value:"+this.value;
	}
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryFilterSimple l = (QueryFilterSimple)obj;
		return (this.field == null?l.field==null:this.field.equals(l.field))
			&& (this.operator == null?l.operator==null:this.operator.equals(l.operator)) 
			&& (this.value == null?l.value==null:this.value.equals(l.value));
	}
}
