package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import siena.ClassInfo;
import siena.Generator;
import siena.Id;
import siena.Query;
import siena.QueryFilterSearch;
import siena.SienaException;
import siena.Util;
import siena.jdbc.h2.FullText;

public class H2PersistenceManager extends JdbcPersistenceManager {
	private static final String DB = "H2";
	
	private String dbMode = "h2";
	
	protected static Map<String, Boolean> tableIndexMap = new ConcurrentHashMap<String, Boolean>();

	public H2PersistenceManager() {
		
	}
	
	public H2PersistenceManager(ConnectionManager connectionManager, Class<?> listener) {
		super(connectionManager, listener);
	}
	
	public H2PersistenceManager(ConnectionManager connectionManager, Class<?> listener, String dbMode) {
		super(connectionManager, listener);
		this.dbMode = dbMode; 
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
	
	protected <T> List<T> doSearch(Query<T> query, int limit, int offset){
		// TODO this is a very raw impl: need some work certainly 
		try {
			Connection conn = this.getConnection();
			ClassInfo ci = ClassInfo.getClassInfo(query.getQueriedClass());
			// doesn't index a table that has already been indexed
			if(!tableIndexMap.containsKey(ci.tableName)){
				List<String> colList = ci.getUpdateFieldsColumnNames();
				String cols = null;
				if(!colList.isEmpty()){
					cols = "";
					// removes auto generated IDs from index
					int sz = colList.size();
					for (int i=0; i<sz; i++) {
						if("h2".equals(dbMode)) cols+=colList.get(i).toUpperCase();
						// !!! mysql mode means case INsensitive to lowercase !!!!
						else if("mysql".equals(dbMode)) cols+=colList.get(i).toLowerCase();
						else cols+=colList.get(i).toUpperCase();
						
						if(i<sz-1) cols += ",";
					}
				}
				// creates the index
				FullText.createIndex(conn, "PUBLIC", ci.tableName.toUpperCase(), cols);
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
			
			ResultSet rs = FullText.searchData(conn, searchString, limit, offset);
			List<T> res = new ArrayList<T>();
			while(rs.next()) {
				//String queryStr = rs.getString("QUERY");
				//String score = rs.getString("SCORE");
				//Array columns = rs.getArray("COLUMNS");
				Object[] keys = (Object[])rs.getArray("KEYS").getArray();
				if(res == null) res = this.getByKeys(query.getQueriedClass(), keys);
				else res.addAll(this.getByKeys(query.getQueriedClass(), keys));
			}
			return res;
		} catch (SQLException e) {
			throw new SienaException(e);
		}
	}
	
	protected <T> List<T> doSearchKeys(Query<T> query, int limit, int offset){
		// TODO this is a very raw impl: need some work certainly 
		try {
			Connection conn = this.getConnection();
			ClassInfo ci = ClassInfo.getClassInfo(query.getQueriedClass());
			// doesn't index a table that has already been indexed
			if(!tableIndexMap.containsKey(ci.tableName)){
				List<String> colList = ci.getUpdateFieldsColumnNames();
				String cols = null;
				if(!colList.isEmpty()){
					cols = "";
					// removes auto generated IDs from index
					int sz = colList.size();
					for (int i=0; i<sz; i++) {
						if("h2".equals(dbMode)) cols+=colList.get(i).toUpperCase();
						// !!! mysql mode means case INsensitive to lowercase !!!!
						else if("mysql".equals(dbMode)) cols+=colList.get(i).toLowerCase();
						else cols+=colList.get(i).toUpperCase();
						
						if(i<sz-1) cols += ",";
					}
				}
				// creates the index
				FullText.createIndex(conn, "PUBLIC", ci.tableName.toUpperCase(), cols);
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
			
			ResultSet rs = FullText.searchData(conn, searchString, limit, offset);
			List<T> res = new ArrayList<T>();
			Class<T> clazz = query.getQueriedClass();
			while(rs.next()) {
				//String queryStr = rs.getString("QUERY");
				//String score = rs.getString("SCORE");
				//Array columns = rs.getArray("COLUMNS");
				Object[] keys = (Object[])rs.getArray("KEYS").getArray();
				for(Object key: keys){
					T obj = Util.createObjectInstance(clazz);
					for (Field field : JdbcClassInfo.getClassInfo(clazz).keys) {
						JdbcMappingUtils.setFromObject(obj, field, key);
					}
					
					res.add(obj);
				}				
			}
			return res;
		} catch (SQLException e) {
			throw new SienaException(e);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	protected <T> int doSearchCount(Query<T> query){
		// TODO this is a very raw impl: need some work certainly 
		try {
			Connection conn = this.getConnection();
			ClassInfo ci = ClassInfo.getClassInfo(query.getQueriedClass());
			// doesn't index a table that has already been indexed
			if(!tableIndexMap.containsKey(ci.tableName)){
				List<String> colList = ci.getUpdateFieldsColumnNames();
				String cols = null;
				if(!colList.isEmpty()){
					cols = "";
					// removes auto generated IDs from index
					int sz = colList.size();
					for (int i=0; i<sz; i++) {
						if("h2".equals(dbMode)) cols+=colList.get(i).toUpperCase();
						// !!! mysql mode means case INsensitive to lowercase !!!!
						else if("mysql".equals(dbMode)) cols+=colList.get(i).toLowerCase();
						else cols+=colList.get(i).toUpperCase();
						
						if(i<sz-1) cols += ",";
					}
				}
				// creates the index
				FullText.createIndex(conn, "PUBLIC", ci.tableName.toUpperCase(), cols);
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
			int count = 0;
			while(rs.next()) {
				//String queryStr = rs.getString("QUERY");
				//String score = rs.getString("SCORE");
				//Array columns = rs.getArray("COLUMNS");
				Object[] keys = (Object[])rs.getArray("KEYS").getArray();
				count += keys.length;
			}
			return count;
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
			return doSearch(query, 0, 0);
		}
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		if(query.getSearches().isEmpty()){
			return super.fetch(query, limit);
		}
		else {
			return doSearch(query, limit, 0);
		}
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		if(query.getSearches().isEmpty()){
			return super.fetch(query, limit, (Integer)offset);
		}
		else {
			return doSearch(query, limit, (Integer)offset);
		}
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		if(query.getSearches().isEmpty()){
			return super.fetchKeys(query);
		}
		else {
			return doSearchKeys(query, 0, 0);
		}
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		if(query.getSearches().isEmpty()){
			return super.fetchKeys(query, limit);
		}
		else {
			return doSearchKeys(query, limit, 0);
		}
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		if(query.getSearches().isEmpty()){
			return super.fetchKeys(query, limit, (Integer)offset);
		}
		else {
			return doSearchKeys(query, limit, (Integer)offset);
		}
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		// TODO Auto-generated method stub
		return super.iter(query);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return super.iter(query, limit);
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return super.iter(query, limit, offset);
	}

	@Override
	public <T> int count(Query<T> query) {
		if(query.getSearches().isEmpty()){
			return super.count(query);
		}
		else {
			return doSearchCount(query);
		}
	}
	
	
}
