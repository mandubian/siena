package siena.sdb;

import siena.core.options.PmOption;


public class PmOptionSdbReadConsistency extends PmOption{
    public static final int ID 	= 0x3101;
	
    public boolean isConsistentRead = false;
    
	public PmOptionSdbReadConsistency() {
		super(ID);
	}

	public PmOptionSdbReadConsistency(boolean isConsistentRead) {
		super(ID);
		this.isConsistentRead = isConsistentRead;
	}
	
	public PmOptionSdbReadConsistency(PmOptionSdbReadConsistency option) {
		super(option);
		this.isConsistentRead = option.isConsistentRead;
	}
	
	public boolean equals(PmOptionSdbReadConsistency opt){
		return super.equals(opt) && this.isConsistentRead == opt.isConsistentRead;
	}

	public String toString() {
		return super.toString() + " - isConsistentRead:"+isConsistentRead;
	}
}
