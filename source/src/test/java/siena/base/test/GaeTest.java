package siena.base.test;

import java.util.List;

import siena.PersistenceManager;
import siena.Query;
import siena.SienaRestrictedApiException;
import siena.base.test.model.PersonUUID;
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
		//PersistenceManagerFactory.install(pm, Discovery4GeneratorNone.class);
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
    
// SPECIAL OVERRIDE    
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
	public void testFetchOrderOnLongAutoId() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnLongAutoId();
	}

	@Override
	public void testFetchOrderOnLongManualId() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnLongManualId();
	}

	@Override
	public void testFetchOrderOnStringId() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnStringId();
	}

	@Override
	public void testFetchOrderOnUUID() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnUUID();
	}

	@Override
	public void testFetchOrderOnLongAutoIdDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnLongAutoIdDesc();
	}

	@Override
	public void testFetchOrderOnLongManualIdDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnLongManualIdDesc();
	}

	@Override
	public void testFetchOrderOnStringIdDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnStringIdDesc();
	}

	@Override
	public void testFetchOrderOnUUIDDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnUUIDDesc();
	}

	@Override
	public void testFilterOperatorEqualString() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualString();
	}

	@Override
	public void testFilterOperatorEqualInt() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualInt();
	}

	@Override
	public void testFilterOperatorEqualUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualUUID();
	}

	@Override
	public void testFilterOperatorEqualLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualLongAutoID();
	}

	@Override
	public void testFilterOperatorEqualLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualLongManualID();
	}

	@Override
	public void testFilterOperatorNotEqualString() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualString();
	}

	@Override
	public void testFilterOperatorNotEqualInt() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualInt();
	}

	@Override
	public void testFilterOperatorNotEqualUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualUUID();
	}

	@Override
	public void testFilterOperatorNotEqualLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualLongAutoID();
	}

	@Override
	public void testFilterOperatorNotEqualLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualLongManualID();
	}

	@Override
	public void testFilterOperatorNotEqualStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualStringID();
	}

	@Override
	public void testFilterOperatorIn() {
		// TODO Auto-generated method stub
		super.testFilterOperatorIn();
	}

	@Override
	public void testFilterOperatorInOrder() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInOrder();
	}

	@Override
	public void testFilterOperatorInForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInForUUID();
	}

	@Override
	public void testFilterOperatorInForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInForLongAutoID();
	}

	@Override
	public void testFilterOperatorInForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInForLongManualID();
	}

	@Override
	public void testFilterOperatorInForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInForStringID();
	}

	@Override
	public void testFilterOperatorLessThan() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThan();
	}

	@Override
	public void testFilterOperatorLessThanForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanForUUID();
	}

	@Override
	public void testFilterOperatorLessThanForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanForLongAutoID();
	}

	@Override
	public void testFilterOperatorLessThanForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanForLongManualID();
	}

	@Override
	public void testFilterOperatorLessThanForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanForStringID();
	}

	@Override
	public void testFilterOperatorLessThanOrEqual() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqual();
	}

	@Override
	public void testFilterOperatorLessThanOrEqualForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqualForUUID();
	}

	@Override
	public void testFilterOperatorLessThanOrEqualForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqualForLongAutoID();
	}

	@Override
	public void testFilterOperatorLessThanOrEqualForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqualForLongManualID();
	}

	@Override
	public void testFilterOperatorLessThanOrEqualForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqualForStringID();
	}

	@Override
	public void testFilterOperatorMoreThan() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThan();
	}

	@Override
	public void testFilterOperatorMoreThanForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanForUUID();
	}

	@Override
	public void testFilterOperatorMoreThanForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanForLongAutoID();
	}

	@Override
	public void testFilterOperatorMoreThanForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanForLongManualID();
	}

	@Override
	public void testFilterOperatorMoreThanForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanForStringID();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqual() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqual();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqualForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqualForUUID();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqualForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqualForLongAutoID();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqualForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqualForLongManualID();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqualForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqualForStringID();
	}

	@Override
	public void testCountFilter() {
		// TODO Auto-generated method stub
		super.testCountFilter();
	}

	@Override
	public void testCountFilterUUID() {
		// TODO Auto-generated method stub
		super.testCountFilterUUID();
	}

	@Override
	public void testCountFilterLongAutoID() {
		// TODO Auto-generated method stub
		super.testCountFilterLongAutoID();
	}

	@Override
	public void testCountFilterLongManualID() {
		// TODO Auto-generated method stub
		super.testCountFilterLongManualID();
	}

	@Override
	public void testCountFilterStringID() {
		// TODO Auto-generated method stub
		super.testCountFilterStringID();
	}

	@Override
	public void testFetchLimit() {
		// TODO Auto-generated method stub
		super.testFetchLimit();
	}

	@Override
	public void testFetchLimitUUID() {
		// TODO Auto-generated method stub
		super.testFetchLimitUUID();
	}

	@Override
	public void testFetchLimitLongAutoID() {
		// TODO Auto-generated method stub
		super.testFetchLimitLongAutoID();
	}

	@Override
	public void testFetchLimitLongManualID() {
		// TODO Auto-generated method stub
		super.testFetchLimitLongManualID();
	}

	@Override
	public void testFetchLimitStringID() {
		// TODO Auto-generated method stub
		super.testFetchLimitStringID();
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
	public void testInsertUUID() {
		// TODO Auto-generated method stub
		super.testInsertUUID();
	}

	@Override
	public void testInsertLongAutoID() {
		// TODO Auto-generated method stub
		super.testInsertLongAutoID();
	}

	@Override
	public void testInsertLongManualID() {
		// TODO Auto-generated method stub
		super.testInsertLongManualID();
	}

	@Override
	public void testInsertStringID() {
		// TODO Auto-generated method stub
		super.testInsertStringID();
	}

	@Override
	public void testGetUUID() {
		// TODO Auto-generated method stub
		super.testGetUUID();
	}

	@Override
	public void testGetLongAutoID() {
		// TODO Auto-generated method stub
		super.testGetLongAutoID();
	}

	@Override
	public void testGetLongManualID() {
		// TODO Auto-generated method stub
		super.testGetLongManualID();
	}

	@Override
	public void testGetStringID() {
		// TODO Auto-generated method stub
		super.testGetStringID();
	}

	@Override
	public void testUpdateUUID() {
		// TODO Auto-generated method stub
		super.testUpdateUUID();
	}

	@Override
	public void testUpdateLongAutoID() {
		// TODO Auto-generated method stub
		super.testUpdateLongAutoID();
	}

	@Override
	public void testDeleteUUID() {
		// TODO Auto-generated method stub
		super.testDeleteUUID();
	}

	@Override
	public void testIterFullUUID() {
		// TODO Auto-generated method stub
		super.testIterFullUUID();
	}

	@Override
	public void testIterFullLongAutoID() {
		// TODO Auto-generated method stub
		super.testIterFullLongAutoID();
	}

	@Override
	public void testIterFullLongManualID() {
		// TODO Auto-generated method stub
		super.testIterFullLongManualID();
	}

	@Override
	public void testIterFullLongStringID() {
		// TODO Auto-generated method stub
		super.testIterFullLongStringID();
	}

	@Override
	public void testIterLimitUUID() {
		// TODO Auto-generated method stub
		super.testIterLimitUUID();
	}

	@Override
	public void testIterLimitLongAutoID() {
		// TODO Auto-generated method stub
		super.testIterLimitLongAutoID();
	}

	@Override
	public void testIterLimitLongManualID() {
		// TODO Auto-generated method stub
		super.testIterLimitLongManualID();
	}

	@Override
	public void testIterLimitLongStringID() {
		// TODO Auto-generated method stub
		super.testIterLimitLongStringID();
	}

	@Override
	public void testIterLimitOffsetUUID() {
		// TODO Auto-generated method stub
		super.testIterLimitOffsetUUID();
	}

	@Override
	public void testIterLimitOffsetLongAutoID() {
		// TODO Auto-generated method stub
		super.testIterLimitOffsetLongAutoID();
	}

	@Override
	public void testIterLimitOffsetLongManualID() {
		// TODO Auto-generated method stub
		super.testIterLimitOffsetLongManualID();
	}

	@Override
	public void testIterLimitOffsetLongStringID() {
		// TODO Auto-generated method stub
		super.testIterLimitOffsetLongStringID();
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
	public void testOrderLongAutoId() {
		// TODO Auto-generated method stub
		super.testOrderLongAutoId();
	}

	@Override
	public void testOrderLongManualId() {
		// TODO Auto-generated method stub
		super.testOrderLongManualId();
	}

	@Override
	public void testOrderStringId() {
		// TODO Auto-generated method stub
		super.testOrderStringId();
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
	public void testJoinAnnotation() {
		// TODO Auto-generated method stub
		super.testJoinAnnotation();
	}

	@Override
	public void testFetchPrivateFields() {
		// TODO Auto-generated method stub
		super.testFetchPrivateFields();
	}

	@Override
	public void testFetchPaginate() {
		// TODO Auto-generated method stub
		//super.testFetchPaginate();
	}

	@Override
	public void testFetchKeysPaginate() {
		// TODO Auto-generated method stub
		//super.testFetchKeysPaginate();
	}

	@Override
	public void testIterPaginate() {
		// TODO Auto-generated method stub
		//super.testIterPaginate();
	}

	@Override
	public void testIterFetchPaginate() {
		// TODO Auto-generated method stub
		//super.testIterFetchPaginate();
	}

	
}
