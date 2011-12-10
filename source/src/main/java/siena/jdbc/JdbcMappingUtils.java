package siena.jdbc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import siena.ClassInfo;
import siena.Json;
import siena.Query;
import siena.QueryJoin;
import siena.SienaException;
import siena.Util;
import siena.core.DecimalPrecision;
import siena.core.Polymorphic;
import siena.embed.Embedded;
import siena.embed.JsonSerializer;
import siena.jdbc.JdbcPersistenceManager.JdbcClassInfo;

public class JdbcMappingUtils {
	public static <T> List<Field> getJoinFields(Query<T> query) {
		List<Field> joinFields = null;
		// adds all join fields brought by call to .join() functions
		if(query.getJoins().size()>0){
			joinFields = new ArrayList<Field>();
			for(QueryJoin join:query.getJoins())
				joinFields.add(join.field);
		}
		// then adds the remaining joins coming from @Join if not added yet 
		ClassInfo ci = ClassInfo.getClassInfo(query.getQueriedClass());
		if(ci.joinFields.size() > 0){
			if(joinFields == null) joinFields = new ArrayList<Field>();
			for(Field f: ci.joinFields){
				if(!joinFields.contains(f)) joinFields.add(f);
			}
		}
		return joinFields;
	}
	
	public static <T> List<Field> getJoinFields(Query<T> query, JdbcClassInfo info) {
		List<Field> joinFields = null;
		// adds all join fields brought by call to .join() functions
		if(query.getJoins()!=null && query.getJoins().size()>0){
			joinFields = new ArrayList<Field>();
			for(QueryJoin join:query.getJoins())
				joinFields.add(join.field);
		}
		// then adds the remaining joins coming from @Join if not added yet 
		if(info.joinFields!=null && info.joinFields.size() > 0){
			if(joinFields == null) joinFields = new ArrayList<Field>();
			for(Field f: info.joinFields){
				if(!joinFields.contains(f)) joinFields.add(f);
			}
		}
		return joinFields;
	}
	
	public static <T> T mapObject(Class<T> clazz, ResultSet rs, String tableName, List<Field >joinFields) {
		try {
			T obj = Util.createObjectInstance(clazz);
			mapObject(obj, rs, tableName, joinFields);
			return obj;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	public static void mapObject(Object obj, ResultSet rs, String tableName, List<Field >joinFields) {
		Class<?> clazz = obj.getClass();
		for (Field field : JdbcClassInfo.getClassInfo(clazz).allFields) {
			mapField(obj, field, rs, tableName, joinFields);
		}
	}

	public static <T> List<T> mapList(Class<T> clazz, ResultSet rs, String tableName, List<Field> joinFields, int pageSize) {
		try {
			List<T> objects = new ArrayList<T>();
			if(pageSize==0){
				while(rs.next()) {
					objects.add(mapObject(clazz, rs, tableName, joinFields));
				}
			}else {
				for(int i=0; i<pageSize && rs.next();i++){
					objects.add(mapObject(clazz, rs, tableName, joinFields));
				}
			}
			return objects;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}
	
	
	public static <T> T mapObjectKeys(Class<T> clazz, ResultSet rs, String tableName, List<Field> joinFields) {
		try {
			T obj = Util.createObjectInstance(clazz);
			mapObjectKeys(obj, rs, tableName, joinFields);
			return obj;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	public static void mapObjectKeys(Object obj, ResultSet rs, String tableName, List<Field> joinFields) {
		Class<?> clazz = obj.getClass();
		for (Field field : JdbcClassInfo.getClassInfo(clazz).keys) {
			mapField(obj, field, rs, tableName, joinFields);
		}
	}
	
	public static <T> List<T> mapListKeys(Class<T> clazz, ResultSet rs, String tableName, List<Field> joinFields, int pageSize) {
		try {
			List<T> objects = new ArrayList<T>();
			if(pageSize==0){
				while(rs.next()) {
				objects.add(mapObjectKeys(clazz, rs, tableName, joinFields));
				}
			}else {
				for(int i=0; i<pageSize && rs.next();i++){
					objects.add(mapObjectKeys(clazz, rs, tableName, joinFields));
				}
			}
			return objects;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	public static void mapField(Object obj, Field field, ResultSet rs, String tableName, List<Field> joinFields) {
		Class<?> type = field.getType();
		//field.setAccessible(true);
		try {
			if(ClassInfo.isModel(type) && !ClassInfo.isEmbedded(field)) {
				JdbcClassInfo fieldClassInfo = JdbcClassInfo.getClassInfo(type);
				
				if(joinFields==null || joinFields.size()==0 || !joinFields.contains(field)){
					String[] fks = ClassInfo.getColumnNames(field, tableName);
					Object rel = Util.createObjectInstance(type);
					boolean none = false;
					int i = 0;
					checkForeignKeyMapping(fieldClassInfo.keys, fks, obj.getClass(), field);
					for(Field f : fieldClassInfo.keys) {
						Object o = rs.getObject(JdbcClassInfo.aliasFromCol(fks[i++]));
						if(o == null) {
							none = true;
							break;
						}
						setFromObject(rel, f, o);
					}
					if(!none){
						Util.setField(obj, field, rel);
						//field.set(obj, rel);
					}
				}
				else {
					// this is a JOIN field
					// first verifies the field is not null
					Object val = rs.getObject(
							JdbcClassInfo.aliasFromCol(
									ClassInfo.getColumnNames(field, tableName)[0]));
					if(val == null){
						Util.setField(obj, field, null);
						return;
					}
					// uses join field alias
					// Object rel = mapObject(type, rs, fieldClassInfo.tableName, null);
					Object rel = mapObject(type, rs, fieldClassInfo.joinFieldAliases.get(field.getName()), null);
					Util.setField(obj, field, rel);
				}
			} else {
				Object val = rs.getObject(ClassInfo.getColumnNames(field, tableName)[0].replace('.', '_'));
				setFromObject(obj, field, val);
			}
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	public static void checkForeignKeyMapping(List<Field> keys, String[] columns, Class<?> clazz, Field field) {
		if (keys.size() != columns.length) {
			throw new SienaException("Bad mapping for field '"+field.getName()+"'. " +
					"Related class "+field.getType().getName()+" has "+keys.size()+" primary keys, " +
					"but '"+clazz.getName()+"' only has mappings for "+columns.length+" foreign keys");
		}
	}
	
	public static void setFromObject(Object object, Field f, Object value)
		throws IllegalArgumentException, IllegalAccessException {
		Util.setField(object, f, fromObject(f, value));
	}
	
	public static Object fromObject(Field field, Object value) {
		Class<?> type = field.getType();
		// in H2 database, mediumtext is mapped to CLOB
		if(Json.class.isAssignableFrom(type) && value != null && java.sql.Clob.class.isAssignableFrom(value.getClass())) {
			java.sql.Clob clob = (java.sql.Clob)value;
			try {
				return Json.load(new BufferedReader(clob.getCharacterStream()));
			} catch (SQLException e) {
				throw new SienaException(e);
			}
		} 

		if(field.getAnnotation(Embedded.class) != null && value != null && java.sql.Clob.class.isAssignableFrom(value.getClass())) {
			java.sql.Clob clob = (java.sql.Clob)value;
			try {
				Json data = Json.load(new BufferedReader(clob.getCharacterStream()));
				return JsonSerializer.deserialize(field, data);
			} catch (SQLException e) {
				throw new SienaException(e);
			}
		}

        // issue https://github.com/mandubian/siena/issues/5
        if (value != null && java.sql.Clob.class.isAssignableFrom(value.getClass())) {
            java.sql.Clob clob = (java.sql.Clob) value;
            try {
                // @see http://osdir.com/ml/h2-database/2011-06/msg00170.html
                return clob.getSubString(1, (int) clob.length());
            } catch (SQLException e) {
                throw new SienaException(e);
            }
        }

		
		if(field.isAnnotationPresent(Polymorphic.class)){
			try {
				if(java.sql.Blob.class.isAssignableFrom(value.getClass())){
					java.sql.Blob blob = (java.sql.Blob)value;
					ObjectInputStream in = 
						new ObjectInputStream(new ByteArrayInputStream(blob.getBytes(0, (int)blob.length())));
					return in.readObject();
				}else {
					ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream((byte[])value));
					return in.readObject();
				}
			} catch (IOException e) {
				throw new SienaException(e);
			} catch (ClassNotFoundException e) {
				throw new SienaException(e);
			} catch(SQLException e){
				throw new SienaException(e);
			}
		}
		
		if(byte[].class == type && value != null && java.sql.Blob.class.isAssignableFrom(value.getClass())){
			java.sql.Blob blob = (java.sql.Blob)value;
			try {
				// converts the blob into a byte[]...
				// TODO what to do with a very long blob????
				return blob.getBytes(0, (int)blob.length());
			} catch (SQLException e) {
				throw new SienaException(e);
			}
		}
		if(BigDecimal.class == type){
			DecimalPrecision ann = field.getAnnotation(DecimalPrecision.class);
			if(ann==null){
				return (BigDecimal)value;
			}else {
				switch(ann.storageType()){
				case DOUBLE:
					return BigDecimal.valueOf((Double)value);
				case STRING:
					return new BigDecimal((String)value);
				case NATIVE:
					return (BigDecimal)value;
				}
			}
		}
		return Util.fromObject(field, value);
	}
}
