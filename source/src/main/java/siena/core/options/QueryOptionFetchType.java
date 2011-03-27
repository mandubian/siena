package siena.core.options;

import siena.embed.EmbeddedMap;

@EmbeddedMap
public class QueryOptionFetchType extends QueryOption{
    public static final int ID 	= 0x04;
	
    public enum Type {
    	NORMAL,
    	KEYS_ONLY,
    	ITER
    }
    
    public Type fetchType = Type.NORMAL;
    
	public QueryOptionFetchType() {
		super(ID);
	}

	public QueryOptionFetchType(Type type) {
		super(ID);
		this.fetchType = type;
	}
	
	public QueryOptionFetchType(QueryOptionFetchType option) {
		super(option);
		this.fetchType = option.fetchType;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionFetchType(this);
	}
	
	public boolean equals(QueryOptionFetchType opt){
		return super.equals(opt) && this.fetchType == opt.fetchType && this.state == opt.state;
	}

	public String toString() {
		return "type:FETCHTYPE - state:"+this.state+ " - type:"+fetchType.toString();
	}
}
