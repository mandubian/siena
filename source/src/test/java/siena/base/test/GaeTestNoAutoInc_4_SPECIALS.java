package siena.base.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import siena.PersistenceManager;
import siena.Query;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.base.test.model.PersonStringID;
import siena.gae.GaePersistenceManager;
import siena.sdb.SdbPersistenceManager;

public class GaeTestNoAutoInc_4_SPECIALS extends BaseTestNoAutoInc_4_SPECIALS {
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
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supportsAutoincrement() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsMultipleKeys() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsDeleteException() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean supportsSearchStart() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean supportsSearchEnd() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean supportsTransaction() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean supportsListStore() {
		// TODO Auto-generated method stub
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

	@Override
	public void testEnum() {
		// TODO Auto-generated method stub
		super.testEnum();
	}
   
    
}
