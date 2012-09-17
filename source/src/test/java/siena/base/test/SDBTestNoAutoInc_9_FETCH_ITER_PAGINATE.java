package siena.base.test;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import siena.PersistenceManager;
import siena.sdb.SdbPersistenceManager;

public class SDBTestNoAutoInc_9_FETCH_ITER_PAGINATE extends BaseTestNoAutoInc_9_FETCH_ITER_PAGINATE {

	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes)
			throws Exception {
		
		SdbPersistenceManager sdb = new SdbPersistenceManager();
		sdb.init(SimpleDBConfig.getSienaAWSProperties());
		
		return sdb;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createClasses(List<Class<?>> classes) {
		// TODO Auto-generated method stub
		super.createClasses(classes);
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
		return false;
	}

	@Override
	public boolean supportsListStore() {
		// TODO Auto-generated method stub
		return false;
	}

}
