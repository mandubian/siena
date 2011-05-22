package siena.base.test;

import java.util.Arrays;
import java.util.List;

import siena.PersistenceManager;
import siena.Query;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.base.test.model.Discovery;
import siena.base.test.model.Discovery4Join;
import siena.base.test.model.Discovery4Search;
import siena.base.test.model.PersonUUID;
import siena.gae.GaePersistenceManager;
import siena.gae.QueryOptionGaeContext;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeTest extends BaseTest {
	private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private static GaePersistenceManager pm;
	
	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes)
			throws Exception {
		if(pm==null){
			pm = new GaePersistenceManager();
			//PersistenceManagerFactory.install(pm, Discovery4GeneratorNone.class);
			pm.init(null);
		}
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
    	helper.setUp();
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
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



	
	public void testFilterWithOperatorINStateful() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		Query<PersonUUID> query = 
			pm.createQuery(PersonUUID.class)
			.filter("id IN", Arrays.asList( l.get(0).id, l.get(1).id))
			.stateful()
			.paginate(1);

		List<PersonUUID> people = query.fetch();
		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(people);
		assertEquals(1, people.size());
		assertEquals(l.get(0), people.get(0));
		
		people = query.nextPage().fetch();
		gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);
		assertNotNull(people);
		assertEquals(1, people.size());
		assertEquals(l.get(1), people.get(0));
	}
	
	public void testFilterWithOperatorINLotsOfEntitiesStateful() {
		Discovery[] discs = new Discovery[200];
		for(int i=0; i<200; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = 
			pm.createQuery(Discovery.class)
			.filter("id IN", Arrays.asList( discs[48].id, discs[73].id, discs[86].id));
		List<Discovery> people = query.fetch();

		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(people);
		assertEquals(3, people.size());
		assertEquals(discs[48], people.get(0));
		assertEquals(discs[73], people.get(1));
		assertEquals(discs[86], people.get(2));
	}
	
	public void testFilterWithOperatorINLotsOfEntitiesPaginateStateless() {
		Discovery[] discs = new Discovery[200];
		for(int i=0; i<200; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = 
			pm.createQuery(Discovery.class)
			.filter("id IN", Arrays.asList( discs[48].id, discs[73].id, discs[86].id))
			.paginate(2);
		List<Discovery> people = query.fetch();

		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(people);
		assertEquals(2, people.size());
		assertEquals(discs[48], people.get(0));
		assertEquals(discs[73], people.get(1));
		
		people = query.nextPage().fetch();
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(people);
		assertEquals(1, people.size());
		assertEquals(discs[86], people.get(0));
		
		people = query.nextPage().fetch();
		assertNotNull(people);
		assertEquals(0, people.size());
		
		people = query.previousPage().fetch();
		assertNotNull(people);
		assertEquals(1, people.size());
		assertEquals(discs[86], people.get(0));
		
		people = query.previousPage().fetch();
		assertNotNull(people);
		assertEquals(2, people.size());
		assertEquals(discs[48], people.get(0));
		assertEquals(discs[73], people.get(1));
		
		people = query.previousPage().fetch();
		assertNotNull(people);
		assertEquals(0, people.size());
	}
	
	public void testFilterWithOperatorINLotsOfEntitiesPaginateStateful() {
		Discovery[] discs = new Discovery[200];
		for(int i=0; i<200; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = 
			pm.createQuery(Discovery.class)
			.filter("id IN", Arrays.asList( discs[48].id, discs[73].id, discs[86].id))
			.stateful()
			.paginate(2);
		List<Discovery> people = query.fetch();

		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(people);
		assertEquals(2, people.size());
		assertEquals(discs[48], people.get(0));
		assertEquals(discs[73], people.get(1));
		
		people = query.nextPage().fetch();
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(people);
		assertEquals(1, people.size());
		assertEquals(discs[86], people.get(0));
		
		people = query.nextPage().fetch();
		assertNotNull(people);
		assertEquals(0, people.size());
		
		people = query.previousPage().fetch();
		assertNotNull(people);
		assertEquals(1, people.size());
		assertEquals(discs[86], people.get(0));
		
		people = query.previousPage().fetch();
		assertNotNull(people);
		assertEquals(2, people.size());
		assertEquals(discs[48], people.get(0));
		assertEquals(discs[73], people.get(1));
		
		people = query.previousPage().fetch();
		assertNotNull(people);
		assertEquals(0, people.size());
	}
	
	public void testFilterWithOperatorNotEqualStateful() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		Query<PersonUUID> query = 
			pm.createQuery(PersonUUID.class)
			.filter("id!=", l.get(0).id)
			.order("id")
			.stateful()
			.paginate(1);
		
		List<PersonUUID> people = query.fetch();

		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(people);
		assertEquals(1, people.size());
		assertEquals(l.get(1), people.get(0));
		
		people = query.nextPage().fetch();

		gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);
		assertNotNull(people);
		assertEquals(1, people.size());
		assertEquals(l.get(2), people.get(0));
	}
	
	
	public void testFilterWithOperatorNotEqualLotsOfEntitiesStateful() {
		Discovery[] discs = new Discovery[200];
		for(int i=0; i<200; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = 
			pm.createQuery(Discovery.class)
			.stateful()
			.filter("id !=", discs[48].id);
		List<Discovery> res = query.fetch();

		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(res);
		assertEquals(200-1, res.size());
		for(Discovery disc:res){
			assertFalse(discs[48].equals(disc));
		}
	}
	
	public void testFilterWithOperatorNotEqualLotsOfEntitiesStateless() {
		Discovery[] discs = new Discovery[200];
		for(int i=0; i<200; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = 
			pm.createQuery(Discovery.class)
			.filter("id !=", discs[48].id);
		List<Discovery> res = query.fetch();

		QueryOptionGaeContext gaeCtx = (QueryOptionGaeContext)query.option(QueryOptionGaeContext.ID);
		assertFalse(gaeCtx.useCursor);		
		assertNotNull(res);
		assertEquals(200-1, res.size());
		for(Discovery disc:res){
			assertFalse(discs[48].equals(disc));
		}
	}
	
	public void testSearchSingleFieldEqualsSingleResult() {
		Discovery4Search[] discs = new Discovery4Search[10];
		for(int i=0; i<10; i++){
			if(i%2==0) discs[i] = new Discovery4Search("even_"+i, LongAutoID_CURIE);
			else discs[i] = new Discovery4Search("odd_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("even_4", "name").order("name");
		
		List<Discovery4Search> res = query.fetch();

		assertEquals(1, res.size());
		assertEquals(discs[4], res.get(0));
	}

	public void testSearchSingleFieldEqualsSeveralResults() {
		Discovery4Search[] discs = new Discovery4Search[5];
		discs[0] = new Discovery4Search("alpha", LongAutoID_CURIE);
		discs[1] = new Discovery4Search("beta", LongAutoID_CURIE);
		discs[2] = new Discovery4Search("gamma", LongAutoID_CURIE);
		discs[3] = new Discovery4Search("delta", LongAutoID_CURIE);
		discs[4] = new Discovery4Search("eta", LongAutoID_CURIE);
		pm.insert((Object[])discs);		
		
		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("beta eta", "name");
		
		List<Discovery4Search> res = query.fetch();

		assertEquals(2, res.size());
		assertEquals(discs[1], res.get(0));
		assertEquals(discs[4], res.get(1));
	}
	
	public void testSearchSingleFieldBeginSingleResults() {
		Discovery4Search[] discs = new Discovery4Search[5];
		discs[0] = new Discovery4Search("alpha", LongAutoID_CURIE);
		discs[1] = new Discovery4Search("beta", LongAutoID_CURIE);
		discs[2] = new Discovery4Search("gamma", LongAutoID_CURIE);
		discs[3] = new Discovery4Search("delta", LongAutoID_CURIE);
		discs[4] = new Discovery4Search("eta", LongAutoID_CURIE);
		pm.insert((Object[])discs);		
		
		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("gamma*", "name");
		
		List<Discovery4Search>res = query.fetch();

		assertEquals(1, res.size());
		assertEquals(discs[2], res.get(0));
	}
	
	public void testSearchSingleFieldBeginSeveralResults() {
		Discovery4Search[] discs = new Discovery4Search[5];
		discs[0] = new Discovery4Search("alpha", LongAutoID_CURIE);
		discs[1] = new Discovery4Search("beta", LongAutoID_CURIE);
		discs[2] = new Discovery4Search("alphagamma", LongAutoID_CURIE);
		discs[3] = new Discovery4Search("delta", LongAutoID_CURIE);
		discs[4] = new Discovery4Search("eta", LongAutoID_CURIE);
		pm.insert((Object[])discs);		
		
		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("alpha*", "name");
		
		List<Discovery4Search> res = query.fetch();

		assertEquals(2, res.size());
		assertEquals(discs[0], res.get(0));
		assertEquals(discs[2], res.get(1));
	}
	
	public void testSearchSingleFieldBeginSeveralResultsKeysOnly() {
		Discovery4Search[] discs = new Discovery4Search[5];
		discs[0] = new Discovery4Search("alpha", LongAutoID_CURIE);
		discs[1] = new Discovery4Search("beta", LongAutoID_CURIE);
		discs[2] = new Discovery4Search("alphagamma", LongAutoID_CURIE);
		discs[3] = new Discovery4Search("delta", LongAutoID_CURIE);
		discs[4] = new Discovery4Search("eta", LongAutoID_CURIE);
		pm.insert((Object[])discs);		
		
		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("alpha*", "name");
		
		List<Discovery4Search> res = query.fetchKeys();

		assertEquals(2, res.size());
		assertEquals(discs[0].id, res.get(0).id);
		assertTrue(res.get(0).isOnlyIdFilled());
		assertEquals(discs[2].id, res.get(1).id);
		assertTrue(res.get(1).isOnlyIdFilled());
	}
	
	public void testSearchSingleFieldBeginSeveralResultsCount() {
		Discovery4Search[] discs = new Discovery4Search[5];
		discs[0] = new Discovery4Search("alpha", LongAutoID_CURIE);
		discs[1] = new Discovery4Search("beta", LongAutoID_CURIE);
		discs[2] = new Discovery4Search("alphagamma", LongAutoID_CURIE);
		discs[3] = new Discovery4Search("delta", LongAutoID_CURIE);
		discs[4] = new Discovery4Search("eta", LongAutoID_CURIE);
		pm.insert((Object[])discs);		
		
		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("alpha*", "name");
		
		int res = query.count();

		assertEquals(2, res);

	}
	
	public void testSearchSingleFieldEndException() {
		Discovery4Search[] discs = new Discovery4Search[5];
		discs[0] = new Discovery4Search("alpha", LongAutoID_CURIE);
		discs[1] = new Discovery4Search("beta", LongAutoID_CURIE);
		discs[2] = new Discovery4Search("alphagamma", LongAutoID_CURIE);
		discs[3] = new Discovery4Search("delta", LongAutoID_CURIE);
		discs[4] = new Discovery4Search("eta", LongAutoID_CURIE);
		pm.insert((Object[])discs);			
		try {
			Query<Discovery4Search> query = 
				pm.createQuery(Discovery4Search.class).search("*gamma", "name");
			query.fetch();
		}catch(SienaException ex ){
			return;
		}
		fail();
	}
	

	// GENERIC TESTS OVERRIDE
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
	public void testFilterOperatorEqualStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualStringID();
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
	@Deprecated
	public void testCountLimit() {
		// TODO Auto-generated method stub
		super.testCountLimit();
	}

	@Override
	public void testFetchLimitReal() {
		// TODO Auto-generated method stub
		super.testFetchLimitReal();
	}

	@Override
	public void testFetchLimitOffsetReal() {
		// TODO Auto-generated method stub
		super.testFetchLimitOffsetReal();
	}

	@Override
	public void testSearchSingle() {
		// TODO Auto-generated method stub
		super.testSearchSingle();
	}


	@Override
	public void testSearchSingleKeysOnly() {
		// TODO Auto-generated method stub
		super.testSearchSingleKeysOnly();
	}

	@Override
	public void testSearchSingleTwice() {
		// TODO Auto-generated method stub
		super.testSearchSingleTwice();
	}

	@Override
	public void testSearchSingleCount() {
		// TODO Auto-generated method stub
		super.testSearchSingleCount();
	}

	@Override
	public void testFetchLimitOffset() {
		// TODO Auto-generated method stub
		super.testFetchLimitOffset();
	}

	@Override
	@Deprecated
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
	public void testFetchPaginateStatelessNextPage() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessNextPage();
	}

	@Override
	public void testFetchPaginateStatelessNextPageToEnd() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessNextPageToEnd();
	}

	@Override
	public void testFetchPaginateStatelessPreviousPageFromScratch() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessPreviousPageFromScratch();
	}

	@Override
	public void testFetchPaginateStatelessPreviousPage() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessPreviousPage();
	}

	@Override
	public void testFetchPaginateStatelessSeveralTimes() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessSeveralTimes();
	}

	@Override
	public void testFetchPaginateStatefulNextPage() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulNextPage();
	}

	@Override
	public void testFetchPaginateStatefulNextPageToEnd() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulNextPageToEnd();
	}

	@Override
	public void testFetchPaginateStatefulPreviousPageFromScratch() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulPreviousPageFromScratch();
	}

	@Override
	public void testFetchPaginateStatefulPreviousPage() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulPreviousPage();
	}

	@Override
	public void testFetchPaginateStatefulPreviouPageSeveralTimes() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulPreviouPageSeveralTimes();
	}

	@Override
	public void testFetchKeysPaginateStatelessNextPage() {
		// TODO Auto-generated method stub
		super.testFetchKeysPaginateStatelessNextPage();
	}

	@Override
	public void testFetchKeysPaginateStatelessPreviousPageFromScratch() {
		// TODO Auto-generated method stub
		super.testFetchKeysPaginateStatelessPreviousPageFromScratch();
	}

	@Override
	public void testFetchKeysPaginateStatelessPreviousPage() {
		// TODO Auto-generated method stub
		super.testFetchKeysPaginateStatelessPreviousPage();
	}

	@Override
	public void testFetchKeysPaginateStatelessPreviouPageSeveralTimes() {
		// TODO Auto-generated method stub
		super.testFetchKeysPaginateStatelessPreviouPageSeveralTimes();
	}

	@Override
	public void testFetchKeysPaginateStatefulNextPage() {
		// TODO Auto-generated method stub
		super.testFetchKeysPaginateStatefulNextPage();
	}

	@Override
	public void testFetchKeysPaginateStatefulPreviousPageFromScratch() {
		// TODO Auto-generated method stub
		super.testFetchKeysPaginateStatefulPreviousPageFromScratch();
	}

	@Override
	public void testFetchKeysPaginateStatefulPreviousPage() {
		// TODO Auto-generated method stub
		super.testFetchKeysPaginateStatefulPreviousPage();
	}

	@Override
	public void testFetchKeysPaginateStatefulSeveralTimes() {
		// TODO Auto-generated method stub
		super.testFetchKeysPaginateStatefulSeveralTimes();
	}

	@Override
	public void testIterPaginateStatelessNextPage() {
		// TODO Auto-generated method stub
		super.testIterPaginateStatelessNextPage();
	}

	@Override
	public void testIterPaginateStatelessPreviousPageFromScratch() {
		// TODO Auto-generated method stub
		super.testIterPaginateStatelessPreviousPageFromScratch();
	}

	@Override
	public void testIterPaginateStatelessPreviousPage() {
		// TODO Auto-generated method stub
		super.testIterPaginateStatelessPreviousPage();
	}

	@Override
	public void testIterPaginateStatelessPreviouPageSeveralTimes() {
		// TODO Auto-generated method stub
		super.testIterPaginateStatelessPreviouPageSeveralTimes();
	}

	@Override
	public void testIterPaginateStatefulNextPage() {
		// TODO Auto-generated method stub
		super.testIterPaginateStatefulNextPage();
	}

	@Override
	public void testIterPaginateStatefulPreviousPageFromScratch() {
		// TODO Auto-generated method stub
		super.testIterPaginateStatefulPreviousPageFromScratch();
	}

	@Override
	public void testIterPaginateStatefulPreviousPage() {
		// TODO Auto-generated method stub
		super.testIterPaginateStatefulPreviousPage();
	}

	@Override
	public void testIterPaginateStatefulPreviouPageSeveralTimes() {
		// TODO Auto-generated method stub
		super.testIterPaginateStatefulPreviouPageSeveralTimes();
	}

	@Override
	public void testIterLotsOfEntitiesStateless() {
		// TODO Auto-generated method stub
		super.testIterLotsOfEntitiesStateless();
	}

	@Override
	public void testIterLotsOfEntitiesStateful() {
		// TODO Auto-generated method stub
		super.testIterLotsOfEntitiesStateful();
	}

	@Override
	public void testIterLotsOfEntitiesStatefulMixed() {
		// TODO Auto-generated method stub
		super.testIterLotsOfEntitiesStatefulMixed();
	}

	@Override
	public void testIterLotsOfEntitiesStatefulMixed2() {
		// TODO Auto-generated method stub
		super.testIterLotsOfEntitiesStatefulMixed2();
	}

	@Override
	public void testIterLotsOfEntitiesStatefulMixed3() {
		// TODO Auto-generated method stub
		super.testIterLotsOfEntitiesStatefulMixed3();
	}

	@Override
	public void testFetchLotsOfEntitiesStatefulMixed() {
		// TODO Auto-generated method stub
		super.testFetchLotsOfEntitiesStatefulMixed();
	}

	@Override
	public void testFetchLotsOfEntitiesStatefulMixed2() {
		// TODO Auto-generated method stub
		super.testFetchLotsOfEntitiesStatefulMixed2();
	}

	@Override
	public void testFetchIterLotsOfEntitiesStatefulMixed() {
		// TODO Auto-generated method stub
		super.testFetchIterLotsOfEntitiesStatefulMixed();
	}

	@Override
	public void testFetchIterLotsOfEntitiesStatefulMixed2() {
		// TODO Auto-generated method stub
		super.testFetchIterLotsOfEntitiesStatefulMixed2();
	}

	@Override
	public void testFetchIterLotsOfEntitiesStatefulMixed3() {
		// TODO Auto-generated method stub
		super.testFetchIterLotsOfEntitiesStatefulMixed3();
	}

	@Override
	public void testBatchInsert() {
		// TODO Auto-generated method stub
		super.testBatchInsert();
	}

	@Override
	public void testBatchInsertList() {
		// TODO Auto-generated method stub
		super.testBatchInsertList();
	}

	@Override
	public void testBatchDelete() {
		// TODO Auto-generated method stub
		super.testBatchDelete();
	}

	@Override
	public void testBatchDeleteList() {
		// TODO Auto-generated method stub
		super.testBatchDeleteList();
	}

	@Override
	public void testBatchDeleteByKeys() {
		// TODO Auto-generated method stub
		super.testBatchDeleteByKeys();
	}

	@Override
	public void testBatchDeleteByKeysList() {
		// TODO Auto-generated method stub
		super.testBatchDeleteByKeysList();
	}

	@Override
	public void testBatchGet() {
		// TODO Auto-generated method stub
		super.testBatchGet();
	}

	@Override
	public void testBatchGetList() {
		// TODO Auto-generated method stub
		super.testBatchGetList();
	}

	@Override
	public void testBatchGetByKeys() {
		// TODO Auto-generated method stub
		super.testBatchGetByKeysList();
	}

	@Override
	public void testBatchGetByKeysList() {
		// TODO Auto-generated method stub
		super.testBatchGetByKeysList();
	}

	@Override
	public void testLimitStateless() {
		// TODO Auto-generated method stub
		super.testLimitStateless();
	}

	@Override
	public void testLimitStateful() {
		// TODO Auto-generated method stub
		super.testLimitStateful();
	}

	@Override
	public void testOffsetStateless() {
		// TODO Auto-generated method stub
		super.testOffsetStateless();
	}

	@Override
	public void testOffsetStateful() {
		// TODO Auto-generated method stub
		super.testOffsetStateful();
	}

	@Override
	public void testOffsetLimitStateless() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStateless();
	}

	@Override
	public void testOffsetLimitStateful() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStateful();
	}

	@Override
	public void testOffsetLimitStatelessPaginate() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatelessPaginate();
	}

	@Override
	public void testOffsetLimitStatefulPaginate() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatefulPaginate();
	}

	@Override
	public void testOffsetLimitStatelessPaginate2() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatelessPaginate2();
	}

	@Override
	public void testOffsetLimitStatefulPaginate2() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatefulPaginate2();
	}

	@Override
	public void testFetchPaginateStatelessTwice() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessTwice();
	}

	@Override
	public void testFetchPaginateStatefulTwice() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulTwice();
	}

	@Override
	public void testLimitStatelessKeys() {
		// TODO Auto-generated method stub
		super.testLimitStatelessKeys();
	}

	@Override
	public void testLimitStatefulKeys() {
		// TODO Auto-generated method stub
		super.testLimitStatefulKeys();
	}

	@Override
	public void testOffsetStatelessKeys() {
		// TODO Auto-generated method stub
		super.testOffsetStatelessKeys();
	}

	@Override
	public void testOffsetStatefulKeys() {
		// TODO Auto-generated method stub
		super.testOffsetStatefulKeys();
	}

	@Override
	public void testOffsetLimitStatelessKeys() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatelessKeys();
	}

	@Override
	public void testOffsetLimitStatefulKeys() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatefulKeys();
	}

	@Override
	public void testOffsetLimitStatelessPaginateKeys() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatelessPaginateKeys();
	}

	@Override
	public void testOffsetLimitStatefulPaginateKeys() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatefulPaginateKeys();
	}

	@Override
	public void testOffsetLimitStatelessPaginate2Keys() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatelessPaginate2Keys();
	}

	@Override
	public void testOffsetLimitStatefulPaginate2Keys() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatefulPaginate2Keys();
	}

	@Override
	public void testFetchPaginateStatelessTwiceKeys() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessTwiceKeys();
	}

	@Override
	public void testFetchPaginateStatefulTwiceKeys() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulTwiceKeys();
	}

	@Override
	public void testLimitStatelessIter() {
		// TODO Auto-generated method stub
		super.testLimitStatelessIter();
	}

	@Override
	public void testLimitStatefulIter() {
		// TODO Auto-generated method stub
		super.testLimitStatefulIter();
	}

	@Override
	public void testOffsetStatelessIter() {
		// TODO Auto-generated method stub
		super.testOffsetStatelessIter();
	}

	@Override
	public void testOffsetStatefulIter() {
		// TODO Auto-generated method stub
		super.testOffsetStatefulIter();
	}

	@Override
	public void testOffsetLimitStatelessIter() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatelessIter();
	}

	@Override
	public void testOffsetLimitStatefulIter() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatefulIter();
	}

	@Override
	public void testOffsetLimitStatelessPaginateIter() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatelessPaginateIter();
	}

	@Override
	public void testOffsetLimitStatefulPaginateIter() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatefulPaginateIter();
	}

	@Override
	public void testOffsetLimitStatelessPaginate2Iter() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatelessPaginate2Iter();
	}

	@Override
	public void testOffsetLimitStatefulPaginate2Iter() {
		// TODO Auto-generated method stub
		super.testOffsetLimitStatefulPaginate2Iter();
	}

	@Override
	public void testFetchPaginateStatelessTwiceIter() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatelessTwiceIter();
	}

	@Override
	public void testFetchPaginateStatefulTwiceIter() {
		// TODO Auto-generated method stub
		super.testFetchPaginateStatefulTwiceIter();
	}

	@Override
	public void testFetchStringAutoInc() {
		// TODO Auto-generated method stub
		super.testFetchStringAutoInc();
	}

	@Override
	public void testIterPerPageStateless() {
		// TODO Auto-generated method stub
		super.testIterPerPageStateless();
	}

	@Override
	public void testIterPerPageStateless2() {
		// TODO Auto-generated method stub
		super.testIterPerPageStateless2();
	}

	@Override
	public void testIterPerPageStateless3() {
		// TODO Auto-generated method stub
		super.testIterPerPageStateless3();
	}

	@Override
	public void testIterPerPageStateful() {
		// TODO Auto-generated method stub
		super.testIterPerPageStateful();
	}

	@Override
	public void testIterPerPageStateful2() {
		// TODO Auto-generated method stub
		super.testIterPerPageStateful2();
	}

	@Override
	public void testDump() {
		// TODO Auto-generated method stub
		super.testDump();
	}

	@Override
	public void testRestore() {
		// TODO Auto-generated method stub
		super.testRestore();
	}

	@Override
	public void testIterPerPageStatefull3() {
		// TODO Auto-generated method stub
		super.testIterPerPageStatefull3();
	}

	@Override
	public void testInsertObjectWithNullJoinObject() {
		// TODO Auto-generated method stub
		super.testInsertObjectWithNullJoinObject();
	}

	@Override
	public void testInsertObjectWithDoubleNullJoinObject() {
		// TODO Auto-generated method stub
		super.testInsertObjectWithDoubleNullJoinObject();
	}

	@Override
	public void testJoinAnnotationDouble() {
		// TODO Auto-generated method stub
		super.testJoinAnnotationDouble();
	}

	@Override
	public void testBatchUpdate() {
		// TODO Auto-generated method stub
		super.testBatchUpdate();
	}

	@Override
	public void testBatchUpdateList() {
		// TODO Auto-generated method stub
		super.testBatchUpdateList();
	}

	@Override
	public void testGetByKeyUUID() {
		// TODO Auto-generated method stub
		super.testGetByKeyUUID();
	}

	@Override
	public void testGetByKeyLongAutoID() {
		// TODO Auto-generated method stub
		super.testGetByKeyLongAutoID();
	}

	@Override
	public void testGetByKeyLongManualID() {
		// TODO Auto-generated method stub
		super.testGetByKeyLongManualID();
	}

	@Override
	public void testGetByKeyStringID() {
		// TODO Auto-generated method stub
		super.testGetByKeyStringID();
	}

	@Override
	public void testSaveLongAutoID() {
		// TODO Auto-generated method stub
		super.testSaveLongAutoID();
	}

	@Override
	public void testSaveUUID() {
		// TODO Auto-generated method stub
		super.testSaveUUID();
	}

	@Override
	public void testSaveLongManualID() {
		// TODO Auto-generated method stub
		super.testSaveLongManualID();
	}

	@Override
	public void testSaveStringID() {
		// TODO Auto-generated method stub
		super.testSaveStringID();
	}

	@Override
	public void testBatchSave() {
		// TODO Auto-generated method stub
		super.testBatchSave();
	}

	@Override
	public void testBatchSaveList() {
		// TODO Auto-generated method stub
		super.testBatchSaveList();
	}

	@Override
	public void testPolymorphic() {
		// TODO Auto-generated method stub
		super.testPolymorphic();
	}

	@Override
	public void testPolymorphic2() {
		// TODO Auto-generated method stub
		super.testPolymorphic2();
	}

	@Override
	public void testEmbeddedModel() {
		// TODO Auto-generated method stub
		super.testEmbeddedModel();
	}


	
}
