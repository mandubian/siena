package siena;


public class QueryOptionReuse extends QueryOption{
    public static final int ID 	= 0x03;
	
	public QueryOptionReuse() {
		super(ID);
	}

	public QueryOptionReuse(QueryOptionReuse option) {
		super(option);
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionReuse(this);
	}

	public String toString() {
		return "type:REUSE - state:"+this.state;
	}
}
