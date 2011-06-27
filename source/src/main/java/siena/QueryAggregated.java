package siena;

import java.lang.reflect.Field;

import siena.embed.EmbeddedMap;
@EmbeddedMap
public class QueryAggregated {
	
	public Object aggregator;
	public Field field;
	
	public QueryAggregated() {
		
	}
			
	public QueryAggregated(Object aggregator, Field field) {
		this.aggregator = aggregator;
		this.field = field;
	}
	
	public String toString() {
		return "aggregator:"+aggregator!=null?this.aggregator.toString():"null"
			+" - field:"+this.field!=null?this.field.getName():"null";
	}
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryAggregated l = (QueryAggregated)obj;
		
		return (this.aggregator == null?l.aggregator==null:this.aggregator.equals(l.aggregator)) 
			&& (this.field == null?l.field==null:this.field.equals(l.field)) ;
	}
}
