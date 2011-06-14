package siena;

import java.lang.reflect.Field;

import siena.embed.EmbeddedMap;
@EmbeddedMap
public class QueryJoin {
	
	public Field field;
	public String[] sortFields;
	
	public QueryJoin() {
		
	}
			
	public QueryJoin(Field field, String... sortFields) {
		this.field = field;
		this.sortFields = sortFields;
	}
	
	public String toString() {
		return "field:"+this.field!=null?this.field.getName():"null"
			+" - sortFields:"+this.sortFields;
	}
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryJoin l = (QueryJoin)obj;
		boolean ok = true;
		for(int i=0; i<sortFields.length; i++){
			if((sortFields[i]==null && l.sortFields[i]!=null) 
				|| !sortFields[i].equals(l.sortFields[i])) 
			{
				ok = false;
				break;
			}
		}
		return (this.field == null?l.field==null:this.field.equals(l.field)) && ok;
	}
}
