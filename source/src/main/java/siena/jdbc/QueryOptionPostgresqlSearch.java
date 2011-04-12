package siena.jdbc;

import siena.core.options.QueryOption;
import siena.embed.EmbeddedMap;

@EmbeddedMap
public class QueryOptionPostgresqlSearch extends QueryOption{
    public static final int ID 	= 0x1003;
	
    public String language = "english";
    
	public QueryOptionPostgresqlSearch() {
		super(ID);
	}
    
	public QueryOptionPostgresqlSearch(String language) {
		super(ID);
		this.language = language;
	}

	public QueryOptionPostgresqlSearch(QueryOptionPostgresqlSearch option) {
		super(option);
		this.language = option.language;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionPostgresqlSearch(this);
	}

	public String toString() {
		return "type:JDBC_SEARCH - state:"+this.state+ " - language:"+language;
	}
}
