package siena.core.options;

import siena.Json;
import siena.SienaException;
import siena.Util;
import siena.embed.EmbeddedMap;
import siena.embed.JsonSerializer;


@EmbeddedMap
public abstract class QueryOption {
    /* the state of an option */
    public enum State {
        ACTIVE, PASSIVE
    }

    /* an option has a type, a state and an optional value (pagesize for  PAGINATE for example) */
    public int type;
    protected State state = State.PASSIVE;
    
    public QueryOption(int option, State active, Object value){
        this.type = option;
        this.state = active;
    }

    public QueryOption(int option, Object value){
        this.type = option;
        this.state = State.PASSIVE;
    }

    public QueryOption(int option){
        this.type = option;
        this.state = State.PASSIVE;       
    }

    public QueryOption(QueryOption option){
        this.type = option.type;
        this.state = option.state;
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
    
	abstract public QueryOption clone();
		
	public String toString() {
		return "type:"+this.type+" - state:"+this.state;
	}
	
	public boolean equals(QueryOption opt){
		return this.type == opt.type && this.state == opt.state;
	}
	
	public QueryOptionJson dump() {
		QueryOptionJson jsonOpt = new QueryOptionJson();
		jsonOpt.type = this.getClass().getName();
		jsonOpt.value = JsonSerializer.serialize(this).toString(); 
		
		return jsonOpt;
	}

	public static QueryOption restore(String jsonStr) {	
		try {
			QueryOptionJson optJson = (QueryOptionJson)JsonSerializer.deserialize(QueryOptionJson.class, Json.loads(jsonStr));
			Class<?> clazz = Class.forName(optJson.type);
			
			QueryOption opt = (QueryOption)JsonSerializer.deserialize(clazz, Json.loads(optJson.value));
			return opt;
		}catch(Exception ex) {
			throw new SienaException("Unable to restore QueryOption", ex);
		}
	}

	@EmbeddedMap
	public static class QueryOptionJson {
		public String type;
		public String value;
	}
}
