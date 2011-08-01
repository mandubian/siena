package siena;

import java.lang.reflect.Field;

import siena.embed.EmbeddedMap;
@EmbeddedMap
public class QueryOwned {
	
	public Object owner;
	public Field field;
	
	public QueryOwned() {
		
	}
			
	public QueryOwned(Object aggregator, Field field) {
		this.owner = aggregator;
		this.field = field;
	}
	
	public String toString() {
		return "owner:"+owner!=null?this.owner.toString():"null"
			+" - field:"+this.field!=null?this.field.getName():"null";
	}
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryOwned l = (QueryOwned)obj;
		
		return (this.owner == null?l.owner==null:this.owner.equals(l.owner)) 
			&& (this.field == null?l.field==null:this.field.equals(l.field)) ;
	}
}
