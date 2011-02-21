/*
 * Copyright 2008 Alberto Gimeno <gimenete at gmail.com>
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
package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import siena.AbstractPersistenceManager;
import siena.BaseQuery;
import siena.ClassInfo;
import siena.DateTime;
import siena.Generator;
import siena.Id;
import siena.Json;
import siena.Query;
import siena.QueryFilter;
import siena.QueryJoin;
import siena.QueryOption;
import siena.QueryOrder;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.SimpleDate;
import siena.Time;
import siena.Util;
import siena.embed.Embedded;
import siena.embed.JsonSerializer;

public class JdbcPersistenceManager extends AbstractPersistenceManager {
	private static final String DB = "JDBC";
	
	private ConnectionManager connectionManager;

	public JdbcPersistenceManager() {
	}

	public JdbcPersistenceManager(ConnectionManager connectionManager, Class<?> listener) {
		this();
		this.connectionManager = connectionManager;
	}

	public void init(Properties p) {
		String cm = p.getProperty("transactions");
		if(cm != null) {
			try {
				connectionManager = (ConnectionManager) Class.forName(cm).newInstance();
			} catch (Exception e) {
				throw new SienaException(e);
			}
		} else {
			connectionManager = new ThreadedConnectionManager();
		}

		connectionManager.init(p);
	}

	protected Connection getConnection() throws SQLException {
		return connectionManager.getConnection();
	}

	private static Object readField(Object object, Field field) {
		field.setAccessible(true);
		try {
			return field.get(object);
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			field.setAccessible(false);
		}
	}

	public void delete(Object obj) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(classInfo.deleteSQL);
			addParameters(obj, classInfo.keys, ps, 1);
			int n = ps.executeUpdate();
			if(n == 0) {
				throw new SienaException("No updated rows");
			}
			if(n > 1) {
				throw new SienaException(n+" rows deleted");
			}
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			closeStatement(ps);
		}
	}

	public void get(Object obj) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection().prepareStatement(classInfo.selectSQL);
			addParameters(obj, classInfo.keys, ps, 1);
			rs = ps.executeQuery();
			if(rs.next()) {
				mapObject(obj, rs, null, null);
			} else {
				throw new SienaException("No such object");
			}
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			closeResultSet(rs);
			closeStatement(ps);
		}
	}

	public int addParameters(Object obj, List<Field> fields, PreparedStatement ps, int i) throws SQLException {
		for (Field field : fields) {
			Class<?> type = field.getType();
			if(ClassInfo.isModel(type)) {
				JdbcClassInfo ci = JdbcClassInfo.getClassInfo(type);
				Object rel = readField(obj, field);
				for(Field f : ci.keys) {
					if(rel != null) {
						Object value = readField(rel, f);
						if(value instanceof Json)
							value = ((Json)value).toString();
						setParameter(ps, i++, value);
					} else {
						setParameter(ps, i++, null);
					}
				}
			} else {
				Object value = readField(obj, field);
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

	public void insert(Object obj) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		try {
			for (Field field : classInfo.keys) {
				Id id = field.getAnnotation(Id.class);
				if (id.value() == Generator.UUID) {
					field.set(obj, UUID.randomUUID().toString());
				}
			}
			// TODO: implement primary key generation: SEQUENCE

			if (!classInfo.generatedKeys.isEmpty()) {
				insertWithAutoIncrementKey(classInfo, obj);
			} else {
				ps = getConnection().prepareStatement(classInfo.insertSQL);
				addParameters(obj, classInfo.insertFields, ps, 1);
				ps.executeUpdate();
			}
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			closeStatement(ps);
		}
	}

	public void update(Object obj) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(classInfo.updateSQL);
			int i = 1;
			i = addParameters(obj, classInfo.updateFields, ps, i);
			addParameters(obj, classInfo.keys, ps, i);
			ps.executeUpdate();
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			closeStatement(ps);
		}
	}

	protected static <T> T mapObject(Class<T> clazz, ResultSet rs, String tableName, List<Field >joinFields) {
		try {
			T obj = clazz.newInstance();
			mapObject(obj, rs, tableName, joinFields);
			return obj;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	private static void mapObject(Object obj, ResultSet rs, String tableName, List<Field >joinFields) {
		Class<?> clazz = obj.getClass();
		for (Field field : JdbcClassInfo.getClassInfo(clazz).allFields) {
			mapField(obj, field, rs, tableName, joinFields);
		}
	}

	protected <T> List<T> mapList(Class<T> clazz, ResultSet rs, String tableName, List<Field> joinFields, int pageSize) {
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
	
	
	protected <T> T mapObjectKeys(Class<T> clazz, ResultSet rs, String tableName, List<Field> joinFields) {
		try {
			T obj = clazz.newInstance();
			mapObjectKeys(obj, rs, tableName, joinFields);
			return obj;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	private void mapObjectKeys(Object obj, ResultSet rs, String tableName, List<Field> joinFields) {
		Class<?> clazz = obj.getClass();
		for (Field field : JdbcClassInfo.getClassInfo(clazz).keys) {
			mapField(obj, field, rs, tableName, joinFields);
		}
	}
	
	protected <T> List<T> mapListKeys(Class<T> clazz, ResultSet rs, String tableName, List<Field> joinFields, int pageSize) {
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

	private static void mapField(Object obj, Field field, ResultSet rs, String tableName, List<Field> joinFields) {
		Class<?> type = field.getType();
		field.setAccessible(true);
		try {
			if(ClassInfo.isModel(type)) {
				JdbcClassInfo fieldClassInfo = JdbcClassInfo.getClassInfo(type);
				
				if(joinFields==null || joinFields.size()==0 || !joinFields.contains(field)){
					String[] fks = ClassInfo.getColumnNames(field, tableName);
					Object rel = type.newInstance();
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
					if(!none)
						field.set(obj, rel);
				}
				else {
					Object rel = mapObject(type, rs, fieldClassInfo.tableName, null);
					field.set(obj, rel);
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

	public void beginTransaction(int isolationLevel) {
		connectionManager.beginTransaction(isolationLevel);
	}

	public void commitTransaction() {
		connectionManager.commitTransaction();
	}

	public void rollbackTransaction() {
		connectionManager.rollbackTransaction();
	}

	public void closeConnection() {
		connectionManager.closeConnection();
	}

	private PreparedStatement createStatement(String sql,
			List<Object> parameters) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(sql);
		if(parameters != null) {
			int i = 1;
			for (Object parameter : parameters) {
				setParameter(statement, i++, parameter);
			}
		}
		return statement;
	}

	protected void insertWithAutoIncrementKey(JdbcClassInfo classInfo, Object obj) throws SQLException, IllegalAccessException {
		ResultSet gk = null;
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(classInfo.insertSQL,
					Statement.RETURN_GENERATED_KEYS);
			addParameters(obj, classInfo.insertFields, ps, 1);
			ps.executeUpdate();
			gk = ps.getGeneratedKeys();
			if (!gk.next())
				throw new SienaException("No such generated keys");
			int i = 1;
			for (Field field : classInfo.generatedKeys) {
				field.setAccessible(true);
				Util.setFromObject(obj, field, gk.getObject(i));
				// field.set(obj, gk.getObject(i));
				i++;
			}
		} finally {
			closeResultSet(gk);
			closeStatement(ps);
		}
	}

	protected void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
		ps.setObject(index, value);
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	private static <T> List<Field> getJoinFields(Query<T> query) {
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
	
	private static <T> List<Field> getJoinFields(Query<T> query, JdbcClassInfo info) {
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
	
	private <T> List<T> fetch(Query<T> query, String suffix) {
		QueryOption pag = query.option(QueryOption.PAGINATE.type);
		QueryOption offset = query.option(QueryOption.OFFSET.type);
		QueryOption cludge = query.option(QueryOption.DB_CLUDGE.type);
		QueryOption reusable = query.option(QueryOption.REUSABLE.type);
		int pageSize = (Integer)pag.value();
		int offsetIdx = (Integer)offset.value();
		
		if(!reusable.isActive() || (reusable.isActive() && !cludge.isActive())) {
			Class<T> clazz = query.getQueriedClass();
			List<Object> parameters = new ArrayList<Object>();
			StringBuilder sql = buildSqlSelect(query);
			appendSqlWhere(query, sql, parameters);
			appendSqlOrder(query, sql);
			appendSqlLimitOffset(query, sql, parameters);
			//sql.append(suffix);
			PreparedStatement statement = null;
			ResultSet rs = null;
			try {
				statement = createStatement(sql.toString(), parameters);
				if(pag.isActive()) {
					// this is just a hint to the DB so wonder if it should be used
					statement.setFetchSize(pageSize);
				}
				rs = statement.executeQuery();
				List<T> result = mapList(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, 
						getJoinFields(query), pageSize);
				// increases offset
				if(offset.isActive())
					offset.value(offsetIdx+result.size());
				return result;
			} catch(SQLException e) {
				throw new SienaException(e);
			} finally {
				if(!reusable.isActive()){
					closeResultSet(rs);
					closeStatement(statement);
				}else {
					Integer idxOffset = parameters.size()-1;
					Integer idxLimit = idxOffset - 1;
					// store indexes of offset and limit for reuse
					cludge.activate().value(new Object[] { statement, idxLimit, idxOffset});
				}
			}
		}else {
			// payload has been initialized so goes on
			Class<T> clazz = query.getQueriedClass();
			Object[] obj = (Object[])cludge.value();
			PreparedStatement st = (PreparedStatement)obj[0];
			Integer idxLimit = (Integer)obj[1];
			Integer idxOffset = (Integer)obj[2];
			try {
				// when paginating, should update limit and offset
				if(pag.isActive()){
					// update limit and offset
					st.setObject(idxLimit, pag.value());
				}
				if(offset.isActive()){
					st.setObject(idxOffset, offset.value());
				}
				
				ResultSet rs = st.getResultSet();
				List<T> result = mapList(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, 
					getJoinFields(query), pageSize);
				// increases offset
				if(offset.isActive())
					offset.value(offsetIdx+result.size());
				return result;
			}catch(SQLException ex){
				throw new SienaException(ex);
			} finally {
				closeStatement(st);
			}
		}
	}


	@Override
	public <T> List<T> fetch(Query<T> query) {
		List<T> result = fetch(query, "");
		//query.setNextOffset(result.size());
		return result;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		query.option(QueryOption.PAGINATE.type).activate().value(limit);
		List<T> result = fetch(query, "");
		//List<T> result = fetch(query, " LIMIT "+limit);
		//query.setNextOffset(result.size());
		return result;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		query.option(QueryOption.PAGINATE.type).activate().value(limit);
		query.option(QueryOption.OFFSET.type).activate().value(offset);
		List<T> result = fetch(query, "");
		//List<T> result = fetch(query, " LIMIT "+limit+" OFFSET "+offset);
		//query.setNextOffset(result.size());
		return result;
	}

	private <T> StringBuilder buildSqlSelect(Query<T> query) {
		Class<T> clazz = query.getQueriedClass();
		JdbcClassInfo info = JdbcClassInfo.getClassInfo(clazz);
		List<String> cols = new ArrayList<String>();

		List<Field> joinFields = getJoinFields(query, info);
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
				throw new SienaRestrictedApiException(DB, "join", "Join not possible: Field "+field.getName()+" is not a relation field");
			}
			// removes the field itself from columns
			cols.remove( info.tableName+"."+field.getName());
			
			// adds all field columns
			JdbcClassInfo.calculateColumns(fieldInfo.allFields, cols, fieldInfo.tableName, "");
			String[] columns = ClassInfo.getColumnNames(field, info.tableName);		
			if (columns.length > 1 || fieldInfo.keys.size() > 1){
				throw new SienaRestrictedApiException(DB, "join", "Join not possible: join field "+field.getName()+" has multiple keys");
			}
			sql.append(" JOIN " + fieldInfo.tableName 
					+ " ON " + columns[0]
					+ " = " + fieldInfo.tableName+"."+fieldInfo.keys.get(0).getName());
		}

		sql.insert(0, "SELECT " + Util.join(cols, ", "));
		return sql;
	}
	
	private <T> void appendSqlWhere(Query<T> query, StringBuilder sql, List<Object> parameters) {
		List<QueryFilter> filters = query.getFilters();
		if(filters.isEmpty()) return;

		sql.append(" WHERE ");
		boolean first = true;
		for (QueryFilter filter : filters) {
			String op    = filter.operator;
			Object value = filter.value;
			Field f      = filter.field;

			if(!first)
				sql.append(" AND ");
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
				checkForeignKeyMapping(classInfo.keys, columns, query.getQueriedClass(), f);
				for (Field key : classInfo.keys) {
					if(value == null) {
						sql.append(columns[i++]+" IS NULL");
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
					sql.append(columns[0]+" IS NULL");
				} else if(value == null && op.equals("!=")) {
					sql.append(columns[0]+" IS NOT NULL");
				} else {
					sql.append(columns[0]+op+"?");
					if(value == null) {
						parameters.add(Types.NULL);
					} else {
						if (value instanceof Date) {
							value = translateDate(f, (Date) value);
						}
						parameters.add(value);
					}
				}
			}

		}
	}

	private <T> void appendSqlOrder(Query<T> query, StringBuilder sql) {
		List<QueryOrder> orders = query.getOrders();
		List<QueryJoin> joins = query.getJoins();
		if(orders.isEmpty() && joins.isEmpty()) return;

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
					throw new SienaException("Order not possible: join sort field "+order.field.getName()+" is not a known field of "+order.parentField.getName());
				}
			}
		}
	}

	private <T> void appendSqlLimitOffset(Query<T> query, StringBuilder sql, List<Object> parameters) {
		QueryOption pag = query.option(QueryOption.PAGINATE.type);
		QueryOption offset = query.option(QueryOption.OFFSET.type);

		if(pag.isActive()) {
			sql.append(" LIMIT ?");
			parameters.add(pag.value());
		}

		if(offset.isActive()) {
			sql.append(" OFFSET ?");
			parameters.add(offset.value());
		}
	}
	
	private Object translateDate(Field f, Date value) {
		long t = value.getTime();

		SimpleDate simpleDate = f.getAnnotation(SimpleDate.class);
		if(simpleDate != null) {
			return new java.sql.Date(t);
		}

		DateTime dateTime = f.getAnnotation(DateTime.class);
		if(dateTime != null) {
			return new java.sql.Timestamp(t); 
		}

		Time time = f.getAnnotation(Time.class);
		if(time != null) {
			return new java.sql.Time(t); 
		}

		return new java.sql.Timestamp(t);
	}

	@Override
	public <T> int count(Query<T> query) {
		ClassInfo info = ClassInfo.getClassInfo(query.getQueriedClass());
		List<Object> parameters = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
		sql.append(info.tableName);
		appendSqlWhere(query, sql, parameters);
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = createStatement(sql.toString(), parameters);
			rs = statement.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
	}

	@Override
	public <T> int delete(Query<T> query) {
		ClassInfo info = ClassInfo.getClassInfo(query.getQueriedClass());
		List<Object> parameters = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder("DELETE FROM ");
		sql.append(info.tableName);
		appendSqlWhere(query, sql, parameters);
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = createStatement(sql.toString(), parameters);
			return statement.executeUpdate();
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
	}
	
	private <T> List<T> fetchKeys(Query<T> query, String suffix) {
		QueryOption pag = query.option(QueryOption.PAGINATE.type);
		QueryOption offset = query.option(QueryOption.OFFSET.type);
		QueryOption cludge = query.option(QueryOption.DB_CLUDGE.type);
		QueryOption reusable = query.option(QueryOption.REUSABLE.type);
		int pageSize = (Integer)pag.value();
		int offsetIdx = (Integer)offset.value();
		
		if(!reusable.isActive() || (reusable.isActive() && !cludge.isActive())) {
			Class<T> clazz = query.getQueriedClass();
			List<Object> parameters = new ArrayList<Object>();
			StringBuilder sql = buildSqlSelect(query);
			appendSqlWhere(query, sql, parameters);
			appendSqlOrder(query, sql);
			appendSqlLimitOffset(query, sql, parameters);
			//sql.append(suffix);
			PreparedStatement statement = null;
			ResultSet rs = null;
			try {
				statement = createStatement(sql.toString(), parameters);
				if(pag.isActive()) {
					// this is just a hint to the DB so wonder if it should be used
					statement.setFetchSize(pageSize);
				}
				rs = statement.executeQuery();
				List<T> result = mapListKeys(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, 
						getJoinFields(query), pageSize);
				// increases offset
				if(offset.isActive())
					offset.value(offsetIdx+result.size());
				return result;
			} catch(SQLException e) {
				throw new SienaException(e);
			} finally {
				if(!reusable.isActive()){
					closeResultSet(rs);
					closeStatement(statement);
				}else {
					Integer idxOffset = parameters.size()-1;
					Integer idxLimit = idxOffset - 1;
					// store indexes of offset and limit for reuse
					cludge.activate().value(new Object[] { statement, idxLimit, idxOffset});
				}
			}
		}else {
			// payload has been initialized so goes on
			Class<T> clazz = query.getQueriedClass();
			Object[] obj = (Object[])cludge.value();
			PreparedStatement st = (PreparedStatement)obj[0];
			Integer idxLimit = (Integer)obj[1];
			Integer idxOffset = (Integer)obj[2];
			try {
				// when paginating, should update limit and offset
				if(pag.isActive()){
					// update limit and offset
					st.setObject(idxLimit, pag.value());
				}
				if(offset.isActive()){
					st.setObject(idxOffset, offset.value());
				}
				
				ResultSet rs = st.getResultSet();
				List<T> result = mapListKeys(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, 
					getJoinFields(query), pageSize);
				// increases offset
				if(offset.isActive())
					offset.value(offsetIdx+result.size());
				return result;
			}catch(SQLException ex){
				throw new SienaException(ex);
			} finally {
				closeStatement(st);
			}
		}
	}
	

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		List<T> result = fetchKeys(query, "");
		//query.setNextOffset(result.size());
		return result;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		query.option(QueryOption.PAGINATE.type).activate().value(limit);
		List<T> result = fetchKeys(query, " LIMIT "+limit);
		//query.setNextOffset(result.size());
		return result;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		query.option(QueryOption.PAGINATE.type).activate().value(limit);
		query.option(QueryOption.OFFSET.type).activate().value(offset);
		List<T> result = fetchKeys(query, " LIMIT "+limit+" OFFSET "+offset);
		//query.setNextOffset(result.size());
		return result;
	}
	
	
	private <T> Iterable<T> iter(Query<T> query, String suffix) {
		QueryOption pag = query.option(QueryOption.PAGINATE.type);
		QueryOption offset = query.option(QueryOption.OFFSET.type);
		QueryOption cludge = query.option(QueryOption.DB_CLUDGE.type);
		QueryOption reusable = query.option(QueryOption.REUSABLE.type);

		// forces the reusable option since iteration requires it!!!
		reusable.activate();
		
		int pageSize = (Integer)pag.value();
		int offsetIdx = (Integer)offset.value();
		
		if(!reusable.isActive() || (reusable.isActive() && !cludge.isActive())) {
			Class<T> clazz = query.getQueriedClass();
			List<Object> parameters = new ArrayList<Object>();
			StringBuilder sql = buildSqlSelect(query);
			appendSqlWhere(query, sql, parameters);
			appendSqlOrder(query, sql);
			appendSqlLimitOffset(query, sql, parameters);
			//sql.append(suffix);
			PreparedStatement statement = null;
			ResultSet rs = null;
			try {
				statement = createStatement(sql.toString(), parameters);
				if(pag.isActive()) {
					// this is just a hint to the DB so wonder if it should be used
					statement.setFetchSize(pageSize);
				}
				rs = statement.executeQuery();
				
				// increases offset with fetch size
				if(offset.isActive())
					offset.value(offsetIdx+rs.getFetchSize());			
				
				return new SienaJdbcIterable<T>(statement, rs, query);
			} catch(SQLException e) {
				throw new SienaException(e);
			} finally {
				if(!reusable.isActive()){
					closeResultSet(rs);
					closeStatement(statement);
				}else {
					Integer idxOffset = parameters.size();
					Integer idxLimit = idxOffset - 1;
					// store indexes of offset and limit for reuse
					cludge.activate().value(new Object[] { statement, idxLimit, idxOffset});
				}
			}
		}else {
			// payload has been initialized so goes on
			Object[] obj = (Object[])cludge.value();
			PreparedStatement st = (PreparedStatement)obj[0];
			Integer idxLimit = (Integer)obj[1];
			Integer idxOffset = (Integer)obj[2];
			try {
				// when paginating, should update limit and offset
				if(pag.isActive()){
					// update limit and offset
					st.setObject(idxLimit, pag.value());
				}
				if(offset.isActive()){
					st.setObject(idxOffset, offset.value());
				}
				
				ResultSet rs = st.getResultSet();
				
				// increases offset with fetch size
				if(offset.isActive())
					offset.value(offsetIdx+rs.getFetchSize());			
				
				return new SienaJdbcIterable<T>(st, rs, query);
			}catch(SQLException ex){
				throw new SienaException(ex);
			} finally {
				if(!reusable.isActive()){
					closeStatement(st);
				}
			}
		}
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		return iter(query, "");
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		query.option(QueryOption.PAGINATE.type).activate().value(limit);
		return iter(query, " LIMIT "+limit);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		query.option(QueryOption.PAGINATE.type).activate().value(limit);
		query.option(QueryOption.OFFSET.type).activate().value(offset);
		return iter(query, " LIMIT "+limit+" OFFSET "+offset);
	}

	@Override
	public <T> void release(Query<T> query) {
		QueryOption pag = query.option(QueryOption.PAGINATE.type);
		QueryOption offset = query.option(QueryOption.OFFSET.type);
		QueryOption cludge = query.option(QueryOption.DB_CLUDGE.type);
		QueryOption reusable = query.option(QueryOption.REUSABLE.type);
		
		// resets offset
		if(offset.isActive()) 
			offset.passivate().value(0);
		// disables reusable and cludge
		if(reusable.isActive()){
			reusable.passivate();
			// payload has been initialized so goes on
			Object[] obj = (Object[])cludge.value();
			PreparedStatement st = (PreparedStatement)obj[0];
			closeStatement(st);	
			cludge.passivate().value(null);
		}
	}

	private static final String[] supportedOperators = new String[]{ "<", ">", ">=", "<=", "!=", "=", "IN" };

	@Override
	public String[] supportedOperators() {
		return supportedOperators;
	}

	public static class JdbcClassInfo {
		protected static Map<Class<?>, JdbcClassInfo> infoClasses = new ConcurrentHashMap<Class<?>, JdbcClassInfo>();

		// encapsulates a classinfo
		public ClassInfo info;
		
		public String tableName;
		public String insertSQL;
		public String updateSQL;
		public String deleteSQL;
		public String selectSQL;
		public String baseSelectSQL;
		public String keySelectSQL;
		public String baseKeySelectSQL;

		public List<Field> keys = null;
		public List<Field> insertFields = null;
		public List<Field> updateFields = null;
		public List<Field> generatedKeys = null;
		public List<Field> allFields = null;
		public List<Field> joinFields = null;

		public JdbcClassInfo(Class<?> clazz, ClassInfo info) {
			keys = info.keys;
			insertFields = info.insertFields;
			updateFields = info.updateFields;
			generatedKeys = info.generatedKeys;
			allFields = info.allFields;
			tableName = info.tableName;
			joinFields = info.joinFields;

			List<String> keyColumns = new ArrayList<String>();
			List<String> keyWhereColumns = new ArrayList<String>();
			List<String> insertColumns = new ArrayList<String>();
			List<String> updateColumns = new ArrayList<String>();
			List<String> allColumns = new ArrayList<String>();

			calculateColumns(info.insertFields, insertColumns, null, "");
			calculateColumns(info.updateFields, updateColumns, null, "=?");
			calculateColumns(info.keys, keyColumns, null, "");
			calculateColumns(info.keys, keyWhereColumns, null, "=?");
			calculateColumns(info.allFields, allColumns, null, "");

			deleteSQL = "DELETE FROM "+tableName+" WHERE "+Util.join(keyWhereColumns, " AND ");

			String[] is = new String[insertColumns.size()];
			Arrays.fill(is, "?");
			insertSQL = "INSERT INTO "+tableName+" ("+Util.join(insertColumns, ", ")+") VALUES("+Util.join(Arrays.asList(is), ", ")+")";

			updateSQL = "UPDATE "+tableName+" SET ";
			updateSQL += Util.join(updateColumns, ", ");
			updateSQL += " WHERE ";
			updateSQL += Util.join(keyWhereColumns, " AND ");

			baseSelectSQL = "SELECT "+Util.join(allColumns, ", ")+" FROM "+tableName;
			baseKeySelectSQL = "SELECT "+Util.join(keyColumns, ", ")+" FROM "+tableName;

			selectSQL = baseSelectSQL+" WHERE "+Util.join(keyWhereColumns, " AND ");
			keySelectSQL = baseKeySelectSQL+" WHERE "+Util.join(keyWhereColumns, " AND ");
		}

		public static void calculateColumns(List<Field> fields, List<String> columns, String tableName, String suffix) {
			for (Field field : fields) {
				String[] columnNames = ClassInfo.getColumnNames(field, tableName);
				for (String columnName : columnNames) {
					columns.add(columnName+suffix);
				}
			}
		}
		
		public static JdbcClassInfo getClassInfo(Class<?> clazz) {
			JdbcClassInfo ci = infoClasses.get(clazz);

			if(ci == null) {
				ci = new JdbcClassInfo(clazz, ClassInfo.getClassInfo(clazz));
				infoClasses.put(clazz, ci);
			}
			return ci;
		}
	}

	/**
	 * @author mandubian
	 * 
	 *         A Siena Iterable<Model> encapsulating a Jdbc ResultSet
	 *         its Iterator<Model>...
	 */
	public static class SienaJdbcIterable<T> implements Iterable<T> {
	    /**
	     * The wrapped <code>Statement</code>.
	     */
	    private final Statement st;
		
		/**
	     * The wrapped <code>ResultSet</code>.
	     */
	    private final ResultSet rs;
	    
	    /**
	     * The wrapped <code>Query</code>.
	     */
	    Query<T> query;
	    
	    /**
	     * The wrapped <code>Pagination QueryOption</code>.
	     */
	    QueryOption pag;

		SienaJdbcIterable(Statement st, ResultSet rs, Query<T> query) {
			this.st = st;
			this.rs = rs;
			this.query = query;
			this.pag = query.option(QueryOption.PAGINATE.type);

		}

		@Override
		public Iterator<T> iterator() {
			return new SienaJdbcIterator<T>(query);
		}

		// only constructs the iterator with Class<V> in order to transmit the generic type T
		public class SienaJdbcIterator<V> implements Iterator<V> {
			Query<V> query;
			int idx = 0;

			SienaJdbcIterator(Query<V> query) {
				this.query = query;
			}

			@Override
			public boolean hasNext() {
				try {
		            if(!rs.isLast()){
		            	if(pag.isActive())
		            		return idx<(Integer)pag.value();
		            	return true;
		            }
		            return false;
		        } catch (SQLException ex) {
		            throw new SienaException(ex);
		        }
			}

			@Override
			public V next() {
				try {
					if(rs.next()){
						Class<V> clazz = query.getQueriedClass();
						if(pag.isActive() && idx<(Integer)pag.value()){
							idx++;
							return mapObject(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, getJoinFields(query));
						}else return mapObject(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, getJoinFields(query));
					}
					else throw new NoSuchElementException();
				} catch (SQLException e) {
					throw new SienaException(e);
		        }
			}

			@Override
			public void remove() {
				// doesn't delete row because it REALLY deletes row from DB!!!
				// need to think about it
				/*try {
					
		            rs.deleteRow();
		        } catch (SQLException e) {
		        	throw new SienaException(e);
		        }*/
			}

			@Override
			protected void finalize() throws Throwable {
				closeResultSet(rs);
				closeStatement(st);
				super.finalize();
			}

		}

		@Override
		protected void finalize() throws Throwable {
			closeResultSet(rs);
			closeStatement(st);
			super.finalize();
		}

	}

	
}
