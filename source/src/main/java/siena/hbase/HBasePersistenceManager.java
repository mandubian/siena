package siena.hbase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.util.Bytes;

import siena.ClassInfo;
import siena.PersistenceManager;
import siena.Query;
import siena.QueryFilter;
import siena.QueryOrder;
import siena.QuerySearch;
import siena.SienaException;

public class HBasePersistenceManager implements PersistenceManager {
	
	private HBaseConfiguration config;
	
	public HBasePersistenceManager() {
		config = new HBaseConfiguration();
	}

	public void beginTransaction(int isolationLevel) {
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
			table.deleteAll(id.get(obj).toString());
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
			
			RowResult rowResult = table.getRow(id.get(obj).toString());
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
			
			BatchUpdate batchUpdate = new BatchUpdate(id.get(obj).toString());
			
			List<Field> fields = info.insertFields;
			for (Field field : fields) {
				batchUpdate.put("string:"+ClassInfo.getColumnNames(field)[0], 
						Bytes.toBytes(field.get(obj).toString()));
			}
			table.commit(batchUpdate);
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
	
	private <T> void mapObject(Class<T> clazz, Object obj, RowResult rowResult) {
		try {
			String id = Bytes.toString(rowResult.getRow());
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			for (Field field : info.insertFields) {
				String column = "string:"+ClassInfo.getColumnNames(field)[0];
				String value = Bytes.toString(rowResult.get(column).getValue());
				field.setAccessible(true);
				field.set(obj, value);
			}
			ClassInfo.getIdField(clazz).set(obj, id);
		} catch(Exception e) {
			throw new SienaException(e);
		}
	}
	
	private <T> T mapObject(Class<T> clazz, RowResult rowResult) {
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
			Scanner scanner = null;
			try {
				HTable table = new HTable(config, info.tableName);
				List<Field> fields = info.insertFields;
				List<String> names = new ArrayList<String>();
				for (Field field : fields) {
					names.add("string:"+ClassInfo.getColumnNames(field)[0]);
				}
				
				String[] namesArray = names.toArray(new String[names.size()]);
				
				scanner = table.getScanner(namesArray);
				List<T> results = new ArrayList<T>();
				for (RowResult rowResult : scanner) {
					results.add(mapObject(clazz, rowResult));
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
			// TODO Auto-generated method stub
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
		public Iterable<T> iter(String field, int max) {
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
		public List<QuerySearch> getSearches() {
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
	public String[] supportedOperators() {
		// TODO Auto-generated method stub
		return null;
	}

}
