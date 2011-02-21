package siena.base.test;

import static siena.Json.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManager;
import siena.Query;
import siena.base.test.model.Address;
import siena.base.test.model.AutoInc;
import siena.base.test.model.Contact;
import siena.base.test.model.DataTypes;
import siena.base.test.model.DataTypes.EnumLong;
import siena.base.test.model.Discovery;
import siena.base.test.model.Discovery4Join;
import siena.base.test.model.DiscoveryPrivate;
import siena.base.test.model.MultipleKeys;
import siena.base.test.model.PersonLongAutoID;
import siena.base.test.model.PersonLongManualID;
import siena.base.test.model.PersonStringID;
import siena.base.test.model.PersonUUID;

public abstract class BaseTest extends TestCase {
	
	private PersistenceManager pm;

	private static PersonUUID UUID_TESLA = new PersonUUID("Nikola", "Tesla", "Smiljam", 1);
	private static PersonUUID UUID_CURIE = new PersonUUID("Marie", "Curie", "Warsaw", 2);
	private static PersonUUID UUID_EINSTEIN = new PersonUUID("Albert", "Einstein", "Ulm", 3);
	
	private static PersonLongAutoID LongAutoID_TESLA = new PersonLongAutoID("Nikola", "Tesla", "Smiljam", 1);
	private static PersonLongAutoID LongAutoID_CURIE = new PersonLongAutoID("Marie", "Curie", "Warsaw", 2);
	private static PersonLongAutoID LongAutoID_EINSTEIN = new PersonLongAutoID("Albert", "Einstein", "Ulm", 3);

	private static PersonLongManualID LongManualID_TESLA = new PersonLongManualID(1L, "Nikola", "Tesla", "Smiljam", 1);
	private static PersonLongManualID LongManualID_CURIE = new PersonLongManualID(2L, "Marie", "Curie", "Warsaw", 2);
	private static PersonLongManualID LongManualID_EINSTEIN = new PersonLongManualID(3L, "Albert", "Einstein", "Ulm", 3);
	
	private static PersonStringID StringID_TESLA = new PersonStringID("TESLA", "Nikola", "Tesla", "Smiljam", 1);
	private static PersonStringID StringID_CURIE = new PersonStringID("CURIE", "Marie", "Curie", "Warsaw", 2);
	private static PersonStringID StringID_EINSTEIN = new PersonStringID("EINSTEIN", "Albert", "Einstein", "Ulm", 3);

	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	
	public abstract boolean supportsAutoincrement();
	
	public abstract boolean supportsMultipleKeys();
	
	public abstract boolean mustFilterToOrder();
	
	private List<PersonUUID> getOrderedPersonUUIDs() {
		ArrayList<PersonUUID> l = new ArrayList<PersonUUID>() {{ 
			add(UUID_TESLA); 
			add(UUID_CURIE);
			add(UUID_EINSTEIN);
		}};

		Collections.sort(l, new Comparator<PersonUUID>(){
			public int compare(PersonUUID p1,PersonUUID p2){
                return p1.id.compareTo(p2.id);
			}
		});
		
		return l;
	}
	public Query<PersonUUID> queryPersonUUIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonUUID> query = pm.createQuery(PersonUUID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}

	public Query<PersonLongAutoID> queryPersonLongAutoIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}
	
	public Query<PersonLongManualID> queryPersonLongManualIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonLongManualID> query = pm.createQuery(PersonLongManualID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}
	
	public Query<PersonStringID> queryPersonStringIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonStringID> query = pm.createQuery(PersonStringID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}
	
	public void testCount() {
		assertEquals(3, pm.createQuery(PersonUUID.class).count());
	}

	public void testFetch() {
		List<PersonUUID> people = queryPersonUUIDOrderBy("n", 0, false).fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));
	}
	
	public void testFetchKeys() {
		List<PersonUUID> people = queryPersonUUIDOrderBy("n", 0, false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA.id, people.get(0).id);
		assertEquals(UUID_CURIE.id, people.get(1).id);
		assertEquals(UUID_EINSTEIN.id, people.get(2).id);
		
		assertTrue(people.get(0).isOnlyIdFilled());
		assertTrue(people.get(1).isOnlyIdFilled());
		assertTrue(people.get(2).isOnlyIdFilled());

	}

	public void testFetchOrder() {
		List<PersonUUID> people = queryPersonUUIDOrderBy("firstName", "", false).fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_EINSTEIN, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_TESLA, people.get(2));
	}
	
	public void testFetchOrderKeys() {
		List<PersonUUID> people = queryPersonUUIDOrderBy("firstName", "", false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_EINSTEIN.id, people.get(0).id);
		assertEquals(UUID_CURIE.id, people.get(1).id);
		assertEquals(UUID_TESLA.id, people.get(2).id);
	}

	public void testFetchOrderDesc() {
		List<PersonUUID> people = queryPersonUUIDOrderBy("lastName", "", true).fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
		assertEquals(UUID_CURIE, people.get(2));
	}

	public void testFetchOrderDescKeys() {
		List<PersonUUID> people = queryPersonUUIDOrderBy("lastName", "", true).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA.id, people.get(0).id);
		assertEquals(UUID_EINSTEIN.id, people.get(1).id);
		assertEquals(UUID_CURIE.id, people.get(2).id);
	}
	
	public void testFetchOrderOnLongAutoId() {
		List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongAutoID_TESLA.id, people.get(0).id);
		assertEquals(LongAutoID_CURIE.id, people.get(1).id);
		assertEquals(LongAutoID_EINSTEIN.id, people.get(2).id);
	}

	public void testFetchOrderOnLongManualId() {
		List<PersonLongManualID> people = queryPersonLongManualIDOrderBy("id", "", false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongManualID_TESLA.id, people.get(0).id);
		assertEquals(LongManualID_CURIE.id, people.get(1).id);
		assertEquals(LongManualID_EINSTEIN.id, people.get(2).id);
	}
	
	public void testFetchOrderOnStringId() {
		List<PersonStringID> people = queryPersonStringIDOrderBy("id", "", false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(StringID_CURIE.id, people.get(0).id);
		assertEquals(StringID_EINSTEIN.id, people.get(1).id);
		assertEquals(StringID_TESLA.id, people.get(2).id);
	}
		
	public void testFetchOrderOnUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		List<PersonUUID> people = queryPersonUUIDOrderBy("id", "", false).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(l.get(0).id, people.get(0).id);
		assertEquals(l.get(1).id, people.get(1).id);
		assertEquals(l.get(2).id, people.get(2).id);
	}
	
	public void testFetchOrderOnLongAutoIdDesc() {
		List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", true).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongAutoID_EINSTEIN.id, people.get(0).id);
		assertEquals(LongAutoID_CURIE.id, people.get(1).id);
		assertEquals(LongAutoID_TESLA.id, people.get(2).id);
	}
		
	public void testFetchOrderOnLongManualIdDesc() {
		List<PersonLongManualID> people = queryPersonLongManualIDOrderBy("id", "", true).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongManualID_EINSTEIN.id, people.get(0).id);
		assertEquals(LongManualID_CURIE.id, people.get(1).id);
		assertEquals(LongManualID_TESLA.id, people.get(2).id);
	}
	
	public void testFetchOrderOnStringIdDesc() {
		List<PersonStringID> people = queryPersonStringIDOrderBy("id", "", true).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(StringID_TESLA.id, people.get(0).id);
		assertEquals(StringID_EINSTEIN.id, people.get(1).id);
		assertEquals(StringID_CURIE.id, people.get(2).id);
	}
	
	public void testFetchOrderOnUUIDDesc() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		List<PersonUUID> people = queryPersonUUIDOrderBy("id", "", true).fetchKeys();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(l.get(2).id, people.get(0).id);
		assertEquals(l.get(1).id, people.get(1).id);
		assertEquals(l.get(0).id, people.get(2).id);
	}
	
	
	public void testFilterOperatorEqualString() {
		PersonUUID person = pm.createQuery(PersonUUID.class).filter("firstName", "Albert").get();
		assertNotNull(person);
		assertEquals(UUID_EINSTEIN, person);
	}
	
	public void testFilterOperatorEqualInt() {
		PersonUUID person = pm.createQuery(PersonUUID.class).filter("n", 3).get();
		assertNotNull(person);
		assertEquals(UUID_EINSTEIN, person);
	}
	
	public void testFilterOperatorEqualUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
	
		PersonUUID person = pm.createQuery(PersonUUID.class).filter("id", l.get(0).id).get();
		assertNotNull(person);
		assertEquals(l.get(0), person);
	}
	
	public void testFilterOperatorEqualLongAutoID() {
		PersonLongAutoID person = pm.createQuery(PersonLongAutoID.class).filter("id", LongAutoID_EINSTEIN.id).get();
		assertNotNull(person);
		assertEquals(LongAutoID_EINSTEIN, person);
	}
		
	public void testFilterOperatorEqualLongManualID() {
		PersonLongManualID person = pm.createQuery(PersonLongManualID.class).filter("id", 3L).get();
		assertNotNull(person);
		assertEquals(LongManualID_EINSTEIN, person);
	}
	
	public void testFilterOperatorNotEqualString() {
		List<PersonStringID> people = pm.createQuery(PersonStringID.class).filter("firstName!=", "Albert").order("firstName").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}
	
	public void testFilterOperatorNotEqualInt() {
		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("n!=", 3).order("n").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
	}

	public void testFilterOperatorNotEqualUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("id!=", l.get(0).id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(l.get(1), people.get(0));
		assertEquals(l.get(2), people.get(1));
	}
	
	public void testFilterOperatorNotEqualLongAutoID() {
		List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id!=", LongAutoID_EINSTEIN.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
	}

	public void testFilterOperatorNotEqualLongManualID() {
		List<PersonLongManualID> people = pm.createQuery(PersonLongManualID.class).filter("id!=", 3L).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorNotEqualStringID() {
		List<PersonStringID> people = pm.createQuery(PersonStringID.class).filter("id!=", StringID_EINSTEIN.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}

	public void testFilterOperatorIn() {
		List<PersonUUID> people = 
			pm.createQuery(PersonUUID.class)
				.filter("n IN", new ArrayList<Integer>(){{ 
					add(2);
					add(3);
				}})
				.order("n")
				.fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_CURIE, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorInOrder() {
		List<PersonUUID> people = 
			pm.createQuery(PersonUUID.class)
				.filter("n IN", new ArrayList<Integer>(){{ 
					add(3);
					add(2);
				}})
				.order("n")
				.fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_CURIE, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorInForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		
		List<PersonUUID> people = 
			pm.createQuery(PersonUUID.class)
				.filter("id IN", Arrays.asList( l.get(0).id, l.get(1).id))
				.fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
	}
	
	public void testFilterOperatorInForLongAutoID() {
		List<PersonLongAutoID> people = 
			pm.createQuery(PersonLongAutoID.class)
				.filter("id IN", new ArrayList<Long>(){{ 
					add(LongAutoID_TESLA.id);
					add(LongAutoID_CURIE.id);
				}})
				.fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
	}

	public void testFilterOperatorInForLongManualID() {
		List<PersonLongManualID> people = 
			pm.createQuery(PersonLongManualID.class)
				.filter("id IN", new ArrayList<Long>(){{ 
					add(LongManualID_TESLA.id);
					add(LongManualID_CURIE.id);
				}})
				.fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorInForStringID() {
		List<PersonStringID> people = 
			pm.createQuery(PersonStringID.class)
				.filter("id IN", new ArrayList<String>(){{ 
					add(StringID_TESLA.id);
					add(StringID_CURIE.id);
				}})
				.order("id")
				.fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}
	
	public void testFilterOperatorLessThan() {
		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("n<", 3).order("n").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorLessThanForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();

		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("id<", l.get(2).id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		
		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
	}
	
	public void testFilterOperatorLessThanForLongAutoID() {
		List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id<", LongAutoID_EINSTEIN.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorLessThanForLongManualID() {
		List<PersonLongManualID> people = pm.createQuery(PersonLongManualID.class).filter("id<", 3L).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorLessThanForStringID() {
		List<PersonStringID> people = pm.createQuery(PersonStringID.class).filter("id<", StringID_TESLA.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorLessThanOrEqual() {
		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("n<=", 3).order("n").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));		
	}
	
	public void testFilterOperatorLessThanOrEqualForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();

		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("id<=", l.get(2).id).order("id").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		
		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
		assertEquals(l.get(2), people.get(2));
	}
	
	public void testFilterOperatorLessThanOrEqualForLongAutoID() {
		List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id<=", LongAutoID_EINSTEIN.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
		assertEquals(LongAutoID_EINSTEIN, people.get(2));
	}
	
	public void testFilterOperatorLessThanOrEqualForLongManualID() {
		List<PersonLongManualID> people = pm.createQuery(PersonLongManualID.class).filter("id<=", LongManualID_EINSTEIN.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
		assertEquals(LongManualID_EINSTEIN, people.get(2));
	}
	
	public void testFilterOperatorLessThanOrEqualForStringID() {
		List<PersonStringID> people = pm.createQuery(PersonStringID.class).filter("id<=", StringID_TESLA.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_EINSTEIN, people.get(1));
		assertEquals(StringID_TESLA, people.get(2));
	}
	
	
	public void testFilterOperatorMoreThan() {
		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("n>", 1).order("n").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_CURIE, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();

		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("id>", l.get(0).id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		
		assertEquals(l.get(1), people.get(0));
		assertEquals(l.get(2), people.get(1));
	}
	
	public void testFilterOperatorMoreThanForLongAutoID() {
		List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id>", LongAutoID_TESLA.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_CURIE, people.get(0));
		assertEquals(LongAutoID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanForLongManualID() {
		List<PersonLongManualID> people = pm.createQuery(PersonLongManualID.class).filter("id>", LongManualID_TESLA.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_CURIE, people.get(0));
		assertEquals(LongManualID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanForStringID() {
		List<PersonStringID> people = pm.createQuery(PersonStringID.class).filter("id>", StringID_CURIE.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_EINSTEIN, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}

	
	public void testFilterOperatorMoreThanOrEqual() {
		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("n>=", 1).order("n").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));
	}

	public void testFilterOperatorMoreThanOrEqualForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();

		List<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("id>=", l.get(0).id).order("id").fetch();

		assertNotNull(people);
		assertEquals(3, people.size());

		
		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
		assertEquals(l.get(2), people.get(2));
	}
	
	public void testFilterOperatorMoreThanOrEqualForLongAutoID() {
		List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id>=", LongAutoID_CURIE.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_CURIE, people.get(0));
		assertEquals(LongAutoID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanOrEqualForLongManualID() {
		List<PersonLongManualID> people = pm.createQuery(PersonLongManualID.class).filter("id>=", LongManualID_CURIE.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_CURIE, people.get(0));
		assertEquals(LongManualID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanOrEqualForStringID() {
		List<PersonStringID> people = pm.createQuery(PersonStringID.class).filter("id>=", StringID_EINSTEIN.id).order("id").fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_EINSTEIN, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}
	
	public void testCountFilter() {
		assertEquals(2, pm.createQuery(PersonUUID.class).filter("n<", 3).count());
	}

	public void testCountFilterUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		assertEquals(2, pm.createQuery(PersonUUID.class).filter("id<", l.get(2).id).count());
	}
	
	public void testCountFilterLongAutoID() {
		assertEquals(2, pm.createQuery(PersonLongAutoID.class).filter("id<", LongAutoID_EINSTEIN.id).count());
	}

	public void testCountFilterLongManualID() {
		assertEquals(2, pm.createQuery(PersonLongManualID.class).filter("id<", LongManualID_EINSTEIN.id).count());
	}
	
	public void testCountFilterStringID() {
		assertEquals(2, pm.createQuery(PersonStringID.class).filter("id<", StringID_TESLA.id).count());
	}
	
	public void testFetchLimit() {
		List<PersonUUID> people = queryPersonUUIDOrderBy("n", 0, false).fetch(1);

		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(UUID_TESLA, people.get(0));
	}

	public void testFetchLimitUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		List<PersonUUID> people = queryPersonUUIDOrderBy("id", l.get(0), false).fetch(1);

		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(l.get(0), people.get(0));
	}
	
	public void testFetchLimitLongAutoID() {
		List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", 0, false).fetch(1);

		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
	}
	
	public void testFetchLimitLongManualID() {
		List<PersonLongManualID> people = queryPersonLongManualIDOrderBy("id", 0, false).fetch(1);

		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
	}
	
	public void testFetchLimitStringID() {
		List<PersonStringID> people = queryPersonStringIDOrderBy("id", StringID_CURIE, false).fetch(1);

		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(StringID_CURIE, people.get(0));
	}
	
	@Deprecated
	public void testCountLimit() {
		assertEquals(1, pm.createQuery(PersonUUID.class).filter("n<", 3).count(1));
	}

	public void testFetchLimitOffset() {
		Query<PersonUUID> query = queryPersonUUIDOrderBy("n", 0, false);
		query.fetch(1);
		List<PersonUUID> people = query.fetch(2, 1);

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_CURIE, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}

	@Deprecated
	public void testCountLimitOffset() {
		Query<PersonUUID> query = queryPersonUUIDOrderBy("n", 0, false);
		query.fetch(1);
		assertEquals(2, query.count(2, 1));
	}

	public void testInsertUUID() {
		PersonUUID maxwell = new PersonUUID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);

		List<PersonUUID> people = queryPersonUUIDOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}

	public void testInsertLongAutoID() {
		PersonLongAutoID maxwell = new PersonLongAutoID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);

		List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
		assertEquals(LongAutoID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}

	public void testInsertLongManualID() {
		PersonLongManualID maxwell = new PersonLongManualID();
		maxwell.id = 4L;
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertEquals((Long)4L, maxwell.id);

		List<PersonLongManualID> people = queryPersonLongManualIDOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
		assertEquals(LongManualID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}
	
	public void testInsertStringID() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertEquals(maxwell.id, "MAXWELL");

		List<PersonStringID> people = queryPersonStringIDOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(StringID_TESLA, people.get(0));
		assertEquals(StringID_CURIE, people.get(1));
		assertEquals(StringID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}
	
	public void testGetUUID() {
		PersonUUID curie = getPersonUUID(UUID_CURIE.id);
		assertEquals(UUID_CURIE, curie);
	}

	public void testGetLongAutoID() {
		PersonLongAutoID curie = getPersonLongAutoID(LongAutoID_CURIE.id);
		assertEquals(LongAutoID_CURIE, curie);
	}

	public void testGetLongManualID() {
		PersonLongManualID curie = getPersonLongManualID(LongManualID_CURIE.id);
		assertEquals(LongManualID_CURIE, curie);
	}

	public void testGetStringID() {
		PersonStringID curie = getPersonStringID(StringID_CURIE.id);
		assertEquals(StringID_CURIE, curie);
	}

	public void testUpdateUUID() {
		PersonUUID curie = getPersonUUID(UUID_CURIE.id);
		curie.lastName = "Sklodowska–Curie";
		pm.update(curie);
		PersonUUID curie2 = getPersonUUID(UUID_CURIE.id);
		assertEquals(curie2, curie);
	}

	public void testUpdateLongAutoID() {
		PersonLongAutoID curie = getPersonLongAutoID(LongAutoID_CURIE.id);
		curie.lastName = "Sklodowska–Curie";
		pm.update(curie);
		PersonLongAutoID curie2 = getPersonLongAutoID(LongAutoID_CURIE.id);
		assertEquals(curie2, curie);
	}
	
	public void testDeleteUUID() {
		PersonUUID curie = getPersonUUID(UUID_CURIE.id);
		pm.delete(curie);
		
		List<PersonUUID> people = queryPersonUUIDOrderBy("n", 0, false).fetch();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}


	public void testIterFullUUID() {
		Iterable<PersonUUID> people = pm.createQuery(PersonUUID.class).order("n").iter();

		assertNotNull(people);

		ArrayList<PersonUUID> l = new ArrayList<PersonUUID>() {{ 
			add(UUID_TESLA); 
			add(UUID_CURIE);
			add(UUID_EINSTEIN);
		}};
		
		int i = 0;
		for (PersonUUID person : people) {
			assertEquals( l.get(i), person);
			i++;
		}
	}
	
	public void testIterFullLongAutoID() {
		Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter();

		assertNotNull(people);

		PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_TESLA, LongAutoID_CURIE, LongAutoID_EINSTEIN };

		int i = 0;
		for (PersonLongAutoID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}

	public void testIterFullLongManualID() {
		Iterable<PersonLongManualID> people = pm.createQuery(PersonLongManualID.class).order("n").iter();

		assertNotNull(people);

		PersonLongManualID[] array = new PersonLongManualID[] { LongManualID_TESLA, LongManualID_CURIE, LongManualID_EINSTEIN };

		int i = 0;
		for (PersonLongManualID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterFullLongStringID() {
		Iterable<PersonStringID> people = pm.createQuery(PersonStringID.class).order("n").iter();

		assertNotNull(people);

		PersonStringID[] array = new PersonStringID[] { StringID_TESLA, StringID_CURIE, StringID_EINSTEIN  };

		int i = 0;
		for (PersonStringID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterLimitUUID() {
		Iterable<PersonUUID> people = pm.createQuery(PersonUUID.class).order("n").iter(2);

		assertNotNull(people);

		ArrayList<PersonUUID> l = new ArrayList<PersonUUID>() {{ 
			add(UUID_TESLA); 
			add(UUID_CURIE);
		}};
		
		int i = 0;
		for (PersonUUID person : people) {
			assertEquals( l.get(i), person);
			i++;
		}
	}
	
	public void testIterLimitLongAutoID() {
		Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter(2);

		assertNotNull(people);

		PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_TESLA, LongAutoID_CURIE };

		int i = 0;
		for (PersonLongAutoID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}

	public void testIterLimitLongManualID() {
		Iterable<PersonLongManualID> people = pm.createQuery(PersonLongManualID.class).order("n").iter(2);

		assertNotNull(people);

		PersonLongManualID[] array = new PersonLongManualID[] { LongManualID_TESLA, LongManualID_CURIE };

		int i = 0;
		for (PersonLongManualID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterLimitLongStringID() {
		Iterable<PersonStringID> people = pm.createQuery(PersonStringID.class).order("n").iter(2);

		assertNotNull(people);

		PersonStringID[] array = new PersonStringID[] { StringID_TESLA, StringID_CURIE };

		int i = 0;
		for (PersonStringID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterLimitOffsetUUID() {
		Iterable<PersonUUID> people = pm.createQuery(PersonUUID.class).order("n").iter(2,1);

		assertNotNull(people);

		ArrayList<PersonUUID> l = new ArrayList<PersonUUID>() {{ 
			add(UUID_CURIE);
			add(UUID_EINSTEIN);
		}};
		
		int i = 0;
		for (PersonUUID person : people) {
			assertEquals( l.get(i), person);
			i++;
		}
	}
	
	public void testIterLimitOffsetLongAutoID() {
		Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter(2, 1);

		assertNotNull(people);

		PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_CURIE, LongAutoID_EINSTEIN };

		int i = 0;
		for (PersonLongAutoID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}

	public void testIterLimitOffsetLongManualID() {
		Iterable<PersonLongManualID> people = pm.createQuery(PersonLongManualID.class).order("n").iter(2,1);

		assertNotNull(people);

		PersonLongManualID[] array = new PersonLongManualID[] { LongManualID_CURIE, LongManualID_EINSTEIN };

		int i = 0;
		for (PersonLongManualID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterLimitOffsetLongStringID() {
		Iterable<PersonStringID> people = pm.createQuery(PersonStringID.class).order("n").iter(2,1);

		assertNotNull(people);

		PersonStringID[] array = new PersonStringID[] { StringID_CURIE, StringID_EINSTEIN };

		int i = 0;
		for (PersonStringID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterFilter() {
		Iterable<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("n>", 1).order("n").iter();

		assertNotNull(people);

		PersonUUID[] array = new PersonUUID[] { UUID_CURIE, UUID_EINSTEIN };

		int i = 0;
		for (PersonUUID PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testIterFilterLimit() {
		Iterable<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("n>", 1).order("n").iter(1);

		assertNotNull(people);

		PersonUUID[] array = new PersonUUID[] { UUID_CURIE };

		int i = 0;
		for (PersonUUID PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testIterFilterLimitOffset() {
		Iterable<PersonUUID> people = pm.createQuery(PersonUUID.class).filter("n>", 1).order("n").iter(2, 1);

		assertNotNull(people);

		PersonUUID[] array = new PersonUUID[] { UUID_EINSTEIN };

		int i = 0;
		for (PersonUUID PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testOrderLongAutoId() {
		List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", false).fetch();
		
		assertNotNull(people);
		assertEquals(3, people.size());
		
		PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_TESLA, LongAutoID_CURIE, LongAutoID_EINSTEIN };

		int i = 0;
		for (PersonLongAutoID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testOrderLongManualId() {
		List<PersonLongManualID> people = queryPersonLongManualIDOrderBy("id", "", false).fetch();
		
		assertNotNull(people);
		assertEquals(3, people.size());
		
		PersonLongManualID[] array = new PersonLongManualID[] { LongManualID_TESLA, LongManualID_CURIE, LongManualID_EINSTEIN };

		int i = 0;
		for (PersonLongManualID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testOrderStringId() {
		List<PersonStringID> people = queryPersonStringIDOrderBy("id", "", false).fetch();
		
		assertNotNull(people);
		assertEquals(3, people.size());
		
		PersonStringID[] array = new PersonStringID[] { StringID_CURIE, StringID_EINSTEIN, StringID_TESLA };

		int i = 0;
		for (PersonStringID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testGetObjectNotFound() {
		try {
			getPersonUUID("");
			fail();
		} catch(Exception e) {
			System.out.println("Everything is OK");
		}
		
		assertNull(pm.createQuery(PersonUUID.class).filter("firstName", "John").get());
	}
	
	public void testDeleteObjectNotFound() {
		try {
			PersonUUID p = new PersonUUID();
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
		Discovery radioactivity = new Discovery("Radioactivity", LongAutoID_CURIE);
		Discovery relativity = new Discovery("Relativity", LongAutoID_EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", LongAutoID_TESLA);
		Discovery foo = new Discovery(null, LongAutoID_TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(teslaCoil);
		pm.insert(foo);

		Discovery relativity2 = pm.createQuery(Discovery.class).filter("discoverer", LongAutoID_EINSTEIN).get();
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
		
		dataTypes.typeEnum = EnumLong.ALPHA;
		
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
		
		if(dataTypes.typeEnum != null && same.typeEnum != null) {
			assertEquals(dataTypes.typeEnum.getCode(), same.typeEnum.getCode());
		} else {
			assertNull(dataTypes.typeEnum);
			assertNull(same.typeEnum);
		}
	}
	
	public void testQueryDelete() {
		Discovery radioactivity = new Discovery("Radioactivity", LongAutoID_CURIE);
		Discovery relativity = new Discovery("Relativity", LongAutoID_EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", LongAutoID_TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(teslaCoil);

		int n = pm.createQuery(Discovery.class).delete();
		assertEquals(3, n);
		
		List<Discovery> res = pm.createQuery(Discovery.class).fetch();
		assertEquals(0, res.size());
	}
	
	public void testQueryDeleteFiltered() {
		Discovery radioactivity = new Discovery("Radioactivity", LongAutoID_CURIE);
		Discovery relativity = new Discovery("Relativity", LongAutoID_EINSTEIN);
		Discovery foo = new Discovery("Foo", LongAutoID_EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", LongAutoID_TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);

		int n = pm.createQuery(Discovery.class).filter("discoverer", LongAutoID_EINSTEIN).delete();
		assertEquals(2, n);

		List<Discovery> res = pm.createQuery(Discovery.class).order("name").fetch();
		assertEquals(2, res.size());
		assertEquals(radioactivity, res.get(0));
		assertEquals(teslaCoil, res.get(1));
	}

	public void testJoin() {
		Discovery radioactivity = new Discovery("Radioactivity", LongAutoID_CURIE);
		Discovery relativity = new Discovery("Relativity", LongAutoID_EINSTEIN);
		Discovery foo = new Discovery("Foo", LongAutoID_EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", LongAutoID_TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);
		
		List<Discovery> res = pm.createQuery(Discovery.class).join("discoverer").order("name").fetch();
		assertEquals(4, res.size());
		assertEquals(foo, res.get(0));
		assertEquals(radioactivity, res.get(1));
		assertEquals(relativity, res.get(2));
		assertEquals(teslaCoil, res.get(3));
		
		assertEquals(LongAutoID_EINSTEIN, res.get(0).discoverer);
		assertEquals(LongAutoID_CURIE, res.get(1).discoverer);
		assertEquals(LongAutoID_EINSTEIN, res.get(2).discoverer);
		assertEquals(LongAutoID_TESLA, res.get(3).discoverer);
	}
	
	public void testJoinSortFields() {
		Discovery radioactivity = new Discovery("Radioactivity", LongAutoID_CURIE);
		Discovery relativity = new Discovery("Relativity", LongAutoID_EINSTEIN);
		Discovery foo = new Discovery("Foo", LongAutoID_EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", LongAutoID_TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);
		
		List<Discovery> res = pm.createQuery(Discovery.class).join("discoverer", "firstName").order("name").fetch();
		assertEquals(4, res.size());
		assertEquals(foo, res.get(0));
		assertEquals(relativity, res.get(1));
		assertEquals(radioactivity, res.get(2));
		assertEquals(teslaCoil, res.get(3));
		
		assertEquals(LongAutoID_EINSTEIN, res.get(0).discoverer);
		assertEquals(LongAutoID_EINSTEIN, res.get(1).discoverer);
		assertEquals(LongAutoID_CURIE, res.get(2).discoverer);
		assertEquals(LongAutoID_TESLA, res.get(3).discoverer);
	}
	
	
	public void testJoinAnnotation() {
		Discovery4Join radioactivity = new Discovery4Join("Radioactivity", LongAutoID_CURIE, LongAutoID_TESLA);
		Discovery4Join relativity = new Discovery4Join("Relativity", LongAutoID_EINSTEIN, LongAutoID_TESLA);
		Discovery4Join foo = new Discovery4Join("Foo", LongAutoID_EINSTEIN, LongAutoID_EINSTEIN);
		Discovery4Join teslaCoil = new Discovery4Join("Tesla Coil", LongAutoID_TESLA, LongAutoID_CURIE);
		
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
		
		assertEquals(LongAutoID_CURIE, res.get(0).discovererJoined);
		assertEquals(LongAutoID_EINSTEIN, res.get(1).discovererJoined);
		assertEquals(LongAutoID_EINSTEIN, res.get(2).discovererJoined);
		assertEquals(LongAutoID_TESLA, res.get(3).discovererJoined);

		assertEquals(LongAutoID_TESLA.id, res.get(0).discovererNotJoined.id);
		assertEquals(LongAutoID_TESLA.id, res.get(1).discovererNotJoined.id);
		assertEquals(LongAutoID_EINSTEIN.id, res.get(2).discovererNotJoined.id);
		assertEquals(LongAutoID_CURIE.id, res.get(3).discovererNotJoined.id);
		
		assertTrue(res.get(0).discovererNotJoined.isOnlyIdFilled());
		assertTrue(res.get(1).discovererNotJoined.isOnlyIdFilled());
		assertTrue(res.get(2).discovererNotJoined.isOnlyIdFilled());
		assertTrue(res.get(3).discovererNotJoined.isOnlyIdFilled());
	}

	public void testFetchPrivateFields() {
		DiscoveryPrivate radioactivity = new DiscoveryPrivate(1L, "Radioactivity", LongAutoID_CURIE);
		DiscoveryPrivate relativity = new DiscoveryPrivate(2L, "Relativity", LongAutoID_EINSTEIN);
		DiscoveryPrivate foo = new DiscoveryPrivate(3L, "Foo", LongAutoID_EINSTEIN);
		DiscoveryPrivate teslaCoil = new DiscoveryPrivate(4L, "Tesla Coil", LongAutoID_TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);

		List<DiscoveryPrivate> res = pm.createQuery(DiscoveryPrivate.class).order("name").fetch();
		assertEquals(foo, res.get(0));
		assertEquals(radioactivity, res.get(1));
		assertEquals(relativity, res.get(2));
		assertEquals(teslaCoil, res.get(3));
	}
	
	public void testFetchPaginate() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
	}
	public void testFetchKeysPaginate() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testIterPaginate() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		try {
			Iterable<Discovery> res = query.iter();
			Iterator<Discovery> it = res.iterator();
			int i=0;
			while(it.hasNext()){
				assertEquals(discs[i++], it.next());
			}
			res = query.iter();
			it = res.iterator();
			while(it.hasNext()){
				assertEquals(discs[i++], it.next());
			}
		}finally {
			query.release();
		}
	}
	
	
	public void testIterFetchPaginate() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		Iterable<Discovery> res = query.iter();
		Iterator<Discovery> it = res.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		List<Discovery> res2 = query.fetch();
		assertEquals(5, res2.size());
		for(int j=0; j<5; j++){
			assertEquals(discs[j+5], res2.get(j));
		}
	}
	
	private PersonUUID getPersonUUID(String id) {
		PersonUUID p = new PersonUUID();
		p.id = id;
		pm.get(p);
		return p;
	}

	private PersonLongAutoID getPersonLongAutoID(Long id) {
		PersonLongAutoID p = new PersonLongAutoID();
		p.id = id;
		pm.get(p);
		return p;
	}
	
	private PersonLongManualID getPersonLongManualID(Long id) {
		PersonLongManualID p = new PersonLongManualID();
		p.id = id;
		pm.get(p);
		return p;
	}

	private PersonStringID getPersonStringID(String id) {
		PersonStringID p = new PersonStringID();
		p.id = id;
		pm.get(p);
		return p;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(PersonUUID.class);
		classes.add(PersonLongAutoID.class);
		classes.add(PersonLongManualID.class);
		classes.add(PersonStringID.class);
		if(supportsAutoincrement())
			classes.add(AutoInc.class);
		if(supportsMultipleKeys())
			classes.add(MultipleKeys.class);
		classes.add(Discovery.class);
		classes.add(Discovery4Join.class);
		classes.add(DiscoveryPrivate.class);
		classes.add(DataTypes.class);
		pm = createPersistenceManager(classes);
		
		for (Class<?> clazz : classes) {
			List<?> items = pm.createQuery(clazz).fetch();
			for (Object object : items) {
				pm.delete(object);
			}
		}
		
		pm.insert(UUID_TESLA);
		pm.insert(UUID_CURIE);
		pm.insert(UUID_EINSTEIN);
				
		pm.insert(LongAutoID_TESLA);
		pm.insert(LongAutoID_CURIE);
		pm.insert(LongAutoID_EINSTEIN);

		pm.insert(LongManualID_TESLA);
		pm.insert(LongManualID_CURIE);
		pm.insert(LongManualID_EINSTEIN);

		pm.insert(StringID_TESLA);
		pm.insert(StringID_CURIE);
		pm.insert(StringID_EINSTEIN);
}

}
