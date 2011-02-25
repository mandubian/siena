package siena;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueryOption {
    /* the state of an option */
    public enum State {
        ACTIVE, PASSIVE
    }

    /* an option has a type, a state and an optional value (pagesize for  PAGINATE for example) */
    protected int type;
    protected State state = State.PASSIVE;
    //private Object value = null;
    
    /*
    private static final Map<Integer, QueryOption> defaults = new ConcurrentHashMap<Integer, QueryOption>() {
    	private static final long serialVersionUID = -2761955744911014026L;
	{
    	put(PAGINATE, new QueryOption(PAGINATE, 0));
    	put(OFFSET, new QueryOption(OFFSET, 0));
    	put(DB_CLUDGE, new QueryOption(DB_CLUDGE));
    	put(REUSABLE, new QueryOption(REUSABLE));
    	put(SEARCH, new QueryOption(SEARCH));
    }};
    */
    
    /* DEFAULT OPTIONS THAT BE ADDED DIRECTLY TO QUERY */
    /* PAGINATE is in fact an option and the page size is the value */
    /* try to reserve all number under 0x100 = 256 */
    /*public static final int PAGINATE 	= 0x01;
    public static final int DB_CLUDGE 	= 0x02;
    public static final int REUSABLE 	= 0x03;
    public static final int OFFSET 		= 0x04;
    public static final int SEARCH 		= 0x05;
    public static final int MAX_RESERVED= 0x100;*/
    
    public QueryOption(int option, State active, Object value){
        this.type = option;
        this.state = active;
//        this.value = value;
    }

    public QueryOption(int option, Object value){
        this.type = option;
        this.state = State.PASSIVE;
//        this.value = value;
    }

    public QueryOption(int option){
        this.type = option;
        this.state = State.PASSIVE;       
    }

    public QueryOption(QueryOption option){
        this.type = option.type;
        this.state = option.state;
//        this.value = option.value;
    }
    
    public QueryOption activate() {
        this.state = State.ACTIVE;
        return this;
    }
   
    public QueryOption passivate() {
        this.state = State.PASSIVE;
        return this;
    }
   
    public boolean isActive() {
        if(state == State.ACTIVE) return true;
        else return false;
    }
    
    /*public QueryOption value(Object value){
    	this.value = value;
    	return this;
    }
    
    public Object value(){
    	return this.value;
    }*/

    public QueryOption state(State state){
    	this.state = state;
    	return this;
    }

    public State state(){
    	return this.state;
    }
    
    public QueryOption type(int type){
    	this.type = type;
    	return this;
    }

    public int type(){
    	return this.type;
    }
    
	public QueryOption clone() {
		return new QueryOption(this);
	}
    /*
	public static QueryOption getInstance(int type) {
		if(defaults.containsKey(type)){
			return defaults.get(type).clone();
		}
		else {
			if(type>MAX_RESERVED) throw new SienaReservedQueryOptionTypeException(type, MAX_RESERVED);
			return new QueryOption(type);
		}
	}
	
	public static QueryOption getInstance(int type, State state) {
		if(defaults.containsKey(type)) {
			return defaults.get(type).clone().state(state);
		}
		else {
			if(type>MAX_RESERVED) throw new SienaReservedQueryOptionTypeException(type, MAX_RESERVED);
			return new QueryOption(type, state);
		}
	}
	
	public static QueryOption getInstance(int type, State state, Object val) {
		if(defaults.containsKey(type)){
			return defaults.get(type).clone().state(state).value(val);
		}
		else {
			if(type>MAX_RESERVED) throw new SienaReservedQueryOptionTypeException(type, MAX_RESERVED);
			return new QueryOption(type, state, val);
		}
	}
	*/
	
	public String toString() {
		return "type:"+this.type+" - state:"+this.state/*+" - value:"+this.value+"}"*/;
	}
}
