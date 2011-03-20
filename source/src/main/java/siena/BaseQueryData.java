package siena;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import siena.core.options.QueryOption;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

/**
 * The base data container of Query<T>/QueryAsync<T> where T is the model being queried (not necessarily inheriting siena.Model)
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 * @param <T>
 */
public class BaseQueryData<T> implements QueryData<T> {
	protected Class<T> clazz;
	
	protected List<QueryFilter> filters;
	protected List<QueryOrder> orders;
	protected List<QueryFilterSearch> searches;
	protected List<QueryJoin> joins;
	
	protected Map<Integer, QueryOption> options = defaultOptions();
	
	public static Map<Integer, QueryOption> defaultOptions() {
		return new HashMap<Integer, QueryOption>() {
			private static final long serialVersionUID = -7438657296637379900L;
			{
				put(QueryOptionPage.ID, new QueryOptionPage(0));
				put(QueryOptionOffset.ID, new QueryOptionOffset(0));
				put(QueryOptionState.ID, new QueryOptionState());
				//the fetch type is activated by default and set to NORMAL
				put(QueryOptionFetchType.ID, (new QueryOptionFetchType()).activate());
			}};	
	}
	
	public BaseQueryData(Class<T> clazz) {
		this.clazz = clazz;
		
		filters = new ArrayList<QueryFilter>();
		orders = new ArrayList<QueryOrder>();
		searches = new ArrayList<QueryFilterSearch>();
		joins = new ArrayList<QueryJoin>();
	}
	
	public BaseQueryData(BaseQueryData<T> data) {
		this.clazz = data.clazz;		
		
		/* NO COPY TO KEEP DATA AND STATEFUL QUERIES
		this.filters = new ArrayList<QueryFilter>();
		this.orders = new ArrayList<QueryOrder>();
		this.searches = new ArrayList<QueryFilterSearch>();
		this.joins = new ArrayList<QueryJoin>();
		
		Collections.copy(this.filters, data.filters);
		Collections.copy(this.orders, data.orders);
		Collections.copy(this.searches, data.searches);
		Collections.copy(this.joins, data.joins);
		
		for(Integer key : data.options.keySet()){
			this.options.put(key, data.options.get(key).clone());
		}*/

		this.filters = data.filters;
		this.orders = data.orders;
		this.searches = data.searches;
		this.joins = data.joins;

		for(Integer key : data.options.keySet()){
			this.options.put(key, data.options.get(key));
		}
	}
	
	public Class<T> getQueriedClass(){
		return clazz;		
	}
	
	public List<QueryFilter> getFilters() {
		return filters;
	}

	public List<QueryOrder> getOrders() {
		return orders;
	}

	public List<QueryFilterSearch> getSearches() {
		return searches;
	}

	public List<QueryJoin> getJoins() {
		return joins;
	}

	
	public QueryOption option(int option) {
		return options.get(option);
	}
	
	public Map<Integer, QueryOption> options() {
		return options;
	}


/*
 * PROTECTED FUNCTIONS AVAILABLE FOR BASEQUERY	
 */
	protected void addFilter(String fieldName, Object value, String[] supportedOperators) {
		String op = "=";
		for (String s : supportedOperators) {
			if(fieldName.endsWith(s)) {
				op = s;
				fieldName = fieldName.substring(0, fieldName.length() - op.length());;
				break;
			}
		}
		fieldName = fieldName.trim();
		
		try {
			Field field = clazz.getDeclaredField(fieldName);
			filters.add(new QueryFilterSimple(field, op, value));
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	protected void addOrder(String fieldName) {
		boolean ascending = true;
		
		if(fieldName.startsWith("-")) {
			fieldName = fieldName.substring(1);
			ascending = false;
		}
		try {
			Field field = clazz.getDeclaredField(fieldName);
			orders.add(new QueryOrder(field, ascending));
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}
	
	protected void addSearch(String match, String... fields) {
		QueryFilterSearch q = new QueryFilterSearch(match, fields);
		filters.add(q);
		searches.add(q);
	}
	
	protected void addSearch(String match, QueryOption opt, String... fields) {
		QueryFilterSearch q = new QueryFilterSearch(match, opt, fields);
		filters.add(q);
		searches.add(q);
	}
	
	protected void addJoin(String fieldName, String... sortFields) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			joins.add(new QueryJoin(field, sortFields));
			// add immediately orders to keep order of orders 
			// sets joined field as parent field to manage order on the right joined table for ex
			for(String sortFieldName: sortFields){
				boolean ascending = true;
				
				if(sortFieldName.startsWith("-")) {
					sortFieldName = sortFieldName.substring(1);
					ascending = false;
				}
				try {
					Field sortField = field.getType().getField(sortFieldName);
					orders.add(new QueryOrder(sortField, ascending, field));
				} catch(NoSuchFieldException ex){
					throw new SienaException("Join not possible: join sort field "+sortFieldName+" is not a known field of "+fieldName, ex);
				}
			}
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}
	
	protected void optionPaginate(int pageSize) {
		// sets the pagination
		QueryOptionPage opt = (QueryOptionPage)(options.get(QueryOptionPage.ID));
		QueryOptionOffset offOpt = (QueryOptionOffset)options.get(QueryOptionOffset.ID);
		QueryOptionState stateOpt = (QueryOptionState)(options.get(QueryOptionState.ID)).activate();
		// can't change pagination after it has been initialized because it breaks all the cursor mechanism
		
		if(opt.isActive() && opt.isPaginating()){
			throw new SienaException("Can't change pagination after it has been initialized...");
		}
		
		opt.activate();
		opt.pageSize=pageSize;
		opt.pageType = QueryOptionPage.PageType.PAGINATING;
		
		if(stateOpt.isStateful()){
			offOpt.passivate();
		}else {
			offOpt.activate();
		}
	}
	
	protected void optionStateful() {
		QueryOptionState opt = (QueryOptionState)(options.get(QueryOptionState.ID)).activate();
		opt.lifeCycle = QueryOptionState.LifeCycle.STATEFUL;
	}
	
	protected void optionStateless() {
		QueryOptionState opt = (QueryOptionState)(options.get(QueryOptionState.ID)).activate();
		opt.lifeCycle = QueryOptionState.LifeCycle.STATELESS;
	}
	
	protected void addOptions(QueryOption... options) {
		for(QueryOption option: options){
			this.options.put(option.type, option);
		}
	}

	protected void reset() {
		options.clear();
		filters.clear();
		orders.clear();
		searches.clear();
		joins.clear();
		options = defaultOptions();
	}
	
	protected void resetOptions() {
		options = defaultOptions();
	}

}
