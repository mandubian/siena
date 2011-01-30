package siena.base.test;

import java.util.List;

import siena.PersistenceManager;
import siena.SienaRestrictedApiException;
import siena.gae.GaePersistenceManager;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeTest extends BaseTest {
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
	public boolean supportsAutoincrement() {
		return false;
	}

	@Override
	public boolean supportsMultipleKeys() {
		return false;
	}
	
	@Override
	public boolean mustFilterToOrder() {
		return false;
	}

    @Override
    public void setUp() throws Exception {
		/*ApiProxy.setEnvironmentForCurrentThread(new TestEnvironment());
		ApiProxy.setDelegate(new ApiProxyLocalImpl(new File(".")){});
        ApiProxyLocalImpl proxy = (ApiProxyLocalImpl) ApiProxy.getDelegate();
        proxy.setProperty(LocalDatastoreService.NO_STORAGE_PROPERTY, Boolean.TRUE.toString());*/
    	helper.setUp();
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        /*ApiProxyLocalImpl proxy = (ApiProxyLocalImpl) ApiProxy.getDelegate();
        LocalDatastoreService datastoreService = (LocalDatastoreService) proxy.getService("datastore_v3");
        datastoreService.clearProfiles();
		ApiProxy.setDelegate(null);
		ApiProxy.setEnvironmentForCurrentThread(null);*/
        helper.tearDown();
    }

	@Override
	public void testCount() {
		// TODO Auto-generated method stub
		super.testCount();
	}

	@Override
	public void testFetch() {
		// TODO Auto-generated method stub
		super.testFetch();
	}

	@Override
	public void testFetchKeys() {
		// TODO Auto-generated method stub
		super.testFetchKeys();
	}

	@Override
	public void testFetchOrder() {
		// TODO Auto-generated method stub
		super.testFetchOrder();
	}

	@Override
	public void testFetchOrderKeys() {
		// TODO Auto-generated method stub
		super.testFetchOrderKeys();
	}

	@Override
	public void testFetchOrderDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderDesc();
	}

	@Override
	public void testFetchOrderDescKeys() {
		// TODO Auto-generated method stub
		super.testFetchOrderDescKeys();
	}

	@Override
	public void testFilterOperatorEqual() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqual();
	}

	@Override
	public void testFetchOrderOnId() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnId();
	}

	@Override
	public void testFetchOrderOnIdDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnIdDesc();
	}

	@Override
	public void testFilterOperatorNotEqual() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqual();
	}

	@Override
	public void testFilterOperatorLessThan() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThan();
	}

	@Override
	public void testFilterOperatorLessThanOrEqual() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqual();
	}

	@Override
	public void testFilterOperatorMoreThan() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThan();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqual() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqual();
	}

	@Override
	public void testCountFilter() {
		// TODO Auto-generated method stub
		super.testCountFilter();
	}

	@Override
	public void testFetchLimit() {
		// TODO Auto-generated method stub
		super.testFetchLimit();
	}

	@Override
	public void testCountLimit() {
		// TODO Auto-generated method stub
		super.testCountLimit();
	}

	@Override
	public void testFetchLimitOffset() {
		// TODO Auto-generated method stub
		super.testFetchLimitOffset();
	}

	@Override
	public void testCountLimitOffset() {
		// TODO Auto-generated method stub
		super.testCountLimitOffset();
	}

	@Override
	public void testInsert() {
		// TODO Auto-generated method stub
		super.testInsert();
	}

	@Override
	public void testGet() {
		// TODO Auto-generated method stub
		super.testGet();
	}

	@Override
	public void testUpdate() {
		// TODO Auto-generated method stub
		super.testUpdate();
	}

	@Override
	public void testDelete() {
		// TODO Auto-generated method stub
		super.testDelete();
	}

	@Override
	public void testIter1() {
		// TODO Auto-generated method stub
		super.testIter1();
	}

	@Override
	public void testIter2() {
		// TODO Auto-generated method stub
		super.testIter2();
	}

	
	@Override
	public void testIterFull() {
		// TODO Auto-generated method stub
		super.testIterFull();
	}
	
	

	@Override
	public void testIterLimit() {
		// TODO Auto-generated method stub
		super.testIterLimit();
	}

	@Override
	public void testOrderId() {
		// TODO Auto-generated method stub
		super.testOrderId();
	}

	@Override
	public void testIterLimitOffset() {
		// TODO Auto-generated method stub
		super.testIterLimitOffset();
	}

	@Override
	public void testIterFilter() {
		// TODO Auto-generated method stub
		super.testIterFilter();
	}

	@Override
	public void testIterFilterLimit() {
		// TODO Auto-generated method stub
		super.testIterFilterLimit();
	}

	@Override
	public void testIterFilterLimitOffset() {
		// TODO Auto-generated method stub
		super.testIterFilterLimitOffset();
	}

	@Override
	public void testGetObjectNotFound() {
		// TODO Auto-generated method stub
		super.testGetObjectNotFound();
	}

	@Override
	public void testDeleteObjectNotFound() {
		// TODO Auto-generated method stub
		super.testDeleteObjectNotFound();
	}

	@Override
	public void testAutoincrement() {
		// TODO Auto-generated method stub
		super.testAutoincrement();
	}

	@Override
	public void testRelationship() {
		// TODO Auto-generated method stub
		super.testRelationship();
	}

	@Override
	public void testMultipleKeys() {
		// TODO Auto-generated method stub
		super.testMultipleKeys();
	}

	@Override
	public void testDataTypesNull() {
		// TODO Auto-generated method stub
		super.testDataTypesNull();
	}

	@Override
	public void testDataTypesNotNull() {
		// TODO Auto-generated method stub
		super.testDataTypesNotNull();
	}

	@Override
	public void testQueryDelete() {
		// TODO Auto-generated method stub
		super.testQueryDelete();
	}

	@Override
	public void testQueryDeleteFiltered() {
		// TODO Auto-generated method stub
		super.testQueryDeleteFiltered();
	}

	@Override
	public void testJoin() {
		// TODO Auto-generated method stub
		super.testJoin();
	}

	@Override
	public void testJoinSortFields() {
		try {
			super.testJoinSortFields();
		}catch(SienaRestrictedApiException ex){
			return;
		}
		
		fail();
	}

	@Override
	public void testJoinAnnotation() {
		// TODO Auto-generated method stub
		super.testJoinAnnotation();
	}


    
}
