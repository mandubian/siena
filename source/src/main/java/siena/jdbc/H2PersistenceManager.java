package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import siena.jdbc.JdbcPersistenceManager.JdbcClassInfo;

public class H2PersistenceManager extends JdbcPersistenceManager {
	private static final String DB = "H2";
	
	public H2PersistenceManager() {
		
	}
	
	public H2PersistenceManager(ConnectionManager connectionManager, Class<?> listener) {
		super(connectionManager, listener);
	}
	
	/*
	 * Overrides the batch insert since H2 getGeneratedKeys doesn't return all generated identities but only the last one.
	 * This is a known limitation: http://markmail.org/message/hsgzgktbj4srz657
	 * It is planned in H2 v1.4 roadmap: http://www.h2database.com/html/roadmap.html
	 * Meanwhile, no batch insert is possible
	 *  
	 * (non-Javadoc)
	 * @see siena.jdbc.JdbcPersistenceManager#insertBatchWithAutoIncrementKey(siena.jdbc.JdbcPersistenceManager.JdbcClassInfo, java.util.Map)
	 */
	@Override
	protected int insertBatchWithAutoIncrementKey(JdbcClassInfo classInfo, Map<JdbcClassInfo, List<Object>> objMap) throws SQLException, IllegalAccessException {
		PreparedStatement ps = null;
		ps = getConnection().prepareStatement(classInfo.insertSQL,
				Statement.RETURN_GENERATED_KEYS);
		
		int res = 0;
		for(Object obj: objMap.get(classInfo)){
			for (Field field : classInfo.keys) {
				Id id = field.getAnnotation(Id.class);
				if (id.value() == Generator.UUID) {
					field.set(obj, UUID.randomUUID().toString());
				}
			}
			// TODO: implement primary key generation: SEQUENCE
			addParameters(obj, classInfo.insertFields, ps, 1);
			ps.executeUpdate();
			
			if(!classInfo.generatedKeys.isEmpty()){
				ResultSet gk = ps.getGeneratedKeys();
				int i;
				while(gk.next()) {
					i=1;
					for (Field field : classInfo.generatedKeys) {
						field.setAccessible(true);
						JdbcMappingUtils.setFromObject(obj, field, gk.getObject(i++));
					}
				}
			}
			
			res++;
		}
		
		return res;
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
				// in H2 "on duplicate" is not supported but MERGE is
				// merge into employees (id, first_name, last_name) values(1, 'test2', 'test2');
				List<String> allColumns = new ArrayList<String>();
				JdbcClassInfo.calculateColumns(classInfo.allFields, allColumns, null, "");
				String[] is = new String[allColumns.size()];
				Arrays.fill(is, "?");
				
				ps = getConnection().prepareStatement(
						"MERGE INTO "+ classInfo.tableName + " (" + Util.join(allColumns, ",") + ") " 
						+ "VALUES(" + Util.join(Arrays.asList(is), ",") + ")"  
				);
				
				int i = 1;
				i = addParameters(obj, classInfo.allFields, ps, i);
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
