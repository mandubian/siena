package siena.base.test;

import java.util.List;

import siena.PersistenceManager;
import siena.gae.GaePersistenceManager;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeModelTest extends BaseModelTest{
	private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes)
			throws Exception {
		GaePersistenceManager pm = new GaePersistenceManager();
		pm.init(null);
		return pm;
	}
	
	@Override
    public void setUp() throws Exception {
    	helper.setUp();
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        helper.tearDown();
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
		super.testFetchAsync();
	}

	@Override
	public void testFetchAsync2Models() {
		// TODO Auto-generated method stub
		super.testFetchAsync2Models();
	}

	@Override
	public void testFetchAsyncAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
		super.testFetchAsyncAndGetAndResetAsync2Models();
	}

	@Override
	public void testFetchPaginateSyncAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
		super.testFetchPaginateSyncAndGetAndResetAsync2Models();
	}

	@Override
	public void testFetchAsyncAndGetAndResetSync2Models() {
		// TODO Auto-generated method stub
		super.testFetchAsyncAndGetAndResetSync2Models();
	}

	@Override
	public void testFetchPaginateAsyncAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
		super.testFetchPaginateAsyncAndGetAndResetAsync2Models();
	}

	@Override
	public void testFetchPaginateAsyncStatefulAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
		super.testFetchPaginateAsyncStatefulAndGetAndResetAsync2Models();
	}

	@Override
	public void testFetchPaginateStatefulAsyncAndGetAndResetAsync2Models() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulAsyncAndGetAndResetAsync2Models();
	}

	@Override
	public void testFetchPaginateStatefulAsyncAndGetAndResetSync2Models() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulAsyncAndGetAndResetSync2Models();
	}

	@Override
	public void testFetchPaginateAsync2Sync2AsyncAndGetAndResetSync2Models() {
		// TODO Auto-generated method stub
		super.testFetchPaginateAsync2Sync2AsyncAndGetAndResetSync2Models();
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
		super.testFetchPaginateStatefulAsyncUpdateData();
	}

	@Override
	public void testFetchPaginateStatelessAsyncUpdateData() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessAsyncUpdateData();
	}

	@Override
	public void testFetchPaginateStatefulRealAsyncUpdateData() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulRealAsyncUpdateData();
	}

	@Override
	public void testInsert() {
		// TODO Auto-generated method stub
		super.testInsert();
	}

	@Override
	public void testInsertManyAsync() {
		// TODO Auto-generated method stub
		super.testInsertManyAsync();
	}

	@Override
	public void testInsertMany() {
		// TODO Auto-generated method stub
		super.testInsertMany();
	}

	@Override
	public void testInsertAsync() {
		// TODO Auto-generated method stub
		super.testInsertAsync();
	}

	@Override
	public void testInsertAutoQuery() {
		// TODO Auto-generated method stub
		super.testInsertAutoQuery();
	}



	@Override
	public void testInsertAutoQueryAsyncFetchSync() {
		// TODO Auto-generated method stub
		super.testInsertAutoQueryAsyncFetchSync();
	}

	@Override
	public void testInsertAutoQueryAsyncFetchAsync() {
		// TODO Auto-generated method stub
		super.testInsertAutoQueryAsyncFetchAsync();
	}

	@Override
	public void testInsertAutoQueryMany() {
		// TODO Auto-generated method stub
		super.testInsertAutoQueryMany();
	}


	@Override
	public void testInsertBatchAsync() {
		// TODO Auto-generated method stub
		super.testInsertBatchAsync();
	}

}
