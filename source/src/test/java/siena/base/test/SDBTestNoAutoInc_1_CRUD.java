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

public class SDBTestNoAutoInc_1_CRUD extends BaseTestNoAutoInc_1_CRUD {

	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes)
			throws Exception {
		
		Properties p = new Properties();
		// don't want to give my AWS ID/secrets :D
	    p.load(new FileInputStream("/home/pascal/work/mandubian/aws/siena-aws.properties"));
		
		//p.setProperty("implementation", "siena.sdb.SdbPersistenceManager");
		//p.setProperty("awsAccessKeyId", "");
		//p.setProperty("awsSecretAccessKey", "");
		//p.setProperty("prefix", "siena_devel_");
		
		SdbPersistenceManager sdb = new SdbPersistenceManager();
		sdb.init(p);
		
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
	
}
