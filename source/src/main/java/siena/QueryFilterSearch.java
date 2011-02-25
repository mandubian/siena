package siena;

public class QueryFilterSearch extends QueryFilter {
	
	public String match;
	public QueryOption option;
	public String[] fields;
	
	public QueryFilterSearch(String match, String... fields) {
		this.match = match;
		this.fields = fields;
	}
	
	public QueryFilterSearch(String match, QueryOption option, String... fields) {
		this.match = match;
		this.option = option;
		this.fields = fields;
	}
	
}
