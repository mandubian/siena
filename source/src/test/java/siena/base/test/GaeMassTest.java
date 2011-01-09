package siena.base.test;

import java.util.List;

import siena.PersistenceManager;
import siena.gae.GaePersistenceManager;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeMassTest extends BaseMassTest {
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
	public void testMassFetch() {
		// TODO Auto-generated method stub
		super.testMassFetch();
	}


	@Override
	public void testMassDelete() {
		// TODO Auto-generated method stub
		super.testMassDelete();
	}


	@Override
	public void testMassIter() {
		// TODO Auto-generated method stub
		super.testMassIter();
	}

	    
}
