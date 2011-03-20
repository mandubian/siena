package siena.jdbc;

import java.sql.PreparedStatement;

import siena.core.options.QueryOption;

public class QueryOptionJdbcContext extends QueryOption{
    public static final int ID 	= 0x1001;
	
    public PreparedStatement statement;
    public int limitParamIdx;
    public int offsetParamIdx;
    public boolean noMoreDataBefore = false;
    public boolean noMoreDataAfter = false;
    
	public QueryOptionJdbcContext() {
		super(ID);
	}
    
	public QueryOptionJdbcContext(PreparedStatement statement, int limitParamIdx, int offsetParamIdx) {
		super(ID);
		this.statement = statement;
		this.limitParamIdx = limitParamIdx;
		this.offsetParamIdx = offsetParamIdx;
	}

	public QueryOptionJdbcContext(QueryOptionJdbcContext option) {
		super(option);
		this.statement = option.statement;
		this.limitParamIdx = option.limitParamIdx;
		this.offsetParamIdx = option.offsetParamIdx;
		this.noMoreDataAfter = option.noMoreDataAfter;
		this.noMoreDataBefore = option.noMoreDataBefore;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionJdbcContext(this);
	}

	public String toString() {
		return "type:JDBC_CONTEXT - state:"+this.state
				+ " - limitParamIdx:"+limitParamIdx+" - offsetParamIdx:"+offsetParamIdx
				+ " - noMoreDataAfter:"+noMoreDataAfter+" - noMoreDataBefore:"+noMoreDataBefore;
	}
}
