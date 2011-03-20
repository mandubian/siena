package siena.core.options;


public class QueryOptionState extends QueryOption{
    public static final int ID 	= 0x03;
	
    // by default a query is stateless
    public LifeCycle lifeCycle = LifeCycle.STATELESS;
    
    public enum LifeCycle {
    	STATELESS,
    	STATEFUL
    }
    
	public QueryOptionState() {
		super(ID);
	}

	public QueryOptionState(QueryOptionState option) {
		super(option);
		this.lifeCycle = option.lifeCycle;
	}
	
	@Override
	public QueryOption clone() {
		return new QueryOptionState(this);
	}

	public boolean isStateful() {
		return lifeCycle == LifeCycle.STATEFUL;		
	}

	public boolean isStateless() {
		return lifeCycle == LifeCycle.STATELESS;		
	}

	public String toString() {
		return "type:STATE - state:"+this.lifeCycle;
	}
}
