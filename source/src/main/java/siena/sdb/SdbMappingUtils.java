package siena.sdb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import siena.ClassInfo;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSimple;
import siena.QueryOrder;
import siena.SienaException;
import siena.Util;
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
	
	public static String getAttributeName(Field field) {
		return ClassInfo.getColumnNames(field)[0];
	}

	public static String getItemName(Class<?> clazz, Object obj){
		Field idField = ClassInfo.getIdField(clazz);
		String idVal = (String) Util.readField(obj, idField);
		if(idVal == null) { // TODO: only if auto-generated
			idVal = UUID.randomUUID().toString();
			Util.setField(obj, idField, idVal);
		}
		return idVal;
	}
	
	public static PutAttributesRequest createPutRequest(String domain, Class<?> clazz, ClassInfo info, Object obj) {
		PutAttributesRequest req = new PutAttributesRequest().withDomainName(domain);
		req.withItemName(getItemName(clazz, obj));
		
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			try {
				String value = toString(obj, field);
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
				String value = toString(obj, field);
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
	
	public static DeleteAttributesRequest createDeleteRequest(String domain, Class<?> clazz, Object obj) {
		DeleteAttributesRequest req = 
			new DeleteAttributesRequest().withDomainName(domain).withItemName(getItemName(clazz, obj));
		
		return req;
	}
	
	public static String toString(Object obj, Field field) {
		Object val = Util.readField(obj, field);
		if(val == null) return null;
		
		Class<?> type = field.getType();
		if(type == Integer.class || type == int.class) {
			return toString((Integer)val);
		}
		if(ClassInfo.isModel(type)) {
			try {
				return toString(val, ClassInfo.getIdField(type)); 
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

	
	public static void fillModel(String itemName, List<Attribute> attrs, Class<?> clazz, Object obj) {
		Field idField = ClassInfo.getIdField(clazz);
		try {
			Util.setFromString(obj, idField, itemName);
		} catch (Exception e) {
			throw new SienaException(e);
		}
		
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
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> query(Query<T> query, String prefix, String suffix, String nextToken) {
		Class<?> clazz = query.getQueriedClass();
		String domain = getDomainName(clazz, prefix);
//		String q = buildQuery(query, "select * from " + domain) + suffix;
		
		
//		SelectResponse response = ws.select(q, nextToken);
//		query.setNextOffset(response.nextToken);
//		List<Item> items = response.items;
//		List<T> result = new ArrayList<T>(items.size());
		List<T> result = new ArrayList<T>();
//		for (Item item : items) {
//			try {
//				T object = (T) clazz.newInstance();
//				fillModel(item, object);
//				result.add(object);
//			} catch (Exception e) {
//				throw new SienaException(e);
//			}
//		}
		return result;
	}

	public static String quote(String s) {
		return "\""+s.replace("'", "''")+"\"";
	}
	
	public static final String WHERE = " WHERE ";
	public static final String AND = " AND ";
	public static final String IS_NULL = " IS NULL";
	public static final String IS_NOT_NULL = " IS NOT NULL";
	public static final String ITEM_NAME = "itemName()";
	public static final String SELECT = "select ";
	public static final String FROM = " from ";
	public static final String ORDER_BY = " order by ";
	public static final String DESC = " desc";
	public static final String IN_BEGIN = " in(";
	public static final String IN_END = ")";

	public static <T> SelectRequest buildBatchGetQuery(Iterable<T> objects, String prefix) {
		String domain = null;
		Class<?> clazz = null;
		StringBuilder q = new StringBuilder();
		
		boolean first = true;
		for(T obj: objects){
			if(clazz == null){
				clazz = obj.getClass();
				domain = getDomainName(clazz, prefix);
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

	public static <T> String buildQuery(Query<T> query, String prefix) {
		Class<?> clazz = query.getQueriedClass();
		String domain = getDomainName(clazz, prefix);
		
		StringBuilder q = new StringBuilder(SELECT + "*" + FROM + domain);
		
		List<QueryFilter> filters = query.getFilters();
		if(!filters.isEmpty()) {
			q.append(WHERE);
			
			boolean first = true;
			
			for (QueryFilter filter : filters) {
				if(QueryFilterSimple.class.isAssignableFrom(filter.getClass())){
					QueryFilterSimple qf = (QueryFilterSimple)filter;
					Field f = qf.field;
					Object value = qf.value;
					String op = qf.operator;
					
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
						if(!op.equals("=")) {
							throw new SienaException("Unsupported operator for relationship: "+op);
						}
						ClassInfo relInfo = ClassInfo.getClassInfo(f.getType());
						int i = 0;
						for (Field key : relInfo.keys) {
							if(value == null) {
								q.append(columns[i++] + IS_NULL);
							} else {
								Object keyVal = Util.readField(value, key);
								q.append(columns[i++] + op + SimpleDB.quote(toString(keyVal, f)));
							}
						}
					} else {
						String column = null;
						if(ClassInfo.isId(f)) {
							column = "itemName()";
						} else {
							column = ClassInfo.getColumnNames(f)[0];
						}
						
						if(value == null && op.equals("=")) {
							q.append(column + IS_NULL);
						} else if(value == null && op.equals("!=")) {
							q.append(column + IS_NOT_NULL);
						} else {
							q.append(column + op + SimpleDB.quote(toString(value, f)));
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
				q.append(ORDER_BY + ITEM_NAME);
			} else {
				q.append(ORDER_BY);
				q.append(ClassInfo.getColumnNames(field)[0]);
			}
			if(!last.ascending)
				q.append(DESC);
		}
		
		return q.toString();
	}
}
