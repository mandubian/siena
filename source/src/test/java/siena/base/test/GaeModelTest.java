package siena.base.test;

import java.util.List;

import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.SienaException;
import siena.base.test.model.AggregateChildModel;
import siena.base.test.model.AggregateParentModel;
import siena.base.test.model.TransactionAccountFrom;
import siena.base.test.model.TransactionAccountFromModel;
import siena.base.test.model.TransactionAccountTo;
import siena.base.test.model.TransactionAccountToModel;
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
		PersistenceManagerFactory.install(pm, AggregateChildModel.class);
		PersistenceManagerFactory.install(pm, AggregateParentModel.class);
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

	@Override
	public void testInsertAutoQueryAsyncFetchAsyncQueryAsync() {
		// TODO Auto-generated method stub
		super.testInsertAutoQueryAsyncFetchAsyncQueryAsync();
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
	public void testAggregate() {
		// TODO Auto-generated method stub
		super.testAggregate();
	}

	@Override
	public void testAggregateUpdate() {
		// TODO Auto-generated method stub
		super.testAggregateUpdate();
	}
	
	@Override
	public void testAggregateSave() {
		// TODO Auto-generated method stub
		super.testAggregateSave();
	}

	@Override
	public void testAggregateDelete() {
		// TODO Auto-generated method stub
		super.testAggregateDelete();
	}

	@Override
	public void testAggregateListQuerysFetch() {
		// TODO Auto-generated method stub
		super.testAggregateListQuerysFetch();
	}
	
	@Override
	public void testAggregateListQuerysFetchLimit() {
		// TODO Auto-generated method stub
		super.testAggregateListQuerysFetchLimit();
	}

	@Override
	public void testAggregateListQuerysFetchLimitOffset() {
		// TODO Auto-generated method stub
		super.testAggregateListQuerysFetchLimitOffset();
	}
	
	@Override
	public void testAggregateListQuerysFetchKeys() {
		// TODO Auto-generated method stub
		super.testAggregateListQuerysFetchKeys();
	}


	@Override
	public void testAggregateListQuerysFetchKeysLimit() {
		// TODO Auto-generated method stub
		super.testAggregateListQuerysFetchKeysLimit();
	}

	@Override
	public void testAggregateListQuerysFetchKeysLimitOffset() {
		// TODO Auto-generated method stub
		super.testAggregateListQuerysFetchKeysLimitOffset();
	}

	@Override
	public void testAggregateListQueryDelete() {
		// TODO Auto-generated method stub
		super.testAggregateListQueryDelete();
	}

	@Override
	public void testAggregateListQueryGet() {
		// TODO Auto-generated method stub
		super.testAggregateListQueryGet();
	}

	@Override
	public void testAggregateListQueryCount() {
		// TODO Auto-generated method stub
		super.testAggregateListQueryCount();
	}

	@Override
	public void testAggregateListQueryFilter() {
		// TODO Auto-generated method stub
		super.testAggregateListQueryFilter();
	}

	@Override
	public void testAggregateListQueryOrder() {
		// TODO Auto-generated method stub
		super.testAggregateListQueryOrder();
	}

	// SPECIFIC TESTS FOR GAE (transaction on one entity in a given group)
	public void testTransactionSave() {
		TransactionAccountFromModel accFrom = new TransactionAccountFromModel(1000L);
		
		accFrom.insert();
	
		try {
			accFrom.getPersistenceManager().beginTransaction();
			accFrom.amount-=100L;
			accFrom.save();
			accFrom.getPersistenceManager().commitTransaction();
		}catch(SienaException e){
			accFrom.getPersistenceManager().rollbackTransaction();
			fail();
		}finally{
			accFrom.getPersistenceManager().closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(900L == accFromAfter.amount);

	}
	
	public void testTransactionSaveFailure() {
		TransactionAccountFromModel accFrom = new TransactionAccountFromModel(1000L);
		accFrom.insert();
	
		try {
			accFrom.getPersistenceManager().beginTransaction();
			accFrom.amount-=100L;
			accFrom.save();
			throw new SienaException("test");
		}catch(SienaException e){
			accFrom.getPersistenceManager().rollbackTransaction();
		}finally{
			accFrom.getPersistenceManager().closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
	}

}
