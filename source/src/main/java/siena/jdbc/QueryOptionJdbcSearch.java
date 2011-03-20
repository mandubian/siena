package siena.jdbc;

import siena.core.options.QueryOption;

public class QueryOptionJdbcSearch extends QueryOption{
    public static final int ID 	= 0x1002;
	
    public boolean booleanMode = true;
    
	public QueryOptionJdbcSearch() {
		super(ID);
	}
    
	public QueryOptionJdbcSearch(boolean booleanMode) {
		super(ID);
		this.booleanMode = booleanMode;
	}

	public QueryOptionJdbcSearch(QueryOptionJdbcSearch option) {
		super(option);
		this.booleanMode = option.booleanMode;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionJdbcSearch(this);
	}

	public String toString() {
		return "type:JDBC_SEARCH - state:"+this.state+ " - booleanMode:"+booleanMode;
	}
}
