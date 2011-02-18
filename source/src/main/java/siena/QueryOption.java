package siena;

public class QueryOption {
    /* an enum defining the types of options */
    public enum Type {
        PAGINATE,
        DB_CLUDGE,
        REUSABLE,
        OFFSET
    }

    /* the state of an option */
    public enum State {
        ACTIVE, PASSIVE
    }

    /* an option has a type, a state and an optional value (pagesize for  PAGINATE for example) */
    public Type type;
    private State state = State.PASSIVE;
    private Object value = null;
    
    /* DEFAULT OPTIONS THAT BE ADDED DIRECTLY TO QUERY */
    /* PAGINATE is in fact an option and the page size is the value */
    static public final QueryOption PAGINATE = new QueryOption(Type.PAGINATE, 0);

    /* this one is the horrible cludge I propose to store specific DB stuff between query calls... it's like a void* in C but anyway, this is the best way I found :) */
    static public final QueryOption DB_CLUDGE = new QueryOption(Type.DB_CLUDGE);
   
    /* makes a query reusable keeping some opened resources between calls... the statement for JDBC... as you may deduce, the previous DB_CLUDGE will be used to keep this state */
    static public final QueryOption REUSABLE = new QueryOption(Type.REUSABLE);

    /* makes a query reusable keeping some opened resources between calls... the statement for JDBC... as you may deduce, the previous DB_CLUDGE will be used to keep this state */
    static public final QueryOption OFFSET = new QueryOption(Type.OFFSET, 0);
    
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
}
