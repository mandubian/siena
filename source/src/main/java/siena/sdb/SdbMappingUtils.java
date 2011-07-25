package siena.sdb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import siena.ClassInfo;
import siena.Id;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSimple;
import siena.QueryOrder;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.Util;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.sdb.ws.SimpleDB;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeletableItem;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

public class SdbMappingUtils {
	private static long ioffset = Math.abs(0L+Integer.MIN_VALUE);

	public static String getDomainName(Class<?> clazz, String prefix) {
		String domain = prefix+ClassInfo.getClassInfo(clazz).tableName;
		return domain;
	}
	
	public static void getDomainName(StringBuffer str, Class<?> clazz, String prefix) {
		str.append(prefix+ClassInfo.getClassInfo(clazz).tableName);
	}
	
	public static String getAttributeName(Field field) {
		return ClassInfo.getColumnNames(field)[0];
	}

	public static String getItemName(Class<?> clazz, Object obj){
		Field idField = ClassInfo.getIdField(clazz);
		Id id = idField.getAnnotation(Id.class);

		String keyVal = null;
		if(id != null){
			switch(id.value()) {
			case NONE:
			{
				Object idVal = Util.readField(obj, idField);
				if(idVal == null)
					throw new SienaException("Id Field " + idField.getName() + " value null");
				keyVal = Util.toString(idField, idVal);				
				break;
			}
			case AUTO_INCREMENT:
				// manages String ID as not long!!!
				throw new SienaRestrictedApiException("DB", "getItemName", "@Id AUTO_INCREMENT not supported by SDB");
			case UUID:
			{
				Object idVal = Util.readField(obj, idField);
				if(idVal == null){
					keyVal = UUID.randomUUID().toString();
				}else {
					keyVal = Util.toString(idField, idVal);
				}
				Util.setField(obj, idField, keyVal);
				break;
			}
			default:
				throw new SienaRestrictedApiException("DB", "createEntityInstance", "Id Generator "+id.value()+ " not supported");
			}
		}
		else throw new SienaException("Field " + idField.getName() + " is not an @Id field");

		return keyVal;
	}
	
	public static PutAttributesRequest createPutRequest(String domain, Class<?> clazz, ClassInfo info, Object obj) {
		PutAttributesRequest req = new PutAttributesRequest().withDomainName(domain);
		req.withItemName(getItemName(clazz, obj));
		
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			try {
				String value = objectFieldToString(obj, field);
				if(value != null){
					ReplaceableAttribute attr = new ReplaceableAttribute(getAttributeName(field), value, true);
					req.withAttributes(attr);
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return req;
	}
	
	public static ReplaceableItem createItem(Object obj) {
		Class<?> clazz = obj.getClass();
		
		ReplaceableItem item = new ReplaceableItem(getItemName(clazz, obj));
		
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			try {
				String value = objectFieldToString(obj, field);
				if(value != null){
					ReplaceableAttribute attr = new ReplaceableAttribute(getAttributeName(field), value, true);
					item.withAttributes(attr);
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}

		return item;
	}

	public static DeletableItem createDeletableItem(Object obj) {
		Class<?> clazz = obj.getClass();
		
		return new DeletableItem().withName(getItemName(clazz, obj));
	}
	
	public static GetAttributesRequest createGetRequest(String domain, Class<?> clazz, Object obj) {
		GetAttributesRequest req = 
			new GetAttributesRequest().withDomainName(domain).withItemName(getItemName(clazz, obj));
		
		return req;
	}
	
	public static GetAttributesRequest createGetRequestFromKey(String domain, Class<?> clazz, Object key) {
		GetAttributesRequest req = 
			new GetAttributesRequest().withDomainName(domain).withItemName(key.toString());
		
		return req;
	}
	
	public static DeleteAttributesRequest createDeleteRequest(String domain, Class<?> clazz, Object obj) {
		DeleteAttributesRequest req = 
			new DeleteAttributesRequest().withDomainName(domain).withItemName(getItemName(clazz, obj));
		
		return req;
	}
	
	public static String objectFieldToString(Object obj, Field field) {
		Object val = Util.readField(obj, field);
		if(val == null) return null;
		
		return toString(field, val);
	}
	
	public static String toString(Field field, Object val) {
		Class<?> type = field.getType();
		if(type == Integer.class || type == int.class) {
			return toString((Integer)val);
		}
		if(ClassInfo.isModel(type)) {
			try {
				return objectFieldToString(val, ClassInfo.getIdField(type)); 
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return Util.toString(field, val);
	}
	
	public static String toString(int i) {
		return String.format("%010d", i+ioffset);
	}
	
	public static int fromString(String s) {
		long l = Long.parseLong(s);
		return (int) (l-ioffset);
	}

	public static void fillModelKeysOnly(String itemName, Class<?> clazz, Object obj) {
		Field idField = ClassInfo.getIdField(clazz);
		try {
			Util.setFromString(obj, idField, itemName);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	public static void fillModel(String itemName, List<Attribute> attrs, Class<?> clazz, Object obj) {
		fillModelKeysOnly(itemName, clazz, obj);
		
		Attribute theAttr;
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			theAttr = null;
			String attrName = getAttributeName(field);
			// searches attribute and if found, removes it from the list to reduce number of attributes
			for(Attribute attr: attrs){
				if(attrName.equals(attr.getName())){
					theAttr = attr;
					attrs.remove(attr);
					break;
				}
			}
			if(theAttr != null){
				String val = theAttr.getValue();
				
				if(field.getType() == Integer.class || field.getType() == int.class) {
					Util.setField(obj, field, fromString(val));
				} else {
					Class<?> type = field.getType();
					if(ClassInfo.isModel(type)) {
						Object rel = Util.createObjectInstance(type);
						Field relIdField = ClassInfo.getIdField(type);
						Util.setField(rel, relIdField, val);
						
						Util.setField(obj, field, rel);
					} else {
						Util.setFromString(obj, field, val);
					}
				}
			}
		}	
	}
	
	public static void fillModel(String itemName, GetAttributesResult res, Class<?> clazz, Object obj) {
		fillModel(itemName, res.getAttributes(), clazz, obj);
	}
	
	public static void fillModel(Item item, Class<?> clazz, ClassInfo info, Object obj) {
		fillModel(item.getName(), item.getAttributes(), clazz, obj);
	}
	
	public static void fillModelKeysOnly(Item item, Class<?> clazz, ClassInfo info, Object obj) {
		fillModelKeysOnly(item.getName(), clazz, obj);
	}
	
	public static <T> int mapSelectResult(SelectResult res, Iterable<T> objects) {
		List<Item> items = res.getItems();
		
		Class<?> clazz = null;
		ClassInfo info = null;
		int nb = 0;
		for(T obj: objects){
			if(clazz == null){
				clazz = obj.getClass();
				info = ClassInfo.getClassInfo(clazz);				
			}
			String itemName = getItemName(clazz, obj);
			Item theItem = null;
			for(Item item:items){
				if(item.getName().equals(itemName)){
					theItem = item;
					items.remove(item);
					break;
				}
			}
			if(theItem != null){
				fillModel(theItem, clazz, info, obj);
				nb++;
			}
		}
		
		return nb;
	}
	
	public static <T> List<T> mapSelectResultToList(SelectResult res, Class<T> clazz) {
		List<T> l = new ArrayList<T>();
		List<Item> items = res.getItems();
		
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		for(Item item: items){
			T obj = Util.createObjectInstance(clazz);
			fillModel(item, clazz, info, obj);
			l.add(obj);
		}
		
		return l;
	}
	
	public static <T> List<T> mapSelectResultToList(SelectResult res, Class<T> clazz, int offset) {
		List<T> l = new ArrayList<T>();
		List<Item> items = res.getItems();
		
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		for(int i=offset; i<items.size(); i++){
			Item item = items.get(i);
			T obj = Util.createObjectInstance(clazz);
			fillModel(item, clazz, info, obj);
			l.add(obj);
		}
		
		return l;
	}
	
	
	public static <T> List<T> mapSelectResultToListKeysOnly(SelectResult res, Class<T> clazz) {
		List<T> l = new ArrayList<T>();
		List<Item> items = res.getItems();
		
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		for(Item item: items){
			T obj = Util.createObjectInstance(clazz);
			fillModelKeysOnly(item, clazz, info, obj);
			l.add(obj);
		}
		
		return l;
	}
	
	public static <T> List<T> mapSelectResultToListKeysOnly(SelectResult res, Class<T> clazz, int offset) {
		List<T> l = new ArrayList<T>();
		List<Item> items = res.getItems();
		
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		for(int i=offset; i<items.size(); i++){
			Item item = items.get(i);
			T obj = Util.createObjectInstance(clazz);
			fillModelKeysOnly(item, clazz, info, obj);
			l.add(obj);
		}
		
		return l;
	}
	
	public static int mapSelectResultToCount(SelectResult res) {
		Item item = res.getItems().get(0);
		if(item != null){
			Attribute attr = item.getAttributes().get(0);
			if("Count".equals(attr.getName())){
				return Integer.parseInt(attr.getValue());
			}
		}
		
		return -1;
	}

	public static String quote(String s) {
		return "\""+s.replace("'", "''")+"\"";
	}
	
	public static final String WHERE = " where ";
	public static final String AND = " and ";
	public static final String IS_NULL = " is null ";
	public static final String IS_NOT_NULL = " is not null ";
	public static final String ITEM_NAME = "itemName()";
	public static final String ALL_COLS = "*";
	public static final String SELECT = "select ";
	public static final String FROM = " from ";
	public static final String ORDER_BY = " order by ";
	public static final String DESC = " desc";
	public static final String IN_BEGIN = " in(";
	public static final String IN_END = ")";
	public static final String COUNT_BEGIN = " count(";
	public static final String COUNT_END = ")";
	public static final String LIMIT = " limit ";

	public static <T> SelectRequest buildBatchGetQuery(Iterable<T> objects, String prefix, StringBuffer domainBuf) {
		Class<?> clazz = null;
		StringBuilder q = new StringBuilder();
		String domain = null;
		boolean first = true;
		for(T obj: objects){
			if(clazz == null){
				clazz = obj.getClass();
				domain = getDomainName(clazz, prefix);
				domainBuf.append(domain);
				q.append(SELECT + "*" + FROM + domain + WHERE + ITEM_NAME + IN_BEGIN);
			}
			
			String itemName = getItemName(clazz, obj);
			if(!first){
				q.append(",");
			} else {
				first = false;
			}
			q.append(quote(itemName));
		}

		q.append(IN_END);
		
		return new SelectRequest(q.toString());		
	}

	public static <T> SelectRequest buildBatchGetQueryByKeys(Class<T> clazz, Iterable<?> keys, String prefix, StringBuffer domainBuf) {
		String domain = getDomainName(clazz, prefix);;
		domainBuf.append(domain);
		StringBuilder q = new StringBuilder();
		
		q.append(SELECT + "*" + FROM + domain + WHERE + ITEM_NAME + IN_BEGIN);
		boolean first = true;
		for(Object key: keys){			
			String itemName = key.toString();
			if(!first){
				q.append(",");
			} else {
				first = false;
			}
			q.append(quote(itemName));
		}

		q.append(IN_END);
		
		return new SelectRequest(q.toString());		
	}
	
	public static <T> SelectRequest buildCountQuery(Query<T> query, String prefix, StringBuffer domainBuf) {
		String domain = getDomainName(query.getQueriedClass(), prefix);;
		domainBuf.append(domain);
		StringBuilder q = new StringBuilder();
		
		q.append(SELECT + COUNT_BEGIN + "*" + COUNT_END + FROM + domain);
		
		return new SelectRequest(buildFilterOrder(query, q).toString());		
	}
	
	public static <T> SelectRequest buildQuery(Query<T> query, String prefix, StringBuffer domainBuf) {	
		Class<?> clazz = query.getQueriedClass();
		String domain = getDomainName(clazz, prefix);
		domainBuf.append(domain);
		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);

		StringBuilder q = new StringBuilder();
		
		switch(fetchType.fetchType){
		case KEYS_ONLY:
			q.append(SELECT + ITEM_NAME + FROM + domain);
			break;
		case NORMAL:
		default:
			q.append(SELECT + ALL_COLS + FROM + domain);
			break;
		}
		
		return new SelectRequest(buildFilterOrder(query, q).toString());		
	}
	
	public static <T> StringBuilder buildFilterOrder(Query<T> query, StringBuilder q){
		List<QueryFilter> filters = query.getFilters();
		Set<Field> filteredFields = new HashSet<Field>();
		if(!filters.isEmpty()) {
			q.append(WHERE);
			
			boolean first = true;
						
			for (QueryFilter filter : filters) {
				if(QueryFilterSimple.class.isAssignableFrom(filter.getClass())){
					QueryFilterSimple qf = (QueryFilterSimple)filter;
					Field f = qf.field;
					Object value = qf.value;
					String op = qf.operator;
					
					// for order verification in case the order is not on a filtered field
					filteredFields.add(f);
					
					if(!first) {
						q.append(AND);
					}
					first = false;
					
					String[] columns = ClassInfo.getColumnNames(f);
					if("IN".equals(op)) {
						if(!Collection.class.isAssignableFrom(value.getClass()))
							throw new SienaException("Collection needed when using IN operator in filter() query");
						StringBuilder s = new StringBuilder();
						Collection<?> col = (Collection<?>) value;
						for (Object object : col) {
							// todo manages model collection
							s.append(","+object);
						}
						
						String column = null;
						if(ClassInfo.isId(f)) {
							column = ITEM_NAME;
						} else {
							column = ClassInfo.getColumnNames(f)[0];
						}

						q.append(column+" in("+s.toString().substring(1)+")");
					} else if(ClassInfo.isModel(f.getType())) {
						// TODO could manage other ops here
						if(!op.equals("=")) {
							throw new SienaException("Unsupported operator for relationship: "+op);
						}
						ClassInfo relInfo = ClassInfo.getClassInfo(f.getType());
						int i = 0;
						for (Field key : relInfo.keys) {
							if(value == null) {
								q.append(columns[i++] + IS_NULL);
							} else {
								q.append(columns[i++] + op + SimpleDB.quote(objectFieldToString(value, key)));
							}
						}
					} else {
						String column = null;
						if(ClassInfo.isId(f)) {
							column = "itemName()";
							
							if(value == null && op.equals("=")) {
								throw new SienaException("SDB filter on @Id field with 'IS NULL' is not possible");
							}
						} else {
							column = ClassInfo.getColumnNames(f)[0];
						}
						
						if(value == null && op.equals("=")) {
							q.append(column + IS_NULL);
						} else if(value == null && op.equals("!=")) {
							q.append(column + IS_NOT_NULL);
						} else {
							q.append(column + op + SimpleDB.quote(toString(f, value)));
						}
					}
				}
			}
		}
		
		List<QueryOrder> orders = query.getOrders();
		if(!orders.isEmpty()) {
			QueryOrder last = orders.get(orders.size()-1);
			Field field = last.field;
			if(ClassInfo.isId(field)) {
				if(!filteredFields.contains(field)){
					if(filters.isEmpty()) {
						q.append(WHERE);
					}
					q.append(ITEM_NAME + IS_NOT_NULL);
				}
				q.append(ORDER_BY + ITEM_NAME);
			} else {
				String column = ClassInfo.getColumnNames(field)[0];
				if(!filteredFields.contains(field)){
					if(filters.isEmpty()) {
						q.append(WHERE);
					}
					q.append(column + IS_NOT_NULL);
				}
				q.append(ORDER_BY + column);
			}
			if(!last.ascending)
				q.append(DESC);
		}
		
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		QueryOptionOffset off = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		if(sdbCtx != null && sdbCtx.realPageSize != 0){
			if(off!=null && off.isActive()){
				// if offset is active, adds it to the page size to be sure to retrieve enough elements
				q.append(LIMIT + (sdbCtx.realPageSize + off.offset));
			}else {
				q.append(LIMIT + sdbCtx.realPageSize);
			}
		}
		
		return q;
	}

}
