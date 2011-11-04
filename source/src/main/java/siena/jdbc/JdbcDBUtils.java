package siena.jdbc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
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
import siena.Max;
import siena.PersistenceManager;
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
	
	public static int toSqlType(Object obj) {
		if(obj == null) return -1;
		Class<?> type = obj.getClass();
		
		if(type == Byte.class         || type == Byte.TYPE)    return Types.TINYINT;
		else if(type == Short.class   || type == Short.TYPE)   return Types.SMALLINT;
		else if(type == Integer.class || type == Integer.TYPE) return Types.INTEGER;
		else if(type == Long.class    || type == Long.TYPE)    return Types.BIGINT;
		else if(type == Float.class   || type == Float.TYPE)   return Types.FLOAT; // TODO verify
		else if(type == Double.class  || type == Double.TYPE)  return Types.DOUBLE; // TODO verify
		else if(type == String.class) {
			String str = (String)obj;
			if(str.length() > 500) return Types.LONGVARCHAR;
			return Types.VARCHAR;
		}
		else if(type == Boolean.class || type == Boolean.TYPE) return Types.BOOLEAN;
		else if(type == Date.class) {
			return Types.TIMESTAMP;
		} else if(type == Json.class) {
			return Types.LONGVARCHAR;
		} else if(type == byte[].class){
			return Types.BLOB;
		} else if(Enum.class.isAssignableFrom(type)){
			return Types.VARCHAR;
		} else if(type == BigDecimal.class){						
			return Types.DECIMAL;
		}
		else {
			return Types.BLOB;
		}
	}
	
	public static void setObject(PreparedStatement ps, int index, Object obj) throws SQLException {
		if(obj == null) ps.setNull(index, JdbcDBUtils.toSqlType(obj));
		
		Class<?> type = obj.getClass();
		
		if(type == Byte.class         || type == Byte.TYPE)    ps.setByte(index, (Byte)obj);
		else if(type == Short.class   || type == Short.TYPE)   ps.setShort(index, (Short)obj);
		else if(type == Integer.class || type == Integer.TYPE) ps.setInt(index, (Integer)obj);
		else if(type == Long.class    || type == Long.TYPE)    ps.setLong(index, (Long)obj);
		else if(type == Float.class   || type == Float.TYPE)   ps.setFloat(index, (Float)obj);
		else if(type == Double.class  || type == Double.TYPE)  ps.setDouble(index, (Double)obj);
		else if(type == String.class) {
			ps.setString(index, (String)obj);
		}
		else if(type == Boolean.class || type == Boolean.TYPE) ps.setBoolean(index, (Boolean)obj);
		else if(type == Date.class) {
			java.sql.Date d = new java.sql.Date(((Date)obj).getTime());			
			ps.setDate(index, d);
		} else if(type == Json.class) {
			ps.setString(index, (String)obj);
		} else if(type == byte[].class){
			// TODO
		} else if(Enum.class.isAssignableFrom(type)){
			ps.setString(index, (String)obj);
		} else if(type == BigDecimal.class){						
			// TODO
		}
		else {
			// TODO
		}
	}
}
