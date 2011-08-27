package siena.core;

import java.lang.reflect.Field;
import java.util.List;

import siena.QueryFilter;
import siena.embed.JsonDeserializeAs;

public class QueryFilterEmbedded extends QueryFilter {
	
	public List<Field> fields;
	public String operator;
	public String fieldSeparator;
	@JsonDeserializeAs(String.class)
	public Object value;
	
	public QueryFilterEmbedded(){
	}
	
	public QueryFilterEmbedded(List<Field> fields, String operator, String fieldSeparator, Object value) {
		this.fields = fields;
		this.operator = operator;
		this.value = value;
		this.fieldSeparator = fieldSeparator;
	}
	
	public String toString() {
		return "fields:"+this.fields!=null?this.fields.toString():"null"
			+" - operator:"+this.operator
			+" - fieldSeparator"+this.fieldSeparator
			+" - value:"+this.value;
	}
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryFilterEmbedded l = (QueryFilterEmbedded)obj;
		boolean b = true;
		for(int i=0; i<fields.size();i++){
			if(fields.get(i) != null && !fields.get(i).equals(l.fields.get(i))){
				b = false;
			}
		}
		return b
			&&(this.operator == null?l.operator==null:this.operator.equals(l.operator)) 
			&& (this.fieldSeparator == null?l.fieldSeparator==null:this.fieldSeparator.equals(l.fieldSeparator)) 
			&& (this.value == null?l.value==null:this.value.equals(l.value));
	}
}
