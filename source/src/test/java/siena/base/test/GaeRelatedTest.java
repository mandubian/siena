package siena.base.test;

import java.util.List;

import siena.PersistenceManager;
import siena.gae.GaePersistenceManager;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeRelatedTest extends BaseRelatedTest{
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
	public void testRelatedSimpleReference() {
		// TODO Auto-generated method stub
		super.testRelatedSimpleReference();
	}

	@Override
	public void testRelatedSimpleOwned() {
		// TODO Auto-generated method stub
		super.testRelatedSimpleOwned();
	}

	@Override
	public void testRelatedSimpleOwnedNull() {
		// TODO Auto-generated method stub
		super.testRelatedSimpleOwnedNull();
	}

	@Override
	public void testRelatedSeveralQuery() {
		// TODO Auto-generated method stub
		super.testRelatedSeveralQuery();
	}

	@Override
	public void testRelatedSeveralQueryLotsPaginate() {
		// TODO Auto-generated method stub
		super.testRelatedSeveralQueryLotsPaginate();
	}

	@Override
	public void testRelatedSeveralQueryNoAs() {
		// TODO Auto-generated method stub
		super.testRelatedSeveralQueryNoAs();
	}

	@Override
	public void testRelatedManyOldInsertWay() {
		// TODO Auto-generated method stub
		super.testRelatedManyOldInsertWay();
	}

	@Override
	public void testRelatedManyCascadeInsert() {
		// TODO Auto-generated method stub
		super.testRelatedManyCascadeInsert();
	}

	@Override
	public void testRelatedManyCascadeInsertFetch() {
		// TODO Auto-generated method stub
		super.testRelatedManyCascadeInsertFetch();
	}

	@Override
	public void testRelatedManyCascadeInsertMany() {
		// TODO Auto-generated method stub
		super.testRelatedManyCascadeInsertMany();
	}

	@Override
	public void testRelatedManyCascadeUpdateMany() {
		// TODO Auto-generated method stub
		super.testRelatedManyCascadeUpdateMany();
	}

	@Override
	public void testRelatedManyCascadeUpdateManyRemove() {
		// TODO Auto-generated method stub
		super.testRelatedManyCascadeUpdateManyRemove();
	}
	

}
