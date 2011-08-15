package siena.base.test;

import java.util.List;

import siena.PersistenceManager;
import siena.gae.GaePersistenceManager;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeAggregatedTest extends BaseAggregatedTest{
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
	public void testAggregateMostSimple() {
		// TODO Auto-generated method stub
		super.testAggregateMostSimple();
	}

	@Override
	public void testAggregateMostSimpleMultiple() {
		// TODO Auto-generated method stub
		super.testAggregateMostSimpleMultiple();
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
	public void testAggregateDeleteChildOfChildren() {
		// TODO Auto-generated method stub
		super.testAggregateDeleteChildOfChildren();
	}

	@Override
	public void testAggregateAddChild() {
		// TODO Auto-generated method stub
		super.testAggregateAddChild();
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

	@Override
	public void testAggregateMostSimpleManual() {
		// TODO Auto-generated method stub
		super.testAggregateMostSimpleManual();
	}


	

}
