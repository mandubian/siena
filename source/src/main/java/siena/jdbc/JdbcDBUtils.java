package siena.jdbc;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import siena.ClassInfo;
import siena.DateTime;
import siena.Json;
import siena.Query;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.SimpleDate;
import siena.Text;
import siena.Time;
import siena.Util;
import siena.core.DecimalPrecision;
import siena.core.Polymorphic;
import siena.embed.Embedded;
import siena.jdbc.JdbcPersistenceManager.JdbcClassInfo;

public class JdbcDBUtils {
	
	public static final String WHERE = " WHERE ";
	public static final String AND = " AND ";
	public static final String IS_NULL = " IS NULL";
	public static final String IS_NOT_NULL = " IS NOT NULL";
	
	public static void closeStatement(Statement st) {
		if(st == null) return;
		try {
			st.close();
		} catch (SQLException e) {
			throw new SienaException(e);
		} 
	}

	public static void closeResultSet(ResultSet rs) {
		if(rs == null) return;
		try {
			rs.close();
		} catch (SQLException e) {
			throw new SienaException(e);
		}
	}

	public static void closeStatementAndConnection(JdbcPersistenceManager pm, Statement st) {
		try {
			if(st != null){
				st.close();
			}
		} catch (SQLException e) {
			throw new SienaException(e);
		} finally {
			try {
				if(pm.getConnection().getAutoCommit()){
					pm.closeConnection();
				}
			} 
			catch(SQLException ex){
				// don't do anything with it
			}
			catch(SienaException ex){
				// don't do anything with it
			}
		}
	}
	
	public static <T> StringBuilder buildSqlSelect(Query<T> query) {
		Class<T> clazz = query.getQueriedClass();
		JdbcClassInfo info = JdbcClassInfo.getClassInfo(clazz);
		List<String> cols = new ArrayList<String>();

		List<Field> joinFields = JdbcMappingUtils.getJoinFields(query, info);
		if(joinFields==null){
			JdbcClassInfo.calculateColumnsAliases(info.allFields, cols, info.tableName, "");
			
			StringBuilder sql = 
				new StringBuilder("SELECT " + Util.join(cols, ", ") + " FROM " + info.tableName);
			
			return sql;
		}

		// builds fields from primary class
		JdbcClassInfo.calculateColumnsAliases(info.allFields, cols, info.tableName, "");
		StringBuilder sql = new StringBuilder(" FROM " + info.tableName);
		int i=0;
		String alias;
		for(Field field: joinFields){
			JdbcClassInfo fieldInfo = JdbcClassInfo.getClassInfo(field.getType());
			if (!ClassInfo.isModel(field.getType())){
				throw new SienaException("Join not possible: Field "+field.getName()+" is not a relation field");
			}
			alias = fieldInfo.tableName + i++;
			fieldInfo.joinFieldAliases.put(field.getName(), alias);
			
			// DO NOT remove the field itself from columns because it allows to find NULL fields
			// cols.remove( info.tableName+"."+field.getName());
			// adds all field columns using Alias
			JdbcClassInfo.calculateColumnsAliases(fieldInfo.allFields, cols, alias, "");
			String[] columns = ClassInfo.getColumnNames(field, info.tableName);		
			if (columns.length > 1 || fieldInfo.keys.size() > 1){
				throw new SienaException("Join not possible: join field "+field.getName()+" has multiple keys");
			}
			// LEFT INNER JOIN TO GET NULL FIELDS
			sql.append(" LEFT JOIN " + fieldInfo.tableName + " AS " +  alias
					+ " ON " + columns[0]
					+ " = " + alias + "." + fieldInfo.keys.get(0).getName());
		}

		sql.insert(0, "SELECT " + Util.join(cols, ", "));
		return sql;
	}


	public static <T> void appendSqlOrder(Query<T> query, StringBuilder sql) {
		Class<T> clazz = query.getQueriedClass();
		JdbcClassInfo info = JdbcClassInfo.getClassInfo(clazz);
		List<QueryOrder> orders = query.getOrders();
		List<QueryJoin> joins = query.getJoins();
		if(orders.isEmpty() && joins.isEmpty()) { return; }

		sql.append(" ORDER BY ");
		boolean first = true;
		for (QueryOrder order : orders) {
			if(!first) {
				sql.append(", ");
			}
			first = false;

			if(order.parentField==null){
				String[] columns = ClassInfo.getColumnNames(order.field, info.tableName);
				for (String column : columns) {
					sql.append(column+ (order.ascending? "" : " DESC"));
				}
			}else {
				try {
					JdbcClassInfo parentCi = JdbcClassInfo.getClassInfo(order.parentField.getType());
					Field subField = order.parentField.getType().getField(order.field.getName());
					// get columns using join field alias
					//String[] columns = ClassInfo.getColumnNames(subField, parentCi.tableName);
					String[] columns = 
						ClassInfo.getColumnNames(
								subField, parentCi.joinFieldAliases.get(order.parentField.getName()));
					for (String column : columns) {
						sql.append(column+ (order.ascending? "" : " DESC"));
					}
				}catch(NoSuchFieldException ex){
					throw new SienaException("Order not possible: join sort field "+order.field.getName()+" is not a known field of "+order.parentField.getName(), ex);
				}
			}
		}
	}

	public static <T> void appendSqlLimitOffset(Query<T> query, StringBuilder sql, List<Object> parameters) {
		//QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		//QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
		QueryOptionJdbcContext jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);

		sql.append(" LIMIT ?");
		parameters.add(jdbcCtx.realPageSize);
		
		sql.append(" OFFSET ?");
		parameters.add(jdbcCtx.realOffset);
		
		/*if(pag.isActive()) {
			sql.append(" LIMIT ?");
			parameters.add(jdbcCtx.realPageSize);
			
			sql.append(" OFFSET ?");
			parameters.add(jdbcCtx.realOffset);					
		}
		// offset without paging is non sense in JDBC
		// so puts the MAX_VALUE as page size
		else {
			sql.append(" LIMIT ?");
			parameters.add(Integer.MAX_VALUE);
			
			sql.append(" OFFSET ?");
			parameters.add(jdbcCtx.realOffset);
		}*/
	}
	
	public static int toSqlType(Object obj, Field field, String DB) {
		if(obj == null) return -1;
		Class<?> type = field.getType();
		
		if(type == Byte.class         || type == Byte.TYPE)    return Types.TINYINT;
		else if(type == Short.class   || type == Short.TYPE)   return Types.SMALLINT;
		else if(type == Integer.class || type == Integer.TYPE) return Types.INTEGER;
		else if(type == Long.class    || type == Long.TYPE)    return Types.BIGINT;
		else if(type == Float.class   || type == Float.TYPE)   return Types.FLOAT; // TODO verify
		else if(type == Double.class  || type == Double.TYPE)  return Types.DOUBLE; // TODO verify
		else if(type == String.class) {
			if(field.getAnnotation(Text.class) != null) {
				return Types.LONGVARCHAR;
			} else {
				return Types.VARCHAR;
			}
		}
		else if(type == Boolean.class || type == Boolean.TYPE) return Types.BOOLEAN;
		else if(type == Date.class) {
			if(field.getAnnotation(DateTime.class) != null)
				return Types.TIMESTAMP;
			else if(field.getAnnotation(Time.class) != null)
				return Types.TIME;
			else if(field.getAnnotation(SimpleDate.class) != null)
				return Types.DATE;
			else
				return Types.TIMESTAMP;
		} else if(type == Json.class) {
			return Types.LONGVARCHAR;
		} else if(type == byte[].class){
			return Types.BLOB;
		} else if(Enum.class.isAssignableFrom(type)){
			return Types.VARCHAR;
		} else if(type == BigDecimal.class){						
			DecimalPrecision an = field.getAnnotation(DecimalPrecision.class);
			if(an == null) {
				return Types.DECIMAL;
			}
			else {
				if(an.storageType() == DecimalPrecision.StorageType.NATIVE){
					return Types.DECIMAL;
				}else if(an.storageType() == DecimalPrecision.StorageType.STRING) {
					return Types.VARCHAR;
				}else if(an.storageType() == DecimalPrecision.StorageType.DOUBLE) {
					return Types.DOUBLE;					
				}else {
					return Types.DECIMAL;
				}
			}
		}
		else {
			Embedded embedded = field.getAnnotation(Embedded.class);
			if(embedded != null) {
				if("h2".equals(DB)){
					return Types.CLOB;
				}
				else {
					return Types.LONGVARCHAR;
				}
			} else if(field.isAnnotationPresent(Polymorphic.class)){
				return Types.BLOB;
		    }else {				
				throw new SienaRestrictedApiException(DB, "createColumn", "Unsupported type for field "
						+type.getName()+"."+field.getName());
			}
		}
	}
	
	public static void setObject(PreparedStatement ps, int index, Object value, Field field, String DB) throws SQLException {
		if(value == null) {
			ps.setNull(index, JdbcDBUtils.toSqlType(value, field, DB));
			return;
		}
		
		Class<?> type = field.getType();
		
		if(type == Byte.class         || type == Byte.TYPE)    ps.setByte(index, (Byte)value);
		else if(type == Short.class   || type == Short.TYPE)   ps.setShort(index, (Short)value);
		else if(type == Integer.class || type == Integer.TYPE) ps.setInt(index, (Integer)value);
		else if(type == Long.class    || type == Long.TYPE)    ps.setLong(index, (Long)value);
		else if(type == Float.class   || type == Float.TYPE)   ps.setFloat(index, (Float)value);
		else if(type == Double.class  || type == Double.TYPE)  ps.setDouble(index, (Double)value);
		else if(type == String.class) {
			ps.setString(index, (String)value);
		}
		else if(type == Boolean.class || type == Boolean.TYPE) ps.setBoolean(index, (Boolean)value);
		else if(type == Date.class) {						
			if(field.getAnnotation(DateTime.class) != null){
				java.sql.Timestamp ts = new java.sql.Timestamp(((Date)value).getTime());
				ps.setTimestamp(index, ts);
			}
			else if(field.getAnnotation(Time.class) != null){
				java.sql.Time ts = new java.sql.Time(((Date)value).getTime());
				ps.setTime(index, ts);
			}
			else if(field.getAnnotation(SimpleDate.class) != null){
				java.sql.Date d = new java.sql.Date(((Date)value).getTime());
				ps.setDate(index, d);
			}
			else {
				java.sql.Timestamp ts = new java.sql.Timestamp(((Date)value).getTime());
				ps.setTimestamp(index, ts);
			}			
		} else if(type == Json.class) {
			ps.setString(index, (String)value);
		} else if(type == byte[].class){
			ByteArrayInputStream bis = new ByteArrayInputStream((byte[])value);
			ps.setBlob(index, bis);
		} else if(Enum.class.isAssignableFrom(type)){
			ps.setString(index, (String)value);
		} else if(type == BigDecimal.class){						
			DecimalPrecision an = field.getAnnotation(DecimalPrecision.class);
			if(an == null) {
				ps.setObject(index, value);
			}
			else {
				if(an.storageType() == DecimalPrecision.StorageType.NATIVE){
					ps.setBigDecimal(index, (BigDecimal)value);
				}else if(an.storageType() == DecimalPrecision.StorageType.STRING) {
					ps.setString(index, ((BigDecimal)value).toPlainString());
				}else if(an.storageType() == DecimalPrecision.StorageType.DOUBLE) {
					ps.setDouble(index, ((BigDecimal)value).doubleValue());
				}else {
					ps.setBigDecimal(index, (BigDecimal)value);
				}
			}
		}
		else {
			Embedded embedded = field.getAnnotation(Embedded.class);
			if(embedded != null) {
				if("h2".equals(DB)){
					StringReader reader = new StringReader((String)value);
					ps.setClob(index, reader);
				}
				else {
					ps.setString(index, (String)value);
				}
			} else if(field.isAnnotationPresent(Polymorphic.class)){
				ByteArrayInputStream bis = new ByteArrayInputStream((byte[])value);
				ps.setBlob(index, bis);
		    }else {				
				throw new SienaRestrictedApiException(DB, "createColumn", "Unsupported type for field "
						+type.getName()+"."+field.getName());
			}
		}
	}
}
