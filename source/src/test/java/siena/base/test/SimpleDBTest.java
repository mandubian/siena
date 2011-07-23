package siena.base.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import siena.PersistenceManager;
import siena.SienaException;
import siena.base.test.model.PersonStringID;
import siena.sdb.SdbPersistenceManager;

public class SimpleDBTest extends AbstractTest {

	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes)
			throws Exception {
		
		Properties p = new Properties();
		// don't want to give my AWS ID/secrets :D
	    p.load(new FileInputStream("/home/mandubian/work/aws/siena-aws.properties"));
		
		//p.setProperty("implementation", "siena.sdb.SdbPersistenceManager");
		//p.setProperty("awsAccessKeyId", "");
		//p.setProperty("awsSecretAccessKey", "");
		//p.setProperty("prefix", "siena_devel_");
		
		SdbPersistenceManager sdb = new SdbPersistenceManager();
		sdb.init(p);
		
		return sdb;
	}

	/*@Override
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
	}*/

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createClasses(List<Class<?>> classes) {
		classes.add(PersonStringID.class);
	}

	public void testInsertPersonStringID() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertEquals(maxwell.id, "MAXWELL");
		
		PersonStringID maxwellbis = new PersonStringID();
		maxwellbis.id = "MAXWELL";
		pm.get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.update(maxwell);

		maxwellbis = new PersonStringID();
		maxwellbis.id = "MAXWELL";
		pm.get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
		
		pm.delete(maxwell);
		try {
			pm.get(maxwell);
		}catch(SienaException ex){
			return;
		}
		fail();
	}
	
	public void testInsertPersonStringIDMultiple() {
		// INSERTS
		ArrayList<PersonStringID> l = new ArrayList<PersonStringID>();
		for(int i=0; i<10; i++){
			PersonStringID maxwell = new PersonStringID();
			maxwell.id = "MAXWELL"+i;
			maxwell.firstName = "James"+i;
			maxwell.lastName = "Maxwell"+i;
			maxwell.city = "Edinburgh"+i;
			maxwell.n = i;
			l.add(maxwell);
		}
		
		int nb = pm.insert(l);
		assertEquals(10, nb);
		
		// GETS
		ArrayList<PersonStringID> l2 = new ArrayList<PersonStringID>();
		for(int i=0; i<10; i++){
			PersonStringID maxwell = new PersonStringID();
			maxwell.id = "MAXWELL"+i;
			l2.add(maxwell);
		}
		nb = pm.option(SdbPersistenceManager.CONSISTENT_READ).get(l2);
		assertEquals(10, nb);

		for(int i=0; i<10; i++){
			assertEquals(l.get(i), l2.get(i));
		}
		
		// UPDATES
		for(int i=0; i<10; i++){
			PersonStringID maxwell = l.get(i);
			maxwell.firstName = "James UPD"+i;
			maxwell.lastName = "Maxwell UPD"+i;
			maxwell.city = "Edinburgh UPD"+i;
			maxwell.n = i+5;
		}
		
		nb = pm.update(l);
		assertEquals(10, nb);
		
		nb = pm.option(SdbPersistenceManager.CONSISTENT_READ).get(l2);
		assertEquals(10, nb);

		for(int i=0; i<10; i++){
			assertEquals(l.get(i), l2.get(i));
		}
	}

	
}
