package siena.base.test;

import static siena.Json.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;
import siena.Json;
import siena.PersistenceManager;
import siena.Query;

public abstract class BaseTest extends TestCase {
	
	private PersistenceManager pm;

	private static Person TESLA = new Person("Nikola", "Tesla", "Smiljam", 1);
	private static Person CURIE = new Person("Marie", "Curie", "Warsaw", 2);
	private static Person EINSTEIN = new Person("Albert", "Einstein", "Ulm", 3);
	
	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	
	public abstract boolean supportsAutoincrement();
	
	public abstract boolean supportsMultipleKeys();
	
	public abstract boolean mustFilterToOrder();
	
	public Query<Person> queryPersonOrderBy(String order, Object value, boolean desc) {
		Query<Person> query = pm.createQuery(Person.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}
	
	public void testCount() {
		assertEquals(3, pm.createQuery(Person.class).count());
	}

	public void testFetch() {
		List<Person> people = queryPersonOrderBy("n", 0, false).fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(TESLA, people.get(0));
		assertEquals(CURIE, people.get(1));
		assertEquals(EINSTEIN, people.get(2));
	}
	
	public void testFetchKeys() {
		List<Person> people = queryPersonOrderBy("n", 0, false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(TESLA.id, people.get(0).id);
		assertEquals(CURIE.id, people.get(1).id);
		assertEquals(EINSTEIN.id, people.get(2).id);
	}

	public void testFetchOrder() {
		List<Person> people = queryPersonOrderBy("firstName", "", false).fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(EINSTEIN, people.get(0));
		assertEquals(CURIE, people.get(1));
		assertEquals(TESLA, people.get(2));
	}
	
	public void testFetchOrderKeys() {
		List<Person> people = queryPersonOrderBy("firstName", "", false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(EINSTEIN.id, people.get(0).id);
		assertEquals(CURIE.id, people.get(1).id);
		assertEquals(TESLA.id, people.get(2).id);
	}

	public void testFetchOrderDesc() {
		List<Person> people = queryPersonOrderBy("lastName", "", true).fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(TESLA, people.get(0));
		assertEquals(EINSTEIN, people.get(1));
		assertEquals(CURIE, people.get(2));
	}

	public void testFetchOrderDescKeys() {
		List<Person> people = queryPersonOrderBy("lastName", "", true).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(TESLA.id, people.get(0).id);
		assertEquals(EINSTEIN.id, people.get(1).id);
		assertEquals(CURIE.id, people.get(2).id);
	}
	
	public void testFetchOrderOnId() {
		List<Person> people = queryPersonOrderBy("id", "", false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(TESLA.id, people.get(0).id);
		assertEquals(CURIE.id, people.get(1).id);
		assertEquals(EINSTEIN.id, people.get(2).id);
	}
		
	public void testFetchOrderOnIdDesc() {
		List<Person> people = queryPersonOrderBy("id", "", true).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(EINSTEIN.id, people.get(0).id);
		assertEquals(CURIE.id, people.get(1).id);
		assertEquals(TESLA.id, people.get(2).id);
	}
	
	public void testFilterOperatorEqual() {
		Person person = pm.createQuery(Person.class).filter("firstName", "Albert").get();
		assertNotNull(person);
		assertEquals(EINSTEIN, person);
	}

	public void testFilterOperatorNotEqual() {
		List<Person> people = pm.createQuery(Person.class).filter("n!=", 3).order("n").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(TESLA, people.get(0));
		assertEquals(CURIE, people.get(1));
	}

	public void testFilterOperatorLessThan() {
		List<Person> people = pm.createQuery(Person.class).filter("n<", 3).order("n").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(TESLA, people.get(0));
		assertEquals(CURIE, people.get(1));
	}
	
	public void testFilterOperatorLessThanOrEqual() {
		List<Person> people = pm.createQuery(Person.class).filter("n<=", 3).order("n").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(TESLA, people.get(0));
		assertEquals(CURIE, people.get(1));
		assertEquals(EINSTEIN, people.get(2));		
	}
	
	public void testFilterOperatorMoreThan() {
		List<Person> people = pm.createQuery(Person.class).filter("n>", 1).order("n").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(CURIE, people.get(0));
		assertEquals(EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanOrEqual() {
		List<Person> people = pm.createQuery(Person.class).filter("n>=", 1).order("n").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(TESLA, people.get(0));
		assertEquals(CURIE, people.get(1));
		assertEquals(EINSTEIN, people.get(2));
	}

	public void testCountFilter() {
		assertEquals(2, pm.createQuery(Person.class).filter("n<", 3).count());
	}

	public void testFetchLimit() {
		List<Person> people = queryPersonOrderBy("n", 0, false).fetch(1);

		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(TESLA, people.get(0));
	}

	@Deprecated
	public void testCountLimit() {
		assertEquals(1, pm.createQuery(Person.class).filter("n<", 3).count(1));
	}

	public void testFetchLimitOffset() {
		Query<Person> query = queryPersonOrderBy("n", 0, false);
		query.fetch(1);
		List<Person> people = query.fetch(2, query.nextOffset());

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(CURIE, people.get(0));
		assertEquals(EINSTEIN, people.get(1));
	}

	@Deprecated
	public void testCountLimitOffset() {
		Query<Person> query = queryPersonOrderBy("n", 0, false);
		query.fetch(1);
		assertEquals(2, query.count(2, query.nextOffset()));
	}

	public void testInsert() {
		Person maxwell = new Person();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);

		List<Person> people = queryPersonOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(TESLA, people.get(0));
		assertEquals(CURIE, people.get(1));
		assertEquals(EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}

	public void testGet() {
		Person curie = getPerson(CURIE.id);
		assertEquals(CURIE, curie);
	}

	public void testUpdate() {
		Person curie = getPerson(CURIE.id);
		curie.lastName = "Sklodowska–Curie";
		pm.update(curie);
		Person curie2 = getPerson(CURIE.id);
		assertEquals(curie2, curie);
	}

	public void testDelete() {
		Person curie = getPerson(CURIE.id);
		pm.delete(curie);
		
		List<Person> people = queryPersonOrderBy("n", 0, false).fetch();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(TESLA, people.get(0));
		assertEquals(EINSTEIN, people.get(1));
	}

	// FIXME
	public void testIter1() {
		/*Iterable<Person> people = pm.createQuery(Person.class).iter("n", 3);

		assertNotNull(people);

		Person[] array = new Person[] { TESLA, CURIE, EINSTEIN };

		int i = 0;
		for (Person PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}*/
	}

	// FIXME
	public void testIter2() {
		/*Iterable<Person> people = pm.createQuery(Person.class).iter("n", 2);

		assertNotNull(people);

		Person[] array = new Person[] { TESLA, CURIE, EINSTEIN };

		int i = 0;
		for (Person PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}*/
	}
	
	public void testIterFull() {
		Iterable<Person> people = pm.createQuery(Person.class).iter();

		assertNotNull(people);

		Person[] array = new Person[] { TESLA, CURIE, EINSTEIN };

		int i = 0;
		for (Person PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}

	public void testIterLimit() {
		Iterable<Person> people = pm.createQuery(Person.class).iter(2);

		assertNotNull(people);

		Person[] array = new Person[] { TESLA, CURIE };

		int i = 0;
		for (Person PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testIterLimitOffset() {
		Iterable<Person> people = pm.createQuery(Person.class).iter(2, 1);

		assertNotNull(people);

		Person[] array = new Person[] { CURIE, EINSTEIN };

		int i = 0;
		for (Person PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testIterFilter() {
		Iterable<Person> people = pm.createQuery(Person.class).filter("n>", 1).iter();

		assertNotNull(people);

		Person[] array = new Person[] { CURIE, EINSTEIN };

		int i = 0;
		for (Person PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testIterFilterLimit() {
		Iterable<Person> people = pm.createQuery(Person.class).filter("n>", 1).iter(1);

		assertNotNull(people);

		Person[] array = new Person[] { CURIE };

		int i = 0;
		for (Person PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testIterFilterLimitOffset() {
		Iterable<Person> people = pm.createQuery(Person.class).filter("n>", 1).iter(2, 1);

		assertNotNull(people);

		Person[] array = new Person[] { EINSTEIN };

		int i = 0;
		for (Person PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testOrderId() {
		List<Person> people = queryPersonOrderBy("id", "", false).fetch();
		assertEquals(3, people.size());
	}
	
	public void testGetObjectNotFound() {
		try {
			getPerson("");
			fail();
		} catch(Exception e) {
			System.out.println("Everything is OK");
		}
		
		assertNull(pm.createQuery(Person.class).filter("firstName", "John").get());
	}
	
	public void testDeleteObjectNotFound() {
		try {
			Person p = new Person();
			pm.delete(p);
			fail();
		} catch(Exception e) {
			System.out.println("Everything is OK");
		}
	}
	
	public void testAutoincrement() {
		if(!supportsAutoincrement()) return;

		AutoInc first = new AutoInc();
		first.name = "first";
		pm.insert(first);
		assertTrue(first.id > 0);

		AutoInc second = new AutoInc();
		second.name = "second";
		pm.insert(second);
		assertTrue(second.id > 0);
		
		assertTrue(second.id > first.id);
	}
	
	public void testRelationship() {
		Discovery radioactivity = new Discovery("Radioactivity", CURIE);
		Discovery relativity = new Discovery("Relativity", EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", TESLA);
		Discovery foo = new Discovery(null, TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(teslaCoil);
		pm.insert(foo);

		Discovery relativity2 = pm.createQuery(Discovery.class).filter("discoverer", EINSTEIN).get();
		assertTrue(relativity.name.equals(relativity2.name));
		
		Discovery foo2 = pm.createQuery(Discovery.class).filter("name", null).get();
		assertTrue(foo.id.equals(foo2.id));
	}
	
	public void testMultipleKeys() {
		if(!supportsMultipleKeys()) return;
		
		MultipleKeys a = new MultipleKeys();
		a.id1 = "aid1";
		a.id2 = "aid2";
		a.name = "first";
		a.parent = null;
		pm.insert(a);

		MultipleKeys b = new MultipleKeys();
		b.id1 = "bid1";
		b.id2 = "bid2";
		b.name = "second";
		b.parent = null;
		pm.insert(b);
		
		b.parent = a;
		pm.update(b);
	}
	
	public void testDataTypesNull() {
		DataTypes dataTypes = new DataTypes();
		pm.insert(dataTypes);
		
		assertEqualsDataTypes(dataTypes, pm.createQuery(DataTypes.class).get());
	}
	
	public void testDataTypesNotNull() {
		char[] c = new char[501];
		Arrays.fill(c, 'x');
		
		DataTypes dataTypes = new DataTypes();
		dataTypes.typeByte = 1;
		dataTypes.typeShort = 2;
		dataTypes.typeInt = 3;
		dataTypes.typeLong = 4;
		dataTypes.typeFloat = 5;
		dataTypes.typeDouble = 6;
		dataTypes.typeDate = new Date();
		dataTypes.typeString = "hello";
		dataTypes.typeLargeString = new String(c);
		dataTypes.typeJson = map().put("foo", "bar");
		dataTypes.addresses = new ArrayList<Address>();
		dataTypes.addresses.add(new Address("Castellana", "Madrid"));
		dataTypes.addresses.add(new Address("Diagonal", "Barcelona"));
		dataTypes.contacts = new HashMap<String, Contact>();
		dataTypes.contacts.put("id1", new Contact("Somebody", Arrays.asList("foo", "bar")));
		
		// Blob
		dataTypes.typeBlob = new byte[] { 
				(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
				(byte)0x10,	(byte)0X11, (byte)0xF0, (byte)0xF1, 
				(byte)0xF9,	(byte)0xFF };
		pm.insert(dataTypes);
		
		// to test that fields are read back correctly
		pm.createQuery(DataTypes.class).filter("id", dataTypes.id).get();
		
		DataTypes same = pm.createQuery(DataTypes.class).get();
		assertEqualsDataTypes(dataTypes, same);
	}
	
	private void assertEqualsDataTypes(DataTypes dataTypes, DataTypes same) {
		assertEquals(dataTypes.id, same.id);
		assertEquals(dataTypes.typeByte, same.typeByte);
		assertEquals(dataTypes.typeShort, same.typeShort);
		assertEquals(dataTypes.typeInt, same.typeInt);
		assertEquals(dataTypes.typeLong, same.typeLong);
		assertEquals(dataTypes.typeFloat, same.typeFloat);
		assertEquals(dataTypes.typeDouble, same.typeDouble);
		if(dataTypes.typeDate != null && same.typeDate != null) {
			assertEquals(dataTypes.typeDate.getTime() / 1000, same.typeDate.getTime() / 1000);
		} else {
			assertNull(dataTypes.typeDate);
			assertNull(same.typeDate);
		}
		assertEquals(dataTypes.typeString, same.typeString);
		assertEquals(dataTypes.typeLargeString, same.typeLargeString);
		assertEquals(dataTypes.typeJson, same.typeJson);

		if(dataTypes.addresses != null && same.addresses != null) {
			assertEquals(dataTypes.addresses.size(), same.addresses.size());
			int size = dataTypes.addresses.size();
			for (int i=0; i<size; i++) {
				assertEquals(dataTypes.addresses.get(i), same.addresses.get(i));
			}
		} else {
			assertNull(dataTypes.addresses);
			assertNull(same.addresses);
		}

		if(dataTypes.contacts != null && same.contacts != null) {
			assertEquals(dataTypes.contacts.size(), same.contacts.size());
			for (String key : dataTypes.contacts.keySet()) {
				assertEquals(dataTypes.contacts.get(key).name, same.contacts.get(key).name);
				
				List<String> a = dataTypes.contacts.get(key).tags;
				List<String> b = same.contacts.get(key).tags;
				
				assertEquals(a.size(), b.size());
				
				for (String string : b) {
					assertTrue(a.contains(string));
				}
			}
		} else {
			assertNull(dataTypes.contacts);
			assertNull(same.contacts);
		}
		if(dataTypes.typeBlob != null && same.typeBlob != null) {
			assertTrue(Arrays.equals(dataTypes.typeBlob, same.typeBlob));
		} else {
			assertNull(dataTypes.typeBlob);
			assertNull(same.typeBlob);
		}
	}
	
	public void testQueryDelete() {
		Discovery radioactivity = new Discovery("Radioactivity", CURIE);
		Discovery relativity = new Discovery("Relativity", EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(teslaCoil);

		int n = pm.createQuery(Discovery.class).delete();
		assertEquals(3, n);
	}
	
	public void testQueryDeleteFiltered() {
		Discovery radioactivity = new Discovery("Radioactivity", CURIE);
		Discovery relativity = new Discovery("Relativity", EINSTEIN);
		Discovery foo = new Discovery("Foo", EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);

		int n = pm.createQuery(Discovery.class).filter("discoverer", EINSTEIN).delete();
		assertEquals(2, n);
	}

	public void testJoin() {
		Discovery radioactivity = new Discovery("Radioactivity", CURIE);
		Discovery relativity = new Discovery("Relativity", EINSTEIN);
		Discovery foo = new Discovery("Foo", EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);
		
		List<Discovery> res = pm.createQuery(Discovery.class).join("discoverer").fetch();
		assertEquals(4, res.size());
		assertEquals(radioactivity, res.get(0));
		assertEquals(relativity, res.get(1));
		assertEquals(foo, res.get(2));
		assertEquals(teslaCoil, res.get(3));
		
		assertEquals(CURIE, res.get(0).discoverer);
		assertEquals(EINSTEIN, res.get(1).discoverer);
		assertEquals(EINSTEIN, res.get(2).discoverer);
		assertEquals(TESLA, res.get(3).discoverer);
	}
	
	public void testJoinSortFields() {
		Discovery radioactivity = new Discovery("Radioactivity", CURIE);
		Discovery relativity = new Discovery("Relativity", EINSTEIN);
		Discovery foo = new Discovery("Foo", EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);
		
		List<Discovery> res = pm.createQuery(Discovery.class).join("discoverer", "firstName").fetch();
		assertEquals(4, res.size());
		assertEquals(radioactivity, res.get(0));
		assertEquals(relativity, res.get(1));
		assertEquals(foo, res.get(2));
		assertEquals(teslaCoil, res.get(3));
		
		assertEquals(CURIE, res.get(0).discoverer);
		assertEquals(EINSTEIN, res.get(1).discoverer);
		assertEquals(EINSTEIN, res.get(2).discoverer);
		assertEquals(TESLA, res.get(3).discoverer);
	}
	
	
	public void testJoinAnnotation() {
		Discovery4Join radioactivity = new Discovery4Join("Radioactivity", CURIE, TESLA);
		Discovery4Join relativity = new Discovery4Join("Relativity", EINSTEIN, TESLA);
		Discovery4Join foo = new Discovery4Join("Foo", EINSTEIN, EINSTEIN);
		Discovery4Join teslaCoil = new Discovery4Join("Tesla Coil", TESLA, CURIE);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);
		
		List<Discovery4Join> res = pm.createQuery(Discovery4Join.class).fetch();
		assertEquals(4, res.size());
		assertEquals(radioactivity, res.get(0));
		assertEquals(relativity, res.get(1));
		assertEquals(foo, res.get(2));
		assertEquals(teslaCoil, res.get(3));
		
		assertEquals(CURIE, res.get(0).discovererJoined);
		assertEquals(EINSTEIN, res.get(1).discovererJoined);
		assertEquals(EINSTEIN, res.get(2).discovererJoined);
		assertEquals(TESLA, res.get(3).discovererJoined);

		assertEquals(TESLA.id, res.get(0).discovererNotJoined.id);
		assertEquals(TESLA.id, res.get(1).discovererNotJoined.id);
		assertEquals(EINSTEIN.id, res.get(2).discovererNotJoined.id);
		assertEquals(CURIE.id, res.get(3).discovererNotJoined.id);
		
		assertTrue(res.get(0).discovererNotJoined.isOnlyIdFilled());
		assertTrue(res.get(1).discovererNotJoined.isOnlyIdFilled());
		assertTrue(res.get(2).discovererNotJoined.isOnlyIdFilled());
		assertTrue(res.get(3).discovererNotJoined.isOnlyIdFilled());
	}

	
	private Person getPerson(String id) {
		Person p = new Person();
		p.id = id;
		pm.get(p);
		return p;
	}

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(Person.class);
		if(supportsAutoincrement())
			classes.add(AutoInc.class);
		if(supportsMultipleKeys())
			classes.add(MultipleKeys.class);
		classes.add(Discovery.class);
		classes.add(Discovery4Join.class);
		classes.add(DataTypes.class);
		pm = createPersistenceManager(classes);
		
		for (Class<?> clazz : classes) {
			List<?> items = pm.createQuery(clazz).fetch();
			for (Object object : items) {
				pm.delete(object);
			}
		}
		
		pm.insert(TESLA);
		pm.insert(CURIE);
		pm.insert(EINSTEIN);
				
	}

}
