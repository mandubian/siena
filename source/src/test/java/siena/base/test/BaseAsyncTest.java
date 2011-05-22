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
import siena.Query;
import siena.SienaException;
import siena.base.test.model.Address;
import siena.base.test.model.AutoInc;
import siena.base.test.model.Contact;
import siena.base.test.model.DataTypes;
import siena.base.test.model.DataTypes.EnumLong;
import siena.base.test.model.ContainerModel;
import siena.base.test.model.Discovery;
import siena.base.test.model.Discovery4Join;
import siena.base.test.model.Discovery4Join2;
import siena.base.test.model.Discovery4Search;
import siena.base.test.model.DiscoveryNoColumn;
import siena.base.test.model.DiscoveryNoColumnMultipleKeys;
import siena.base.test.model.DiscoveryPrivate;
import siena.base.test.model.EmbeddedModel;
import siena.base.test.model.MultipleKeys;
import siena.base.test.model.PersonLongAutoID;
import siena.base.test.model.PersonLongManualID;
import siena.base.test.model.PersonStringAutoIncID;
import siena.base.test.model.PersonStringID;
import siena.base.test.model.PersonUUID;
import siena.base.test.model.PolymorphicModel;
import siena.core.async.PersistenceManagerAsync;
import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;

public abstract class BaseAsyncTest extends TestCase {
	protected PersistenceManagerAsync pm;

	private static PersonUUID UUID_TESLA = new PersonUUID("Nikola", "Tesla", "Smiljam", 1);
	private static PersonUUID UUID_CURIE = new PersonUUID("Marie", "Curie", "Warsaw", 2);
	private static PersonUUID UUID_EINSTEIN = new PersonUUID("Albert", "Einstein", "Ulm", 3);
	
	private static PersonLongAutoID LongAutoID_TESLA = new PersonLongAutoID("Nikola", "Tesla", "Smiljam", 1);
	protected static PersonLongAutoID LongAutoID_CURIE = new PersonLongAutoID("Marie", "Curie", "Warsaw", 2);
	private static PersonLongAutoID LongAutoID_EINSTEIN = new PersonLongAutoID("Albert", "Einstein", "Ulm", 3);

	private static PersonLongManualID LongManualID_TESLA = new PersonLongManualID(1L, "Nikola", "Tesla", "Smiljam", 1);
	private static PersonLongManualID LongManualID_CURIE = new PersonLongManualID(2L, "Marie", "Curie", "Warsaw", 2);
	private static PersonLongManualID LongManualID_EINSTEIN = new PersonLongManualID(3L, "Albert", "Einstein", "Ulm", 3);
	
	private static PersonStringID StringID_TESLA = new PersonStringID("TESLA", "Nikola", "Tesla", "Smiljam", 1);
	private static PersonStringID StringID_CURIE = new PersonStringID("CURIE", "Marie", "Curie", "Warsaw", 2);
	private static PersonStringID StringID_EINSTEIN = new PersonStringID("EINSTEIN", "Albert", "Einstein", "Ulm", 3);

	public abstract PersistenceManagerAsync createPersistenceManager(List<Class<?>> classes) throws Exception;
	
	public abstract boolean supportsAutoincrement();
	
	public abstract boolean supportsMultipleKeys();
	
	public abstract boolean mustFilterToOrder();
	
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
		classes.add(Discovery4Join2.class);
		classes.add(DiscoveryPrivate.class);
		classes.add(Discovery4Search.class);
		classes.add(DataTypes.class);
		pm = createPersistenceManager(classes);
		
		for (Class<?> clazz : classes) {
			pm.createQuery(clazz).delete().get();			
		}
		
		pm.insert(UUID_TESLA, UUID_CURIE, UUID_EINSTEIN).get();
		pm.insert(LongAutoID_TESLA, LongAutoID_CURIE, LongAutoID_EINSTEIN).get();
		pm.insert(LongManualID_TESLA, LongManualID_CURIE, LongManualID_EINSTEIN).get();
		pm.insert(StringID_TESLA, StringID_CURIE, StringID_EINSTEIN).get();

	}
	
	protected List<PersonUUID> getOrderedPersonUUIDs() {
		ArrayList<PersonUUID> l = new ArrayList<PersonUUID>() {
			private static final long serialVersionUID = 1L;
		{ 
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
	
	private PersonUUID getPersonUUID(String id) {
		PersonUUID p = new PersonUUID();
		p.id = id;
		pm.get(p).get();
		return p;
	}

	private PersonLongAutoID getPersonLongAutoID(Long id) {
		PersonLongAutoID p = new PersonLongAutoID();
		p.id = id;
		pm.get(p).get();
		return p;
	}
	
	private PersonLongManualID getPersonLongManualID(Long id) {
		PersonLongManualID p = new PersonLongManualID();
		p.id = id;
		pm.get(p).get();
		return p;
	}

	private PersonStringID getPersonStringID(String id) {
		PersonStringID p = new PersonStringID();
		p.id = id;
		pm.get(p).get();
		return p;
	}

	
	protected QueryAsync<PersonUUID> queryPersonUUIDOrderBy(String order, Object value, boolean desc) {
		QueryAsync<PersonUUID> query = pm.createQuery(PersonUUID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}

	protected QueryAsync<PersonLongAutoID> queryPersonLongAutoIDOrderBy(String order, Object value, boolean desc) {
		QueryAsync<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}
	
	protected QueryAsync<PersonLongManualID> queryPersonLongManualIDOrderBy(String order, Object value, boolean desc) {
		QueryAsync<PersonLongManualID> query = pm.createQuery(PersonLongManualID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}
	
	public QueryAsync<PersonStringID> queryPersonStringIDOrderBy(String order, Object value, boolean desc) {
		QueryAsync<PersonStringID> query = pm.createQuery(PersonStringID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}
	
	public void testCount() {
		assertEquals(3, pm.createQuery(PersonUUID.class).count().get().intValue());
	}
	
	public void testFetch() {
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("n", 0, false).fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));
	}
	
	public void testFetchKeys() {
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("n", 0, false).fetchKeys();

		List<PersonUUID> people = future.get();
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
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("firstName", "", false).fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_EINSTEIN, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_TESLA, people.get(2));
	}
	
	public void testFetchOrderKeys() {
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("firstName", "", false).fetchKeys();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_EINSTEIN.id, people.get(0).id);
		assertEquals(UUID_CURIE.id, people.get(1).id);
		assertEquals(UUID_TESLA.id, people.get(2).id);
	}

	public void testFetchOrderDesc() {
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("lastName", "", true).fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
		assertEquals(UUID_CURIE, people.get(2));
	}

	public void testFetchOrderDescKeys() {
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("lastName", "", true).fetchKeys();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA.id, people.get(0).id);
		assertEquals(UUID_EINSTEIN.id, people.get(1).id);
		assertEquals(UUID_CURIE.id, people.get(2).id);
	}
	
	public void testFetchOrderOnLongAutoId() {
		SienaFuture<List<PersonLongAutoID>> future = queryPersonLongAutoIDOrderBy("id", "", false).fetchKeys();

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongAutoID_TESLA.id, people.get(0).id);
		assertEquals(LongAutoID_CURIE.id, people.get(1).id);
		assertEquals(LongAutoID_EINSTEIN.id, people.get(2).id);
	}
	
	public void testFetchOrderOnLongManualId() {
		SienaFuture<List<PersonLongManualID>> future = queryPersonLongManualIDOrderBy("id", "", false).fetchKeys();

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongManualID_TESLA.id, people.get(0).id);
		assertEquals(LongManualID_CURIE.id, people.get(1).id);
		assertEquals(LongManualID_EINSTEIN.id, people.get(2).id);
	}
	
	public void testFetchOrderOnStringId() {
		SienaFuture<List<PersonStringID>> future = queryPersonStringIDOrderBy("id", "", false).fetchKeys();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(StringID_CURIE.id, people.get(0).id);
		assertEquals(StringID_EINSTEIN.id, people.get(1).id);
		assertEquals(StringID_TESLA.id, people.get(2).id);
	}
		
	public void testFetchOrderOnUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("id", "", false).fetchKeys();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(l.get(0).id, people.get(0).id);
		assertEquals(l.get(1).id, people.get(1).id);
		assertEquals(l.get(2).id, people.get(2).id);
	}
	
	public void testFetchOrderOnLongAutoIdDesc() {
		SienaFuture<List<PersonLongAutoID>> future = queryPersonLongAutoIDOrderBy("id", "", true).fetchKeys();

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongAutoID_EINSTEIN.id, people.get(0).id);
		assertEquals(LongAutoID_CURIE.id, people.get(1).id);
		assertEquals(LongAutoID_TESLA.id, people.get(2).id);
	}
		
	public void testFetchOrderOnLongManualIdDesc() {
		SienaFuture<List<PersonLongManualID>> future = queryPersonLongManualIDOrderBy("id", "", true).fetchKeys();

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongManualID_EINSTEIN.id, people.get(0).id);
		assertEquals(LongManualID_CURIE.id, people.get(1).id);
		assertEquals(LongManualID_TESLA.id, people.get(2).id);
	}
	
	public void testFetchOrderOnStringIdDesc() {
		SienaFuture<List<PersonStringID>> future = queryPersonStringIDOrderBy("id", "", true).fetchKeys();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(StringID_TESLA.id, people.get(0).id);
		assertEquals(StringID_EINSTEIN.id, people.get(1).id);
		assertEquals(StringID_CURIE.id, people.get(2).id);
	}
	
	public void testFetchOrderOnUUIDDesc() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("id", "", true).fetchKeys();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(l.get(2).id, people.get(0).id);
		assertEquals(l.get(1).id, people.get(1).id);
		assertEquals(l.get(0).id, people.get(2).id);
	}
	
	public void testFilterOperatorEqualString() {
		SienaFuture<PersonUUID> future = pm.createQuery(PersonUUID.class).filter("firstName", "Albert").get();		
		PersonUUID person = future.get();
		assertNotNull(person);
		assertEquals(UUID_EINSTEIN, person);
	}
	
	public void testFilterOperatorEqualInt() {
		SienaFuture<PersonUUID> future = pm.createQuery(PersonUUID.class).filter("n", 3).get();
		PersonUUID person = future.get();
		assertNotNull(person);
		assertEquals(UUID_EINSTEIN, person);
	}
	
	public void testFilterOperatorEqualUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();	
		SienaFuture<PersonUUID> future = pm.createQuery(PersonUUID.class).filter("id", l.get(0).id).get();
		PersonUUID person = future.get();
		assertNotNull(person);
		assertEquals(l.get(0), person);
	}
	
	public void testFilterOperatorEqualLongAutoID() {
		SienaFuture<PersonLongAutoID> future = pm.createQuery(PersonLongAutoID.class).filter("id", LongAutoID_EINSTEIN.id).get();
		PersonLongAutoID person = future.get();
		assertNotNull(person);
		assertEquals(LongAutoID_EINSTEIN, person);
	}
		
	public void testFilterOperatorEqualLongManualID() {
		SienaFuture<PersonLongManualID> future = pm.createQuery(PersonLongManualID.class).filter("id", 3L).get();
		PersonLongManualID person = future.get();
		assertNotNull(person);
		assertEquals(LongManualID_EINSTEIN, person);
	}
	
	public void testFilterOperatorEqualStringID() {
		SienaFuture<PersonStringID> future = pm.createQuery(PersonStringID.class).filter("id", "EINSTEIN").get();
		PersonStringID person = future.get();
		assertNotNull(person);
		assertEquals(StringID_EINSTEIN, person);
	}
	
	public void testFilterOperatorNotEqualString() {
		SienaFuture<List<PersonStringID>> future = pm.createQuery(PersonStringID.class).filter("firstName!=", "Albert").order("firstName").fetch();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}
	
	public void testFilterOperatorNotEqualInt() {
		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("n!=", 3).order("n").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
	}

	public void testFilterOperatorNotEqualUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("id!=", l.get(0).id).order("id").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(l.get(1), people.get(0));
		assertEquals(l.get(2), people.get(1));
	}
	
	public void testFilterOperatorNotEqualLongAutoID() {
		SienaFuture<List<PersonLongAutoID>> future = pm.createQuery(PersonLongAutoID.class).filter("id!=", LongAutoID_EINSTEIN.id).order("id").fetch();

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
	}

	public void testFilterOperatorNotEqualLongManualID() {
		SienaFuture<List<PersonLongManualID>> future = pm.createQuery(PersonLongManualID.class).filter("id!=", 3L).order("id").fetch();

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorNotEqualStringID() {
		SienaFuture<List<PersonStringID>> future = pm.createQuery(PersonStringID.class).filter("id!=", StringID_EINSTEIN.id).order("id").fetch();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}
	
	public void testFilterOperatorIn() {
		@SuppressWarnings("serial")
		SienaFuture<List<PersonUUID>> future = 
			pm.createQuery(PersonUUID.class)
				.filter("n IN", new ArrayList<Integer>(){{ 
					add(2);
					add(3);
				}})
				.order("n")
				.fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_CURIE, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorInOrder() {
		@SuppressWarnings("serial")
		SienaFuture<List<PersonUUID>> future = 
			pm.createQuery(PersonUUID.class)
				.filter("n IN", new ArrayList<Integer>(){{ 
					add(3);
					add(2);
				}})
				.order("n")
				.fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_CURIE, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorInForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		
		SienaFuture<List<PersonUUID>> future = 
			pm.createQuery(PersonUUID.class)
				.filter("id IN", Arrays.asList( l.get(0).id, l.get(1).id))
				.fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
	}
	
	public void testFilterOperatorInForLongAutoID() {
		@SuppressWarnings("serial")
		SienaFuture<List<PersonLongAutoID>> future = 
			pm.createQuery(PersonLongAutoID.class)
				.filter("id IN", new ArrayList<Long>(){{ 
					add(LongAutoID_TESLA.id);
					add(LongAutoID_CURIE.id);
				}})
				.fetch();

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
	}

	public void testFilterOperatorInForLongManualID() {
		@SuppressWarnings("serial")
		SienaFuture<List<PersonLongManualID>> future = 
			pm.createQuery(PersonLongManualID.class)
				.filter("id IN", new ArrayList<Long>(){{ 
					add(LongManualID_TESLA.id);
					add(LongManualID_CURIE.id);
				}})
				.fetch();

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorInForStringID() {
		@SuppressWarnings("serial")
		SienaFuture<List<PersonStringID>> future = 
			pm.createQuery(PersonStringID.class)
				.filter("id IN", new ArrayList<String>(){{ 
					add(StringID_TESLA.id);
					add(StringID_CURIE.id);
				}})
				.order("id")
				.fetch();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}
	
	public void testFilterOperatorLessThan() {
		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("n<", 3).order("n").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorLessThanForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();

		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("id<", l.get(2).id).order("id").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());
		
		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
	}
	
	public void testFilterOperatorLessThanForLongAutoID() {
		SienaFuture<List<PersonLongAutoID>> future = pm.createQuery(PersonLongAutoID.class).filter("id<", LongAutoID_EINSTEIN.id).order("id").fetch();

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorLessThanForLongManualID() {
		SienaFuture<List<PersonLongManualID>> future = pm.createQuery(PersonLongManualID.class).filter("id<", 3L).order("id").fetch();

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
	}
	
	public void testFilterOperatorLessThanForStringID() {
		SienaFuture<List<PersonStringID>> future = pm.createQuery(PersonStringID.class).filter("id<", StringID_TESLA.id).order("id").fetch();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorLessThanOrEqual() {
		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("n<=", 3).order("n").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));		
	}
	
	public void testFilterOperatorLessThanOrEqualForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();

		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("id<=", l.get(2).id).order("id").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		
		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
		assertEquals(l.get(2), people.get(2));
	}
	
	public void testFilterOperatorLessThanOrEqualForLongAutoID() {
		SienaFuture<List<PersonLongAutoID>> future = pm.createQuery(PersonLongAutoID.class).filter("id<=", LongAutoID_EINSTEIN.id).order("id").fetch();

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
		assertEquals(LongAutoID_EINSTEIN, people.get(2));
	}
	
	public void testFilterOperatorLessThanOrEqualForLongManualID() {
		SienaFuture<List<PersonLongManualID>> future = pm.createQuery(PersonLongManualID.class).filter("id<=", LongManualID_EINSTEIN.id).order("id").fetch();

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
		assertEquals(LongManualID_EINSTEIN, people.get(2));
	}
	
	public void testFilterOperatorLessThanOrEqualForStringID() {
		SienaFuture<List<PersonStringID>> future = pm.createQuery(PersonStringID.class).filter("id<=", StringID_TESLA.id).order("id").fetch();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(StringID_CURIE, people.get(0));
		assertEquals(StringID_EINSTEIN, people.get(1));
		assertEquals(StringID_TESLA, people.get(2));
	}
	
	
	public void testFilterOperatorMoreThan() {
		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("n>", 1).order("n").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_CURIE, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();

		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("id>", l.get(0).id).order("id").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());
		
		assertEquals(l.get(1), people.get(0));
		assertEquals(l.get(2), people.get(1));
	}
	
	public void testFilterOperatorMoreThanForLongAutoID() {
		SienaFuture<List<PersonLongAutoID>> future = pm.createQuery(PersonLongAutoID.class).filter("id>", LongAutoID_TESLA.id).order("id").fetch();

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_CURIE, people.get(0));
		assertEquals(LongAutoID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanForLongManualID() {
		SienaFuture<List<PersonLongManualID>> future = pm.createQuery(PersonLongManualID.class).filter("id>", LongManualID_TESLA.id).order("id").fetch();

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_CURIE, people.get(0));
		assertEquals(LongManualID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanForStringID() {
		SienaFuture<List<PersonStringID>> future = pm.createQuery(PersonStringID.class).filter("id>", StringID_CURIE.id).order("id").fetch();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_EINSTEIN, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}

	
	public void testFilterOperatorMoreThanOrEqual() {
		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("n>=", 1).order("n").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));
	}

	public void testFilterOperatorMoreThanOrEqualForUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();

		SienaFuture<List<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("id>=", l.get(0).id).order("id").fetch();

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(3, people.size());

		
		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
		assertEquals(l.get(2), people.get(2));
	}
	
	public void testFilterOperatorMoreThanOrEqualForLongAutoID() {
		SienaFuture<List<PersonLongAutoID>> future = pm.createQuery(PersonLongAutoID.class).filter("id>=", LongAutoID_CURIE.id).order("id").fetch();

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongAutoID_CURIE, people.get(0));
		assertEquals(LongAutoID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanOrEqualForLongManualID() {
		SienaFuture<List<PersonLongManualID>> future = pm.createQuery(PersonLongManualID.class).filter("id>=", LongManualID_CURIE.id).order("id").fetch();

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(LongManualID_CURIE, people.get(0));
		assertEquals(LongManualID_EINSTEIN, people.get(1));
	}
	
	public void testFilterOperatorMoreThanOrEqualForStringID() {
		SienaFuture<List<PersonStringID>> future = pm.createQuery(PersonStringID.class).filter("id>=", StringID_EINSTEIN.id).order("id").fetch();

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(StringID_EINSTEIN, people.get(0));
		assertEquals(StringID_TESLA, people.get(1));
	}
	
	public void testCountFilter() {
		assertEquals(2, pm.createQuery(PersonUUID.class).filter("n<", 3).count().get().intValue());
	}

	public void testCountFilterUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		assertEquals(2, pm.createQuery(PersonUUID.class).filter("id<", l.get(2).id).count().get().intValue());
	}
	
	public void testCountFilterLongAutoID() {
		assertEquals(2, pm.createQuery(PersonLongAutoID.class).filter("id<", LongAutoID_EINSTEIN.id).count().get().intValue());
	}

	public void testCountFilterLongManualID() {
		assertEquals(2, pm.createQuery(PersonLongManualID.class).filter("id<", LongManualID_EINSTEIN.id).count().get().intValue());
	}
	
	public void testCountFilterStringID() {
		assertEquals(2, pm.createQuery(PersonStringID.class).filter("id<", StringID_TESLA.id).count().get().intValue());
	}
	
	public void testFetchLimit() {
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("n", 0, false).fetch(1);

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(UUID_TESLA, people.get(0));
	}

	public void testFetchLimitUUID() {
		List<PersonUUID> l = getOrderedPersonUUIDs();
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("id", l.get(0), false).fetch(1);

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(l.get(0), people.get(0));
	}
	
	public void testFetchLimitLongAutoID() {
		SienaFuture<List<PersonLongAutoID>> future = queryPersonLongAutoIDOrderBy("id", 0, false).fetch(1);

		List<PersonLongAutoID> people = future.get();
		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
	}
	
	public void testFetchLimitLongManualID() {
		SienaFuture<List<PersonLongManualID>> future = queryPersonLongManualIDOrderBy("id", 0, false).fetch(1);

		List<PersonLongManualID> people = future.get();
		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
	}
	
	public void testFetchLimitStringID() {
		SienaFuture<List<PersonStringID>> future = queryPersonStringIDOrderBy("id", StringID_CURIE, false).fetch(1);

		List<PersonStringID> people = future.get();
		assertNotNull(people);
		assertEquals(1, people.size());

		assertEquals(StringID_CURIE, people.get(0));
	}
	
	public void testFetchLimitReal() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}

		pm.insert((Object[])discs).get();

		SienaFuture<List<Discovery>> future = pm.createQuery(Discovery.class).order("name").fetch(3);
		List<Discovery> res = future.get();
		assertNotNull(res);
		assertEquals(3, res.size());
		
		assertEquals(discs[0], res.get(0));
		assertEquals(discs[1], res.get(1));
		assertEquals(discs[2], res.get(2));
	}

	public void testFetchLimitOffsetReal() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}

		pm.insert((Object[])discs).get();
		
		SienaFuture<List<Discovery>> future = pm.createQuery(Discovery.class).order("name").fetch(3, 5);
		List<Discovery> res = future.get();
		assertNotNull(res);
		assertEquals(3, res.size());
		
		assertEquals(discs[5], res.get(0));
		assertEquals(discs[6], res.get(1));
		assertEquals(discs[7], res.get(2));
	}
	
	public void testFetchLimitOffset() {
		QueryAsync<PersonUUID> query = queryPersonUUIDOrderBy("n", 0, false);
		SienaFuture<List<PersonUUID>> future = query.fetch(2, 1);

		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_CURIE, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}

	public void testInsertUUID() {
		PersonUUID maxwell = new PersonUUID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell).get();
		assertNotNull(maxwell.id);

		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("n", 0, false).fetch();
		List<PersonUUID> people = future.get();
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

		pm.insert(maxwell).get();
		assertNotNull(maxwell.id);

		SienaFuture<List<PersonLongAutoID>> future = queryPersonLongAutoIDOrderBy("n", 0, false).fetch();
		List<PersonLongAutoID> people = future.get();
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

		pm.insert(maxwell).get();
		assertEquals((Long)4L, maxwell.id);

		SienaFuture<List<PersonLongManualID>> future = queryPersonLongManualIDOrderBy("n", 0, false).fetch();
		List<PersonLongManualID> people = future.get();
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

		pm.insert(maxwell).get();
		assertEquals(maxwell.id, "MAXWELL");

		SienaFuture<List<PersonStringID>> future = queryPersonStringIDOrderBy("n", 0, false).fetch();
		List<PersonStringID> people = future.get();
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
		pm.update(curie).get();
		PersonUUID curie2 = getPersonUUID(UUID_CURIE.id);
		assertEquals(curie2, curie);
	}

	public void testUpdateLongAutoID() {
		PersonLongAutoID curie = getPersonLongAutoID(LongAutoID_CURIE.id);
		curie.lastName = "Sklodowska–Curie";
		pm.update(curie).get();
		PersonLongAutoID curie2 = getPersonLongAutoID(LongAutoID_CURIE.id);
		assertEquals(curie2, curie);
	}
	
	public void testDeleteUUID() {
		PersonUUID curie = getPersonUUID(UUID_CURIE.id);
		pm.delete(curie).get();
		
		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("n", 0, false).fetch();
		List<PersonUUID> people = future.get();
		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_EINSTEIN, people.get(1));
	}
	
	public void testIterFullUUID() {
		SienaFuture<Iterable<PersonUUID>> future = pm.createQuery(PersonUUID.class).order("n").iter();

		Iterable<PersonUUID> people = future.get();
		assertNotNull(people);

		@SuppressWarnings("serial")
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
		SienaFuture<Iterable<PersonLongAutoID>> future = pm.createQuery(PersonLongAutoID.class).order("n").iter();

		Iterable<PersonLongAutoID> people = future.get();
		assertNotNull(people);

		PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_TESLA, LongAutoID_CURIE, LongAutoID_EINSTEIN };

		int i = 0;
		for (PersonLongAutoID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}

	public void testIterFullLongManualID() {
		SienaFuture<Iterable<PersonLongManualID>> future = pm.createQuery(PersonLongManualID.class).order("n").iter();

		Iterable<PersonLongManualID> people = future.get();
		assertNotNull(people);

		PersonLongManualID[] array = new PersonLongManualID[] { LongManualID_TESLA, LongManualID_CURIE, LongManualID_EINSTEIN };

		int i = 0;
		for (PersonLongManualID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterFullLongStringID() {
		SienaFuture<Iterable<PersonStringID>> future = pm.createQuery(PersonStringID.class).order("n").iter();

		Iterable<PersonStringID> people = future.get();
		assertNotNull(people);

		PersonStringID[] array = new PersonStringID[] { StringID_TESLA, StringID_CURIE, StringID_EINSTEIN  };

		int i = 0;
		for (PersonStringID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterLimitUUID() {
		SienaFuture<Iterable<PersonUUID>> future = pm.createQuery(PersonUUID.class).order("n").iter(2);

		Iterable<PersonUUID> people = future.get();
		assertNotNull(people);

		@SuppressWarnings("serial")
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
		SienaFuture<Iterable<PersonLongAutoID>> future = pm.createQuery(PersonLongAutoID.class).order("n").iter(2);

		Iterable<PersonLongAutoID> people = future.get();
		assertNotNull(people);

		PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_TESLA, LongAutoID_CURIE };

		int i = 0;
		for (PersonLongAutoID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}

	public void testIterLimitLongManualID() {
		SienaFuture<Iterable<PersonLongManualID>> future = pm.createQuery(PersonLongManualID.class).order("n").iter(2);

		Iterable<PersonLongManualID> people = future.get();
		assertNotNull(people);

		PersonLongManualID[] array = new PersonLongManualID[] { LongManualID_TESLA, LongManualID_CURIE };

		int i = 0;
		for (PersonLongManualID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterLimitLongStringID() {
		SienaFuture<Iterable<PersonStringID>> future = pm.createQuery(PersonStringID.class).order("n").iter(2);

		Iterable<PersonStringID> people = future.get();
		assertNotNull(people);

		PersonStringID[] array = new PersonStringID[] { StringID_TESLA, StringID_CURIE };

		int i = 0;
		for (PersonStringID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterLimitOffsetUUID() {
		SienaFuture<Iterable<PersonUUID>> future = pm.createQuery(PersonUUID.class).order("n").iter(2,1);

		Iterable<PersonUUID> people = future.get();
		assertNotNull(people);

		@SuppressWarnings("serial")
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
		SienaFuture<Iterable<PersonLongAutoID>> future = pm.createQuery(PersonLongAutoID.class).order("n").iter(2, 1);

		Iterable<PersonLongAutoID> people = future.get();
		assertNotNull(people);

		PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_CURIE, LongAutoID_EINSTEIN };

		int i = 0;
		for (PersonLongAutoID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}

	public void testIterLimitOffsetLongManualID() {
		SienaFuture<Iterable<PersonLongManualID>> future = pm.createQuery(PersonLongManualID.class).order("n").iter(2,1);

		Iterable<PersonLongManualID> people = future.get();
		assertNotNull(people);

		PersonLongManualID[] array = new PersonLongManualID[] { LongManualID_CURIE, LongManualID_EINSTEIN };

		int i = 0;
		for (PersonLongManualID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterLimitOffsetLongStringID() {
		SienaFuture<Iterable<PersonStringID>> future = pm.createQuery(PersonStringID.class).order("n").iter(2,1);

		Iterable<PersonStringID> people = future.get();
		assertNotNull(people);

		PersonStringID[] array = new PersonStringID[] { StringID_CURIE, StringID_EINSTEIN };

		int i = 0;
		for (PersonStringID person : people) {
			assertEquals(array[i], person);
			i++;
		}
	}
	
	public void testIterFilter() {
		SienaFuture<Iterable<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("n>", 1).order("n").iter();

		Iterable<PersonUUID> people = future.get();
		assertNotNull(people);

		PersonUUID[] array = new PersonUUID[] { UUID_CURIE, UUID_EINSTEIN };

		int i = 0;
		for (PersonUUID PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testIterFilterLimit() {
		SienaFuture<Iterable<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("n>", 1).order("n").iter(1);

		Iterable<PersonUUID> people = future.get();
		assertNotNull(people);

		PersonUUID[] array = new PersonUUID[] { UUID_CURIE };

		int i = 0;
		for (PersonUUID PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testIterFilterLimitOffset() {
		SienaFuture<Iterable<PersonUUID>> future = pm.createQuery(PersonUUID.class).filter("n>", 1).order("n").iter(2, 1);

		Iterable<PersonUUID> people = future.get();
		assertNotNull(people);

		PersonUUID[] array = new PersonUUID[] { UUID_EINSTEIN };

		int i = 0;
		for (PersonUUID PersonIntKey : people) {
			assertEquals(array[i], PersonIntKey);
			i++;
		}
	}
	
	public void testOrderLongAutoId() {
		SienaFuture<List<PersonLongAutoID>> future = queryPersonLongAutoIDOrderBy("id", "", false).fetch();
		
		List<PersonLongAutoID> people = future.get();
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
		SienaFuture<List<PersonLongManualID>> future = queryPersonLongManualIDOrderBy("id", "", false).fetch();
		
		List<PersonLongManualID> people = future.get();
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
		SienaFuture<List<PersonStringID>> future = queryPersonStringIDOrderBy("id", "", false).fetch();
		
		List<PersonStringID> people = future.get();
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
		
		assertNull(pm.createQuery(PersonUUID.class).filter("firstName", "John").get().get());
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
		
		pm.insert(radioactivity, relativity, teslaCoil, foo).get();

		SienaFuture<Discovery> relativity2 = pm.createQuery(Discovery.class).filter("discoverer", LongAutoID_EINSTEIN).get();
		SienaFuture<Discovery> foo2 = pm.createQuery(Discovery.class).filter("name", null).get();
		assertTrue(relativity.name.equals(relativity2.get().name));
		assertTrue(foo.id.equals(foo2.get().id));
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
		pm.insert(dataTypes).get();
		
		assertEqualsDataTypes(dataTypes, pm.createQuery(DataTypes.class).get().get());
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
		
		pm.insert(dataTypes).get();
		
		// to test that fields are read back correctly
		pm.createQuery(DataTypes.class).filter("id", dataTypes.id).get();
		
		SienaFuture<DataTypes> same = pm.createQuery(DataTypes.class).get();
		assertEqualsDataTypes(dataTypes, same.get());
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
		
		pm.insert(radioactivity, relativity, teslaCoil).get();

		int n = pm.createQuery(Discovery.class).delete().get();
		assertEquals(3, n);
		
		List<Discovery> res = pm.createQuery(Discovery.class).fetch().get();
		assertEquals(0, res.size());
	}
	
	public void testQueryDeleteFiltered() {
		Discovery radioactivity = new Discovery("Radioactivity", LongAutoID_CURIE);
		Discovery relativity = new Discovery("Relativity", LongAutoID_EINSTEIN);
		Discovery foo = new Discovery("Foo", LongAutoID_EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", LongAutoID_TESLA);
		
		pm.insert(radioactivity, relativity, foo, teslaCoil).get();

		int n = pm.createQuery(Discovery.class).filter("discoverer", LongAutoID_EINSTEIN).delete().get();
		assertEquals(2, n);

		List<Discovery> res = pm.createQuery(Discovery.class).order("name").fetch().get();
		assertEquals(2, res.size());
		assertEquals(radioactivity, res.get(0));
		assertEquals(teslaCoil, res.get(1));
	}
	
	public void testJoin() {
		Discovery radioactivity = new Discovery("Radioactivity", LongAutoID_CURIE);
		Discovery relativity = new Discovery("Relativity", LongAutoID_EINSTEIN);
		Discovery foo = new Discovery("Foo", LongAutoID_EINSTEIN);
		Discovery teslaCoil = new Discovery("Tesla Coil", LongAutoID_TESLA);
		
		pm.insert(radioactivity, relativity, foo, teslaCoil).get();
		
		SienaFuture<List<Discovery>> future = pm.createQuery(Discovery.class).join("discoverer").order("name").fetch();
		
		List<Discovery> res = future.get(); 
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
		
		pm.insert(radioactivity, relativity, foo, teslaCoil).get();
		
		SienaFuture<List<Discovery>> future = pm.createQuery(Discovery.class).join("discoverer", "firstName").order("name").fetch();
		List<Discovery> res = future.get(); 
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
		
		pm.insert(radioactivity, relativity, foo, teslaCoil).get();
		
		SienaFuture<List<Discovery4Join>> future = pm.createQuery(Discovery4Join.class).fetch();
		List<Discovery4Join> res = future.get(); 
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
		
		pm.insert(radioactivity, relativity, foo, teslaCoil).get();

		SienaFuture<List<DiscoveryPrivate>> future = pm.createQuery(DiscoveryPrivate.class).order("name").fetch();
		List<DiscoveryPrivate> res = future.get(); 
		assertEquals(foo, res.get(0));
		assertEquals(radioactivity, res.get(1));
		assertEquals(relativity, res.get(2));
		assertEquals(teslaCoil, res.get(3));
	}
	
	public void testFetchPaginateStatelessNextPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
	}
	
	public void testFetchPaginateStatelessNextPageToEnd() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size());

		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size());

		future = query.previousPage().fetch();
		res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatelessNextPageRealAsync() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future1 = query.fetch();
		SienaFuture<List<Discovery>> future2 = query.nextPage().fetch();
		
		List<Discovery> res = future1.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		res = future2.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
	}
	
	public void testFetchPaginateStatelessPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future = query.previousPage().fetch();
		List<Discovery> res = future.get(); 
		assertEquals(0, res.size());

		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(0, res.size());
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(0, res.size());
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(0, res.size());
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateStatelessPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future = query.nextPage().fetch();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateStatelessPreviouPageSeveralTimes() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulNextPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatefulNextPageToEnd() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name").stateful();
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size());

		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size());

		future = query.previousPage().fetch();
		res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatefulNextPageRealAsync() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<List<Discovery>> future1 = query.fetch();
		SienaFuture<List<Discovery>> future2 = query.nextPage().fetch();
		
		List<Discovery> res = future1.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		res = future2.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<List<Discovery>> future = query.previousPage().fetch();
		List<Discovery> res = future.get(); 
		assertEquals(0, res.size());

		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(0, res.size());
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(0, res.size());
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(0, res.size());
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}

		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulPreviouPageSeveralTimes() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchKeysPaginateStatelessNextPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future = query.fetchKeys();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		future = query.nextPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatelessNextPageRealAsync() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future1 = query.fetchKeys();
		SienaFuture<List<Discovery>> future2 = query.nextPage().fetchKeys();
		
		List<Discovery> res = future1.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		res = future2.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatelessPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future = query.previousPage().fetchKeys();
		List<Discovery> res = future.get(); 
		assertEquals(0, res.size());

		future = query.previousPage().fetchKeys();
		res = future.get(); 
		assertEquals(0, res.size());
	}
	
	public void testFetchKeysPaginateStatelessPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<List<Discovery>> future = query.nextPage().fetchKeys();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.previousPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatelessPreviouPageSeveralTimes() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		SienaFuture<List<Discovery>> future = query.fetchKeys();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}		
		
		future = query.nextPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.previousPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.previousPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatefulNextPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.fetchKeys();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		future = query.nextPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatefulNextPageRealAsync() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<List<Discovery>> future1 = query.fetchKeys();
		SienaFuture<List<Discovery>> future2 = query.nextPage().fetchKeys();
		
		List<Discovery> res = future1.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		res = future2.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatefulPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<List<Discovery>> future = query.previousPage().fetchKeys();
		List<Discovery> res = future.get(); 
		assertEquals(0, res.size());

		future = query.previousPage().fetchKeys();
		res = future.get(); 
		assertEquals(0, res.size());
	}
	
	public void testFetchKeysPaginateStatefulPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<List<Discovery>> future = query.fetchKeys();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		future = query.nextPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.previousPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatefulPreviouPageSeveralTimes() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.fetchKeys();
		List<Discovery> res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
		}		
		
		future = query.nextPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10].id, res.get(i).id);
		}
		
		future = query.previousPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
		}
		
		future = query.previousPage().fetchKeys();
		res = future.get(); 
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
		}
	}
	
	public void testIterPaginateStatelessNextPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		try {
			SienaFuture<Iterable<Discovery>> res = query.iter();
			Iterator<Discovery> it = res.get().iterator();
			int i=0;
			while(it.hasNext()){
				assertEquals(discs[i++], it.next());
			}
			assertEquals(5, i);

			res = query.nextPage().iter();
			it = res.get().iterator();
			while(it.hasNext()){
				assertEquals(discs[i++], it.next());
			}
			assertEquals(10, i);
		}finally {
			query.release();
		}
	}
	
	public void testIterPaginateStatelessNextPageRealAsync() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<Iterable<Discovery>> future1 = query.iter();
		SienaFuture<Iterable<Discovery>> future2 = query.nextPage().iter();
		
		Iterator<Discovery> it = future1.get().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(5, i);

		it = future2.get().iterator(); 
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(10, i);

	}
	
	public void testIterPaginateStatelessPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<Iterable<Discovery>> future = query.previousPage().iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(0, i);

		future = query.previousPage().iter();
		i=0;
		it = future.get().iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(0, i);

	}
	
	public void testIterPaginateStatelessPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		SienaFuture<Iterable<Discovery>> future = query.nextPage().iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=5;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(10, i);

		future = query.previousPage().iter();
		it = future.get().iterator();
		i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(5, i);

	}
	
	public void testIterPaginateStatelessPreviouPageSeveralTimes() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(5, i);
		
		future = query.nextPage().iter();
		it = future.get().iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(10, i);
		
		future = query.nextPage().iter();
		it = future.get().iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(15, i);
	
		future = query.previousPage().iter();
		it = future.get().iterator();
		i=5;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(10, i);

		future = query.previousPage().iter();
		it = future.get().iterator();
		i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(5, i);

	}
	
	public void testIterPaginateStatefulNextPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		future = query.nextPage().iter();
		it = future.get().iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		
		assertEquals(10, i);
	}
	
	public void testIterPaginateStatefulNextPageRealAsync() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<Iterable<Discovery>> future1 = query.iter();
		SienaFuture<Iterable<Discovery>> future2 = query.nextPage().iter();
		
		Iterator<Discovery> it = future1.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
		it = future2.get().iterator(); 
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}
		assertEquals(10, i);
	}
	
	public void testIterPaginateStatefulPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<Iterable<Discovery>> future = query.previousPage().iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(0, i);

		it = future.get().iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(0, i);
	}
	
	public void testIterPaginateStatefulPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);

		future = query.nextPage().iter();
		it = future.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		future = query.previousPage().iter();
		it = future.get().iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
	}
	
	public void testIterPaginateStatefulPreviousPageAsync() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		SienaFuture<Iterable<Discovery>> future1 = query.iter();
		SienaFuture<Iterable<Discovery>> future2 = query.nextPage().iter();
		SienaFuture<Iterable<Discovery>> future3 = query.previousPage().iter();
		Iterator<Discovery> it = future1.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);

		it = future2.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		it = future3.get().iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
	}
	
	public void testIterPaginateStatefulPreviouPageSeveralTimes() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);	
		
		future = query.nextPage().iter();
		it = future.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);	
		
		future = query.nextPage().iter();
		it = future.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);	
		
		future = query.previousPage().iter();
		i=5;
		it = future.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);	
		
		future = query.previousPage().iter();
		i=0;
		it = future.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
	}
	
	public void testIterPaginateStatefulSeveralTimesRealAsync() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		SienaFuture<Iterable<Discovery>> future2 = query.nextPage().iter();
		SienaFuture<Iterable<Discovery>> future3 = query.nextPage().iter();
		SienaFuture<Iterable<Discovery>> future4 = query.previousPage().iter();
		SienaFuture<Iterable<Discovery>> future5 = query.previousPage().iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);	
		
		it = future2.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);	
		
		it = future3.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);	
		
		i=5;
		it = future4.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);	
		
		i=0;
		it = future5.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
	}
	
	public void testIterLotsOfEntitiesStateless(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
	
	public void testIterLotsOfEntitiesStateful(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
	
	public void testIterLotsOfEntitiesStatefulMixed(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter(50);
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		future = query.iter(50,50);
		it = future.get().iterator();
		i=100;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	

		future = query.iter(50,100);
		it = future.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
	
	public void testIterLotsOfEntitiesStatefulMixed2(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.paginate(50).iter();
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		future = query.iter(50,50);
		it = future.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	

	}

	public void testIterLotsOfEntitiesStatefulMixed3(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter(50);
		Iterator<Discovery> it = future.get().iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
				
		future = query.paginate(50).iter();
		it = future.get().iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	
	
		future = query.iter();
		it = future.get().iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	

		future = query.nextPage().iter();
		it = future.get().iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
	
	public void testFetchLotsOfEntitiesStatefulMixed(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.paginate(50).fetch();
		List<Discovery> res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.fetch(50);
		res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		future = query.fetch(50);
		res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testFetchLotsOfEntitiesStatefulMixed2(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.fetch(50);
		List<Discovery> res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.paginate(50).fetch(50);
		res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}

		future = query.fetch(50);
		res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}
	}
	
	public void testFetchIterLotsOfEntitiesStatefulMixed(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.fetch(50);
		List<Discovery> res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		SienaFuture<Iterable<Discovery>> future2 = query.iter(50);
		Iterator<Discovery> it = future2.get().iterator();
		int i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	

		future = query.paginate(25).fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		future2 = query.nextPage().iter();
		it = future2.get().iterator();
		i=125;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+125], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get(); 
		assertEquals(0, res.size());
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+125], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+25], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get(); 
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchIterLotsOfEntitiesStatefulMixed2(){
		Discovery[] discs = new Discovery[200];
		for(int i=0; i<200; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.fetch(50);
		List<Discovery> res = future.get(); 
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		SienaFuture<Iterable<Discovery>> future2 = query.iter(50);
		Iterator<Discovery> it = future2.get().iterator();
		int i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	
		
		future = query.fetch(50);
		res = future.get(); 
		assertEquals(50, res.size());
		for(i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		future2 = query.iter(50);
		it = future2.get().iterator();
		i=150;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(200, i);	

	}
	
	
	public void testSearchSingle() {
		Discovery4Search[] discs = new Discovery4Search[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		SienaFuture<List<Discovery4Search>> future = query.fetch();
		List<Discovery4Search> res = future.get();
				
		assertEquals(1, res.size());
		assertEquals(discs[5], res.get(0));
	}
	
	
	public void testSearchSingleKeysOnly() {
		Discovery4Search[] discs = new Discovery4Search[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		SienaFuture<List<Discovery4Search>> future = query.fetchKeys();
		List<Discovery4Search> res = future.get();
				
		assertEquals(1, res.size());
		assertEquals(discs[5].id, res.get(0).id);
		assertTrue(res.get(0).isOnlyIdFilled());
	}
	
	public void testSearchSingleTwice() {
		Discovery4Search[] discs = new Discovery4Search[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		SienaFuture<List<Discovery4Search>> future = query.fetch();
		List<Discovery4Search> res = future.get();
				
		assertEquals(1, res.size());
		assertEquals(discs[5], res.get(0));

		query = 
			pm.createQuery(Discovery4Search.class).search("Disc_48", "name");
		
		future = query.fetch();
		res = future.get();
				
		assertEquals(1, res.size());
		assertEquals(discs[48], res.get(0));

	}

	public void testSearchSingleCount() {
		Discovery4Search[] discs = new Discovery4Search[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		int res = query.count().get();
				
		assertEquals(1, res);
	}
	
	public void testBatchInsert() {
		Object[] discs = new Discovery[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		SienaFuture<Integer> resf = pm.insert(discs);
		assertEquals(discs.length, (int)resf.get());
		
		SienaFuture<List<Discovery>> future = 
			pm.createQuery(Discovery.class).fetch();
		List<Discovery> res = future.get();
		
		assertEquals(discs.length, res.size());
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs[i++], disc);
		}
	}
	public void testBatchInsertList() {
		List<Discovery> discs = new ArrayList<Discovery>();
		for(int i=0; i<100; i++){
			discs.add(new Discovery("Disc_"+i, LongAutoID_CURIE));
		}
		SienaFuture<Integer> resf = pm.insert(discs);
		assertEquals(discs.size(), (int)resf.get());

		SienaFuture<List<Discovery>> future = 
			pm.createQuery(Discovery.class).fetch();
		List<Discovery> res = future.get();
		
		assertEquals(discs.size(), res.size());
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs.get(i++), disc);
		}
	}
	
	public void testBatchDelete() {
		Object[] discs = new Discovery[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert(discs).get();

		SienaFuture<List<Discovery>> future = 
			pm.createQuery(Discovery.class).fetch();
		List<Discovery> res = future.get();
		
		assertEquals(discs.length, res.size());
		
		SienaFuture<Integer> resf = pm.delete(discs);
		assertEquals(discs.length, (int)resf.get());
		
		future = 
			pm.createQuery(Discovery.class).fetch();
		res = future.get();
		
		assertEquals(0, res.size());
	}
	
	public void testBatchDeleteList() {
		List<Discovery> discs = new ArrayList<Discovery>();
		for(int i=0; i<100; i++){
			Discovery disc = new Discovery("Disc_"+i, LongAutoID_CURIE);
			discs.add(disc);
		}
		pm.insert(discs).get();

		SienaFuture<List<Discovery>> future = 
			pm.createQuery(Discovery.class).fetch();
		List<Discovery> res = future.get();
		
		assertEquals(discs.size(), res.size());
		
		pm.delete(discs).get();
		
		future = 
			pm.createQuery(Discovery.class).fetch();
		res = future.get();
		
		assertEquals(0, res.size());
	}
	
	
	public void testBatchDeleteByKeys() {
		SienaFuture<Integer> resf = pm.deleteByKeys(PersonStringID.class, "TESLA", "CURIE");
		assertEquals(2, (int)resf.get());
		
		List<PersonStringID> res = 
			pm.createQuery(PersonStringID.class).fetch().get();
		
		assertEquals(1, res.size());
		assertEquals(StringID_EINSTEIN, res.get(0));
	}
	
	public void testBatchDeleteByKeysList() {
		SienaFuture<Integer> resf = pm.deleteByKeys(PersonStringID.class, new ArrayList<String>(){
			private static final long serialVersionUID = 1L;
			{add("TESLA"); add( "CURIE");}
		});
		assertEquals(2, (int)resf.get());

		List<PersonStringID> res = 
			pm.createQuery(PersonStringID.class).fetch().get();
		
		assertEquals(1, res.size());
		assertEquals(StringID_EINSTEIN, res.get(0));
	}
	
	public void testBatchGet() {
		Discovery[] discs = new Discovery[100];
		
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		Discovery[] discs2Get = new Discovery[100];
		for(int i=0; i<100; i++){
			discs2Get[i] = new Discovery();
			discs2Get[i].id = discs[i].id;
		}
		
		SienaFuture<Integer> resf = pm.get((Object[])discs2Get);
		assertEquals(discs.length, (int)resf.get());

		assertEquals(discs.length, discs2Get.length);
		for(int i=0; i<discs.length; i++){
			assertEquals(discs[i], discs2Get[i]);
		}		
	}
	
	public void testBatchGetList() {
		Discovery[] discs = new Discovery[100];
		
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		List<Discovery> discs2Get = new ArrayList<Discovery>();
		for(int i=0; i<100; i++){
			Discovery disc = new Discovery();
			disc.id = discs[i].id;
			discs2Get.add(disc);
		}
		
		SienaFuture<Integer> resf = pm.get(discs2Get);
		assertEquals(discs.length, (int)resf.get());

		int i=0;
		for(Discovery disc:discs2Get){
			assertEquals(discs[i++], disc);
		}		
	}
	
	public void testBatchGetByKeys() {
		SienaFuture<List<PersonStringID>> future = pm.getByKeys(PersonStringID.class, "TESLA", "CURIE");
		
		List<PersonStringID> res = future.get();
		assertEquals(2, res.size());
		assertEquals(StringID_TESLA, res.get(0));
		assertEquals(StringID_CURIE, res.get(1));
	}
	
	public void testBatchGetByKeysList() {
		Discovery[] discs = new Discovery[100];
		
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		List<Long> discsKeys = new ArrayList<Long>();
		for(int i=0; i<100; i++){
			discsKeys.add(discs[i].id);
		}
		
		SienaFuture<List<Discovery>> future = pm.getByKeys(Discovery.class, discsKeys);
		List<Discovery> discs2Get = future.get();
		int i=0;
		for(Discovery disc:discs2Get){
			assertEquals(discs[i++], disc);
		}		
	}
	
	public void testLimitStateless(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateless().order("id");
		SienaFuture<List<Discovery>> future = query.limit(50).fetch();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.limit(50).fetch();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.fetch(50);
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.paginate(50).fetch();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	
	
	public void testLimitStateful(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.limit(50).fetch();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.fetch(50);
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		future = query.paginate(50).fetch(25);
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}
	}

	public void testOffsetStateless(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<List<Discovery>> future = query.offset(50).fetch();
		List<Discovery> res = future.get();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}

	public void testOffsetStateful(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.offset(50).fetch();
		List<Discovery> res = future.get();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testOffsetLimitStateless(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<List<Discovery>> future = query.offset(50).limit(50).fetch();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testOffsetLimitStateful(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.offset(50).limit(50).fetch();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}


	public void testOffsetLimitStatelessPaginate(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<List<Discovery>> future = query.paginate(50).fetch();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		future = query.limit(50).fetch();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		future = query.offset(50).fetch();
		res = future.get();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		future = query.offset(50).limit(50).fetch();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

	}
	
	public void testOffsetLimitStatefulPaginate(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id").stateful();
		SienaFuture<List<Discovery>> future = query.paginate(50).fetch();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		future = query.limit(50).fetch();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		future = query.offset(50).fetch();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+150], res.get(i));
		}
		
		future = query.offset(50).limit(25).fetch();
		res = future.get();
		assertEquals(25, res.size());
		for(int i=0; i<25; i++){
			assertEquals(discs[i+250], res.get(i));
		}
		try {
			future = query.previousPage().fetch();
		}catch(SienaException ex){
			return;
		}
		fail();
	}
	
	public void testOffsetLimitStatelessPaginate2(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<List<Discovery>> future = query.limit(50).offset(12).fetch();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		future = query.offset(13).limit(30).fetch();
		res = future.get();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+13], res.get(i));
		}
		
		future = query.offset(10).limit(30).fetch(15);
		res = future.get();
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		future = query.paginate(6).fetch();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6], res.get(i));
		}
		
		future = query.offset(10).fetch(10);
		res = future.get();
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		try {
			future = query.nextPage().fetch();
		}catch(SienaException ex){
			future = query.paginate(8).fetch();
			res = future.get();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i], res.get(i));
			}
			
			future = query.nextPage().fetch();
			res = future.get();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+8], res.get(i));
			}
			
			return;
		}
		fail();
		
	}
	
	public void testOffsetLimitStatefulPaginate2(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id").stateful();
		SienaFuture<List<Discovery>> future = query.limit(50).offset(12).fetch();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		future = query.offset(13).limit(30).fetch();
		res = future.get();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		future = query.offset(10).limit(30).fetch(15);
		res = future.get();
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+115], res.get(i));
		}
		
		future = query.paginate(6).fetch();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+130], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+142], res.get(i));
		}
		
		future = query.previousPage().fetch();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136], res.get(i));
		}
		
		future = query.offset(10).fetch(10);
		res = future.get();
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+146], res.get(i));
		}
		
		try {
			future = query.nextPage().fetch();
		}catch(SienaException ex){
			future = query.paginate(8).fetch();
			res = future.get();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+156], res.get(i));
			}
			
			future = query.nextPage().fetch();
			res = future.get();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+164], res.get(i));
			}
		}
	}
	
	public void testFetchPaginateStatelessTwice() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.paginate(8).fetch();
		res = future.get();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(7, res.size());
		for(int i=0; i<7; i++){
			assertEquals(discs[i+8], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulTwice() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().paginate(5).order("id");
		SienaFuture<List<Discovery>> future = query.fetch();
		List<Discovery> res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.paginate(8).fetch();
		res = future.get();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		future = query.nextPage().fetch();
		res = future.get();
		assertEquals(2, res.size());
		for(int i=0; i<2; i++){
			assertEquals(discs[i+13], res.get(i));
		}
	}
	
	
	public void testLimitStatelessKeys(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateless().order("id");
		SienaFuture<List<Discovery>> future = query.limit(50).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.limit(50).fetchKeys();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.fetchKeys(50);
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.paginate(50).fetchKeys();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testLimitStatefulKeys(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.limit(50).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.fetchKeys(50);
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.paginate(50).fetchKeys(25);
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testOffsetStatelessKeys(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<List<Discovery>> future = query.offset(50).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}

	public void testOffsetStatefulKeys(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.offset(50).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testOffsetLimitStatelessKeys(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<List<Discovery>> future = query.offset(50).limit(50).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}

	public void testOffsetLimitStatefulKeys(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future = query.offset(50).limit(50).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}

	
	public void testOffsetLimitStatelessPaginateKeys(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<List<Discovery>> future = query.paginate(50).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		future = query.limit(50).fetchKeys();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		future = query.offset(50).fetchKeys();
		res = future.get();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.offset(50).limit(50).fetchKeys();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

	}
	
	public void testOffsetLimitStatefulPaginateKeys(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id").stateful();
		SienaFuture<List<Discovery>> future = query.paginate(50).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		future = query.limit(50).fetchKeys();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		future = query.offset(50).fetchKeys();
		res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+150].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.offset(50).limit(25).fetchKeys();
		res = future.get();
		assertEquals(25, res.size());
		for(int i=0; i<25; i++){
			assertEquals(discs[i+250].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		try {
			future = query.previousPage().fetch();
		}catch(SienaException ex){
			return;
		}
		fail();
	}
	
	public void testOffsetLimitStatelessPaginate2Keys(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<List<Discovery>> future = query.limit(50).offset(12).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.offset(13).limit(30).fetchKeys();
		res = future.get();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+13].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.offset(10).limit(30).fetchKeys(15);
		res = future.get();
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+10].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.paginate(6).fetchKeys();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+12].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.previousPage().fetchKeys();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.offset(10).fetchKeys(10);
		res = future.get();
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+10].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		try {
			future = query.nextPage().fetchKeys();
		}catch(SienaException ex){
			future = query.paginate(8).fetch();
			res = future.get();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i].id, res.get(i).id);
			}
			
			future = query.nextPage().fetchKeys();
			res = future.get();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+8].id, res.get(i).id);
			}
		}
		
	}
	
	public void testOffsetLimitStatefulPaginate2Keys(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id").stateful();
		SienaFuture<List<Discovery>> future = query.limit(50).offset(12).fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.offset(13).limit(30).fetchKeys();
		res = future.get();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+75].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.offset(10).limit(30).fetchKeys(15);
		res = future.get();
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+115].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.paginate(6).fetchKeys();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+130].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+142].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.previousPage().fetchKeys();
		res = future.get();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.offset(10).fetchKeys(10);
		res = future.get();
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+146].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		try {
			future = query.nextPage().fetchKeys();
		}catch(SienaException ex){
			future = query.paginate(8).fetchKeys();
			res = future.get();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+156].id, res.get(i).id);
				assertTrue(res.get(i).isOnlyIdFilled());
			}
			
			future = query.nextPage().fetchKeys();
			res = future.get();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+164].id, res.get(i).id);
				assertTrue(res.get(i).isOnlyIdFilled());
			}
		}
	}
	
	public void testFetchPaginateStatelessTwiceKeys() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		SienaFuture<List<Discovery>> future = query.fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}		
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.paginate(8).fetchKeys();
		res = future.get();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(7, res.size());
		for(int i=0; i<7; i++){
			assertEquals(discs[i+8].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchPaginateStatefulTwiceKeys() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().paginate(5).order("id");
		SienaFuture<List<Discovery>> future = query.fetchKeys();
		List<Discovery> res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}		
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.paginate(8).fetchKeys();
		res = future.get();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		future = query.nextPage().fetchKeys();
		res = future.get();
		assertEquals(2, res.size());
		for(int i=0; i<2; i++){
			assertEquals(discs[i+13].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testLimitStatelessIter(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateless().order("id");
		SienaFuture<Iterable<Discovery>> future = query.limit(50).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
		
		future = query.limit(50).iter();
		iter = future.get();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
		
		future = query.iter(50);
		iter = future.get();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		future = query.paginate(50).iter();
		iter = future.get();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
	}
	
	public void testLimitStatefulIter(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.limit(50).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
		
		future = query.iter(50);
		iter = future.get();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);
		
		future = query.paginate(50).iter(25);
		iter = future.get();
		it = iter.iterator();
		i=100;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
	}
	
	public void testOffsetStatelessIter(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<Iterable<Discovery>> future = query.offset(50).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
	}
	
	public void testOffsetStatefulIter(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.offset(50).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
	}
	
	public void testOffsetLimitStatelessIter(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<Iterable<Discovery>> future = query.offset(50).limit(50).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);
	}
	
	public void testOffsetLimitStatefulIter(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.offset(50).limit(50).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);
	}
	
	public void testOffsetLimitStatelessPaginateIter(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<Iterable<Discovery>> future = query.paginate(50).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		future = query.limit(50).iter();
		iter = future.get();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);

		future = query.offset(50).iter();
		iter = future.get();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
		
		future = query.offset(50).limit(50).iter();
		iter = future.get();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

	}
	
	public void testOffsetLimitStatefulPaginateIter(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.paginate(50).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		future = query.limit(50).iter();
		iter = future.get();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		future = query.offset(50).iter();
		iter = future.get();
		it = iter.iterator();
		i=150;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(200, i);
		
		future = query.offset(50).limit(25).iter();
		iter = future.get();
		it = iter.iterator();
		i=250;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(275, i);

		try {
			future = query.previousPage().iter();
		}catch(SienaException ex){
			return;
		}
		fail();
	}
	
	public void testOffsetLimitStatelessPaginate2Iter(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<Iterable<Discovery>> future = query.limit(50).offset(12).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=12;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(62, i);
		
		future = query.offset(13).limit(30).iter();
		iter = future.get();
		it = iter.iterator();
		i=13;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(43, i);
		
		future = query.offset(10).limit(30).iter(15);
		iter = future.get();
		it = iter.iterator();
		i=10;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(25, i);

		future = query.paginate(6).iter();
		iter = future.get();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(6, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=6;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(12, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=12;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(18, i);
		
		future = query.previousPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=6;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(12, i);
		
		future = query.offset(10).iter(10);
		iter = future.get();
		it = iter.iterator();
		i=10;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(20, i);
		
		try {
			future = query.nextPage().iter();
		}catch(SienaException ex){
			future = query.paginate(8).iter();
			iter = future.get();
			it = iter.iterator();
			i=0;
			while(it.hasNext()){
				Discovery disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(8, i);
			
			future = query.nextPage().iter();
			iter = future.get();
			it = iter.iterator();
			i=8;
			while(it.hasNext()){
				Discovery disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(16, i);
			
			return;
		}
		fail();
		
	}
	
	public void testOffsetLimitStatefulPaginate2Iter(){
		Discovery[] discs = new Discovery[300];
		for(int i=0; i<300; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> future = query.limit(50).offset(12).iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=12;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(62, i);
		
		future = query.offset(13).limit(30).iter();
		iter = future.get();
		it = iter.iterator();
		i=75;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(105, i);
		
		future = query.offset(10).limit(30).iter(15);
		iter = future.get();
		it = iter.iterator();
		i=115;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(130, i);

		future = query.paginate(6).iter();
		iter = future.get();
		it = iter.iterator();
		i=130;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(136, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=136;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(142, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=142;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(148, i);
		
		future = query.previousPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=136;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(142, i);
		
		future = query.offset(10).iter(10);
		iter = future.get();
		it = iter.iterator();
		i=146;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(156, i);
		
		try {
			future = query.nextPage().iter();
		}catch(SienaException ex){
			future = query.paginate(8).iter();
			iter = future.get();
			it = iter.iterator();
			i=156;
			while(it.hasNext()){
				Discovery disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(164, i);
			
			future = query.nextPage().iter();
			iter = future.get();
			it = iter.iterator();
			i=164;
			while(it.hasNext()){
				Discovery disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(172, i);
			
			return;
		}
		fail();
		
	}
	
	public void testFetchPaginateStatelessTwiceIter() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		future = query.paginate(8).iter();
		iter = future.get();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(8, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=8;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);
	}
	
	public void testFetchPaginateStatefulTwiceIter() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();

		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().paginate(5).order("id");
		SienaFuture<Iterable<Discovery>> future = query.iter();
		Iterable<Discovery> iter = future.get();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		future = query.paginate(8).iter();
		iter = future.get();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(13, i);
		
		future = query.nextPage().iter();
		iter = future.get();
		it = iter.iterator();
		i=13;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);
	}
	
	public void testLimitStatelessRealAsync(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateless().order("id");
		SienaFuture<List<Discovery>> future1 = query.limit(50).fetch();
		SienaFuture<List<Discovery>> future2 = query.offset(50).fetch();
		SienaFuture<List<Discovery>> future21 = query.limit(70).offset(30).fetch();
		SienaFuture<List<Discovery>> future3 = query.fetch(50);
		SienaFuture<List<Discovery>> future4 = query.paginate(50).fetch();
		SienaFuture<List<Discovery>> future5 = query.nextPage().fetch();
		SienaFuture<List<Discovery>> future6 = query.nextPage().fetch();
		SienaFuture<List<Discovery>> future7 = query.previousPage().fetch();

		List<Discovery> res = future1.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = future2.get();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50], res.get(i));
		}
				
		res = future21.get();
		assertEquals(70, res.size());
		for(int i=0; i<70; i++){
			assertEquals(discs[i+30], res.get(i));
		}
		
		res = future3.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = future4.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = future5.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		res = future6.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		res = future7.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}		
	}
	
	public void testLimitStatefulRealAsync(){
		Discovery[] discs = new Discovery[320];
		for(int i=0; i<320; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<List<Discovery>> future1 = query.limit(50).fetch();
		SienaFuture<List<Discovery>> future2 = query.offset(5).fetch(5);
		SienaFuture<List<Discovery>> future21 = query.limit(70).offset(30).fetch();
		SienaFuture<List<Discovery>> future3 = query.fetch(50);
		SienaFuture<List<Discovery>> future4 = query.paginate(50).fetch();
		SienaFuture<List<Discovery>> future5 = query.nextPage().fetch();
		SienaFuture<List<Discovery>> future6 = query.nextPage().fetch();
		SienaFuture<List<Discovery>> future7 = query.previousPage().fetch();

		List<Discovery> res = future1.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = future2.get();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+55], res.get(i));
		}
		// 60		
		res = future21.get();
		assertEquals(70, res.size());
		for(int i=0; i<70; i++){
			assertEquals(discs[i+90], res.get(i));
		}
		// 160
		res = future3.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+160], res.get(i));
		}
		
		res = future4.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+210], res.get(i));
		}
		
		res = future5.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+260], res.get(i));
		}
		
		res = future6.get();
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+310], res.get(i));
		}
		
		res = future7.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+260], res.get(i));
		}		
	}
	
	public void testFetchStringAutoInc() {
		PersonStringAutoIncID person = new PersonStringAutoIncID("TEST1", "TEST2", "TEST3", "TEST4", 123);
		
		pm.insert(person).get();
		
		SienaFuture<List<PersonStringAutoIncID>> future = pm.getByKeys(PersonStringAutoIncID.class, "TEST1");
		List<PersonStringAutoIncID> l = future.get();
		assertEquals(person, l.get(0));
	}
	
	public void testInsertObjectWithNullJoinObject() {
		Discovery4Join model = new Discovery4Join();
		model.discovererJoined = null; // explicitly set the join object to null

		pm.insert(model).get();

		QueryAsync<Discovery4Join> query = pm.createQuery(Discovery4Join.class)
				.filter("id", model.id);
		Discovery4Join modelFromDatabase = pm.get(query).get();
		assertNull(modelFromDatabase.discovererJoined);
	}

	public void testInsertObjectWithDoubleNullJoinObject() {
		Discovery4Join2 model = new Discovery4Join2();
		model.discovererJoined = null; // explicitly set the join object to null
		model.discovererJoined2 = null; // explicitly set the join object to
										// null

		pm.insert(model).get();

		QueryAsync<Discovery4Join2> query = pm.createQuery(Discovery4Join2.class)
				.filter("id", model.id);
		Discovery4Join2 modelFromDatabase = pm.get(query).get();
		assertNull(modelFromDatabase.discovererJoined);
		assertNull(modelFromDatabase.discovererJoined2);
	}

	public void testJoinAnnotationDouble() {
		Discovery4Join2 radioactivity = new Discovery4Join2("Radioactivity",
				LongAutoID_CURIE, LongAutoID_TESLA);
		Discovery4Join2 relativity = new Discovery4Join2("Relativity",
				LongAutoID_EINSTEIN, LongAutoID_TESLA);
		Discovery4Join2 foo = new Discovery4Join2("Foo", LongAutoID_EINSTEIN,
				LongAutoID_EINSTEIN);
		Discovery4Join2 teslaCoil = new Discovery4Join2("Tesla Coil",
				LongAutoID_TESLA, LongAutoID_CURIE);

		pm.insert(radioactivity, relativity, foo, teslaCoil).get();

		List<Discovery4Join2> res = pm.createQuery(Discovery4Join2.class)
				.fetch().get();
		assertEquals(4, res.size());
		assertEquals(radioactivity, res.get(0));
		assertEquals(relativity, res.get(1));
		assertEquals(foo, res.get(2));
		assertEquals(teslaCoil, res.get(3));

		assertEquals(LongAutoID_CURIE, res.get(0).discovererJoined);
		assertEquals(LongAutoID_EINSTEIN, res.get(1).discovererJoined);
		assertEquals(LongAutoID_EINSTEIN, res.get(2).discovererJoined);
		assertEquals(LongAutoID_TESLA, res.get(3).discovererJoined);

		assertEquals(LongAutoID_TESLA, res.get(0).discovererJoined2);
		assertEquals(LongAutoID_TESLA, res.get(1).discovererJoined2);
		assertEquals(LongAutoID_EINSTEIN, res.get(2).discovererJoined2);
		assertEquals(LongAutoID_CURIE, res.get(3).discovererJoined2);

	}
	
	public void testBatchUpdate() {
		Object[] discs = new Discovery[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert(discs).get();

		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch().get();
		
		assertEquals(discs.length, res.size());
		
		for(int i=0; i<100; i++){
			((Discovery)discs[i]).discoverer = LongAutoID_EINSTEIN;
		}
		
		int nb = pm.update(discs).get();
		assertEquals(discs.length, nb);
		res = 
			pm.createQuery(Discovery.class).fetch().get();
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs[i++], disc);
		}
		
	}
	
	public void testBatchUpdateList() {
		List<Discovery> discs = new ArrayList<Discovery>();
		for(int i=0; i<100; i++){
			discs.add(new Discovery("Disc_"+i, LongAutoID_CURIE));
		}
		int nb = pm.insert(discs).get();
		assertEquals(discs.size(), nb);

		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch().get();
		
		assertEquals(discs.size(), res.size());
		
		for(Discovery d: discs){
			d.discoverer = LongAutoID_EINSTEIN;
		}
		
		nb = pm.update(discs).get();
		assertEquals(discs.size(), nb);
		res = 
			pm.createQuery(Discovery.class).fetch().get();
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs.get(i++), disc);
		}
		
	}
	
	
	public void testIterPerPageStateless(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<Iterable<Discovery>> iter = query.iterPerPage(50);
		int i=0;
		for(Discovery disc: iter.get()){
			assertEquals(discs[i++], disc);
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateless2(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<Iterable<Discovery>> iter = query.iterPerPage(50);
		Iterator<Discovery> it = iter.get().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateless3(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).order("id");
		SienaFuture<Iterable<Discovery>> iter = query.offset(25).iterPerPage(50);
		Iterator<Discovery> it = iter.get().iterator();
		int i=25;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateful(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> iter = query.iterPerPage(50);
		int i=0;
		for(Discovery disc: iter.get()){
			assertEquals(discs[i++], disc);
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateful2(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> iter = query.iterPerPage(50);
		Iterator<Discovery> it = iter.get().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStatefull3(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs).get();
		
		QueryAsync<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		SienaFuture<Iterable<Discovery>> iter = query.offset(25).iterPerPage(50);
		Iterator<Discovery> it = iter.get().iterator();
		int i=25;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
	public void testGetByKeyUUID() {
		PersonUUID curie = pm.getByKey(PersonUUID.class, UUID_CURIE.id).get();
		assertEquals(UUID_CURIE, curie);
	}

	public void testGetByKeyLongAutoID() {
		PersonLongAutoID curie = pm.getByKey(PersonLongAutoID.class, LongAutoID_CURIE.id).get();
		assertEquals(LongAutoID_CURIE, curie);
	}

	public void testGetByKeyLongManualID() {
		PersonLongManualID curie = pm.getByKey(PersonLongManualID.class, LongManualID_CURIE.id).get();
		assertEquals(LongManualID_CURIE, curie);
	}

	public void testGetByKeyStringID() {
		PersonStringID curie = pm.getByKey(PersonStringID.class, StringID_CURIE.id).get();
		assertEquals(StringID_CURIE, curie);
	}
	
	public void testSaveLongAutoID() {
		PersonLongAutoID maxwell = new PersonLongAutoID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell).get();
		assertNotNull(maxwell.id);

		SienaFuture<List<PersonLongAutoID>> future = queryPersonLongAutoIDOrderBy("n", 0, false).fetch();
		List<PersonLongAutoID> people = future.get();
		assertEquals(4, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
		assertEquals(LongAutoID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.save(maxwell).get();
		assertNotNull(maxwell.id);
		
		future = queryPersonLongAutoIDOrderBy("n", 0, false).fetch();
		people = future.get();
		assertEquals(4, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
		assertEquals(LongAutoID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}
	
	public void testSaveUUID() {
		PersonUUID maxwell = new PersonUUID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell).get();
		assertNotNull(maxwell.id);

		SienaFuture<List<PersonUUID>> future = queryPersonUUIDOrderBy("n", 0, false).fetch();
		List<PersonUUID> people = future.get();
		assertEquals(4, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.save(maxwell).get();
		assertNotNull(maxwell.id);
		
		future = queryPersonUUIDOrderBy("n", 0, false).fetch();
		people = future.get();
		assertEquals(4, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}


	public void testSaveLongManualID() {
		PersonLongManualID maxwell = new PersonLongManualID();
		maxwell.id = 4L;
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell).get();
		assertEquals((Long)4L, maxwell.id);

		SienaFuture<List<PersonLongManualID>> future = queryPersonLongManualIDOrderBy("n", 0, false).fetch();
		List<PersonLongManualID> people = future.get();
		assertEquals(4, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
		assertEquals(LongManualID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.save(maxwell).get();
		assertEquals((Long)4L, maxwell.id);

		future = queryPersonLongManualIDOrderBy("n", 0, false).fetch();
		people = future.get();
		assertEquals(4, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
		assertEquals(LongManualID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}
	
	public void testSaveStringID() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell).get();
		assertEquals(maxwell.id, "MAXWELL");

		SienaFuture<List<PersonStringID>> future = queryPersonStringIDOrderBy("n", 0, false).fetch();
		List<PersonStringID> people = future.get();
		assertEquals(4, people.size());

		assertEquals(StringID_TESLA, people.get(0));
		assertEquals(StringID_CURIE, people.get(1));
		assertEquals(StringID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));

		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.save(maxwell).get();
		assertEquals(maxwell.id, "MAXWELL");

		future = queryPersonStringIDOrderBy("n", 0, false).fetch();
		people = future.get();
		assertEquals(4, people.size());

		assertEquals(StringID_TESLA, people.get(0));
		assertEquals(StringID_CURIE, people.get(1));
		assertEquals(StringID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
	}
	
	public void testBatchSave() {
		Object[] discs = new Discovery[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.save(discs).get();

		SienaFuture<List<Discovery>> future = 
			pm.createQuery(Discovery.class).fetch();
		List<Discovery> res = future.get();
		assertEquals(discs.length, res.size());
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs[i++], disc);
		}
		
		for(i=0; i<100; i++){
			((Discovery)discs[i]).name += "UPD";
			((Discovery)discs[i]).discoverer = LongAutoID_EINSTEIN;
		}
		
		int nb = pm.save(discs).get();
		assertEquals(discs.length, nb);
		future = 
			pm.createQuery(Discovery.class).fetch();
		res = future.get();
		i=0;
		for(Discovery disc:res){
			assertEquals(discs[i++], disc);
		}
		
	}
	
	
	public void testBatchSaveList() {
		List<Discovery> discs = new ArrayList<Discovery>();
		for(int i=0; i<100; i++){
			discs.add(new Discovery("Disc_"+i, LongAutoID_CURIE));
		}
		int nb = pm.insert(discs).get();
		assertEquals(discs.size(), nb);
		
		SienaFuture<List<Discovery>> future = 
			pm.createQuery(Discovery.class).fetch();
		List<Discovery> res = future.get();
		
		assertEquals(discs.size(), res.size());
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs.get(i++), disc);
		}
		
		for(i=0; i<100; i++){
			((Discovery)discs.get(i)).name += "UPD";
			((Discovery)discs.get(i)).discoverer = LongAutoID_EINSTEIN;
		}
		
		nb = pm.save(discs).get();
		assertEquals(discs.size(), nb);
		future = 
			pm.createQuery(Discovery.class).fetch();
		res = future.get();
		i=0;
		for(Discovery disc:res){
			assertEquals(discs.get(i++), disc);
		}
	}
	
	public void testPolymorphic() {
		PolymorphicModel<String> poly = new PolymorphicModel<String>("test");
		pm.insert(poly).get();
		
		PolymorphicModel<String> poly2 = pm.getByKey(PolymorphicModel.class, poly.id).get();
		assertEquals(poly, poly2);
	}
	
	public void testPolymorphic2() {
		List<String> arr = new ArrayList<String>();
		arr.add("alpha");
		arr.add("beta");
		PolymorphicModel<List<String>> poly = new PolymorphicModel<List<String>>(arr);
		pm.insert(poly).get();
		
		PolymorphicModel<List<String>> poly2 = pm.getByKey(PolymorphicModel.class, poly.id).get();
		assertEquals(poly, poly2);
	}
	
	public void testEmbeddedModel() {
		EmbeddedModel embed = new EmbeddedModel();
		embed.id = "embed";
		embed.alpha = "test";
		embed.beta = 123;
		pm.insert(embed).get();
		
		ContainerModel container = new ContainerModel();
		container.id = "container";
		container.embed = embed;
		pm.insert(container).get();

		ContainerModel afterContainer = pm.getByKey(ContainerModel.class, container.id).get();
		assertNotNull(afterContainer);
		assertEquals(container.id, afterContainer.id);
		assertNotNull(afterContainer.embed);
		assertEquals(embed.id, afterContainer.embed.id);
		assertEquals(embed.alpha, afterContainer.embed.alpha);
		assertEquals(embed.beta, afterContainer.embed.beta);
	}

	public void testNoColumn() {
		DiscoveryNoColumn radioactivity = new DiscoveryNoColumn("Radioactivity", LongAutoID_CURIE, LongAutoID_TESLA);
		DiscoveryNoColumn relativity = new DiscoveryNoColumn("Relativity", LongAutoID_EINSTEIN, LongAutoID_TESLA);
		DiscoveryNoColumn foo = new DiscoveryNoColumn("Foo", LongAutoID_EINSTEIN, LongAutoID_EINSTEIN);
		DiscoveryNoColumn teslaCoil = new DiscoveryNoColumn("Tesla Coil", LongAutoID_TESLA, LongAutoID_CURIE);
		
		pm.insert(radioactivity, relativity, foo, teslaCoil).get();
		
		List<DiscoveryNoColumn> res = pm.createQuery(DiscoveryNoColumn.class).fetch().get();
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

	public void testNoColumnMultipleKeys() {
		if(!supportsMultipleKeys()) return;
		
		MultipleKeys mk1 = new MultipleKeys();
		mk1.id1 = "aid1";
		mk1.id2 = "aid2";
		mk1.name = "first";
		mk1.parent = null;
		pm.insert(mk1).get();

		MultipleKeys mk2 = new MultipleKeys();
		mk2.id1 = "bid1";
		mk2.id2 = "bid2";
		mk2.name = "second";
		mk2.parent = null;
		pm.insert(mk2).get();
		
		mk2.parent = mk1;
		pm.update(mk2).get();
		
		DiscoveryNoColumnMultipleKeys disc = new DiscoveryNoColumnMultipleKeys("disc1", mk1, mk2);
		pm.insert(disc);
		
		DiscoveryNoColumnMultipleKeys afterDisc = pm.getByKey(DiscoveryNoColumnMultipleKeys.class, disc.id).get();
		assertNotNull(afterDisc);
		assertEquals("disc1", afterDisc.name);
		assertEquals(mk1.id1, afterDisc.mk1.id1);
		assertEquals(mk1.id2, afterDisc.mk1.id2);
		assertEquals(mk2.id1, afterDisc.mk2.id1);
		assertEquals(mk2.id2, afterDisc.mk2.id2);
	}
}
