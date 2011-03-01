package siena.gae;

import siena.QueryOption;

import com.google.appengine.api.datastore.PreparedQuery;

public class QueryOptionGaeContext extends QueryOption{
    public static final int ID 	= 0x2001;
	
    public String cursor;
    public boolean useCursor = true;
    public PreparedQuery query;
	public QueryOptionGaeContext() {
		super(ID);
	}
    
	public QueryOptionGaeContext(String cursor, PreparedQuery query) {
		super(ID);
		this.cursor = cursor;
		this.query = query;
	}

	public QueryOptionGaeContext(QueryOptionGaeContext option) {
		super(option);
		this.cursor = option.cursor;
		this.query = option.query;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionGaeContext(this);
	}

	public String toString() {
		return "type:JDBC_CONTEXT - state:"+this.state+ " - cursor:"+cursor+" - useCursor:"+useCursor;
	}
}
