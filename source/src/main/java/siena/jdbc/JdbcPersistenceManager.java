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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.DateTime;
import siena.Generator;
import siena.Id;
import siena.Json;
import siena.Query;
import siena.QueryFilter;
import siena.QueryOrder;
import siena.SienaException;
import siena.SimpleDate;
import siena.Time;
import siena.Util;
import siena.embed.Embedded;
import siena.embed.JsonSerializer;

public class JdbcPersistenceManager extends AbstractPersistenceManager {

	private Map<Class<?>, JdbcClassInfo> infoClasses;

	private ConnectionManager connectionManager;

	public JdbcPersistenceManager() {
		infoClasses = new ConcurrentHashMap<Class<?>, JdbcClassInfo>();
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

	public JdbcClassInfo getClassInfo(Class<?> clazz) {
		JdbcClassInfo ci = infoClasses.get(clazz);
		if(ci == null) {
			ci = new JdbcClassInfo(clazz);
			infoClasses.put(clazz, ci);
		}
		return ci;
	}

	private Object readField(Object object, Field field) {
		field.setAccessible(true);
		try {
			return field.get(object);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	public void delete(Object obj) {
		JdbcClassInfo classInfo = getClassInfo(obj.getClass());

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
		JdbcClassInfo classInfo = getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection().prepareStatement(classInfo.selectSQL);
			addParameters(obj, classInfo.keys, ps, 1);
			rs = ps.executeQuery();
			if(rs.next()) {
				mapObject(obj, rs);
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
				JdbcClassInfo ci = getClassInfo(type);
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
				if(value instanceof Json)
					value = ((Json)value).toString();
				else if(field.getAnnotation(Embedded.class) != null)
					value = JsonSerializer.serialize(value).toString();
				setParameter(ps, i++, value);
			}
		}
		return i;
	}

	public void insert(Object obj) {
		JdbcClassInfo classInfo = getClassInfo(obj.getClass());

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
		JdbcClassInfo classInfo = getClassInfo(obj.getClass());

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

	public String[] getColumnNames(Class<?> clazz, String field) {
		try {
			return ClassInfo.getColumnNames(clazz.getDeclaredField(field));
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	protected <T> T mapObject(Class<T> clazz, ResultSet rs) {
		try {
			T obj = clazz.newInstance();
			mapObject(obj, rs);
			return obj;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	private void mapObject(Object obj, ResultSet rs) {
		Class<?> clazz = obj.getClass();
		for (Field field : getClassInfo(clazz).allFields) {
			mapField(obj, field, rs);
		}
	}

	protected <T> List<T> mapList(Class<T> clazz, ResultSet rs) {
		try {
			List<T> objects = new ArrayList<T>();
			while(rs.next()) {
				objects.add(mapObject(clazz, rs));
			}
			return objects;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	private void mapField(Object obj, Field field, ResultSet rs) {
		Class<?> type = field.getType();
		field.setAccessible(true);
		try {
			if(ClassInfo.isModel(type)) {
				String[] fks = ClassInfo.getColumnNames(field);
				Object rel = type.newInstance();
				JdbcClassInfo classInfo = getClassInfo(type);
				boolean none = false;
				int i = 0;
				checkForeignKeyMapping(classInfo.keys, fks, obj.getClass(), field);
				for(Field f : classInfo.keys) {
					Object o = rs.getObject(fks[i++]);
					if(o == null) {
						none = true;
						break;
					}
					Util.setFromObject(rel, f, o);
					// f.set(rel, o);
				}
				if(!none)
					field.set(obj, rel);
			} else {
				Object val = rs.getObject(ClassInfo.getColumnNames(field)[0]);
				Util.setFromObject(obj, field, val);
				// field.set(obj, val);
			}
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	public void checkForeignKeyMapping(List<Field> keys, String[] columns, Class<?> clazz, Field field) {
		if (keys.size() != columns.length) {
			throw new SienaException("Bad mapping for field '"+field.getName()+"'. " +
					"Related class "+field.getType().getName()+" has "+keys.size()+" primary keys, " +
					"but '"+clazz.getName()+"' only has mappings for "+columns.length+" foreign keys");
		}
	}

	public void closeStatement(Statement st) {
		if(st == null) return;
		try {
			st.close();
		} catch (SQLException e) {
			throw new SienaException(e);
		}
	}

	public void closeResultSet(ResultSet rs) {
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

	private <T> List<T> fetch(Query<T> query, String suffix) {
		Class<T> clazz = query.getQueriedClass();
		List<Object> parameters = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder(getClassInfo(clazz).baseSelectSQL);
		appendSqlWhere(query, sql, parameters);
		appendSqlOrder(query, sql);
		sql.append(suffix);
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = createStatement(sql.toString(), parameters);
			rs = statement.executeQuery();
			List<T> result = mapList(clazz, rs);
			return result;
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			closeResultSet(rs);
			closeStatement(statement);
		}
	}

	@Override
	public <T> List<T> fetch(Query<T> query) {
		List<T> result = fetch(query, "");
		query.setNextOffset(result.size());
		return result;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		List<T> result = fetch(query, " LIMIT "+limit);
		query.setNextOffset(result.size());
		return result;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		List<T> result = fetch(query, " LIMIT "+limit+" OFFSET "+offset);
		query.setNextOffset(result.size());
		return result;
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
			if(op.equals("IN")) {
				if(!(value instanceof Collection<?>))
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
				JdbcClassInfo classInfo = getClassInfo(f.getType());
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
		if(orders.isEmpty()) return;

		sql.append(" ORDER BY ");
		boolean first = true;
		for (QueryOrder order : orders) {
			if(!first) {
				sql.append(", ");
			}
			first = false;

			String[] columns = ClassInfo.getColumnNames(order.field);
			for (String column : columns) {
				sql.append(column+ (order.ascending? "" : " DESC"));
			}
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

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, String field, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, String field, int limit,
			Object offset) {
		// TODO Auto-generated method stub
		return null;
	}


	private static final String[] supportedOperators = new String[]{ "<", ">", ">=", "<=", "=", " IN" };

	@Override
	public String[] supportedOperators() {
		return supportedOperators;
	}

	class JdbcClassInfo {
		public String tableName;
		public String insertSQL;
		public String updateSQL;
		public String deleteSQL;
		public String selectSQL;
		public String baseSelectSQL;

		public List<Field> keys = null;
		public List<Field> insertFields = null;
		public List<Field> updateFields = null;
		public List<Field> generatedKeys = null;
		public List<Field> allFields = null;

		public JdbcClassInfo(Class<?> clazz) {
			ClassInfo info = ClassInfo.getClassInfo(clazz);

			keys = info.keys;
			insertFields = info.insertFields;
			updateFields = info.updateFields;
			generatedKeys = info.generatedKeys;
			allFields = info.allFields;
			tableName = info.tableName;

			List<String> keyColumns = new ArrayList<String>();
			List<String> insertColumns = new ArrayList<String>();
			List<String> updateColumns = new ArrayList<String>();
			List<String> allColumns = new ArrayList<String>();

			calculateColumns(insertFields, insertColumns, "");
			calculateColumns(updateFields, updateColumns, "=?");
			calculateColumns(keys, keyColumns, "=?");
			calculateColumns(allFields, allColumns, "");

			deleteSQL = "DELETE FROM "+tableName+" WHERE "+Util.join(keyColumns, " AND ");

			String[] is = new String[insertColumns.size()];
			Arrays.fill(is, "?");
			insertSQL = "INSERT INTO "+tableName+" ("+Util.join(insertColumns, ", ")+") VALUES("+Util.join(Arrays.asList(is), ", ")+")";

			updateSQL = "UPDATE "+tableName+" SET ";
			updateSQL += Util.join(updateColumns, ", ");
			updateSQL += " WHERE ";
			updateSQL += Util.join(keyColumns, " AND ");

			baseSelectSQL = "SELECT "+Util.join(allColumns, ", ")+" FROM "+tableName;

			selectSQL = baseSelectSQL+" WHERE "+Util.join(keyColumns, " AND ");
		}

		private void calculateColumns(List<Field> fields, List<String> columns, String suffix) {
			for (Field field : fields) {
				String[] columnNames = ClassInfo.getColumnNames(field);
				for (String columnName : columnNames) {
					columns.add(columnName+suffix);
				}
			}
		}

	}

}
