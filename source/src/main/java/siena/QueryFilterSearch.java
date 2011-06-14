package siena;

import siena.core.options.QueryOption;

public class QueryFilterSearch extends QueryFilter {
	
	public String match;
	public QueryOption option;
	public String[] fields;
	
	public QueryFilterSearch() {
		
	}
	
	public QueryFilterSearch(String match, String... fields) {
		this.match = match;
		this.fields = fields;
	}
	
	public QueryFilterSearch(String match, QueryOption option, String... fields) {
		this.match = match;
		this.option = option;
		this.fields = fields;
	}
	
	public String toString() {
		return "match:"+this.match+" - option:"+this.option+" - fields:"+this.fields;
	}
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryFilterSearch l = (QueryFilterSearch)obj;
		boolean ok = true;
		for(int i=0; i<fields.length; i++){
			if((fields[i]==null && l.fields[i]!=null) 
					|| !fields[i].equals(l.fields[i])) 
			{
				ok = false;
				break;
			}
		}
		return (this.match == null?l.match==null:this.match.equals(l.match))
			&& (this.option == null?l.option==null:this.option.equals(l.option)) && ok;
	}
}
