package siena.base.test;

import java.util.List;
import java.util.Properties;

import siena.PersistenceManager;
import siena.sdb.SdbPersistenceManager;

public class SimpleDBTest extends BaseTest {

	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes)
			throws Exception {
		
		Properties p = new Properties();
		p.setProperty("implementation", "siena.sdb.SdbPersistenceManager");
		p.setProperty("awsAccessKeyId", "07CPR9XRN8D3WV5TM6R2");
		p.setProperty("awsSecretAccessKey", "nW8gl6z/l0A2gymgGRk/mnyEvne1ZgqiOWqgyd8e");
		p.setProperty("prefix", "siena_devel_");
		
		SdbPersistenceManager sdb = new SdbPersistenceManager();
		sdb.init(p);
		
		return sdb;
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
		return true;
	}

}
