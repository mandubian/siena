package siena.hbase;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import siena.BaseQueryData;
import siena.ClassInfo;
import siena.PersistenceManager;
import siena.Query;
import siena.QueryAggregated;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.SienaException;
import siena.core.ListQuery;
import siena.core.async.PersistenceManagerAsync;
import siena.core.async.QueryAsync;
import siena.core.batch.Batch;
import siena.core.options.QueryOption;

public class HBasePersistenceManager implements PersistenceManager {
	
	private Configuration config;
	
	public HBasePersistenceManager() {
		config = HBaseConfiguration.create();
	}

	public void beginTransaction(int isolationLevel) {
		// TODO Auto-generated method stub
		
	}
	
	public void beginTransaction() {
		// TODO Auto-generated method stub
		
	}

	public void closeConnection() {
		// TODO Auto-generated method stub
		
	}

	public void commitTransaction() {
		// TODO Auto-generated method stub
		
	}

	public <T> Query<T> createQuery(Class<T> clazz) {
		return new HBaseQuery<T>(clazz);
	}

	public void delete(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		try {
			HTable table = new HTable(config, info.tableName);
			Field id = ClassInfo.getIdField(clazz);
			id.setAccessible(true);
			Delete d = new Delete(Bytes.toBytes(id.get(obj).toString()));
			table.delete(d);
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	public void get(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		try {
			HTable table = new HTable(config, info.tableName);
			Field id = ClassInfo.getIdField(clazz);
			id.setAccessible(true);
			
			Get g = new Get(Bytes.toBytes(id.get(obj).toString()));			
			Result rowResult = table.get(g);
			if(rowResult.isEmpty()) throw new SienaException("No such object");
			mapObject(clazz, obj, rowResult);
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	public void init(Properties p) {
	}

	public void insert(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		try {
			HTable table = new HTable(config, info.tableName);
			Field id = ClassInfo.getIdField(clazz);
			id.setAccessible(true);
			
			Put p = new Put(Bytes.toBytes(id.get(obj).toString()));
			
			List<Field> fields = info.insertFields;
			for (Field field : fields) {
				p.add(Bytes.toBytes("string"), 
						Bytes.toBytes(ClassInfo.getColumnNames(field)[0]), 
						Bytes.toBytes(field.get(obj).toString()));
			}
			table.put(p);
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}

	public void rollbackTransaction() {
		// TODO Auto-generated method stub
		
	}

	public void update(Object obj) {
		insert(obj);
	}
	
	private <T> void mapObject(Class<T> clazz, Object obj, Result result) {
		try {
			String id = Bytes.toString(result.getRow());
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			for (Field field : info.insertFields) {
				String column = "string:"+ClassInfo.getColumnNames(field)[0];
				String value = 
					Bytes.toString(
							result.getValue(
									Bytes.toBytes("string"), 
									Bytes.toBytes(ClassInfo.getColumnNames(field)[0])));
				field.setAccessible(true);
				field.set(obj, value);
			}
			ClassInfo.getIdField(clazz).set(obj, id);
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}
	
	private <T> T mapObject(Class<T> clazz, Result rowResult) {
		try {
			T obj = clazz.newInstance();
			mapObject(clazz, obj, rowResult);
			return obj;
		} catch(SienaException e) {
			throw e;
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}
	
	class HBaseQuery<T> implements Query<T> {
		
		private Class<T> clazz;
		private ClassInfo info;
		
		public HBaseQuery(Class<T> clazz) {
			this.clazz = clazz;
			info = ClassInfo.getClassInfo(clazz);
		}

		@Override
		public int count() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int count(int limit) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int count(int limit, Object offset) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<T> fetch() {
			ResultScanner scanner = null;
			try {
				HTable table = new HTable(config, info.tableName);
				List<Field> fields = info.insertFields;
				List<String> names = new ArrayList<String>();
				
				Scan s = new Scan();
				for (Field field : fields) {
					s.addColumn(
							Bytes.toBytes("string"), 
							Bytes.toBytes(ClassInfo.getColumnNames(field)[0]));
				}
								
				scanner = table.getScanner(s);
				List<T> results = new ArrayList<T>();
				for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {
					// print out the row we found and the columns we were looking for
					results.add(mapObject(clazz, rr));
				}

				return results;
			} catch(SienaException e) {
				throw e;
			} catch(Exception e) {
				throw new SienaException(e);
			} finally {
				scanner.close();
			}
		}


		@Override
		public List<T> fetch(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<T> fetch(int limit, Object offset) {
			// TODO Auto-generated method stub		@Override

			return null;
		}

		@Override
		public Query<T> filter(String fieldName, Object value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T get() {
			List<T> list = fetch();
			if(list.isEmpty()) return null;
			return list.get(0);
		}

		@Override
		public Iterable<T> iter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<T> iter(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<T> iter(int limit, Object offset) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> order(String fieldName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> search(String match, boolean inBooleanMode,
				String index) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public HBaseQuery<T> clone() {
			return null;
		}
		
		@Override
		public Object nextOffset() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int delete() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<T> fetchKeys() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<T> fetchKeys(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<T> fetchKeys(int limit, Object offset) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<QueryFilter> getFilters() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<QueryOrder> getOrders() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<QueryFilterSearch> getSearches() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setNextOffset(Object nextOffset) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Class<T> getQueriedClass() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<QueryJoin> getJoins() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> join(String field, String... sortFields) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> paginate(int size) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> customize(QueryOption... options) {
			// TODO Auto-generated method stub
			return null;
		}



		@Override
		public Map<Integer, QueryOption> options() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> search(String match, String... fields) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> search(String match, QueryOption opt, String... fields) {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public QueryOption option(int option) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> stateful() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> release() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int update(Map<String, ?> fieldValues) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Query<T> nextPage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> previousPage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> stateless() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> resetData() {
			// TODO Auto-generated method stub
			return null;
		}

		

		@Override
		public String dump(QueryOption... options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void dump(OutputStream os, QueryOption... options) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Query<T> restore(String dump, QueryOption... options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> restore(InputStream dump, QueryOption... options) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryAsync<T> async() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PersistenceManager getPersistenceManager() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> limit(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> offset(Object offset) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<T> iterPerPage(int limit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T getByKey(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> copy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<QueryAggregated> getAggregatees() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Query<T> aggregated(Object aggregator, String fieldName) {
			// TODO Auto-generated method stub
			return null;
		}



	}

	@Override
	public <T> T get(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetch(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int count(Query<T> query) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int count(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int count(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int delete(Query<T> query) {
		// TODO Auto-generated method stub
		return 0;
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
	public String[] supportedOperators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void release(Query<T> query) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public <T> Query<T> createQuery(BaseQueryData<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Batch<T> createBatch(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int get(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int get(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int update(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int update(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> void nextPage(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void previousPage(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> PersistenceManagerAsync async() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int insert(Object... objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insert(Iterable<?> objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Iterable<?> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public <T> void paginate(Query<T> query) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public <T> Iterable<T> iterPerPage(Query<T> query, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getByKey(Class<T> clazz, Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int save(Object... objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int save(Iterable<?> objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> ListQuery<T> createListQuery(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}



}
