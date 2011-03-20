package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import siena.ClassInfo;
import siena.Query;
import siena.QueryJoin;
import siena.SienaException;
import siena.Util;
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
			T obj = Util.createModelInstance(clazz);
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
			T obj = Util.createModelInstance(clazz);
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
			if(ClassInfo.isModel(type)) {
				JdbcClassInfo fieldClassInfo = JdbcClassInfo.getClassInfo(type);
				
				if(joinFields==null || joinFields.size()==0 || !joinFields.contains(field)){
					String[] fks = ClassInfo.getColumnNames(field, tableName);
					Object rel = Util.createModelInstance(type);
					boolean none = false;
					int i = 0;
					checkForeignKeyMapping(fieldClassInfo.keys, fks, obj.getClass(), field);
					for(Field f : fieldClassInfo.keys) {
						Object o = rs.getObject(fks[i++]);
						if(o == null) {
							none = true;
							break;
						}
						Util.setFromObject(rel, f, o);
					}
					if(!none){
						Util.setField(obj, field, rel);
						//field.set(obj, rel);
					}
				}
				else {
					Object rel = mapObject(type, rs, fieldClassInfo.tableName, null);
					Util.setField(obj, field, rel);
					//field.set(obj, rel);
				}
			} else {
				Object val = rs.getObject(ClassInfo.getColumnNames(field, tableName)[0]);
				Util.setFromObject(obj, field, val);
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
}
