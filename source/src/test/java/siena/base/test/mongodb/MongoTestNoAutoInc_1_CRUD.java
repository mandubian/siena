package siena.base.test.mongodb;

import java.util.List;
import java.util.Properties;

import siena.PersistenceManager;
import siena.base.test.BaseTestNoAutoInc_1_CRUD;
import siena.mongodb.MongoPersistenceManager;

public class MongoTestNoAutoInc_1_CRUD extends BaseTestNoAutoInc_1_CRUD {

	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes)
			throws Exception {
		
		Properties p = new Properties();
		// don't want to give my AWS ID/secrets :D
	    //p.load(new FileInputStream("/home/pascal/work/mandubian/aws/siena-aws.properties"));
		//p.load(new FileInputStream("/home/mandubian/work/aws/siena-aws.properties"));
		
		
		//p.setProperty("implementation", "siena.sdb.SdbPersistenceManager");
		//p.setProperty("awsAccessKeyId", "");
		//p.setProperty("awsSecretAccessKey", "");
		//p.setProperty("prefix", "siena_devel_");
		
		MongoPersistenceManager mongo = new MongoPersistenceManager();
		mongo.init(p);
		
		return mongo;
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
	public boolean supportsListStore() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void testInsertUUID() {
		// TODO Auto-generated method stub
		super.testInsertUUID();
	}
	
}
