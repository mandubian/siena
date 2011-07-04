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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import siena.core.Aggregated;
import siena.core.InheritFilter;
import siena.core.Many;
import siena.core.One;
import siena.core.Owned;
import siena.core.RelationMode;
import siena.core.lifecycle.LifeCyclePhase;
import siena.core.lifecycle.LifeCycleUtils;
import siena.embed.Embedded;

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
	public List<Field> queryAndAllFields = new ArrayList<Field>();
	public List<Field> aggregatedFields = new ArrayList<Field>();
	public List<Field> ownedFields = new ArrayList<Field>();

	public Map<LifeCyclePhase, List<Method>> lifecycleMethods = new HashMap<LifeCyclePhase, List<Method>>();
	public Map<Field, Map<FieldMapKeys, Object>> queryOwnedFieldMap = new HashMap<Field, Map<FieldMapKeys, Object>>();
	public Map<Field, Map<FieldMapKeys, Object>> manyOwnedFieldMap = new HashMap<Field, Map<FieldMapKeys, Object>>();
	public Map<Field, Map<FieldMapKeys, Object>> singleOwnedFieldMap = new HashMap<Field, Map<FieldMapKeys, Object>>();

	public enum FieldMapKeys {
		CLASS,
		MODE,
		FIELD,
		FILTER
	}
	
	protected ClassInfo(Class<?> clazz) {
		this.clazz = clazz;
		tableName = getTableName(clazz);

		// Takes into account superclass fields for inheritance!!!!
		List<Class<?>> classH = new ArrayList<Class<?>>();
		Class<?> cl = clazz;
		Set<String> removedFields = new HashSet<String>();
        while (cl!=null) {
        	classH.add(0, cl);
        	// add exceptFields
        	InheritFilter iFilter = cl.getAnnotation(InheritFilter.class);
        	if(iFilter != null){
        		String[] efs = iFilter.removedFields();
	        	for(String ef:efs){
	        		removedFields.add(ef);
	        	}
        	}

        	cl = cl.getSuperclass();
        }
        
        for(Class<?> c: classH) {
			for (Field field : c.getDeclaredFields()) {
				if(removedFields.contains(field.getName())) continue;
				Class<?> type = field.getType();
				if((field.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT ||
						(field.getModifiers() & Modifier.STATIC) == Modifier.STATIC ||
						field.isSynthetic() || type == Class.class){
					continue;
				}
				
				if(type == Query.class){
					Filter filter = field.getAnnotation(Filter.class);
					Owned related = field.getAnnotation(Owned.class);
					if(filter == null && related == null ) {
						throw new SienaException("Found Query<T> field without @Filter or @Related annotation at "
								+c.getName()+"."+field.getName());
					}
		
					ParameterizedType pt = (ParameterizedType) field.getGenericType();
					cl = (Class<?>) pt.getActualTypeArguments()[0];
		
					if(filter != null){
						try {
							Map<FieldMapKeys, Object> fieldMap = new HashMap<FieldMapKeys, Object>();
							fieldMap.put(FieldMapKeys.CLASS, cl);
							fieldMap.put(FieldMapKeys.FILTER, filter.value());
							queryOwnedFieldMap.put(field, fieldMap);
							ownedFields.add(field);
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}
					else if(related != null){
						String as = related.mappedBy();
						// if related.as not specified, tries to find the first field with this type
						if("".equals(as) || as == null){
							ClassInfo fieldInfo = ClassInfo.getClassInfo(cl); 
							Field f = fieldInfo.getFirstFieldFromType(clazz);
							if(f == null){
								throw new SienaException("@Related without 'as' attribute and no field of type "
										+ clazz.getName() + "found in class "+type.getName());
							}
							
							as = ClassInfo.getSimplestColumnName(f);
						}
						try {
							Map<FieldMapKeys, Object> fieldMap = new HashMap<FieldMapKeys, Object>();
							fieldMap.put(FieldMapKeys.CLASS, cl);
							fieldMap.put(FieldMapKeys.FILTER, as);
							queryOwnedFieldMap.put(field, fieldMap);
							ownedFields.add(field);
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}
					
					queryAndAllFields.add(field);
					
					// query fields are not added to other kind of fields
					continue;
				}
				else if(type == Many.class){
					ParameterizedType pt = (ParameterizedType) field.getGenericType();
					cl = (Class<?>) pt.getActualTypeArguments()[0];
					
					Aggregated agg = field.getAnnotation(Aggregated.class);
					Filter filter = field.getAnnotation(Filter.class);
					Owned related = field.getAnnotation(Owned.class);
					if((agg!=null && filter!=null) || (agg!=null && related!=null)){
						throw new SienaException("Found Many<T> field "
								+ c.getName()+"."+field.getName() 
								+ "with @Filter+@Aggregated or @Filter+@Related: this is not authorized");
					}
					if(agg != null){
						try {
							Map<FieldMapKeys, Object> fieldMap = new HashMap<FieldMapKeys, Object>();
							fieldMap.put(FieldMapKeys.CLASS, cl);
							fieldMap.put(FieldMapKeys.MODE, RelationMode.AGGREGATION);
							manyOwnedFieldMap.put(field, fieldMap);
							aggregatedFields.add(field);
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}else if(filter != null){
						try {
							Field filterField = cl.getField(filter.value());
							if(filterField == null){
								throw new SienaException("@Filter error: Couldn't find field "
										+ filter.value() 
										+ "in class "+cl.getName());
							}
							Map<FieldMapKeys, Object> fieldMap = new HashMap<FieldMapKeys, Object>();
							fieldMap.put(FieldMapKeys.CLASS, cl);
							fieldMap.put(FieldMapKeys.MODE, RelationMode.RELATION);
							fieldMap.put(FieldMapKeys.FIELD, filterField);
							fieldMap.put(FieldMapKeys.FILTER, filter.value());
							manyOwnedFieldMap.put(field, fieldMap);
							ownedFields.add(field);
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}else if(related != null) {
						String as = related.mappedBy();
						// if related.as not specified, tries to find the first field with this type
						if("".equals(as) || as == null){
							ClassInfo fieldInfo = ClassInfo.getClassInfo(cl); 
							Field f = fieldInfo.getFirstFieldFromType(clazz);
							if(f == null){
								throw new SienaException("@Related without 'as' attribute and no field of type "
										+ clazz.getName() + "found in class "+type.getName());
							}
							
							as = ClassInfo.getSimplestColumnName(f);
						}
						try {
							Field asField = cl.getField(as);
							if(asField == null){
								throw new SienaException("@Filter error: Couldn't find field "
										+ as
										+ "in class "+cl.getName());
							}
							
							Map<FieldMapKeys, Object> fieldMap = new HashMap<FieldMapKeys, Object>();
							fieldMap.put(FieldMapKeys.CLASS, cl);
							fieldMap.put(FieldMapKeys.MODE, RelationMode.RELATION);
							fieldMap.put(FieldMapKeys.FIELD, asField);
							fieldMap.put(FieldMapKeys.FILTER, as);
							manyOwnedFieldMap.put(field, fieldMap);
							ownedFields.add(field);
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}	
					
					queryAndAllFields.add(field);
					
					// query fields are not added to other kind of fields
					continue;
				}
				else if(type == One.class){
					ParameterizedType pt = (ParameterizedType) field.getGenericType();
					cl = (Class<?>) pt.getActualTypeArguments()[0];
					
					Aggregated agg = field.getAnnotation(Aggregated.class);
					Filter filter = field.getAnnotation(Filter.class);
					Owned related = field.getAnnotation(Owned.class);
					if((agg!=null && filter!=null) || (agg!=null && related!=null)){
						throw new SienaException("Found One<T> field "
								+ c.getName()+"."+field.getName() 
								+ "with @Filter+@Aggregated or @Filter+@Related: this is not authorized");
					}
					if(agg != null){
						try {
							Map<FieldMapKeys, Object> fieldMap = new HashMap<FieldMapKeys, Object>();
							fieldMap.put(FieldMapKeys.CLASS, cl);
							fieldMap.put(FieldMapKeys.MODE, RelationMode.AGGREGATION);
							singleOwnedFieldMap.put(field, fieldMap);
							aggregatedFields.add(field);
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}else if(filter != null){
						try {
							Field filterField = cl.getField(filter.value());
							if(filterField == null){
								throw new SienaException("@Filter error: Couldn't find field "
										+ filter.value() 
										+ "in class "+cl.getName());
							}
							Map<FieldMapKeys, Object> fieldMap = new HashMap<FieldMapKeys, Object>();
							fieldMap.put(FieldMapKeys.CLASS, cl);
							fieldMap.put(FieldMapKeys.MODE, RelationMode.RELATION);
							fieldMap.put(FieldMapKeys.FIELD, filterField);
							fieldMap.put(FieldMapKeys.FILTER, filter.value());
							singleOwnedFieldMap.put(field, fieldMap);
							ownedFields.add(field);
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}else if(related != null) {
						String as = related.mappedBy();
						// if related.as not specified, tries to find the first field with this type
						if("".equals(as) || as == null){
							ClassInfo fieldInfo = ClassInfo.getClassInfo(cl); 
							Field f = fieldInfo.getFirstFieldFromType(clazz);
							if(f == null){
								throw new SienaException("@Related without 'as' attribute and no field of type "
										+ clazz.getName() + "found in class "+type.getName());
							}
							
							as = ClassInfo.getSimplestColumnName(f);
						}
						try {
							Field asField = cl.getField(as);
							if(asField == null){
								throw new SienaException("@Filter error: Couldn't find field "
										+ as
										+ "in class "+cl.getName());
							}
							
							Map<FieldMapKeys, Object> fieldMap = new HashMap<FieldMapKeys, Object>();
							fieldMap.put(FieldMapKeys.CLASS, cl);
							fieldMap.put(FieldMapKeys.MODE, RelationMode.RELATION);
							fieldMap.put(FieldMapKeys.FIELD, asField);
							fieldMap.put(FieldMapKeys.FILTER, as);
							singleOwnedFieldMap.put(field, fieldMap);
							ownedFields.add(field);
						} catch (Exception e) {
							throw new SienaException(e);
						}
					}
					
					queryAndAllFields.add(field);
					continue;
				}
				
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
					allFields.add(field);
					queryAndAllFields.add(field);	
					continue;
				} 
				
				if(isJoined(field)){
					if (!ClassInfo.isModel(field.getType())){
						throw new SienaException("Join not possible: Field "+field.getName()+" is not a relation field");
					}
					
					joinFields.add(field);					
				}
				
				updateFields.add(field);
				insertFields.add(field);
				allFields.add(field);
				queryAndAllFields.add(field);				
			}
			
			for(Method m : c.getDeclaredMethods()){
				List<LifeCyclePhase> lcps = LifeCycleUtils.getMethodLifeCycles(m);
				for(LifeCyclePhase lcp: lcps){
					List<Method> methods = lifecycleMethods.get(lcp);
					if(methods == null){
						methods = new ArrayList<Method>();
						lifecycleMethods.put(lcp, methods);
					}
					methods.add(m);
				}
			}
        }
	}

	private String getTableName(Class<?> clazz) {
		Table t = clazz.getAnnotation(Table.class);
		if(t == null) return clazz.getSimpleName();
		return t.value();
	}

	public List<String> getUpdateFieldsColumnNames() {
		List<String> strs = new ArrayList<String>(this.updateFields.size());
		for(Field field: this.updateFields){
			Column c = field.getAnnotation(Column.class);
			if(c != null && c.value().length > 0) {
				strs.add(c.value()[0]);
			}
			
			// default mapping: field names
			else if(isModel(field.getType())) {
				ClassInfo ci = getClassInfo(field.getType());
				for (Field key : ci.keys) {
					Collections.addAll(strs, getColumnNames(key));
				}
			}
			else {
				strs.add(field.getName());
			}
		}
		return strs;
	}
	
	public static String[] getColumnNames(Field field) {
		Column c = field.getAnnotation(Column.class);
		if(c != null && c.value().length > 0) return c.value();
		
		// default mapping: field names
		if(isModel(field.getType())) {
			ClassInfo ci = getClassInfo(field.getType());
			List<String> keys = new ArrayList<String>();
			// if no @column is provided
			// if the model has one single key, we use the local field name
			// if the model has several keys, we concatenate the fieldName+"_"+keyName
			if(ci.keys.size()==1){
				return new String[] { field.getName() };
			}
			for (Field key : ci.keys) {
				// uses the prefix fieldName_ to prevent problem with models having the same field names
				keys.addAll(Arrays.asList(getColumnNamesWithPrefix(key, field.getName()+"_")));
			}
			return keys.toArray(new String[keys.size()]);
		}
		return new String[]{ field.getName() };
	}
	
	public static String getSingleColumnName(Field field) {
		Column c = field.getAnnotation(Column.class);
		if(c != null && c.value().length > 0) return c.value()[0];
		
		// default mapping: field names
		if(isModel(field.getType())) {
			ClassInfo ci = getClassInfo(field.getType());
			String keys = "";
			// if no @column is provided
			// if the model has one single key, we use the local field name
			// if the model has several keys, we concatenate the fieldName+"_"+keyName
			if(ci.keys.size()==1){
				return field.getName();
			}
			// multi keys returns field_key1:field_key2
			int i=0;
			int sz = ci.keys.size();
			for (Field key : ci.keys) {
				// uses the prefix fieldName_ to prevent problem with models having the same field names
				keys += field.getName()+"_"+ getSingleColumnName(key);
				if(i < sz){
					keys += ":";
				}
				i++;
			}
			return keys;
		}
		return field.getName();
	}

	public static String getSimplestColumnName(Field field) {
		Column c = field.getAnnotation(Column.class);
		if(c != null && c.value().length > 0) return c.value()[0];
		
		return field.getName();
	}
	
	public static String[] getColumnNamesWithPrefix(Field field, String prefix) {
		Column c = field.getAnnotation(Column.class);
		if(c != null && c.value().length > 0) {
			String[] cols = c.value();
			for(int i=0;i<cols.length;i++){
				cols[i]=prefix+cols[i];
			}
			return cols;
		}
		
		// default mapping: field names
		if(isModel(field.getType())) {
			ClassInfo ci = getClassInfo(field.getType());
			List<String> keys = new ArrayList<String>();
			// if no @column is provided
			// if the model has one single key, we use the local field name
			// if the model has several keys, we concatenate the fieldName+"_"+keyName
			if(ci.keys.size()==1){
				return new String[] { prefix+field.getName() };
			}
			for (Field key : ci.keys) {
				// concatenates prefix with new prefix
				keys.addAll(Arrays.asList(getColumnNamesWithPrefix(key, prefix+field.getName()+"_")));
			}
			return keys.toArray(new String[keys.size()]);
		}
		return new String[]{ prefix + field.getName() };
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
			// if no @column is provided
			// if the model has one single key, we use the local field name
			// if the model has several keys, we concatenate the fieldName+"_"+keyName
			if(ci.keys.size()==1){
				if(tableName!=null && !("".equals(tableName))){
					return new String[] { tableName+"."+field.getName() };
				}else {
					return new String[] { field.getName() };
				}
			}
			for (Field key : ci.keys) {
				if(tableName!=null && !("".equals(tableName))){
					keys.addAll(Arrays.asList(getColumnNamesWithPrefix(key, tableName+"."+field.getName()+"_")));
				}else {
					keys.addAll(Arrays.asList(getColumnNamesWithPrefix(key, field.getName()+"_")));
				}
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
		// TODO what if type is NULL????
		if(type.getName().startsWith("java.")) return false;
		
		/*if(type == Json.class)*/
		if(Json.class.isAssignableFrom(type)) return false;
		return !ClassInfo.getClassInfo(type).keys.isEmpty();
	}

	public static boolean isId(Field field) {
		return field.isAnnotationPresent(Id.class);
	}
	
	public static boolean isEmbedded(Field field) {
		return field.isAnnotationPresent(Embedded.class);
	}

	public static boolean isAggregated(Field field) {
		return field.isAnnotationPresent(Aggregated.class);
	}
	
	public static boolean isJoined(Field field) {
		return field.isAnnotationPresent(Join.class);
	}	
	
	public static boolean isMany(Field field) {
		return Many.class.isAssignableFrom(field.getType());
	}	
	
	public static boolean isOne(Field field) {
		return One.class.isAssignableFrom(field.getType());
	}	
	
	public static boolean isOwned(Field field) {
		return field.isAnnotationPresent(Owned.class);
	}
	
	public static boolean isGenerated(Field field) {
		Id id = field.getAnnotation(Id.class);
		if(id != null) {
			Class<?> type = field.getType();
			// ONLY long ID can be auto_incremented
			if(id.value() == Generator.AUTO_INCREMENT 
					&& ( Long.TYPE == type || Long.class.isAssignableFrom(type))) {
				return true;
			} if(id.value() == Generator.UUID
					&& (String.class.isAssignableFrom(type)
					|| UUID.class.isAssignableFrom(type))) {
				return true;
			} 
		} 
		return false;
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

	public List<Method> getLifeCycleMethod(LifeCyclePhase lcp){
		return lifecycleMethods.get(lcp);
	}

	public Field getFirstFieldFromType(Class<?> fieldType){
		for(Field f: updateFields){
			if(f.getType().isAssignableFrom(fieldType)){
				return f;
			}
		}
		
		return null;
	}
}
