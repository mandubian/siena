package siena.mongodb;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import siena.ClassInfo;
import siena.Id;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryFilterSimple;
import siena.QueryOrder;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.Util;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoMappingUtils {
	protected static final Map<String, String> supportedOperatorsMap = 
		new HashMap<String, String>() {
			private static final long serialVersionUID = -106865197781525512L;
			{ 
				put("<", "$lt"); 
				put(">", "$gt");
				put(">=", "$gte");
				put("<=", "$lte");
				put("!=", "$ne");
				put("=", ""); 
			}
		};
	
	public static BasicDBObject sienaToMongoForInsert(Class<?> clazz, ClassInfo info, Object obj) {
		BasicDBObject doc = new BasicDBObject();
		
		fillMongoId(doc, clazz, info, obj);
		fillMongo(doc, clazz, info, obj);

		return doc;
	}
	
	public static BasicDBObject sienaToMongoForUpdate(Class<?> clazz, ClassInfo info, Object obj) {
		BasicDBObject doc = new BasicDBObject();
		
		fillMongoIdIfNotNull(doc, clazz, info, obj);
		fillMongo(doc, clazz, info, obj);

		return doc;
	}
	
	public static BasicDBObject sienaToMongoForQuery(Class<?> clazz, ClassInfo info, Object obj) {
		BasicDBObject doc = new BasicDBObject();
		
		fillMongoIdIfNotNull(doc, clazz, info, obj);
		fillMongo(doc, clazz, info, obj);

		return doc;
	}
	
	public static BasicDBObject sienaToMongoNoId(Class<?> clazz, ClassInfo info, Object obj) {
		BasicDBObject doc = new BasicDBObject();
		
		fillMongo(doc, clazz, info, obj);

		return doc;
	}

	public static void fillMongoId(BasicDBObject doc, Class<?> clazz, ClassInfo info, Object obj) {
		Field idField = ClassInfo.getIdField(clazz);
		Id id = idField.getAnnotation(Id.class);

		if(id != null){
			switch(id.value()) {
			case NONE:
			{
				Object idVal = Util.readField(obj, idField);
				if(idVal == null)
					throw new SienaException("Id Field " + idField.getName() + " value null");
				// does not use id name but _id
				doc.put("_id", idVal);
				break;
			}
			case AUTO_INCREMENT:
				// manages String ID as not long!!!
				throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "@Id AUTO_INCREMENT not supported by Mongo");
			case UUID:
				throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "@Id UUID not supported by Mongo");
			case BSON:
				Object idVal = Util.readField(obj, idField);
				if(idVal != null){
					throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "@Id BSON must be null");
				}
				if(idField.getType() == ObjectId.class){
					doc.put("_id", idVal);
				}else {
					throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "@Id BSON must be of ObjectId type");
				}
				break;
			default:
				throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "Id Generator "+id.value()+ " not supported");
			}
		}
		else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
	}
	
	public static void fillMongoIdIfNotNull(BasicDBObject doc, Class<?> clazz, ClassInfo info, Object obj){
		Field idField = ClassInfo.getIdField(clazz);
		Id id = idField.getAnnotation(Id.class);

		Object idVal = Util.readField(obj, idField);
		if(idVal == null) return;
		
		if(id != null){
			switch(id.value()) {
			case NONE:
			{							
				if(idField.getType() == ObjectId.class){
					doc.put("_id", idVal);
				}else if(idField.getType() == String.class){
					/*try {
						ObjectId objid = new ObjectId((String)idVal);
						doc.put("_id", objid);
					}catch(IllegalArgumentException ex){
						throw new SienaException("Manual Id Field " + idField.getName() + " of type String not being an BSON ObjectId");
					}*/
					doc.put("_id", idVal);
				}
				break;
			}
			case AUTO_INCREMENT:
				// manages String ID as not long!!!
				throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "@Id AUTO_INCREMENT not supported by Mongo");
			case UUID:
				throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "@Id UUID not supported by Mongo");
			case BSON:
				if(idField.getType() == ObjectId.class){
					doc.put("_id", idVal);
				}else {
					throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "@Id BSON must be of ObjectId type");
				}
				break;
			default:
				throw new SienaRestrictedApiException("MONGODB", "fillMongoId", "Id Generator "+id.value()+ " not supported");
			}
		}
		else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
	}

	
	public static void fillMongo(BasicDBObject doc, Class<?> clazz, ClassInfo info, Object obj) {
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			Object val = Util.readField(obj, field);
			if(val != null){
				doc.put(ClassInfo.getSimplestColumnName(field), val);
			}
		}
	}

	public static Object mongoToSiena(DBObject doc, Class<?> clazz, ClassInfo info, Object obj) {
		fillSienaId(doc, clazz, info, obj);
		fillSiena(doc, clazz, info, obj);
		return obj;
	}

	public static Object mongoToSienaNoId(DBObject doc, Class<?> clazz, ClassInfo info, Object obj) {
		fillSiena(doc, clazz, info, obj);
		return obj;
	}

	public static void fillSienaId(DBObject doc, Class<?> clazz, ClassInfo info, Object obj) {
		Field idField = ClassInfo.getIdField(clazz);
		Object val = doc.get("_id");
		Util.setFromObject(obj, idField, val);
	}
	
	public static void fillSiena(DBObject doc, Class<?> clazz, ClassInfo info, Object obj) {
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			Object val = doc.get(ClassInfo.getSimplestColumnName(field));
			Util.setFromObject(obj, field, val);
		}
	}
	
	private static String IN = "$in";
	private static String NOT = "$not";
	private static String TYPE = "$type";
	private static int TYPE_NULL = 10;
	
	public static <T> DBObject buildMongoFilter(Query<T> query){
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		List<QueryFilter> filters = query.getFilters();
		Set<Field> filteredFields = new HashSet<Field>();
		
		if(!filters.isEmpty()) {
			for (QueryFilter filter : filters) {
				if(QueryFilterSimple.class.isAssignableFrom(filter.getClass())){
					QueryFilterSimple qf = (QueryFilterSimple)filter;
					Field f = qf.field;
					Object value = qf.value;
					String op = qf.operator;
					
					// for order verification in case the order is not on a filtered field
					filteredFields.add(f);
					
					String[] columns = ClassInfo.getColumnNames(f);
					if("IN".equals(op)) {
						if(!Collection.class.isAssignableFrom(value.getClass()))
							throw new SienaException("Collection needed when using IN operator in filter() query");
						StringBuilder s = new StringBuilder();
						Collection<?> col = (Collection<?>) value;
						
						String column = null;
						if(ClassInfo.isId(f)) {
							column = "_id";
						} else {
							column = ClassInfo.getSimplestColumnName(f);
						}

						builder.add(
							column,
							new BasicDBObject(IN, col));
					} else if(ClassInfo.isModel(f.getType())) {
						// TODO could manage other ops here
						if(!op.equals("=")) {
							throw new SienaException("Unsupported operator for relationship: "+op);
						}
						ClassInfo relInfo = ClassInfo.getClassInfo(f.getType());
						int i = 0;
						for (Field key : relInfo.keys) {
							if(value == null) {
								builder.add(
									columns[i++], 
									new BasicDBObject(TYPE, TYPE_NULL));
							} else {
								builder.add(
									columns[i++], 
									value);
							}
						}
					} else {
						String column = null;
						if(ClassInfo.isId(f)) {
							column = "_id";
						} else {
							column = ClassInfo.getSimplestColumnName(f);
						}
						
						if(value == null && op.equals("=")) {
							builder.add(
								column, 
								new BasicDBObject(TYPE, TYPE_NULL)); 
						} else if(value == null && op.equals("!=")) {
							builder.add(
								column, 
								new BasicDBObject(
									NOT,
									new BasicDBObject(TYPE, TYPE_NULL))); 
						} else {
							builder.add(
								column, 
								new BasicDBObject(supportedOperatorsMap.get(op), value));
						}
					}
				}else if(QueryFilterSearch.class.isAssignableFrom(filter.getClass())){					
				}
			}
		}
		return builder.get();
	}
	
	public static <T> DBObject buildMongoSort(Query<T> query){
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		
		List<QueryOrder> orders = query.getOrders();
		if(!orders.isEmpty()) {
			for(QueryOrder order: orders){			
				Field field = order.field;
				
				String column = "";
				if(ClassInfo.isId(field)) {
					column = "_id";
				} else {
					column = ClassInfo.getSimplestColumnName(field);
				}
				if(!order.ascending)
					builder.add(column, -1);
				else builder.add(column, 1);
			}
		}
		
		return builder.get();
	}
	
	public static <T> DBObject buildMongoQueryFromKey(Object key){
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		
		builder.add("_id", key);
		
		return builder.get();
	}
	
	public static <T> DBObject buildMongoQueryFromKeys(Iterable<?> keys){
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		
		builder.add("_id", new BasicDBObject(IN, keys));
		
		return builder.get();
	}
}
