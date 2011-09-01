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

	@Override
	public boolean supportsAutoincrement() {
		return false;
	}

	@Override
	public boolean supportsMultipleKeys() {
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
	
	/*
	@Override
	public boolean mustFilterToOrder() {
		return true;
	}*/

	@Override
	public boolean supportsTransaction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createClasses(List<Class<?>> classes) {
		classes.add(PersonStringID.class);
	}

	@Override
	public void postInit() {
		// TODO Auto-generated method stub
		
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
		pm.option(SdbPersistenceManager.CONSISTENT_READ).get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.update(maxwell);

		maxwellbis = new PersonStringID();
		maxwellbis.id = "MAXWELL";
		pm.option(SdbPersistenceManager.CONSISTENT_READ).get(maxwellbis);
		
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
		
		// DELETES
		pm.delete(l);
		
		nb = pm.option(SdbPersistenceManager.CONSISTENT_READ).get(l2);
		assertEquals(0, nb);

	}

	public void testSavePersonStringIDMultiple() {
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
		
		int nb = pm.save(l);
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
		
		nb = pm.save(l);
		assertEquals(10, nb);
		
		nb = pm.option(SdbPersistenceManager.CONSISTENT_READ).get(l2);
		assertEquals(10, nb);

		for(int i=0; i<10; i++){
			assertEquals(l.get(i), l2.get(i));
		}
		
		// DELETES
		pm.delete(l);
		
		nb = pm.option(SdbPersistenceManager.CONSISTENT_READ).get(l2);
		assertEquals(0, nb);

	}
	
	public void testGetByKeyPersonStringID() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertEquals(maxwell.id, "MAXWELL");
		
		PersonStringID maxwellbis = pm.option(SdbPersistenceManager.CONSISTENT_READ).getByKey(PersonStringID.class, maxwell.id);
		
		assertEquals(maxwell, maxwellbis);
		
		pm.delete(maxwell);
	}
	
	public void testGetByKeysPersonStringID() {
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
		
		int nb = pm.save(l);
		assertEquals(10, nb);
		
		ArrayList<String> keys = new ArrayList<String>();
		for(int i=0; i<10; i++){
			keys.add(l.get(i).id);
		}
		
		List<PersonStringID> l2 = pm.option(SdbPersistenceManager.CONSISTENT_READ).getByKeys(PersonStringID.class, keys);		

		for(int i=0; i<10; i++){
			assertEquals(l.get(i), l2.get(i));
		}
		
		// DELETES
		pm.delete(l);

	}
	
	public void testSimpleCountPersonStringID() {
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
		
		int nb = pm.save(l);
		assertEquals(10, nb);
		
		Query<PersonStringID> query = pm.createQuery(PersonStringID.class);
		assertEquals(10, pm.option(SdbPersistenceManager.CONSISTENT_READ).count(query));		

		// DELETES
		pm.delete(l);

	}
	
	public void testSimpleCountFilterPersonStringID() {
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
		l.get(5).firstName = "BOB";
		l.get(7).firstName = "BOB";
		int nb = pm.save(l);
		assertEquals(10, nb);
		
		Query<PersonStringID> query = pm.createQuery(PersonStringID.class);
		assertEquals(2, pm.option(SdbPersistenceManager.CONSISTENT_READ).count(query.filter("firstName", "BOB")));		
		// DELETES
		pm.delete(l);

	}

	public void testQueryFetchPersonStringID() {
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
		
		int nb = pm.save(l);
		assertEquals(10, nb);
		
		List<PersonStringID> l2 = pm.option(SdbPersistenceManager.CONSISTENT_READ).createQuery(PersonStringID.class).fetch();

		for(int i=0; i<10; i++){
			assertEquals(l.get(i), l2.get(i));
		}
		
		// DELETES
		pm.delete(l);

	}
	
	public void testQueryFetchLimitPersonStringID() {
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
		
		int nb = pm.save(l);
		assertEquals(10, nb);
		
		List<PersonStringID> l2 = pm.option(SdbPersistenceManager.CONSISTENT_READ).createQuery(PersonStringID.class).fetch(5);
		assertEquals(5, l2.size());
		for(int i=0; i<5; i++){
			assertEquals(l.get(i), l2.get(i));
		}
		
		// DELETES
		pm.delete(l);

	}
	
	public void testQueryFetchLimitOffsetPersonStringID() {
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
		
		int nb = pm.save(l);
		assertEquals(10, nb);
		
		List<PersonStringID> l2 = pm.option(SdbPersistenceManager.CONSISTENT_READ).createQuery(PersonStringID.class).fetch(5,2);
		assertEquals(5, l2.size());
		for(int i=0; i<5; i++){
			assertEquals(l.get(i+2), l2.get(i));
		}
		
		// DELETES
		pm.delete(l);

	}
	
	public void testQueryFetchOrderPersonStringID() {
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
		
		int nb = pm.save(l);
		assertEquals(10, nb);
		
		List<PersonStringID> l2 = pm.option(SdbPersistenceManager.CONSISTENT_READ).createQuery(PersonStringID.class).order("-firstName").fetch();

		for(int i=0; i<10; i++){
			assertEquals(l.get(9-i), l2.get(i));
		}
		
		// DELETES
		pm.delete(l);

	}
	
	public void testQueryPaginatePersonStringID() {
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
		
		int nb = pm.save(l);
		assertEquals(10, nb);
		
		List<PersonStringID> l2 = pm.option(SdbPersistenceManager.CONSISTENT_READ).createQuery(PersonStringID.class).fetch(5,2);
		assertEquals(5, l2.size());
		for(int i=0; i<5; i++){
			assertEquals(l.get(i+2), l2.get(i));
		}
		
		// DELETES
		pm.delete(l);

	}

	@Override
	public boolean supportsListStore() {
		// TODO Auto-generated method stub
		return false;
	}


}
