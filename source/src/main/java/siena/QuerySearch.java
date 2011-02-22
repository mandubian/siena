package siena;

public class QuerySearch {
	
	public String match;
	public QueryOption option;
	public String[] fields;
	
	public QuerySearch(String match, String... fields) {
		this.match = match;
		this.fields = fields;
	}
	
	public QuerySearch(String match, QueryOption option, String... fields) {
		this.match = match;
		this.option = option;
		this.fields = fields;
	}
	
}
