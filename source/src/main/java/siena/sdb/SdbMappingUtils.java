package siena.sdb;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import siena.ClassInfo;
import siena.Id;
import siena.Json;
import siena.Query;
import siena.QueryData;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryFilterSimple;
import siena.QueryOrder;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.Util;
import siena.core.Base64;
import siena.core.DecimalPrecision;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;
import siena.embed.Embedded;
import siena.embed.JavaSerializer;
import siena.embed.JsonSerializer;
import siena.gae.GaeNativeSerializer;
import siena.gae.QueryOptionGaeContext;
import siena.gae.Unindexed;
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
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

public class SdbMappingUtils {
	private static long ioffset = Math.abs(0L+Integer.MIN_VALUE);

	public static String getDomainName(Class<?> clazz, String prefix) {
		ClassInfo ci = ClassInfo.getClassInfo(clazz);
		if(ClassInfo.isAutoIncrement(ci.getIdField())){
			throw new SienaRestrictedApiException("DB", "getItemName", "@Id AUTO_INCREMENT not supported by SDB");
		}
		String domain = prefix + ci.tableName;
		return domain;
	}
	
	public static void getDomainName(StringBuffer str, Class<?> clazz, String prefix) {
		str.append(prefix + ClassInfo.getClassInfo(clazz).tableName);
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
				keyVal = toString(idField, idVal);				
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
					keyVal = toString(idField, idVal);
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
	
	public static String getItemNameFromKey(Class<?> clazz, Object key){
		Field idField = ClassInfo.getIdField(clazz);
		Id id = idField.getAnnotation(Id.class);

		String keyVal = null;
		if(id != null){
			switch(id.value()) {
			case NONE:
			{
				keyVal = toString(idField, key);				
				break;
			}
			case AUTO_INCREMENT:
				// manages String ID as not long!!!
				throw new SienaRestrictedApiException("DB", "getItemName", "@Id AUTO_INCREMENT not supported by SDB");
			case UUID:
			{
				keyVal = toString(idField, key);				
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
				}else {
					if (ClassInfo.isEmbeddedNative(field)){
						SdbNativeSerializer.embed(req, ClassInfo.getSingleColumnName(field), value);						
					}
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
				}else {
					if (ClassInfo.isEmbeddedNative(field)){
						SdbNativeSerializer.embed(item, ClassInfo.getSingleColumnName(field), value);						
					}
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
	
	public static <T> DeletableItem createDeletableItemFromKey(Class<T> clazz, Object key) {	
		return new DeletableItem().withName(getItemNameFromKey(clazz, key));
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
	
	public static String toString(Object val){
		Class<?> type = val.getClass();
		if(type == Integer.class || type == int.class) {
			return toString((Integer)val);
		}
		if(ClassInfo.isModel(type)) {
			try {
				return objectFieldToString(val, ClassInfo.getIdField(type)); 
			} catch (Exception e) {
				throw new SienaException(e);
			}
		} else {
			if (type == Json.class) {
				return val.toString();
			} else if (type == byte[].class) {
				return Base64.encodeBytes((byte[]) val);
			}
			
			else if (type == BigDecimal.class){
				return ((BigDecimal)val).toPlainString();
			}
			// enum is after embedded because an enum can be embedded
			// don't know if anyone will use it but it will work :)
			else if (Enum.class.isAssignableFrom(type)) {
				return val.toString();
			} 
		}
		return val.toString();
	}
	
	public static String toString(Field field, Object val) {
		if(val == null) return null;
		Class<?> type = field.getType();
		if(type == Integer.class || type == int.class) {
			return intToString((Integer)val);
		}
		if(ClassInfo.isModel(type) && !ClassInfo.isEmbedded(field)) {
			try {
				return objectFieldToString(val, ClassInfo.getIdField(type)); 
			} catch (Exception e) {
				throw new SienaException(e);
			}
		} else {
			if (type == Json.class) {
				return val.toString();
			} else if (type == byte[].class) {
				return Base64.encodeBytes((byte[]) val);
			}
			else if (ClassInfo.isEmbedded(field)) {
				Embedded embed = field.getAnnotation(Embedded.class);
				switch(embed.mode()){
				case SERIALIZE_JSON:
					return JsonSerializer.serialize(val).toString();
				case SERIALIZE_JAVA:
					// this embedding mode doesn't manage @EmbedIgnores
					try {
						return Base64.encodeBytes(JavaSerializer.serialize(val));												
					}
					catch(IOException ex) {
						throw new SienaException(ex);
					}
				case NATIVE:
					// returns null because here we need to manage all fields of the embedded entity
					return null;
				}
				
			}
			else if (type == BigDecimal.class){
				DecimalPrecision ann = field.getAnnotation(DecimalPrecision.class);
				if(ann == null) {
					return ((BigDecimal)val).toPlainString();
				}else {
					switch(ann.storageType()){
					case DOUBLE:
						return ((Double)((BigDecimal)val).doubleValue()).toString();
					case STRING:
					case NATIVE:
						return ((BigDecimal)val).toPlainString();
					}
				}
			}
			// enum is after embedded because an enum can be embedded
			// don't know if anyone will use it but it will work :)
			else if (Enum.class.isAssignableFrom(field.getType())) {
				return val.toString();
			} 
		}
		return Util.toString(field, val);
	}
	
	public static String intToString(int i) {
		return String.format("%010d", i+ioffset);
	}
	
	public static int intFromString(String s) {
		long l = Long.parseLong(s);
		return (int) (l-ioffset);
	}

	public static void setFromString(Object obj, Field field, String val) {
		if(val == null) return;
		Class<?> fieldClass = field.getType();
		if(fieldClass == Integer.class || fieldClass == int.class) {
			Util.setField(obj, field, intFromString(val));
			return;
		}
		if(ClassInfo.isModel(fieldClass) && !ClassInfo.isEmbedded(field)) {
			try {
				Object relObj = Util.createObjectInstance(fieldClass);
				Field relIdField = ClassInfo.getIdField(fieldClass);
				setFromString(relObj, relIdField, val);
				Util.setField(obj, field, relObj);
				return;
			} catch (Exception e) {
				throw new SienaException(e);
			}
		} else {
			if (fieldClass == byte[].class) {
				try {
					Util.setField(obj, field, Base64.decode(val));
					return;
				}catch(Exception ex){
					throw new SienaException(ex);
				}
			}
			else if (ClassInfo.isEmbeddedNative(field)) {
				return;
			}
			else if (fieldClass == BigDecimal.class){
				DecimalPrecision ann = field.getAnnotation(DecimalPrecision.class);
				if(ann == null) {
					Util.setField(obj, field, new BigDecimal((String)val));
					return;
				}else {
					switch(ann.storageType()){
					case DOUBLE:
						// TODO add bigdecimal double lexicographic storage
						Util.setField(obj, field, new BigDecimal(val));
						return;
					case STRING:
					case NATIVE:
						Util.setField(obj, field, new BigDecimal(val));
						return;
					}
				}
			}			
		}
		Util.setFromObject(obj, field, val);
	}
	
	public static void fillModelKeysOnly(String itemName, Class<?> clazz, Object obj) {
		Field idField = ClassInfo.getIdField(clazz);
		setFromString(obj, idField, itemName);
	}
	
	public static void fillModel(String itemName, List<Attribute> attrs, Class<?> clazz, Object obj) {
		fillModelKeysOnly(itemName, clazz, obj);
		
		Attribute theAttr;
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {			
			if(!ClassInfo.isEmbeddedNative(field)){
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
					setFromString(obj, field, theAttr.getValue());
				}
			}else {
				Object value = SdbNativeSerializer.unembed(
						field.getType(), ClassInfo.getSingleColumnName(field), attrs);
				Util.setField(obj, field, value);
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
	
	public static <T> List<T> mapSelectResultToListOrderedFromKeys(SelectResult res, Class<T> clazz, Iterable<?> keys) {
		List<T> l = new ArrayList<T>();
		List<Item> items = res.getItems();
		
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		boolean found;
		for(Object key: keys){
			found = false;
			for(Item item: items){
				if(item.getName().equals(getItemNameFromKey(clazz, key))){
					T obj = Util.createObjectInstance(clazz);
					fillModel(item, clazz, info, obj);
					l.add(obj);
					items.remove(item);
					found = true;
					break;
				}
			}
			if(!found){
				// if not found, puts NULL in the list
				l.add(null);
			}
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
	public static final String OR = " or ";
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
	public static final String LIKE = " like ";
	public static final String EQ = " = ";


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
			String itemName = toString(key);
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
		boolean first = true;
		
		if(!filters.isEmpty()) {
			q.append(WHERE);
			
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
							// TODO manages model collection
							// TO BE VERIFIED: SHOULD BE MANAGED by toString!!!
							if(object != null){
								s.append(","+SimpleDB.quote(toString(f, object)));
							}else{
								throw new SienaException("Can't use NULL in collection for IN operator");
							}
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
				}else if(QueryFilterSearch.class.isAssignableFrom(filter.getClass())){
					Class<T> clazz = query.getQueriedClass();
					QueryFilterSearch qf = (QueryFilterSearch)filter;
					//if(qf.fields.length>1)
					//	throw new SienaException("Search not possible for several fields in SDB: only one field");
					try {
						//Field field = Util.getField(clazz, qf.fields[0]);
						//if(field.isAnnotationPresent(Unindexed.class)){
						//	throw new SienaException("Cannot search the @Unindexed field "+field.getName());
						//}
						
						// cuts match into words
						String[] words = qf.match.split("\\s");
						
						// if several words, then only OR operator represented by IN GAE
						Pattern pNormal = Pattern.compile("[\\%]*(\\w+)[\\%]*");
						
						if(!first) {
							q.append(AND);
						}
						
						// forces true
						first = true;
						
						for(String f: qf.fields){
							Field field = Util.getField(clazz, f);
							if(!first) {
								q.append(AND);
							}
							first = false;
							
							q.append(" ( ");
							
							String column = null;
							if(ClassInfo.isId(field)) {
								column = "itemName()";
							} else {
								column = ClassInfo.getColumnNames(field)[0];
							}
							
							first = true;
							for(String word:words){
								if(!first) {
									q.append(OR);								
								}
								first = false;
								
								if(!pNormal.matcher(word).matches()){
									throw new SienaException("'"+word+"' doesn't match pattern [\\%]*(\\w+)[\\%]*");
								}
								if(word.contains("%")){
									q.append(column + LIKE + SimpleDB.quote(word));
								}else {
									q.append(column + EQ + SimpleDB.quote(word));
								}
							}
							q.append(" ) ");
						}
						
					}catch(Exception e){
						throw new SienaException(e);
					}
					break;
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
					}else {
						q.append(AND);
					}
					q.append(ITEM_NAME + IS_NOT_NULL);
				}
				q.append(ORDER_BY + ITEM_NAME);
			} else {
				String column = ClassInfo.getColumnNames(field)[0];
				if(!filteredFields.contains(field)){
					if(filters.isEmpty()) {
						q.append(WHERE);
					}else {
						q.append(AND);
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
	
	public static <T> void nextPage(QueryData<T> query) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		if(sdbCtx==null){
			sdbCtx = new QueryOptionSdbContext();
			query.options().put(sdbCtx.type, sdbCtx);
		}
		
		// if no more data after, doesn't try to go after
		if(sdbCtx.noMoreDataAfter){
			return;
		}
		
		// if no more data before, removes flag to be able and stay there
		if(sdbCtx.noMoreDataBefore){
			sdbCtx.noMoreDataBefore = false;
			return;
		}
		
		if(pag.isPaginating()){
			if(sdbCtx.hasToken()){
				if(sdbCtx.nextToken() == null) {
					// in this case, doesn't advance to the next page 
					// and stays at the offset of the beginning of the 
					// last page
					sdbCtx.noMoreDataAfter = true;
				}else{
					// follows the real offset
					sdbCtx.realOffset += pag.pageSize;
					
					// if currentokenoffset is less than next page realoffset
					// uses offset
					if(sdbCtx.currentTokenOffset() <= sdbCtx.realOffset){
						QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
						offset.activate();
						offset.offset = sdbCtx.realOffset - sdbCtx.currentTokenOffset();
					}
					// if currentokenoffset is greater than previous page realoffset
					// go to previous page again
					else {
						nextPage(query);
					}					
				}
			}else {
				// no token yet, so uses the offset to go to next page
				QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
				offset.activate();
				offset.offset += pag.pageSize;
				// follows the real offset
				sdbCtx.realOffset += pag.pageSize;
			}
		}else {
			// throws exception because it's impossible to reuse nextPage when paginating has been interrupted, the cases are too many
			throw new SienaException("Can't use nextPage after pagination has been interrupted...");
		}
	}

	public static <T> void previousPage(QueryData<T> query) {
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		QueryOptionSdbContext sdbCtx = (QueryOptionSdbContext)query.option(QueryOptionSdbContext.ID);
		if(sdbCtx==null){
			sdbCtx = new QueryOptionSdbContext();
			query.options().put(sdbCtx.type, sdbCtx);
		}
		
		// if no more data before, doesn't try to go before
		if(sdbCtx.noMoreDataBefore){
			return;
		}
		
		// if no more data after, removes flag to be able to go before
		if(sdbCtx.noMoreDataAfter){
			// here the realoffset is not at the end of current pages
			// but at the beginning of the last page
			// so need to fake that we are at the end of the last page
			sdbCtx.realOffset += pag.pageSize;
			
			sdbCtx.noMoreDataAfter = false;
		}
		
		if(pag.isPaginating()){
			if(sdbCtx.hasToken()) {
				// if tokenIdx is 0, it means at first page after beginning 
				if(sdbCtx.tokenIdx == 0){
					sdbCtx.previousToken();
					// follows the real offset
					sdbCtx.realOffset -= pag.pageSize;
					
					// if currentokenoffset is less than previous page realoffset
					// uses offset
					if(sdbCtx.currentTokenOffset() <= sdbCtx.realOffset){
						QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
						offset.activate();
						offset.offset = sdbCtx.realOffset - sdbCtx.currentTokenOffset();
					}
					// if currentokenoffset is greater than previous page realoffset
					// go to previous page again
					else {
						previousPage(query);
					}
				}else {
					if(sdbCtx.previousToken() == null) {
						sdbCtx.realOffset -= pag.pageSize;
						
						// if the realOffset is not null, it means we are not at the index 0 of the table
						// so now uses realOffset
						if(sdbCtx.realOffset >= 0){
							QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
							offset.activate();
							offset.offset = sdbCtx.realOffset;
						}else {
							sdbCtx.noMoreDataBefore = true;
						}
					}else {
						// follows the real offset
						sdbCtx.realOffset -= pag.pageSize;
						
						// if currentokenoffset is less than previous page realoffset
						// uses offset
						if(sdbCtx.currentTokenOffset() <= sdbCtx.realOffset){
							QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
							offset.activate();
							offset.offset = sdbCtx.realOffset - sdbCtx.currentTokenOffset();
						}
						// if currentokenoffset is greater than previous page realoffset
						// go to previous page again
						else {
							previousPage(query);
						}
					}
				}
				
			}else {
				QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
				// means there has been a nextPage performed first and the offset has been used
				// to simulate the nextPage as there was no token yet
				if(offset.offset != 0){
					offset.offset -= pag.pageSize;
					offset.activate();

					// follows the real offset
					sdbCtx.realOffset -= pag.pageSize;
				}else {
					// if the realOffset is not null, it means we are not at the index 0 of the table
					// so now uses realOffset
					if(sdbCtx.realOffset != 0){
						offset.activate();
						offset.offset = sdbCtx.realOffset;
					}
					sdbCtx.noMoreDataBefore = true;
				}
			}
		} else {
			// throws exception because it's impossible to reuse nextPage when paginating has been interrupted, the cases are too many
			throw new SienaException("Can't use nextPage after pagination has been interrupted...");
		}
	}


}
