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

public class GaeEmbeddedTest extends BaseEmbeddedTest{
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
	public void testEmbeddedModel() {
		// TODO Auto-generated method stub
		super.testEmbeddedModel();
	}

	@Override
	public void testEmbeddedModelJava() {
		// TODO Auto-generated method stub
		super.testEmbeddedModelJava();
	}

	@Override
	public void testEmbeddedNative() {
		// TODO Auto-generated method stub
		super.testEmbeddedNative();
	}

	@Override
	public void testEmbeddedNativeFilter() {
		// TODO Auto-generated method stub
		super.testEmbeddedNativeFilter();
	}
	
	
}
