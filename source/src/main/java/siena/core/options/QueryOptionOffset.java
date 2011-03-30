package siena.core.options;



public class QueryOptionOffset extends QueryOption{
	public static final int ID 	= 0x02;
	
	public int offset = 0;
	
	public OffsetType offsetType = OffsetType.MANUAL;
	
	public enum OffsetType {
		MANUAL,
		PAGINATING
	}
	
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

	public boolean isManual() {
		return offsetType == OffsetType.MANUAL;
	}
	
	public boolean isPaginating() {
		return offsetType == OffsetType.PAGINATING;
	}

	public String toString() {
		return "type:OFFSET - state:"+this.state+" - offset:"+this.offset+" - offsetType:"+this.offsetType;
	}
	
	public boolean equals(QueryOptionOffset opt){
		return super.equals(opt) && this.offset == opt.offset && this.offsetType == opt.offsetType;
	}

}
