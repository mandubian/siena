package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import siena.ClassInfo;
import siena.Generator;
import siena.Id;
import siena.QueryFilterSearch;
import siena.SienaException;
import siena.Util;
import siena.core.options.QueryOption;

public class PostgresqlPersistenceManager extends JdbcPersistenceManager {
	private static final String DB = "POSTGRES";
	
	public PostgresqlPersistenceManager() {
		
	}
	
	public PostgresqlPersistenceManager(ConnectionManager connectionManager, Class<?> listener) {
		super(connectionManager, listener);
	}
	
	@Override
    protected void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value != null && value instanceof Date) {
            Date date = (Date) value;
            ps.setTimestamp(index, new Timestamp(date.getTime()));
        } else {
            ps.setObject(index, value);
        }
    }

	@Override
	protected void insertWithAutoIncrementKey(JdbcClassInfo classInfo, Object obj) throws SQLException, IllegalAccessException {
		List<String> keyNames = new ArrayList<String>();
		for (Field field : classInfo.generatedKeys) {
			keyNames.add(field.getName());
		}

		ResultSet gk = null;
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(
					classInfo.insertSQL + " RETURNING " + Util.join(keyNames, ","));
			addParameters(obj, classInfo.insertFields, ps, 1);
			gk = ps.executeQuery();
			if (!gk.next())
				throw new SienaException("No such generated keys");

			int i = 1;
			for (Field field : classInfo.generatedKeys) {
				//field.setAccessible(true);
				Util.setFromObject(obj, field, gk.getObject(i));
				// field.set(obj, gk.getObject(i));
				i++;
			}
		} finally {
			JdbcDBUtils.closeResultSet(gk);
			JdbcDBUtils.closeStatement(ps);
		}
	}
	
	/**
	 * required to be overriden for Postgres
	 * 
	 * @param classInfo
	 * @param objMap
	 * @throws SQLException
	 * @throws IllegalAccessException
	 */
	@Override
	protected int insertBatchWithAutoIncrementKey(JdbcClassInfo classInfo, Map<JdbcClassInfo, List<Object>> objMap) throws SQLException, IllegalAccessException {
		List<String> keyNames = new ArrayList<String>();
		for (Field field : classInfo.generatedKeys) {
			keyNames.add(field.getName());
		}
		
		// can't use batch in Postgres with generated keys... known bug
		// http://postgresql.1045698.n5.nabble.com/PreparedStatement-batch-statement-impossible-td3406927.html
		PreparedStatement ps = null;
		ResultSet gk = null;
		int res = 0;
		try {
			ps = getConnection().prepareStatement(
					classInfo.insertSQL + " RETURNING " + Util.join(keyNames, ","));
			
			for(Object obj: objMap.get(classInfo)){
				for (Field field : classInfo.keys) {
					Id id = field.getAnnotation(Id.class);
					if (id.value() == Generator.UUID) {
						field.set(obj, UUID.randomUUID().toString());
					}
				}
				// TODO: implement primary key generation: SEQUENCE
				addParameters(obj, classInfo.insertFields, ps, 1);
				gk = ps.executeQuery();
				if (!gk.next())
					throw new SienaException("No such generated keys");
	
				int i = 1;
				for (Field field : classInfo.generatedKeys) {
					//field.setAccessible(true);
					Util.setFromObject(obj, field, gk.getObject(i));
					// field.set(obj, gk.getObject(i));
					i++;
				}
				
				JdbcDBUtils.closeResultSet(gk);
				res++;
			}
			
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
		// doesn't work with Postgres because it doesn't manage generated keys
		// int[] res = ps.executeBatch();
		
		return res;
	}
	
	@Override
	public <T> void appendSqlSearch(QueryFilterSearch qf, Class<?> clazz, JdbcClassInfo info, StringBuilder sql, List<Object> parameters) {
		List<String> cols = new ArrayList<String>();
		try {
			for (String field : qf.fields) {
				Field f = Util.getField(clazz, field);
				
				Class<?> cl = f.getType();
				// if a number or date, doesn't try to coalesce
				if(Number.class.isAssignableFrom(cl) 
						||
					Date.class.isAssignableFrom(cl)){
					String[] columns = ClassInfo.getColumnNames(f, info.tableName);
					for (String col : columns) {
						cols.add(col);
					}
				}
				// if is model, gets the key type and does the same as herebefore
				else if(ClassInfo.isModel(cl)) {
					ClassInfo ci = ClassInfo.getClassInfo(cl);
					if(ci.keys.size()==1){
						Field key = ci.keys.get(0);
						if(Number.class.isAssignableFrom(key.getType()) 
								||
							Date.class.isAssignableFrom(key.getType())){
							cols.add(f.getName());
						}else {
							cols.add("coalesce("+f.getName()+", '')");
						}
					}
					else {
						for (Field key : ci.keys) {
							String[] columns = ClassInfo.getColumnNamesWithPrefix(key, f.getName()+"_");
							if(Number.class.isAssignableFrom(key.getType()) 
									||
								Date.class.isAssignableFrom(key.getType())){
								for (String col : columns) {
									cols.add(col);
								}
							}else {
								for (String col : columns) {
									cols.add("coalesce("+col+", '')");
								}
							}
						}
					}
					
				}
				else {
					String[] columns = ClassInfo.getColumnNames(f, info.tableName);
					for (String col : columns) {
						cols.add("coalesce("+col+", '')");
					}
				}
			}
			QueryOption opt = qf.option;
			if(opt != null){
				// only manages QueryOptionJdbcSearch
				if(QueryOptionPostgresqlSearch.class.isAssignableFrom(opt.getClass())){
					String lang = ((QueryOptionPostgresqlSearch)opt).language;
					if(lang != null && !"".equals(lang) ){
						sql.append("to_tsvector('"+lang+"', "+Util.join(cols, " || ' ' || ")+") @@ to_tsquery(?)");
					}
					else {
						sql.append("to_tsvector('english', "+Util.join(cols, " || ' ' || ")+") @@ to_tsquery(?)");
					}
				}else{
				}
			}else {
				sql.append("to_tsvector('english', "+Util.join(cols, " || ' ' || ")+") @@ to_tsquery(?)");
			}
			parameters.add(qf.match);
		}catch(Exception e){
			throw new SienaException(e);
		}
	}

	@Override
	public void save(Object obj) {		
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		List<String> keyNames = new ArrayList<String>();
		for (Field field : classInfo.keys) {
			keyNames.add(field.getName());
		}
		
		PreparedStatement ps = null;
		try {
			Field idField = classInfo.info.getIdField();
			Object idVal = Util.readField(obj, idField);

			if (idVal == null) {
				insert(obj);
			} else {
				// !!! insert or update pour postgres : the less worst solution I found!!!!
				// INSERT INTO myTable (myKey) SELECT myKeyValue WHERE myKeyValue NOT IN (SELECT myKey FROM myTable);
				// UPDATE myTable SET myUpdateCol = myUpdateColValue WHERE myKey = myKeyValue;
				ps = getConnection().prepareStatement(
						"INSERT INTO "+ classInfo.tableName + " (" + Util.join(keyNames, ",") + ") " 
						+ "SELECT ? WHERE ? NOT IN (SELECT "+ Util.join(keyNames, ",")  
						+ " FROM "+ classInfo.tableName + ");"
						+ classInfo.updateSQL);
				int i = 1;
				i = addParameters(obj, classInfo.keys, ps, i);
				i = addParameters(obj, classInfo.keys, ps, i);
				i = addParameters(obj, classInfo.updateFields, ps, i);
				addParameters(obj, classInfo.keys, ps, i);
				ps.executeUpdate();				
			}
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}
	


	@Override
	public int save(Object... objects) {
		Map<JdbcClassInfo, List<Object>> generatedObjMap = new HashMap<JdbcClassInfo, List<Object>>();
		Map<JdbcClassInfo, List<Object>> objMap = new HashMap<JdbcClassInfo, List<Object>>();
		PreparedStatement ps = null;
		
		for(Object obj:objects){
			JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());
			Field idField = classInfo.info.getIdField();
			Object idVal = Util.readField(obj, idField);
			
			if(idVal == null && !classInfo.generatedKeys.isEmpty()){
				if(!generatedObjMap.containsKey(classInfo)){
					List<Object> l = new ArrayList<Object>();
					l.add(obj);
					generatedObjMap.put(classInfo, l);
				}else{
					generatedObjMap.get(classInfo).add(obj);
				}
			} else {
				if(!objMap.containsKey(classInfo)){
					List<Object> l = new ArrayList<Object>();
					l.add(obj);
					objMap.put(classInfo, l);
				}else{
					objMap.get(classInfo).add(obj);
				}
			}
		}
		
		int total = 0;
		try {
			// these are the insertion with generated keys
			for(JdbcClassInfo classInfo: generatedObjMap.keySet()){
				total += insert(generatedObjMap.get(classInfo));
			}
			
			// these are the insertion or update without generated keys
			// can't use batch in Postgres with generated keys... known bug
			// http://postgresql.1045698.n5.nabble.com/PreparedStatement-batch-statement-impossible-td3406927.html
			for(JdbcClassInfo classInfo: objMap.keySet()){
				List<String> keyNames = new ArrayList<String>();
				for (Field field : classInfo.keys) {
					keyNames.add(field.getName());
				}
				
				// !!! insert or update pour postgres : the less worst solution I found!!!!
				// INSERT INTO myTable (myKey) SELECT myKeyValue WHERE myKeyValue NOT IN (SELECT myKey FROM myTable);
				// UPDATE myTable SET myUpdateCol = myUpdateColValue WHERE myKey = myKeyValue;
				ps = getConnection().prepareStatement(
						"INSERT INTO "+ classInfo.tableName + " (" + Util.join(keyNames, ",") + ") " 
						+ "SELECT ? WHERE ? NOT IN (SELECT "+ Util.join(keyNames, ",")  
						+ " FROM "+ classInfo.tableName + ");"
						+ classInfo.updateSQL);
			
				for(Object obj: objMap.get(classInfo)){				
					int i = 1;
					i = addParameters(obj, classInfo.keys, ps, i);
					i = addParameters(obj, classInfo.keys, ps, i);
					i = addParameters(obj, classInfo.updateFields, ps, i);
					addParameters(obj, classInfo.keys, ps, i);
					ps.executeUpdate();
					total++;
				}
			}
			
			return total;			
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	@Override
	public int save(Iterable<?> objects) {
		Map<JdbcClassInfo, List<Object>> generatedObjMap = new HashMap<JdbcClassInfo, List<Object>>();
		Map<JdbcClassInfo, List<Object>> objMap = new HashMap<JdbcClassInfo, List<Object>>();
		PreparedStatement ps = null;
		
		for(Object obj:objects){
			JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());
			Field idField = classInfo.info.getIdField();
			Object idVal = Util.readField(obj, idField);
			
			if(idVal == null && !classInfo.generatedKeys.isEmpty()){
				if(!generatedObjMap.containsKey(classInfo)){
					List<Object> l = new ArrayList<Object>();
					l.add(obj);
					generatedObjMap.put(classInfo, l);
				}else{
					generatedObjMap.get(classInfo).add(obj);
				}
			} else {
				if(!objMap.containsKey(classInfo)){
					List<Object> l = new ArrayList<Object>();
					l.add(obj);
					objMap.put(classInfo, l);
				}else{
					objMap.get(classInfo).add(obj);
				}
			}
		}
		
		int total = 0;
		try {
			// these are the insertion with generated keys
			for(JdbcClassInfo classInfo: generatedObjMap.keySet()){
				total += insert(generatedObjMap.get(classInfo));
			}
			
			// these are the insertion or update without generated keys
			// can't use batch in Postgres with generated keys... known bug
			// http://postgresql.1045698.n5.nabble.com/PreparedStatement-batch-statement-impossible-td3406927.html
			for(JdbcClassInfo classInfo: objMap.keySet()){
				List<String> keyNames = new ArrayList<String>();
				for (Field field : classInfo.keys) {
					keyNames.add(field.getName());
				}
				
				// !!! insert or update pour postgres : the less worst solution I found!!!!
				// INSERT INTO myTable (myKey) SELECT myKeyValue WHERE myKeyValue NOT IN (SELECT myKey FROM myTable);
				// UPDATE myTable SET myUpdateCol = myUpdateColValue WHERE myKey = myKeyValue;
				ps = getConnection().prepareStatement(
						"INSERT INTO "+ classInfo.tableName + " (" + Util.join(keyNames, ",") + ") " 
						+ "SELECT ? WHERE ? NOT IN (SELECT "+ Util.join(keyNames, ",")  
						+ " FROM "+ classInfo.tableName + ");"
						+ classInfo.updateSQL);
			
				for(Object obj: objMap.get(classInfo)){				
					int i = 1;
					i = addParameters(obj, classInfo.keys, ps, i);
					i = addParameters(obj, classInfo.keys, ps, i);
					i = addParameters(obj, classInfo.updateFields, ps, i);
					addParameters(obj, classInfo.keys, ps, i);
					ps.executeUpdate();
					total++;
				}
			}
			
			return total;			
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}
	
	
}
