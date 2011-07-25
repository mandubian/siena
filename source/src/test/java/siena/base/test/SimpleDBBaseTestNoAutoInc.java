package siena.base.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import siena.PersistenceManager;
import siena.Query;
import siena.SienaException;
import siena.base.test.model.PersonStringID;
import siena.sdb.SdbPersistenceManager;

public class SimpleDBBaseTestNoAutoInc extends BaseTestNoAutoInc {

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
	public void createClasses(List<Class<?>> classes) {
		// TODO Auto-generated method stub
		super.createClasses(classes);
	}

	@Override
	public void testCount() {
		// TODO Auto-generated method stub
		super.testCount();
	}

	@Override
	public void testFetch() {
		// TODO Auto-generated method stub
		super.testFetch();
	}

	@Override
	public void testFetchKeys() {
		// TODO Auto-generated method stub
		super.testFetchKeys();
	}

	@Override
	public void testFetchOrder() {
		// TODO Auto-generated method stub
		super.testFetchOrder();
	}

	@Override
	public void testFetchOrderKeys() {
		// TODO Auto-generated method stub
		super.testFetchOrderKeys();
	}

	@Override
	public void testFetchOrderDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderDesc();
	}

	@Override
	public void testFetchOrderDescKeys() {
		// TODO Auto-generated method stub
		super.testFetchOrderDescKeys();
	}

	@Override
	public void testFetchOrderOnLongAutoId() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnLongAutoId();
	}

	@Override
	public void testFetchOrderOnLongManualId() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnLongManualId();
	}

	@Override
	public void testFetchOrderOnStringId() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnStringId();
	}

	@Override
	public void testFetchOrderOnUUID() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnUUID();
	}

	@Override
	public void testFetchOrderOnLongAutoIdDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnLongAutoIdDesc();
	}

	@Override
	public void testFetchOrderOnLongManualIdDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnLongManualIdDesc();
	}

	@Override
	public void testFetchOrderOnStringIdDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnStringIdDesc();
	}

	@Override
	public void testFetchOrderOnUUIDDesc() {
		// TODO Auto-generated method stub
		super.testFetchOrderOnUUIDDesc();
	}

	@Override
	public void testFilterOperatorEqualString() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualString();
	}

	@Override
	public void testFilterOperatorEqualInt() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualInt();
	}

	@Override
	public void testFilterOperatorEqualUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualUUID();
	}

	@Override
	public void testFilterOperatorEqualLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualLongAutoID();
	}

	@Override
	public void testFilterOperatorEqualLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualLongManualID();
	}

	@Override
	public void testFilterOperatorEqualStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorEqualStringID();
	}

	@Override
	public void testFilterOperatorNotEqualString() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualString();
	}

	@Override
	public void testFilterOperatorNotEqualInt() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualInt();
	}

	@Override
	public void testFilterOperatorNotEqualUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualUUID();
	}

	@Override
	public void testFilterOperatorNotEqualLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualLongAutoID();
	}

	@Override
	public void testFilterOperatorNotEqualLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualLongManualID();
	}

	@Override
	public void testFilterOperatorNotEqualStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorNotEqualStringID();
	}

	@Override
	public void testFilterOperatorIn() {
		// TODO Auto-generated method stub
		super.testFilterOperatorIn();
	}

	@Override
	public void testFilterOperatorInOrder() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInOrder();
	}

	@Override
	public void testFilterOperatorInForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInForUUID();
	}

	@Override
	public void testFilterOperatorInForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInForLongAutoID();
	}

	@Override
	public void testFilterOperatorInForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInForLongManualID();
	}

	@Override
	public void testFilterOperatorInForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorInForStringID();
	}

	@Override
	public void testFilterOperatorLessThan() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThan();
	}

	@Override
	public void testFilterOperatorLessThanForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanForUUID();
	}

	@Override
	public void testFilterOperatorLessThanForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanForLongAutoID();
	}

	@Override
	public void testFilterOperatorLessThanForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanForLongManualID();
	}

	@Override
	public void testFilterOperatorLessThanForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanForStringID();
	}

	@Override
	public void testFilterOperatorLessThanOrEqual() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqual();
	}

	@Override
	public void testFilterOperatorLessThanOrEqualForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqualForUUID();
	}

	@Override
	public void testFilterOperatorLessThanOrEqualForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqualForLongAutoID();
	}

	@Override
	public void testFilterOperatorLessThanOrEqualForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqualForLongManualID();
	}

	@Override
	public void testFilterOperatorLessThanOrEqualForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorLessThanOrEqualForStringID();
	}

	@Override
	public void testFilterOperatorMoreThan() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThan();
	}

	@Override
	public void testFilterOperatorMoreThanForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanForUUID();
	}

	@Override
	public void testFilterOperatorMoreThanForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanForLongAutoID();
	}

	@Override
	public void testFilterOperatorMoreThanForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanForLongManualID();
	}

	@Override
	public void testFilterOperatorMoreThanForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanForStringID();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqual() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqual();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqualForUUID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqualForUUID();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqualForLongAutoID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqualForLongAutoID();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqualForLongManualID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqualForLongManualID();
	}

	@Override
	public void testFilterOperatorMoreThanOrEqualForStringID() {
		// TODO Auto-generated method stub
		super.testFilterOperatorMoreThanOrEqualForStringID();
	}

	@Override
	public void testCountFilter() {
		// TODO Auto-generated method stub
		super.testCountFilter();
	}

	@Override
	public void testCountFilterUUID() {
		// TODO Auto-generated method stub
		super.testCountFilterUUID();
	}

	@Override
	public void testCountFilterLongAutoID() {
		// TODO Auto-generated method stub
		super.testCountFilterLongAutoID();
	}

	@Override
	public void testCountFilterLongManualID() {
		// TODO Auto-generated method stub
		super.testCountFilterLongManualID();
	}

	@Override
	public void testCountFilterStringID() {
		// TODO Auto-generated method stub
		super.testCountFilterStringID();
	}



}
