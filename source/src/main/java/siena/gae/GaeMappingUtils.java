package siena.gae;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import siena.ClassInfo;
import siena.Id;
import siena.Json;
import siena.Query;
import siena.QueryAggregated;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.Util;
import siena.core.DecimalPrecision;
import siena.core.Relation;
import siena.core.RelationMode;
import siena.embed.Embedded;
import siena.embed.JavaSerializer;
import siena.embed.JsonSerializer;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

public class GaeMappingUtils {
	
	public static Entity createEntityInstance(Field idField, ClassInfo info, Object obj){
		Entity entity = null;
		Id id = idField.getAnnotation(Id.class);
		Class<?> type = idField.getType();

		if(id != null){
			switch(id.value()) {
			case NONE:
				Object idVal = null;
				idVal = Util.readField(obj, idField);
				if(idVal == null)
					throw new SienaException("Id Field " + idField.getName() + " value null");
				String keyVal = Util.toString(idField, idVal);				
				entity = new Entity(info.tableName, keyVal);
				break;
			case AUTO_INCREMENT:
				// manages String ID as not long!!!
				if(Long.TYPE == type || Long.class.isAssignableFrom(type)){
					entity = new Entity(info.tableName);
				}else {
					Object idStringVal = null;
					idStringVal = Util.readField(obj, idField);
					if(idStringVal == null)
						throw new SienaException("Id Field " + idField.getName() + " value null");
					String keyStringVal = Util.toString(idField, idStringVal);				
					entity = new Entity(info.tableName, keyStringVal);
				}
				break;
			case UUID:
				entity = new Entity(info.tableName, UUID.randomUUID().toString());
				break;
			default:
				throw new SienaRestrictedApiException("DB", "createEntityInstance", "Id Generator "+id.value()+ " not supported");
			}
		}
		else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		
		return entity;
	}
	
	public static Entity createEntityInstanceForUpdate(Field idField, ClassInfo info, Object obj){
		Key key = makeKey(idField, info, obj);
		Entity entity = new Entity(key);
		
		return entity;
	}
	
	public static Entity createEntityInstanceForUpdateFromParent(Field idField, ClassInfo info, Object obj, Key parentKey, ClassInfo parentInfo, Field parentField){
		Key key = makeKeyFromParent(idField, info, obj, parentKey, parentInfo, parentField);
		Entity entity = new Entity(key);
		
		return entity;
	}
	
	public static String getKindWithAncestorField(ClassInfo childInfo, ClassInfo parentInfo, Field field){
		return childInfo.tableName + ":" + parentInfo.tableName + ":" + ClassInfo.getSingleColumnName(field);
	}
	
	public static Entity createEntityInstanceFromParent(
			Field idField, ClassInfo info, Object obj, 
			Key parentKey, ClassInfo parentInfo, Field parentField){
		Entity entity = null;
		Id id = idField.getAnnotation(Id.class);
		Class<?> type = idField.getType();

		if(id != null){
			switch(id.value()) {
			case NONE:
				Object idVal = null;
				idVal = Util.readField(obj, idField);
				if(idVal == null)
					throw new SienaException("Id Field " + idField.getName() + " value null");
				String keyVal = Util.toString(idField, idVal);				
				entity = new Entity(getKindWithAncestorField(info, parentInfo, parentField), keyVal, parentKey);
				break;
			case AUTO_INCREMENT:
				// manages String ID as not long!!!
				if(Long.TYPE == type || Long.class.isAssignableFrom(type)){
					entity = new Entity(getKindWithAncestorField(info, parentInfo, parentField), parentKey);
				}else {
					Object idStringVal = null;
					idStringVal = Util.readField(obj, idField);
					if(idStringVal == null)
						throw new SienaException("Id Field " + idField.getName() + " value null");
					String keyStringVal = Util.toString(idField, idStringVal);				
					entity = new Entity(getKindWithAncestorField(info, parentInfo, parentField), keyStringVal, parentKey);
				}
				break;
			case UUID:
				entity = new Entity(getKindWithAncestorField(info, parentInfo, parentField), UUID.randomUUID().toString(), parentKey);
				break;
			default:
				throw new SienaRestrictedApiException("DB", "createEntityInstance", "Id Generator "+id.value()+ " not supported");
			}
		}
		else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		
		return entity;
	}
	
	public static void setIdFromKey(Field idField, Object obj, Key key) {
		Id id = idField.getAnnotation(Id.class);
		Class<?> type = idField.getType();
		if(id != null){
			switch(id.value()) {
			case NONE:
				//idField.setAccessible(true);
				Object val = null;
				if (Long.TYPE==type || Long.class.isAssignableFrom(type)){
					val = Long.parseLong((String) key.getName());
				}
				else if (String.class.isAssignableFrom(type)){
					val = key.getName();
				}
				else{
					throw new SienaRestrictedApiException("DB", "setKey", "Id Type "+idField.getType()+ " not supported");
				}
					
				Util.setField(obj, idField, val);
				break;
			case AUTO_INCREMENT:
				// Long value means key.getId()
				if (Long.TYPE==type || Long.class.isAssignableFrom(idField.getType())){
					Util.setField(obj, idField, key.getId());
				}else {
					idField.setAccessible(true);
					Object val2 = null;
					if (Long.TYPE==type || Long.class.isAssignableFrom(idField.getType())){
						val = Long.parseLong((String) key.getName());
					}
					else if (String.class.isAssignableFrom(idField.getType())){
						val = key.getName();
					}
					else{
						throw new SienaRestrictedApiException("DB", "setKey", "Id Type "+idField.getType()+ " not supported");
					}
						
					Util.setField(obj, idField, val2);
				}
				break;
			case UUID:
				Util.setField(obj, idField, key.getName());
				break;
			default:
				throw new SienaException("Id Generator "+id.value()+ " not supported");
			}
		}
		else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
	}
	
	protected static Key getKey(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		try {
			Field idField = info.getIdField();
			Object value = Util.readField(obj, idField);
			// TODO verify that returning NULL is not a bad thing
			if(value == null) return null;
			
			Class<?> type = idField.getType();
			
			if(idField.isAnnotationPresent(Id.class)){
				Id id = idField.getAnnotation(Id.class);
				switch(id.value()) {
				case NONE:
					// long or string goes toString
					return KeyFactory.createKey(
						ClassInfo.getClassInfo(clazz).tableName,
						value.toString());
				case AUTO_INCREMENT:
					// as a string with auto_increment can't exist, it is not cast into long
					if (Long.TYPE == type || Long.class.isAssignableFrom(type)){
						return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							(Long)value);
					}
					return KeyFactory.createKey(
						ClassInfo.getClassInfo(clazz).tableName,
						value.toString());
					
				case UUID:
					return KeyFactory.createKey(
						ClassInfo.getClassInfo(clazz).tableName,
						value.toString());
				default:
					throw new SienaException("Id Generator "+id.value()+ " not supported");
				}
			}
			else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	protected static Key getKeyFromParent(Object obj, Key parentKey, ClassInfo parentInfo, Field parentField) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		try {
			Field idField = info.getIdField();
			Object value = Util.readField(obj, idField);
			// TODO verify that returning NULL is not a bad thing
			if(value == null) return null;
			
			Class<?> type = idField.getType();
			
			if(idField.isAnnotationPresent(Id.class)){
				Id id = idField.getAnnotation(Id.class);
				switch(id.value()) {
				case NONE:
					// long or string goes toString
					return KeyFactory.createKey(
							getKindWithAncestorField(info, parentInfo, parentField),
						value.toString());
				case AUTO_INCREMENT:
					// as a string with auto_increment can't exist, it is not cast into long
					if (Long.TYPE == type || Long.class.isAssignableFrom(type)){
						return KeyFactory.createKey(
								getKindWithAncestorField(info, parentInfo, parentField),
							(Long)value);
					}
					return KeyFactory.createKey(
							getKindWithAncestorField(info, parentInfo, parentField),
						value.toString());
					
				case UUID:
					return KeyFactory.createKey(
							getKindWithAncestorField(info, parentInfo, parentField),
						value.toString());
				default:
					throw new SienaException("Id Generator "+id.value()+ " not supported");
				}
			}
			else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	protected static Key makeKey(Class<?> clazz, Object value) {
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		try {
			Field idField = info.getIdField();
			
			if(idField.isAnnotationPresent(Id.class)){
				Id id = idField.getAnnotation(Id.class);
				switch(id.value()) {
				case NONE:
					// long or string goes toString
					return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							value.toString());
				case AUTO_INCREMENT:
					Class<?> type = idField.getType();
					// as a string with auto_increment can't exist, it is not cast into long
					if (Long.TYPE==type || Long.class.isAssignableFrom(type)){
						return KeyFactory.createKey(
							ClassInfo.getClassInfo(clazz).tableName,
							(Long)value);
					}
					return KeyFactory.createKey(
						ClassInfo.getClassInfo(clazz).tableName,
						value.toString());
				case UUID:
					return KeyFactory.createKey(
						ClassInfo.getClassInfo(clazz).tableName,
						value.toString());
				default:
					throw new SienaException("Id Generator "+id.value()+ " not supported");
				}
			}
			else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	protected static Key makeKey(Field field, ClassInfo info, Object object) {
		try {
			Field idField = info.getIdField();
			Object idVal = Util.readField(object, idField);
			if(idVal == null)
				throw new SienaException("Id Field " + idField.getName() + " value null");
			
			if(idField.isAnnotationPresent(Id.class)){
				Id id = idField.getAnnotation(Id.class);
				switch(id.value()) {
				case NONE:
					// long or string goes toString
					return KeyFactory.createKey(
							info.tableName,
							idVal.toString());
				case AUTO_INCREMENT:
					Class<?> type = idField.getType();
					// as a string with auto_increment can't exist, it is not cast into long
					if (Long.TYPE==type || Long.class.isAssignableFrom(type)){
						return KeyFactory.createKey(
							info.tableName,
							(Long)idVal);
					}
					return KeyFactory.createKey(
							info.tableName,
							idVal.toString());
				case UUID:
					return KeyFactory.createKey(
							info.tableName,
							idVal.toString());
				default:
					throw new SienaException("Id Generator "+id.value()+ " not supported");
				}
			}
			else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	protected static Key makeKeyFromParent(Field field, ClassInfo info, Object object, Key parentKey, ClassInfo parentInfo, Field parentField) {
		try {
			Field idField = info.getIdField();
			Object idVal = Util.readField(object, idField);
			if(idVal == null)
				throw new SienaException("Id Field " + idField.getName() + " value null");
			
			if(idField.isAnnotationPresent(Id.class)){
				Id id = idField.getAnnotation(Id.class);
				switch(id.value()) {
				case NONE:
					// long or string goes toString
					return KeyFactory.createKey(
							parentKey,
							getKindWithAncestorField(info, parentInfo, parentField),
							idVal.toString());
				case AUTO_INCREMENT:
					Class<?> type = idField.getType();
					// as a string with auto_increment can't exist, it is not cast into long
					if (Long.TYPE==type || Long.class.isAssignableFrom(type)){
						return KeyFactory.createKey(
							parentKey,
							getKindWithAncestorField(info, parentInfo, parentField),
							(Long)idVal);
					}
					return KeyFactory.createKey(
							parentKey,
							getKindWithAncestorField(info, parentInfo, parentField),
							idVal.toString());
				case UUID:
					return KeyFactory.createKey(
							parentKey,
							getKindWithAncestorField(info, parentInfo, parentField),
							idVal.toString());
				default:
					throw new SienaException("Id Generator "+id.value()+ " not supported");
				}
			}
			else throw new SienaException("Field " + idField.getName() + " is not an @Id field");
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	public static void fillEntity(Object obj, Entity entity) {
		Class<?> clazz = obj.getClass();

		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			String property = ClassInfo.getColumnNames(field)[0];
			Object value = Util.readField(obj, field);
			Class<?> fieldClass = field.getType();
			if (ClassInfo.isModel(fieldClass) 
					&& !ClassInfo.isEmbedded(field)
					/*&& !ClassInfo.isAggregated(field)
					&& !ClassInfo.isOwned(field)*/) {
				if (value == null) {
					entity.setProperty(property, null);
				} else {
					Key key = getKey(value);
					entity.setProperty(property, key);
				}
			} else {
				if (value != null) {
					if (fieldClass == Json.class) {
						value = value.toString();
					} else if (value instanceof String) {
						String s = (String) value;
						if (s.length() > 500)
							value = new Text(s);
					} else if (value instanceof byte[]) {
						byte[] arr = (byte[]) value;
						// GAE Blob doesn't accept more than 1MB
						if (arr.length < 1000000)
							value = new Blob(arr);
						else
							value = new Blob(Arrays.copyOf(arr, 1000000));
					}
					else if (ClassInfo.isEmbedded(field)) {
						Embedded embed = field.getAnnotation(Embedded.class);
						switch(embed.mode()){
						case SERIALIZE_JSON:
							value = JsonSerializer.serialize(value).toString();
							String s = (String) value;
							if (s.length() > 500)
								value = new Text(s);
							break;
						case SERIALIZE_JAVA:
							// this embedding mode doesn't manage @EmbedIgnores
							try {
								byte[] b = JavaSerializer.serialize(value);
								// if length is less than 1Mb, can store in a blob else???
								if(b.length <= 1000000){
									value = new Blob(b);
								}else{
									throw new SienaException("object can be java serialized because it's too large >1mb");
								}								
							}
							catch(IOException ex) {
								throw new SienaException(ex);
							}
							break;
						case NATIVE:
							GaeNativeSerializer.embed(entity, ClassInfo.getSingleColumnName(field), value);
							// has set several new properties in entity so go to next field
							continue;
						}
						
					}
					/*else if (ClassInfo.isAggregated(field)){
						// can't save it now as it requires its parent key to be mapped
						// so don't do anything for the time being
						continue;
					}
					else if (ClassInfo.isOwned(field)){
						// can't save it now as it requires its parent key to be mapped
						// so don't do anything for the time being
						continue;
					}*/
					else if (fieldClass == BigDecimal.class){
						DecimalPrecision ann = field.getAnnotation(DecimalPrecision.class);
						if(ann == null) {
							value = ((BigDecimal)value).toPlainString();
						}else {
							switch(ann.storageType()){
							case DOUBLE:
								value = ((BigDecimal)value).doubleValue();
								break;
							case STRING:
							case NATIVE:
								value = ((BigDecimal)value).toPlainString();
								break;
							}
						}
					}
					// enum is after embedded because an enum can be embedded
					// don't know if anyone will use it but it will work :)
					else if (Enum.class.isAssignableFrom(field.getType())) {
						value = value.toString();
					} 
				}
				Unindexed ui = field.getAnnotation(Unindexed.class);
				if (ui == null) {
					entity.setProperty(property, value);
				} else {
					entity.setUnindexedProperty(property, value);
				}
			}
		}
	}


	public static void fillModel(Object obj, Entity entity) {
		Class<?> clazz = obj.getClass();

		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			String property = ClassInfo.getColumnNames(field)[0];
			try {
				Class<?> fieldClass = field.getType();
				if (ClassInfo.isModel(fieldClass) && !ClassInfo.isEmbedded(field)) {
					/*if(!ClassInfo.isAggregated(field)){*/
						Key key = (Key) entity.getProperty(property);
						if (key != null) {
							Object value = Util.createObjectInstance(fieldClass);
							Field id = ClassInfo.getIdField(fieldClass);
							setIdFromKey(id, value, key);
							Util.setField(obj, field, value);
						}
					/*}*/
				} 
				/*else if(ClassInfo.isAggregated(field)){
					// does nothing for the time being
				}
				else if (ClassInfo.isOwned(field)){
					// does nothing for the time being
				}*/
				else if(ClassInfo.isEmbedded(field) && field.getAnnotation(Embedded.class).mode() == Embedded.Mode.NATIVE){
					Object value = GaeNativeSerializer.unembed(
							field.getType(), ClassInfo.getSingleColumnName(field), entity);
					Util.setField(obj, field, value);
				}
				else {
					setFromObject(obj, field, entity.getProperty(property));
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
	}

	public static void fillModelAndKey(Object obj, Entity entity) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		Field id = info.getIdField();
		Class<?> fieldClass = id.getType();
		Key key = entity.getKey();
		if (key != null) {
			setIdFromKey(id, obj, key);
		}

		for (Field field : info.updateFields) {
			String property = ClassInfo.getColumnNames(field)[0];
			try {
				fieldClass = field.getType();
				if (ClassInfo.isModel(fieldClass) 
						&& !ClassInfo.isEmbedded(field)) {
					/*if(!ClassInfo.isAggregated(field)){*/
						key = (Key) entity.getProperty(property);
						if (key != null) {
							Object value = Util.createObjectInstance(fieldClass);
							id = ClassInfo.getIdField(fieldClass);
							setIdFromKey(id, value, key);
							Util.setField(obj, field, value);
						}
					/*}*/
				} 
				else if(ClassInfo.isEmbedded(field) && field.getAnnotation(Embedded.class).mode() == Embedded.Mode.NATIVE){
					Object value = GaeNativeSerializer.unembed(
								field.getType(), ClassInfo.getSingleColumnName(field), entity);
					Util.setField(obj, field, value);
				}
				/*else if(ClassInfo.isAggregated(field)){
					// does nothing for the time being
				}
				else if (ClassInfo.isOwned(field)){
					// does nothing for the time being
				}*/
				else {
					setFromObject(obj, field, entity.getProperty(property));
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
	}
	
	public static void setFromObject(Object object, Field f, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		if(value != null){
			if(Text.class.isAssignableFrom(value.getClass()))
				value = ((Text) value).getValue();
			else if(Blob.class.isAssignableFrom(value.getClass())) {
				if(f.getType() == byte[].class) {
					value = ((Blob) value).getBytes();
				}
				else {
					Embedded embed = f.getAnnotation(Embedded.class);
					if(embed != null) {
						switch(embed.mode()){
						case SERIALIZE_JSON: 
							break;
						case SERIALIZE_JAVA:
							value = ((Blob) value).getBytes();
							break;
						case NATIVE:
							// shouldn't happen
							break;
						}
					}
				}
			}
			else if(f.getType() == BigDecimal.class){
				DecimalPrecision ann = f.getAnnotation(DecimalPrecision.class);
				if(ann == null) {
					value = new BigDecimal((String)value);
				}else {
					switch(ann.storageType()){
					case DOUBLE:
						value = BigDecimal.valueOf((Double)value);
						break;
					case STRING:
					case NATIVE:
						value = new BigDecimal((String)value);
						break;
					}
				}
			}
		}
		Util.setFromObject(object, f, value);
	}
	
	public static <T> T mapEntityKeysOnly(Entity entity, Class<T> clazz) {
		Field id = ClassInfo.getIdField(clazz);
		T obj;
		try {
			obj = Util.createObjectInstance(clazz);
			setIdFromKey(id, obj, entity.getKey());
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		}
	
		return obj;
	}
	
	public static <T> List<T> mapEntitiesKeysOnly(List<Entity> entities,
			Class<T> clazz) {
		Field id = ClassInfo.getIdField(clazz);
		List<T> list = new ArrayList<T>(entities.size());
		for (Entity entity : entities) {
			T obj;
			try {
				obj = Util.createObjectInstance(clazz);
				list.add(obj);
				setIdFromKey(id, obj, entity.getKey());
			} catch (SienaException e) {
				throw e;
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return list;
	}

	
	public static <T> List<T> mapEntitiesKeysOnly(Iterable<Entity> entities,
			Class<T> clazz) {
		Field id = ClassInfo.getIdField(clazz);
		List<T> list = new ArrayList<T>();
		for (Entity entity : entities) {
			T obj;
			try {
				obj = Util.createObjectInstance(clazz);
				list.add(obj);
				setIdFromKey(id, obj, entity.getKey());
			} catch (SienaException e) {
				throw e;
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return list;
	}
	
	public static <T> T mapEntity(Entity entity, Class<T> clazz) {
		Field id = ClassInfo.getIdField(clazz);
		T obj = null;
		// try to find a constructor
		try {	
			if(entity != null){
				obj = Util.createObjectInstance(clazz);
				fillModel(obj, entity);
				setIdFromKey(id, obj, entity.getKey());
			}
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		}
	
		return obj;
	}
	
	public static <T> List<T> mapEntities(List<Entity> entities,
			Class<T> clazz) {
		Field id = ClassInfo.getIdField(clazz);
		List<T> list = new ArrayList<T>(entities.size());
		for (Entity entity : entities) {
			T obj;
			try {
				obj = Util.createObjectInstance(clazz);
				fillModel(obj, entity);
				list.add(obj);
				setIdFromKey(id, obj, entity.getKey());
			} catch (SienaException e) {
				throw e;
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return list;
	}

	
	public static <T> List<T> mapEntities(Iterable<Entity> entities,
			Class<T> clazz) {
		Field id = ClassInfo.getIdField(clazz);
		List<T> list = new ArrayList<T>();
		for (Entity entity : entities) {
			T obj;
			try {
				obj = Util.createObjectInstance(clazz);
				fillModel(obj, entity);
				list.add(obj);
				setIdFromKey(id, obj, entity.getKey());
			} catch (SienaException e) {
				throw e;
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return list;
	}
	
	public static <T> T mapRelation(Query<T> query, T obj, ClassInfo info) {
		List<QueryAggregated> aggregs = query.getAggregatees();
		if(aggregs.isEmpty()){
			return obj;
		}
		else if(aggregs.size() == 1){
			QueryAggregated aggreg = aggregs.get(0);
			
			Relation rel = 
				new Relation(RelationMode.AGGREGATION, aggreg.aggregator, aggreg.field.getName());
			Util.setField(obj, info.aggregator, rel);
			
			return obj;
		}
		else {
			throw new SienaException("Only one aggregation per query allowed");
		}
	}

	public static <T> List<T> mapRelations(Query<T> query, List<T> objs, ClassInfo info) {
		List<QueryAggregated> aggregs = query.getAggregatees();
		if(aggregs.isEmpty()){
			return objs;
		}
		else if(aggregs.size() == 1){
			QueryAggregated aggreg = aggregs.get(0);
			
			Relation rel = 
				new Relation(RelationMode.AGGREGATION, aggreg.aggregator, aggreg.field.getName());
			for(T obj: objs){
				Util.setField(obj, info.aggregator, rel);
			}
			
			return objs;
		}
		else {
			throw new SienaException("Only one aggregation per query allowed");
		}
	}
}
