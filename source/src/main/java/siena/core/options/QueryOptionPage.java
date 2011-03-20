package siena.core.options;


public class QueryOptionPage extends QueryOption{
	public static final int ID 	= 0x01;
	
	public int pageSize = 0;
	public PageType pageType = PageType.LIMIT;
	
	public enum PageType {
		LIMIT,
		PAGINATING
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
	
	@Override
	public QueryOption clone() {
		return new QueryOptionPage(this);
	}

	public String toString() {
		return "type:PAGE - state:"+this.state+" - pageType:"+pageType+" - pageSize:"+this.pageSize;
	}
}
