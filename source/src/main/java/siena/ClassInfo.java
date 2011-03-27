/*
 * Copyright 2009 Alberto Gimeno <gimenete at gmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package siena;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassInfo {
	
	protected static Map<Class<?>, ClassInfo> infoClasses = new ConcurrentHashMap<Class<?>, ClassInfo>();
	
	public Class<?> clazz;
	
	public String tableName;

	public List<Field> keys = new ArrayList<Field>();
	public List<Field> insertFields = new ArrayList<Field>();
	public List<Field> updateFields = new ArrayList<Field>();
	public List<Field> generatedKeys = new ArrayList<Field>();
	public List<Field> allFields = new ArrayList<Field>();
	public List<Field> joinFields = new ArrayList<Field>();

	protected ClassInfo(Class<?> clazz) {
		this.clazz = clazz;
		tableName = getTableName(clazz);

		Field[] fields = clazz.getDeclaredFields();	

		for (Field field : fields) {
			
			Class<?> type = field.getType();
			if(type == Class.class || type == Query.class ||
					(field.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT ||
					(field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
				continue;
			
			Id id = field.getAnnotation(Id.class);
			if(id != null) {
				// ONLY long ID can be auto_incremented
				if(id.value() == Generator.AUTO_INCREMENT 
						&& ( Long.TYPE == type || Long.class.isAssignableFrom(type))) {
					generatedKeys.add(field);
				} else {
					insertFields.add(field);
				}
				keys.add(field);
			} 
			else {
				updateFields.add(field);
				insertFields.add(field);
			}
			
			if(field.getAnnotation(Join.class) != null){
				if (!ClassInfo.isModel(field.getType())){
					throw new SienaException("Join not possible: Field "+field.getName()+" is not a relation field");
				}
				else joinFields.add(field);
			}
			allFields.add(field);
		}
	}

	private String getTableName(Class<?> clazz) {
		Table t = clazz.getAnnotation(Table.class);
		if(t == null) return clazz.getSimpleName();
		return t.value();
	}

	public static String[] getColumnNames(Field field) {
		Column c = field.getAnnotation(Column.class);
		if(c != null && c.value().length > 0) return c.value();
		
		// default mapping: field names
		if(isModel(field.getType())) {
			ClassInfo ci = getClassInfo(field.getType());
			List<String> keys = new ArrayList<String>();
			for (Field key : ci.keys) {
				keys.addAll(Arrays.asList(getColumnNames(key)));
			}
			return keys.toArray(new String[keys.size()]);
		}
		return new String[]{ field.getName() };
	}

	public static String[] getColumnNames(Field field, String tableName) {
		Column c = field.getAnnotation(Column.class);
		if(c != null && c.value().length > 0) {
			if(tableName!=null && !("".equals(tableName))){
				String[] cols = c.value();
				for(int i=0;i<cols.length;i++){
					cols[i]=tableName+"."+cols[i];
				}
				return cols;
			}
			else return c.value();
		}
		
		// default mapping: field names
		if(isModel(field.getType())) {
			ClassInfo ci = getClassInfo(field.getType());
			List<String> keys = new ArrayList<String>();
			for (Field key : ci.keys) {
				keys.addAll(Arrays.asList(getColumnNames(key, tableName)));
			}
			return keys.toArray(new String[keys.size()]);
		}
		if(tableName!=null && !("".equals(tableName)))
			return new String[]{ tableName+"."+field.getName() };
		else return new String[]{ field.getName() };
	}
	
	public static boolean isModel(Class<?> type) {
		// this way is much better in Java syntax
		if(Model.class.isAssignableFrom(type)) /*if(type.getSuperclass() == Model.class)*/
			return true;
		// TODO: this needs to be tested
		if(type.getName().startsWith("java.")) return false;
		
		/*if(type == Json.class)*/
		if(Json.class.isAssignableFrom(type)) return false;
		return !ClassInfo.getClassInfo(type).keys.isEmpty();
	}

	public static boolean isId(Field field) {
		return field.getAnnotation(Id.class) != null;
	}
	
	/**
	 * Useful for those PersistenceManagers that only support one @Id
	 * @param clazz
	 * @return
	 */
	public static Field getIdField(Class<?> clazz) {
		List<Field> keys = ClassInfo.getClassInfo(clazz).keys;
		if(keys.isEmpty())
			throw new SienaException("No valid @Id defined in class "+clazz.getName());
		if(keys.size() > 1)
			throw new SienaException("Multiple @Id defined in class "+clazz.getName());
		return keys.get(0);
	}

	/**
	 * Useful for those PersistenceManagers that only support one @Id
	 * @param clazz
	 * @return
	 */
	public Field getIdField() {
		if(keys.isEmpty())
			throw new SienaException("No valid @Id defined in class "+tableName);
		if(keys.size() > 1)
			throw new SienaException("Multiple @Id defined in class "+tableName);
		return keys.get(0);
	}
	
	public static ClassInfo getClassInfo(Class<?> clazz) {
		ClassInfo ci = infoClasses.get(clazz);
		if(ci == null) {
			ci = new ClassInfo(clazz);
			infoClasses.put(clazz, ci);
		}
		return ci;
	}


}
