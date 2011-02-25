package siena;


public class QueryOptionOffset extends QueryOption{
	public static final int ID 	= 0x02;
	
	public int offset = 0;
	
	public QueryOptionOffset(int offset) {
		super(ID);
		this.offset = offset;
	}

	public QueryOptionOffset(State active, int offset) {
		super(ID, active);
		this.offset = offset;
	}

	public QueryOptionOffset(QueryOptionOffset option) {
		super(option);
		this.offset = option.offset;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionOffset(this);
	}

	public String toString() {
		return "type: OFFSET - state:"+this.state+" - offset:"+this.offset;
	}
}
