package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import siena.ClassInfo;
import siena.Query;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.SienaException;
import siena.Util;
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
}
