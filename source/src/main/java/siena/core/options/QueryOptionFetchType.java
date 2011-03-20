package siena.core.options;


public class QueryOptionFetchType extends QueryOption{
    public static final int ID 	= 0x04;
	
    public enum Type {
    	NORMAL,
    	KEYS_ONLY,
    	ITER
    }
    
    public Type type = Type.NORMAL;
    
	public QueryOptionFetchType() {
		super(ID);
	}

	public QueryOptionFetchType(Type type) {
		super(ID);
		this.type = type;
	}
	
	public QueryOptionFetchType(QueryOptionFetchType option) {
		super(option);
		this.type = option.type;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionFetchType(this);
	}

	public String toString() {
		return "type:FETCHTYPE - state:"+this.state+ " - type:"+type.toString();
	}
}
