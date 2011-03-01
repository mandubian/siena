package siena;


public class QueryOptionPaginate extends QueryOption{
	public static final int ID 	= 0x01;
	
	public int pageSize = 0;
	
	public QueryOptionPaginate(int pageSize) {
		super(ID);
		this.pageSize = pageSize;
	}

	public QueryOptionPaginate(State active, int pageSize) {
		super(ID, active);
		this.pageSize = pageSize;
	}

	public QueryOptionPaginate(QueryOptionPaginate option) {
		super(option);
		this.pageSize = option.pageSize;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionPaginate(this);
	}

	public String toString() {
		return "type:PAGINATE - state:"+this.state+" - pageSize:"+this.pageSize;
	}
}
