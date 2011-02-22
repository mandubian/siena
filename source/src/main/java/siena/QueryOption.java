package siena;

import java.util.HashMap;
import java.util.Map;

public class QueryOption {
    /* an enum defining the types of options */
    public enum Type {
        PAGINATE,
        DB_CLUDGE,
        REUSABLE,
        OFFSET,
        SEARCH,
        WHAT_YOU_NEED
    }

    /* the state of an option */
    public enum State {
        ACTIVE, PASSIVE
    }

    /* an option has a type, a state and an optional value (pagesize for  PAGINATE for example) */
    public Type type;
    private State state = State.PASSIVE;
    private Object value = null;
    
    static private final Map<Type, QueryOption> defaults = new HashMap<Type, QueryOption>() {{
    	put(Type.PAGINATE, new QueryOption(Type.PAGINATE, 0));
    	put(Type.OFFSET, new QueryOption(Type.OFFSET, 0));
    	put(Type.DB_CLUDGE, new QueryOption(Type.DB_CLUDGE));
    	put(Type.REUSABLE, new QueryOption(Type.REUSABLE));
    	put(Type.SEARCH, new QueryOption(Type.SEARCH));
    	put(Type.WHAT_YOU_NEED, new QueryOption(Type.WHAT_YOU_NEED));
    }};
    
    /* DEFAULT OPTIONS THAT BE ADDED DIRECTLY TO QUERY */
    /* PAGINATE is in fact an option and the page size is the value */
    static public final QueryOption PAGINATE = new QueryOption(Type.PAGINATE, 0);

    /* this one is the horrible cludge I propose to store specific DB stuff between query calls... it's like a void* in C but anyway, this is the best way I found :) */
    static public final QueryOption DB_CLUDGE = new QueryOption(Type.DB_CLUDGE);
   
    /* makes a query reusable keeping some opened resources between calls... the statement for JDBC... as you may deduce, the previous DB_CLUDGE will be used to keep this state */
    static public final QueryOption REUSABLE = new QueryOption(Type.REUSABLE);

    /* makes a query reusable keeping some opened resources between calls... the statement for JDBC... as you may deduce, the previous DB_CLUDGE will be used to keep this state */
    static public final QueryOption OFFSET = new QueryOption(Type.OFFSET, 0);

    /* Search option is specific as it can be added on each QuerySearch depending on the DB*/ 
    static public final QueryOption SEARCH = new QueryOption(Type.SEARCH);
    
    public QueryOption(QueryOption.Type option, State active, Object value){
        this.type = option;
        this.state = active;
        this.value = value;
    }

    public QueryOption(QueryOption.Type option, Object value){
        this.type = option;
        this.state = State.PASSIVE;
        this.value = value;
    }

    public QueryOption(QueryOption.Type option){
        this.type = option;
        this.state = State.PASSIVE;       
    }

    public QueryOption(QueryOption option){
        this.type = option.type;
        this.state = option.state;
        this.value = option.value;
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
    
    public QueryOption value(Object value){
    	this.value = value;
    	return this;
    }
    
    public Object value(){
    	return this.value;
    }

	public QueryOption clone() {
		return new QueryOption(this);
	}
    
	public static QueryOption getInstance(Type type) {
		if(defaults.containsKey(type))
			return defaults.get(type).clone();
		else return new QueryOption(type);
	}
	
	public String toString() {
		return "{ type:"+this.type+" - state:"+this.state+" - value:"+this.value+"}";
	}
}
