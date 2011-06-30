package siena.base.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

import junit.framework.TestResult;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;

import siena.PersistenceManager;
import siena.Query;
import siena.base.test.model.Discovery4Search;
import siena.jdbc.JdbcPersistenceManager;
import siena.jdbc.ddl.DdlGenerator;

public class JdbcModelTest extends BaseModelTest {
	private static JdbcPersistenceManager pm;
	
	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception {
		if(pm == null){
			Properties p = new Properties();
			
			String driver   = "com.mysql.jdbc.Driver";
			String username = "siena";
			String password = "siena";
			String url      = "jdbc:mysql://localhost/siena";
			
			p.setProperty("driver",   driver);
			p.setProperty("user",     username);
			p.setProperty("password", password);
			p.setProperty("url",      url);
	
			Class.forName(driver);
			BasicDataSource dataSource = new BasicDataSource();
			dataSource = new BasicDataSource();
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
			dataSource.setMaxWait(2000); // 2 seconds max for wait a connection.
			
			DdlGenerator generator = new DdlGenerator();
			for (Class<?> clazz : classes) {
				generator.addTable(clazz);
			}
	
			// get the Database model
			Database database = generator.getDatabase();
	
			Platform platform = PlatformFactory.createNewPlatformInstance("mysql");
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, username, password);
			
			System.out.println(platform.getAlterTablesSql(connection, database));
			
			// this will perform the database changes
			platform.alterTables(connection, database, true);
	
			connection.close();
			
			pm = new JdbcPersistenceManager();
			pm.init(p);
		}
		
		return pm;
	}

	@Override
	public void testGet() {
		// TODO Auto-generated method stub
		super.testGet();
	}

	@Override
	public void testFetch() {
		// TODO Auto-generated method stub
		super.testFetch();
	}

	@Override
	public void testFetchAsync() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchAsync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchAsyncAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateSyncAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchAsyncAndGetAndResetSync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateAsyncAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateAsyncStatefulAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateStatefulAsyncAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateStatefulAsyncAndGetAndResetSync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateAsync2Sync2AsyncAndGetAndResetSync2Models() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateStatefulUpdateData() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulUpdateData();
	}

	@Override
	public void testFetchPaginateStatelessUpdateData() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessUpdateData();
	}

	@Override
	public void testFetchPaginateStatefulAsyncUpdateData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateStatelessAsyncUpdateData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testFetchPaginateStatefulRealAsyncUpdateData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testInsert() {
		// TODO Auto-generated method stub
		super.testInsert();
	}

	@Override
	public void testInsertAsync() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testInsertMany() {
		// TODO Auto-generated method stub
		super.testInsertMany();
	}

	@Override
	public void testInsertManyAsync() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testInsertAutoQuery() {
		// TODO Auto-generated method stub
		super.testInsertAutoQuery();
	}

	@Override
	public void testInsertAutoQueryAsyncFetchSync() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testInsertAutoQueryAsyncFetchAsync() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testInsertAutoQueryAsyncFetchAsyncQueryAsync() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testInsertAutoQueryMany() {
		// TODO Auto-generated method stub
		super.testInsertAutoQueryMany();
	}

	@Override
	public void testInsertBatchAsync() {
		// TODO Auto-generated method stub
	}

	@Override
	public void testSimpleInheritance() {
		// TODO Auto-generated method stub
		super.testSimpleInheritance();
	}

	@Override
	public void testDoubleInheritance() {
		// TODO Auto-generated method stub
		super.testDoubleInheritance();
	}

	@Override
	public void testAbstractInheritance() {
		// TODO Auto-generated method stub
		super.testAbstractInheritance();
	}

	@Override
	public void testFilterInheritance() {
		// TODO Auto-generated method stub
		super.testFilterInheritance();
	}

	@Override
	public void testTransactionSave() {
		// TODO Auto-generated method stub
		super.testTransactionSave();
	}

	@Override
	public void testTransactionSaveFailure() {
		// TODO Auto-generated method stub
		super.testTransactionSaveFailure();
	}

	@Override
	public void testAggregate() {
		// TODO Auto-generated method stub
		//super.testAggregate();
	}

	@Override
	public void testAggregateUpdate() {
		// TODO Auto-generated method stub
		//super.testAggregateUpdate();
	}

	@Override
	public void testAggregateSave() {
		// TODO Auto-generated method stub
		//super.testAggregateSave();
	}

	@Override
	public void testAggregateDelete() {
		// TODO Auto-generated method stub
		//super.testAggregateDelete();
	}

	@Override
	public void testAggregateListQuerysFetch() {
		// TODO Auto-generated method stub
		//super.testAggregateListQuerysFetch();
	}

	@Override
	public void testAggregateListQuerysFetchLimit() {
		// TODO Auto-generated method stub
		//super.testAggregateListQuerysFetchLimit();
	}

	@Override
	public void testAggregateListQuerysFetchLimitOffset() {
		// TODO Auto-generated method stub
		//super.testAggregateListQuerysFetchLimitOffset();
	}

	@Override
	public void testAggregateListQuerysFetchKeys() {
		// TODO Auto-generated method stub
		//super.testAggregateListQuerysFetchKeys();
	}

	@Override
	public void testAggregateListQuerysFetchKeysLimit() {
		// TODO Auto-generated method stub
		//super.testAggregateListQuerysFetchKeysLimit();
	}

	@Override
	public void testAggregateListQuerysFetchKeysLimitOffset() {
		// TODO Auto-generated method stub
		//super.testAggregateListQuerysFetchKeysLimitOffset();
	}

	@Override
	public void testAggregateListQueryDelete() {
		// TODO Auto-generated method stub
		//super.testAggregateListQueryDelete();
	}

	@Override
	public void testAggregateListQueryGet() {
		// TODO Auto-generated method stub
		//super.testAggregateListQueryGet();
	}

	@Override
	public void testAggregateListQueryCount() {
		// TODO Auto-generated method stub
		//super.testAggregateListQueryCount();
	}

	@Override
	public void testAggregateListQueryFilter() {
		// TODO Auto-generated method stub
		//super.testAggregateListQueryFilter();
	}

	@Override
	public void testAggregateListQueryOrder() {
		// TODO Auto-generated method stub
		//super.testAggregateListQueryOrder();
	}
	
	
}
