package siena.gae;

import siena.core.options.QueryOption;

public class QueryOptionGaeSearch extends QueryOption{
    public static final int ID 	= 0x2002;
	
    public enum Type {
    	NORMAL
    }
    public Type mode = Type.NORMAL;
    
	public QueryOptionGaeSearch() {
		super(ID);
	}
    
	public QueryOptionGaeSearch(Type mode) {
		super(ID);
		this.mode = mode;
	}

	public QueryOptionGaeSearch(QueryOptionGaeSearch option) {
		super(option);
		this.mode = option.mode;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionGaeSearch(this);
	}

	public String toString() {
		return "type:JDBC_SEARCH - state:"+this.state+ " - mode:"+mode;
	}
}
