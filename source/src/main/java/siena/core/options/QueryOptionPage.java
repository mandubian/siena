package siena.core.options;


public class QueryOptionPage extends QueryOption{
	public static final int ID 	= 0x01;
	
	public int pageSize = 0;
	public PageType pageType = PageType.TEMPORARY;
	
	public enum PageType {
		TEMPORARY,
		MANUAL,
		PAGINATING
	}
	
	public QueryOptionPage() {
		super(ID);
	}
	
	public QueryOptionPage(int pageSize) {
		super(ID);
		this.pageSize = pageSize;
	}

	public QueryOptionPage(int pageSize, PageType type) {
		super(ID);
		this.pageSize = pageSize;
		this.pageType = type;
	}

	public QueryOptionPage(State active, int pageSize) {
		super(ID, active);
		this.pageSize = pageSize;
	}

	public QueryOptionPage(QueryOptionPage option) {
		super(option);
		this.pageSize = option.pageSize;
		this.pageType = option.pageType;
	}
	
	public boolean isPaginating() {
		return pageType == PageType.PAGINATING;
	}
	
	public boolean isManual() {
		return pageType == PageType.MANUAL;
	}
		
	@Override
	public QueryOption clone() {
		return new QueryOptionPage(this);
	}

	public String toString() {
		return "type:PAGE - state:"+this.state+" - pageType:"+pageType+" - pageSize:"+this.pageSize;
	}
	
	public boolean equals(Object obj){
		
		return super.equals(obj) 
			&& this.pageSize == ((QueryOptionPage)obj).pageSize 
			&& this.pageType == ((QueryOptionPage)obj).pageType;
	}

}
