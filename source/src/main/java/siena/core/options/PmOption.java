package siena.core.options;


public abstract class PmOption {
    /* an option has a type, a state and an optional value (pagesize for  PAGINATE for example) */
    public int type;
    
    public PmOption(int option){
        this.type = option;
    }

    public PmOption(PmOption option){
        this.type = option.type;
    }
    
		
	public String toString() {
		return "type:"+this.type;
	}
	
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PmOption opt = (PmOption)obj;
		return this.type == opt.type;
	}
	
}
