package siena;

public class QuerySearch {
	
	public String match;
	public boolean inBooleanMode;
	public String index;
	
	public QuerySearch(String match, boolean inBooleanMode,
			String index) {
		this.match = match;
		this.inBooleanMode = inBooleanMode;
		this.index = index;
	}
	
}
