package siena.jdbc.ddl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;

import siena.ClassInfo;
import siena.DateTime;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Json;
import siena.Max;
import siena.NotNull;
import siena.SienaRestrictedApiException;
import siena.SimpleDate;
import siena.Text;
import siena.Time;
import siena.Unique;
import siena.core.Polymorphic;
import siena.embed.Embedded;

public class DdlGenerator {
	public static final String DB = "JDBC";

	private Map<String, Table> tables = new HashMap<String, Table>();
	private Database database = new Database();
	
	public Table getTable(String name) {
		return tables.get(name);
	}
	
	public Database getDatabase() {
		return database;
	}
	
	public Table addTable(Class<?> clazz) {
		Table table = new Table();
		
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		table.setName(info.tableName);
		database.addTable(table);
		
		/* columns */
		for (Field field : info.allFields) {
			String[] columns = ClassInfo.getColumnNames(field);
			boolean notNull = field.getAnnotation(NotNull.class) != null;
			
			Class<?> type = field.getType();
			if(!ClassInfo.isModel(type)) {
				Column column = createColumn(clazz, field, columns[0]);
				
				if(notNull || type.isPrimitive()) {
					column.setRequired(true);
					
					if(type.isPrimitive() && !ClassInfo.isId(field)) { // TODO: add also Boolean, Long, Double,... ?
						if(type == Boolean.TYPE) {
							column.setDefaultValue("false");
						} else {
							column.setDefaultValue("0");
						}
					}
				}
				
				Id id = field.getAnnotation(Id.class);
				if(id != null) {
					column.setPrimaryKey(true);
					column.setRequired(true);
					
					// auto_increment managed ONLY for long
					if(id.value() == Generator.AUTO_INCREMENT 
							&& (Long.TYPE == type || Long.class.isAssignableFrom(type)))
						column.setAutoIncrement(true);
				}
				
				table.addColumn(column);
			} else {
				List<Field> keys = ClassInfo.getClassInfo(type).keys;
				
				for (int i = 0; i < columns.length; i++) {
					Field f = keys.get(i);
					Column column = createColumn(clazz, f, columns[i]);

					if(notNull)
						column.setRequired(true);
					
					table.addColumn(column);
				}
			}
		}

		Map<String, UniqueIndex> uniques = new HashMap<String, UniqueIndex>();
		Map<String, NonUniqueIndex> indexes = new HashMap<String, NonUniqueIndex>();
		
		/* indexes */
		for (Field field : info.updateFields) {
			Index index = field.getAnnotation(Index.class);
			if(index != null) {
				String[] names = index.value();
				for (String name : names) {
					NonUniqueIndex i = indexes.get(name);
					if(i == null) {
						i = new NonUniqueIndex();
						i.setName(name);
						indexes.put(name, i);
						table.addIndex(i);
					}
					fillIndex(i, field);
				}
			}
			
			Unique unique = field.getAnnotation(Unique.class);
			if(unique != null) {
				String[] names = unique.value();
				for (String name : names) {
					UniqueIndex i = uniques.get(name);
					if(i == null) {
						i = new UniqueIndex();
						i.setName(name);
						uniques.put(name, i);
						table.addIndex(i);
					}
					fillIndex(i, field);
				}
			}
		}
		
		tables.put(table.getName(), table);
		return table;
	}
	
	private void fillIndex(org.apache.ddlutils.model.Index i, Field field) {
		String[] columns = ClassInfo.getColumnNames(field);
		for (String string : columns) {
			IndexColumn ic = new IndexColumn(string);
			i.addColumn(ic);
		}
	}
	
	private Column createColumn(Class<?> clazz, Field field, String col) {
		Class<?> type = field.getType();
		Column column = new Column();
		column.setName(col);

		int columnType;
		
		if(type == Byte.class         || type == Byte.TYPE)    columnType = Types.TINYINT;
		else if(type == Short.class   || type == Short.TYPE)   columnType = Types.SMALLINT;
		else if(type == Integer.class || type == Integer.TYPE) columnType = Types.INTEGER;
		else if(type == Long.class    || type == Long.TYPE)    columnType = Types.BIGINT;
		else if(type == Float.class   || type == Float.TYPE)   columnType = Types.FLOAT; // TODO verify
		else if(type == Double.class  || type == Double.TYPE)  columnType = Types.DOUBLE; // TODO verify
		else if(type == String.class) {
			if(field.getAnnotation(Text.class) != null) {
				columnType = Types.LONGVARCHAR;
			} else {
				columnType = Types.VARCHAR;
				
				Max max = field.getAnnotation(Max.class);
				if(max == null){
					//throw new SienaRestrictedApiException(DB, "createColumn", "Field "+field.getName()+" in class "
					//		+clazz.getName()+" doesn't have a @Max annotation");
					// default is 255 chars as in hibernate
					column.setSize("255");
				}
				else column.setSize(""+max.value());
			}
		}
		else if(type == Boolean.class || type == Boolean.TYPE) columnType = Types.BOOLEAN;
		else if(type == Date.class) {
			if(field.getAnnotation(DateTime.class) != null)
				columnType = Types.TIMESTAMP;
			else if(field.getAnnotation(Time.class) != null)
				columnType = Types.TIME;
			else if(field.getAnnotation(SimpleDate.class) != null)
				columnType = Types.DATE;
			else
				columnType = Types.TIMESTAMP;
		} else if(type == Json.class) {
			columnType = Types.LONGVARCHAR;
		} else if(type == byte[].class){
			columnType = Types.BLOB;
		} else if(Enum.class.isAssignableFrom(type)){
			// enums are stored as string
			columnType = Types.VARCHAR;
			Max max = field.getAnnotation(Max.class);
			if(max == null)
				column.setSize(""+255); // fixes by default to this value in order to prevent alter tables every time
			else column.setSize(""+max.value());
		}						
		else {
			Embedded embedded = field.getAnnotation(Embedded.class);
			if(embedded != null) {
				columnType = Types.LONGVARCHAR;
			} else if(field.isAnnotationPresent(Polymorphic.class)){
		        columnType = Types.BLOB;
		    }else {				
				throw new SienaRestrictedApiException(DB, "createColumn", "Unsupported type for field "
						+clazz.getName()+"."+field.getName());
			}
		}

		column.setTypeCode(columnType);
		
		return column;
	}

}
