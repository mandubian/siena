package siena.core.options;

import siena.Json;
import siena.SienaException;
import siena.core.options.QueryOption.QueryOptionJson;
import siena.embed.EmbeddedMap;
import siena.embed.JsonDeserializeAs;
import siena.embed.JsonDumpable;
import siena.embed.JsonRestorable;
import siena.embed.JsonSerializer;

@JsonDeserializeAs(QueryOptionJson.class)
public abstract class QueryOption implements JsonDumpable {
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
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryOption opt = (QueryOption)obj;
		return this.type == opt.type && this.state.equals(opt.state);
	}
	
	/* (non-Javadoc)
	 * @see siena.embed.JsonDumpable#dump()
	 */
	public Json dump() {
		QueryOptionJson jsonOpt = new QueryOptionJson();
		jsonOpt.type = this.getClass().getName();
		try {
			jsonOpt.value = JsonSerializer.serializeMap(this);
		}catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
		
		return JsonSerializer.serialize(jsonOpt);
	}

	@EmbeddedMap
	public static class QueryOptionJson implements JsonRestorable<QueryOption>{
		public String type;
		public Json value;
		
		public QueryOption restore() {
			try {
				Class<?> clazz = Class.forName(this.type);
				
				QueryOption opt = (QueryOption)JsonSerializer.deserializeMap(clazz, this.value);
				return opt;
			}catch(Exception ex) {
				throw new SienaException("Unable to restore QueryOption", ex);
			}
		}
	}
}
