package siena.base.test;

import java.util.List;

import siena.PersistenceManager;
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

}
