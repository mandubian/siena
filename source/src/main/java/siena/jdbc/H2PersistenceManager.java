package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.h2.fulltext.FullText;

import siena.ClassInfo;
import siena.Generator;
import siena.Id;
import siena.Query;
import siena.QueryFilterSearch;
import siena.SienaException;
import siena.Util;
import siena.core.options.QueryOption;
import siena.jdbc.JdbcPersistenceManager.JdbcClassInfo;

public class H2PersistenceManager extends JdbcPersistenceManager {
	private static final String DB = "H2";
	
	protected static Map<String, Boolean> tableIndexMap = new ConcurrentHashMap<String, Boolean>();

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
			// these are the insertions with generated keys
			for(JdbcClassInfo classInfo: generatedObjMap.keySet()){
				total += insert(generatedObjMap.get(classInfo));
			}
			
			// these are the insertions or updates without generated keys
			// can't use batch in Postgres with generated keys... known bug
			// http://postgresql.1045698.n5.nabble.com/PreparedStatement-batch-statement-impossible-td3406927.html
			for(JdbcClassInfo classInfo: objMap.keySet()){
				List<String> keyNames = new ArrayList<String>();
				for (Field field : classInfo.keys) {
					keyNames.add(field.getName());
				}
				
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
				
				for(Object obj: objMap.get(classInfo)){				
					int i = 1;
					i = addParameters(obj, classInfo.allFields, ps, i);
					ps.addBatch();
				}
				
				int[] res = ps.executeBatch();
				
				total+=res.length;
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
			// these are the insertions with generated keys
			for(JdbcClassInfo classInfo: generatedObjMap.keySet()){
				total += insert(generatedObjMap.get(classInfo));
			}
			
			// these are the insertions or updates without generated keys
			// can't use batch in Postgres with generated keys... known bug
			// http://postgresql.1045698.n5.nabble.com/PreparedStatement-batch-statement-impossible-td3406927.html
			for(JdbcClassInfo classInfo: objMap.keySet()){
				List<String> keyNames = new ArrayList<String>();
				for (Field field : classInfo.keys) {
					keyNames.add(field.getName());
				}
				
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
				
				for(Object obj: objMap.get(classInfo)){				
					int i = 1;
					i = addParameters(obj, classInfo.allFields, ps, i);
					ps.addBatch();
				}
				
				int[] res = ps.executeBatch();
				
				total+=res.length;
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
	
	private boolean isSearchInit = false; 
	
	@Override
	public void init(Properties p) {
		super.init(p);
		// initializes the search mechanism in H2
		try {
			FullText.init(this.getConnection());
		} catch (SQLException e) {
			throw new SienaException(e);
		}
	}
	public <T> List<T> doSearch(Query<T> query){
		// TODO this is a very raw impl: need some work certainly 
		try {
			Connection conn = this.getConnection();
			ClassInfo ci = ClassInfo.getClassInfo(query.getQueriedClass());
			// doesn't index a table that has already been indexed
			if(!tableIndexMap.containsKey(ci.tableName)){
				String cols = null;
				if(!ci.updateFields.isEmpty()){
					cols = "";
					// removes auto generated IDs from index
					int sz = ci.updateFields.size();
					for (int i=0; i<sz; i++) {
						String str = ci.updateFields.get(i).getName().toUpperCase();
						cols += str;
						if(i<sz-1) cols += ",";
					}
				}
				// creates the index
				FullText.createIndex(conn, "PUBLIC", "discoveries_search".toUpperCase(), cols);
				tableIndexMap.put(ci.tableName, true);
			}
			
			String searchString = "";
			Iterator<QueryFilterSearch> it = query.getSearches().iterator();
			boolean first = true;
			while(it.hasNext()){
				if(!first){ 
					searchString += " ";
				}else {
					first = false;
				}
				searchString += it.next().match;				
			}
			
			ResultSet rs = FullText.searchData(conn, searchString, 0, 0);
			List<T> res = new ArrayList<T>();
			while(rs.next()) {
				//String queryStr = rs.getString("QUERY");
				//String score = rs.getString("SCORE");
				Array columns = rs.getArray("COLUMNS");
				Object[] keys = (Object[])rs.getArray("KEYS").getArray();
				if(res == null) res = this.getByKeys(query.getQueriedClass(), keys);
				else res.addAll(this.getByKeys(query.getQueriedClass(), keys));
			}
			return res;
		} catch (SQLException e) {
			throw new SienaException(e);
		}
	}
	
	@Override
	public <T> List<T> fetch(Query<T> query) {
		if(query.getSearches().isEmpty()){
			return super.fetch(query);
		}
		else {
			return doSearch(query);
		}
	}
}
