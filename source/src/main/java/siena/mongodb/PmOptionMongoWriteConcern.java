package siena.mongodb;

import com.mongodb.WriteConcern;

import siena.core.options.PmOption;


public class PmOptionMongoWriteConcern extends PmOption{
    public static final int ID 	= 0x4101;
	
    public WriteConcern concern = WriteConcern.NORMAL;
    
	public PmOptionMongoWriteConcern() {
		super(ID);
	}

	public PmOptionMongoWriteConcern(WriteConcern concern) {
		super(ID);
		this.concern = concern;
	}
	
	public PmOptionMongoWriteConcern(PmOptionMongoWriteConcern option) {
		super(option);
		this.concern = option.concern;
	}
	
	public boolean equals(PmOptionMongoWriteConcern opt){
		return super.equals(opt) && this.concern == opt.concern;
	}

	public String toString() {
		return super.toString() + " - writeConcern:"+concern;
	}
}
