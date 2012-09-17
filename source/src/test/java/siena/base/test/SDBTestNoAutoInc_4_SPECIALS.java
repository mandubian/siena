package siena.base.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import siena.PersistenceManager;
import siena.Query;
import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.base.test.model.PersonStringID;
import siena.sdb.SdbPersistenceManager;

public class SDBTestNoAutoInc_4_SPECIALS extends BaseTestNoAutoInc_4_SPECIALS {

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
	public void testRelationship() {
		// TODO Auto-generated method stub
		super.testRelationship();
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
	public boolean supportsListStore() {
		// TODO Auto-generated method stub
		return false;
	}


}
