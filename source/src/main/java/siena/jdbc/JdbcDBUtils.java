package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import siena.ClassInfo;
import siena.Json;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryFilterSimple;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.SienaException;
import siena.Util;
import siena.core.options.QueryOption;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.embed.Embedded;
import siena.embed.JsonSerializer;
import siena.jdbc.JdbcPersistenceManager.JdbcClassInfo;

public class JdbcDBUtils {
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
	
	public static int addParameters(Object obj, List<Field> fields, PreparedStatement ps, int i) throws SQLException {
		for (Field field : fields) {
			Class<?> type = field.getType();
			if(ClassInfo.isModel(type)) {
				JdbcClassInfo ci = JdbcClassInfo.getClassInfo(type);
				Object rel = Util.readField(obj, field);
				for(Field f : ci.keys) {
					if(rel != null) {
						Object value = Util.readField(rel, f);
						if(value instanceof Json)
							value = ((Json)value).toString();
						setParameter(ps, i++, value);
					} else {
						setParameter(ps, i++, null);
					}
				}
			} else {
				Object value = Util.readField(obj, field);
				if(value != null){
					if(Json.class.isAssignableFrom(field.getType()))
						value = ((Json)value).toString();
					else if(field.getAnnotation(Embedded.class) != null)
						value = JsonSerializer.serialize(value).toString();
					else if(Enum.class.isAssignableFrom(field.getType()))
						value = value.toString();
				}
				setParameter(ps, i++, value);
			}
		}
		return i;
	}
	
	public static void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
		ps.setObject(index, value);
	}
	
	public static <T> StringBuilder buildSqlSelect(Query<T> query) {
		Class<T> clazz = query.getQueriedClass();
		JdbcClassInfo info = JdbcClassInfo.getClassInfo(clazz);
		List<String> cols = new ArrayList<String>();

		List<Field> joinFields = JdbcMappingUtils.getJoinFields(query, info);
		if(joinFields==null){
			JdbcClassInfo.calculateColumns(info.allFields, cols, null, "");
			
			StringBuilder sql = 
				new StringBuilder("SELECT " + Util.join(cols, ", ") + " FROM " + info.tableName);
			
			return sql;
		}

		// builds fields from primary class
		JdbcClassInfo.calculateColumns(info.allFields, cols, info.tableName, "");
		StringBuilder sql = new StringBuilder(" FROM " + info.tableName);
				
		for(Field field: joinFields){
			JdbcClassInfo fieldInfo = JdbcClassInfo.getClassInfo(field.getType());
			
			if (!ClassInfo.isModel(field.getType())){
				throw new SienaException("Join not possible: Field "+field.getName()+" is not a relation field");
			}
			// removes the field itself from columns
			cols.remove( info.tableName+"."+field.getName());
			
			// adds all field columns
			JdbcClassInfo.calculateColumns(fieldInfo.allFields, cols, fieldInfo.tableName, "");
			String[] columns = ClassInfo.getColumnNames(field, info.tableName);		
			if (columns.length > 1 || fieldInfo.keys.size() > 1){
				throw new SienaException("Join not possible: join field "+field.getName()+" has multiple keys");
			}
			sql.append(" JOIN " + fieldInfo.tableName 
					+ " ON " + columns[0]
					+ " = " + fieldInfo.tableName+"."+fieldInfo.keys.get(0).getName());
		}

		sql.insert(0, "SELECT " + Util.join(cols, ", "));
		return sql;
	}
	
	public static final String WHERE = " WHERE ";
	public static final String AND = " AND ";
	public static final String IS_NULL = " IS NULL";
	public static final String IS_NOT_NULL = " IS NOT NULL";

	public static <T> void appendSqlWhere(Query<T> query, StringBuilder sql, List<Object> parameters) {
		List<QueryFilter> filters = query.getFilters();
		if(filters.isEmpty()) { return; }

		sql.append(WHERE);
		boolean first = true;
		for (QueryFilter filter : filters) {
			if(QueryFilterSimple.class.isAssignableFrom(filter.getClass())){
				QueryFilterSimple qf = (QueryFilterSimple)filter;
				String op    = qf.operator;
				Object value = qf.value;
				Field f      = qf.field;
	
				if(!first) {
					sql.append(AND);
				}
				first = false;
	
				String[] columns = ClassInfo.getColumnNames(f);
				if("IN".equals(op)) {
					if(!Collection.class.isAssignableFrom(value.getClass()))
						throw new SienaException("Collection needed when using IN operator in filter() query");
					StringBuilder s = new StringBuilder();
					Collection<?> col = (Collection<?>) value;
					for (Object object : col) {
						// TODO: if object isModel
						parameters.add(object);
						s.append(",?");
					}
					sql.append(columns[0]+" IN("+s.toString().substring(1)+")");
				} else if(ClassInfo.isModel(f.getType())) {
					if(!op.equals("=")) {
						throw new SienaException("Unsupported operator for relationship: "+op);
					}
					JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(f.getType());
					int i = 0;
					JdbcMappingUtils.checkForeignKeyMapping(classInfo.keys, columns, query.getQueriedClass(), f);
					for (Field key : classInfo.keys) {
						if(value == null) {
							sql.append(columns[i++]+IS_NULL);
						} else {
							sql.append(columns[i++]+"=?");
							key.setAccessible(true);
							Object o;
							try {
								o = key.get(value);
								parameters.add(o);
							} catch (Exception e) {
								throw new SienaException(e);
							}
						}
					}
				} else {
					if(value == null && op.equals("=")) {
						sql.append(columns[0]+IS_NULL);
					} else if(value == null && op.equals("!=")) {
						sql.append(columns[0]+IS_NOT_NULL);
					} else {
						sql.append(columns[0]+op+"?");
						if(value == null) {
							parameters.add(Types.NULL);
						} else {
							if (value instanceof Date) {
								value = Util.translateDate(f, (Date) value);
							}
							parameters.add(value);
						}
					}
				}
			}else if(QueryFilterSearch.class.isAssignableFrom(filter.getClass())){
				// adds querysearch 
				Class<T> clazz = query.getQueriedClass();
				QueryFilterSearch qf = (QueryFilterSearch)filter;
				List<String> cols = new ArrayList<String>();
				try {
					for (String field : qf.fields) {
						Field f = clazz.getDeclaredField(field);
						String[] columns = ClassInfo.getColumnNames(f);
						for (String col : columns) {
							cols.add(col);
						}
					}
					QueryOption opt = qf.option;
					if(opt != null){
						// only manages QueryOptionJdbcSearch
						if(QueryOptionJdbcSearch.class.isAssignableFrom(opt.getClass())){
							if(((QueryOptionJdbcSearch)opt).booleanMode){
								sql.append("MATCH("+Util.join(cols, ",")+") AGAINST(? IN BOOLEAN MODE)");
							}
							else {
								
							}
						}else{
							sql.append("MATCH("+Util.join(cols, ",")+") AGAINST(?)");
						}
					}else {
						// as mysql default search is fulltext and as it requires a FULLTEXT index, 
						// by default, we use boolean mode which works without fulltext index
						sql.append("MATCH("+Util.join(cols, ",")+") AGAINST(? IN BOOLEAN MODE)");
					}
					parameters.add(qf.match);
				}catch(Exception e){
					throw new SienaException(e);
				}
			}
		}
	}

	public static <T> void appendSqlOrder(Query<T> query, StringBuilder sql) {
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
				String[] columns = ClassInfo.getColumnNames(order.field);
				for (String column : columns) {
					sql.append(column+ (order.ascending? "" : " DESC"));
				}
			}else {
				try {
					ClassInfo parentCi = ClassInfo.getClassInfo(order.parentField.getType());
					Field subField = order.parentField.getType().getField(order.field.getName());
					String[] columns = ClassInfo.getColumnNames(subField, parentCi.tableName);
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
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);

		if(pag.isActive()) {
			sql.append(" LIMIT ?");
			parameters.add(pag.pageSize);
			
			if(offset.isActive()) {
				sql.append(" OFFSET ?");
				parameters.add(offset.offset);
			}else {
				sql.append(" OFFSET ?");
				parameters.add(0);
			}
		}
		// offset without paging is non sens in JDBC
		// so puts the MAX_VALUE as page size
		else {
			sql.append(" LIMIT ?");
			parameters.add(Integer.MAX_VALUE);
			
			if(offset.isActive()) {			
				sql.append(" OFFSET ?");
				parameters.add(offset.offset);
			}else {
				sql.append(" OFFSET ?");
				parameters.add(0);
			}
		}
	}
}
