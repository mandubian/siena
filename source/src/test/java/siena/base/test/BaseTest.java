package siena.base.test;

import static siena.Json.map;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import siena.BaseQuery;
import siena.Json;
import siena.PersistenceManager;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSearch;
import siena.QueryFilterSimple;
import siena.QueryJoin;
import siena.QueryOrder;
import siena.SienaException;
import siena.base.test.model.Address;
import siena.base.test.model.AutoInc;
import siena.base.test.model.BigDecimalDoubleModel;
import siena.base.test.model.BigDecimalModel;
import siena.base.test.model.BigDecimalModelNoPrecision;
import siena.base.test.model.BigDecimalStringModel;
import siena.base.test.model.Contact;
import siena.base.test.model.EmbeddedContainerModel;
import siena.base.test.model.DataTypes;
import siena.base.test.model.DataTypes.EnumLong;
import siena.base.test.model.Discovery;
import siena.base.test.model.Discovery4Join;
import siena.base.test.model.Discovery4Join2;
import siena.base.test.model.Discovery4Search;
import siena.base.test.model.Discovery4Search2;
import siena.base.test.model.DiscoveryLifeCycle;
import siena.base.test.model.DiscoveryLifeCycleMulti;
import siena.base.test.model.DiscoveryNoColumn;
import siena.base.test.model.DiscoveryNoColumnMultipleKeys;
import siena.base.test.model.DiscoveryPrivate;
import siena.base.test.model.EmbeddedModel;
import siena.base.test.model.EmbeddedSubModel;
import siena.base.test.model.MultipleKeys;
import siena.base.test.model.PersonLongAutoID;
import siena.base.test.model.PersonLongManualID;
import siena.base.test.model.PersonStringAutoIncID;
import siena.base.test.model.PersonStringID;
import siena.base.test.model.PersonUUID;
import siena.base.test.model.PolymorphicModel;
import siena.base.test.model.TextModel;
import siena.base.test.model.TransactionAccountFrom;
import siena.base.test.model.TransactionAccountTo;
import siena.core.PersistenceManagerLifeCycleWrapper;
import siena.core.lifecycle.LifeCyclePhase;
import siena.core.options.QueryOption;
import siena.core.options.QueryOptionPage;
import siena.embed.JsonSerializer;

public abstract class BaseTest extends TestCase {
	
	protected PersistenceManager pm;

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

	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	
	public abstract boolean supportsAutoincrement();
	
	public abstract boolean supportsMultipleKeys();
	
	public abstract boolean mustFilterToOrder();
	
	public static String lifeCyclePhase = "";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(PersonUUID.class);
		classes.add(PersonLongAutoID.class);
		classes.add(PersonLongManualID.class);
		classes.add(PersonStringID.class);
		classes.add(PersonStringAutoIncID.class);
		if(supportsAutoincrement())
			classes.add(AutoInc.class);
		if(supportsMultipleKeys())
			classes.add(MultipleKeys.class);
		classes.add(Discovery.class);
		classes.add(Discovery4Join.class);
		classes.add(Discovery4Join2.class);
		classes.add(DiscoveryPrivate.class);
		classes.add(Discovery4Search.class);
		classes.add(Discovery4Search2.class);
		classes.add(DataTypes.class);
		classes.add(PolymorphicModel.class);
		classes.add(EmbeddedModel.class);
		classes.add(EmbeddedSubModel.class);
		classes.add(EmbeddedContainerModel.class);
		classes.add(DiscoveryNoColumn.class);
		classes.add(DiscoveryNoColumnMultipleKeys.class);
		classes.add(DiscoveryLifeCycle.class);
		classes.add(DiscoveryLifeCycleMulti.class);
		classes.add(BigDecimalModel.class);
		classes.add(BigDecimalModelNoPrecision.class);
		classes.add(BigDecimalStringModel.class);
		classes.add(BigDecimalDoubleModel.class);
		classes.add(TransactionAccountFrom.class);
		classes.add(TransactionAccountTo.class);
        classes.add(TextModel.class);

		pm = createPersistenceManager(classes);
		
		for (Class<?> clazz : classes) {
			if(!Modifier.isAbstract(clazz.getModifiers())){
				pm.createQuery(clazz).delete();			
			}
		}

		pm.insert(UUID_TESLA, UUID_CURIE, UUID_EINSTEIN);
		pm.insert(LongAutoID_TESLA, LongAutoID_CURIE, LongAutoID_EINSTEIN);
		pm.insert(LongManualID_TESLA, LongManualID_CURIE, LongManualID_EINSTEIN);
		pm.insert(StringID_TESLA, StringID_CURIE, StringID_EINSTEIN);
	}
	

	protected List<PersonUUID> getOrderedPersonUUIDs() {
		@SuppressWarnings("serial")
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
	protected Query<PersonUUID> queryPersonUUIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonUUID> query = pm.createQuery(PersonUUID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}

	protected Query<PersonLongAutoID> queryPersonLongAutoIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class);
		if(mustFilterToOrder()) {
			query = query.filter(order+">", value);
		}
		return query.order(desc ? "-"+order : order);
	}
	
	protected Query<PersonLongManualID> queryPersonLongManualIDOrderBy(String order, Object value, boolean desc) {
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
	
	public void testFilterOperatorEqualStringID() {
		PersonStringID person = pm.createQuery(PersonStringID.class).filter("id", "EINSTEIN").get();
		assertNotNull(person);
		assertEquals(StringID_EINSTEIN, person);
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
		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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
				.order("id")
				.fetch();

		assertNotNull(people);
		assertEquals(2, people.size());

		assertEquals(l.get(0), people.get(0));
		assertEquals(l.get(1), people.get(1));
	}
	
	public void testFilterOperatorInForLongAutoID() {
		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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
		@SuppressWarnings("serial")
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

	public void testCountFilterNotEqual() {
		assertEquals(2, pm.createQuery(PersonUUID.class).filter("n!=", 3).count());
	}

	public void testCountFilterIn() {
		assertEquals(2, pm.createQuery(PersonUUID.class).filter("n IN", Arrays.asList(1, 2)).count());
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

	public void testFetchLimitReal() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}

		List<Discovery> res = pm.createQuery(Discovery.class).order("name").fetch(3);
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
			pm.insert(discs[i]);
		}

		List<Discovery> res = pm.createQuery(Discovery.class).order("name").fetch(3, 5);
		assertNotNull(res);
		assertEquals(3, res.size());
		
		assertEquals(discs[5], res.get(0));
		assertEquals(discs[6], res.get(1));
		assertEquals(discs[7], res.get(2));
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

	public void testGetNonExisting() {
		try {
		PersonLongAutoID pers = getPersonLongAutoID(1234567L);
		}catch(SienaException e){
			return;
		}
		
		fail();
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

		@SuppressWarnings("serial")
		ArrayList<PersonUUID> l = new ArrayList<PersonUUID>() {{ 
			add(UUID_TESLA); 
			add(UUID_CURIE);
			add(UUID_EINSTEIN);
		}};
		
		int i = 0;
		for (PersonUUID person : people) {
			PersonUUID p = l.get(i);
			assertEquals(p, person);
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
		
		dataTypes.shortShort = Short.MAX_VALUE;
		dataTypes.intInt = Integer.MAX_VALUE;
		dataTypes.longLong = Long.MAX_VALUE;
		dataTypes.boolBool = Boolean.TRUE;
		
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
	
	protected void assertEqualsDataTypes(DataTypes dataTypes, DataTypes same) {
		assertEquals(dataTypes.id, same.id);
		assertEquals(dataTypes.typeByte, same.typeByte);
		assertEquals(dataTypes.typeShort, same.typeShort);
		assertEquals(dataTypes.typeInt, same.typeInt);
		assertEquals(dataTypes.typeLong, same.typeLong);
		assertEquals(dataTypes.typeFloat, same.typeFloat);
		assertEquals(dataTypes.typeDouble, same.typeDouble);
		
		assertEquals(dataTypes.boolBool, same.boolBool);
		assertEquals(dataTypes.shortShort, same.shortShort);
		assertEquals(dataTypes.longLong, same.longLong);
		assertEquals(dataTypes.intInt, same.intInt);
		
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
	
/*	public void testFetchPaginate() {
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
	
	public void testFetchPaginateReuse() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}
		
		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name").stateful();
		try {
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
		}catch(Exception ex){
			fail(ex.getMessage());
		}
		finally{
			query.release();
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
	
	public void testFetchKeysPaginateReuse() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name").stateful();
		try {
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
		}finally {
			query.release();
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
		try {
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
		}finally {
			query.release();
		}
	}
	

	public void testSearchSingle() {
		Discovery4Search[] discs = new Discovery4Search[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}
		
		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		List<Discovery4Search> res = query.fetch();
		
		assertEquals(1, res.size());
		assertEquals(discs[5], res.get(0));
	}

	public void testBatchInsert() {
		Object[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert(discs);
		
		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.length, res.size());
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs[i++], disc);
		}
	}
	
	public void testBatchInsertList() {
		List<Discovery> discs = new ArrayList<Discovery>();
		for(int i=0; i<10; i++){
			discs.add(new Discovery("Disc_"+i, LongAutoID_CURIE));
		}
		pm.insert(discs);
		
		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.size(), res.size());
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs.get(i++), disc);
		}
	}
	
	public void testBatchDelete() {
		Object[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
			pm.insert(discs[i]);
		}
		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.length, res.size());
		
		pm.delete(discs);
		
		res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(0, res.size());
	}
	
	public void testBatchDeleteList() {
		List<Discovery> discs = new ArrayList<Discovery>();
		for(int i=0; i<10; i++){
			Discovery disc = new Discovery("Disc_"+i, LongAutoID_CURIE);
			discs.add(disc);
			pm.insert(disc);
		}
		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.size(), res.size());
		
		pm.delete(discs);
		
		res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(0, res.size());
	}
	
	
	public void testBatchDeleteByKeys() {
		pm.deleteByKeys(PersonStringID.class, "TESLA", "CURIE");
		
		List<PersonStringID> res = 
			pm.createQuery(PersonStringID.class).fetch();
		
		assertEquals(1, res.size());
		assertEquals(StringID_EINSTEIN, res.get(0));
	}
	
	public void testBatchDeleteByKeysList() {
		pm.deleteByKeys(PersonStringID.class, new ArrayList<String>(){{add("TESLA"); add( "CURIE");}});
		
		List<PersonStringID> res = 
			pm.createQuery(PersonStringID.class).fetch();
		
		assertEquals(1, res.size());
		assertEquals(StringID_EINSTEIN, res.get(0));
	}
	*/
	
	
	public void testFetchPaginateStatelessNextPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		res = query.nextPage().fetch();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());

		res = query.nextPage().fetch();
		assertEquals(0, res.size());

		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatelessPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.previousPage().fetch();
		assertEquals(0, res.size());

		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(0, res.size());
		
		res = query.previousPage().fetch();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatelessPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateStatelessSeveralTimes() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.previousPage().fetch();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatefulNextPageToEnd() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name").stateful();
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());

		res = query.nextPage().fetch();
		assertEquals(0, res.size());

		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());
	}	

	public void testFetchPaginateStatefulPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		List<Discovery> res = query.previousPage().fetch();
		assertEquals(0, res.size());

		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(0, res.size());
		
		res = query.previousPage().fetch();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatefulPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}

		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.previousPage().fetch();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.previousPage().fetch();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		res = query.nextPage().fetchKeys();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.previousPage().fetchKeys();
		assertEquals(0, res.size());

		res = query.previousPage().fetchKeys();
		assertEquals(0, res.size());
	}
	
	public void testFetchKeysPaginateStatelessPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		List<Discovery> res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		List<Discovery> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}		
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		List<Discovery> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		res = query.nextPage().fetchKeys();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		List<Discovery> res = query.previousPage().fetchKeys();
		assertEquals(0, res.size());

		res = query.previousPage().fetchKeys();
		assertEquals(0, res.size());
	}
	
	public void testFetchKeysPaginateStatefulPreviousPage() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		List<Discovery> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatefulSeveralTimes() {
		Discovery[] discs = new Discovery[15];
		for(int i=0; i<15; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		List<Discovery> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
		}		
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10].id, res.get(i).id);
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
		}
		
		res = query.previousPage().fetchKeys();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		try {
			Iterable<Discovery> res = query.iter();
			Iterator<Discovery> it = res.iterator();
			int i=0;
			while(it.hasNext()){
				assertEquals(discs[i++], it.next());
			}
			assertEquals(5, i);

			res = query.nextPage().iter();
			it = res.iterator();
			while(it.hasNext()){
				assertEquals(discs[i++], it.next());
			}
			assertEquals(10, i);
		}finally {
			query.release();
		}
	}

	public void testIterPaginateStatelessPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		Iterable<Discovery> iter = query.previousPage().iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(0, i);

		iter = query.previousPage().iter();
		i=0;
		it = iter.iterator();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("name");
		Iterable<Discovery> iter = query.nextPage().iter();
		Iterator<Discovery> it = iter.iterator();
		int i=5;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(10, i);

		iter = query.previousPage().iter();
		it = iter.iterator();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		Iterable<Discovery> iter = query.iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(5, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(10, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(15, i);
	
		iter = query.previousPage().iter();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(10, i);

		iter = query.previousPage().iter();
		it = iter.iterator();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		Iterable<Discovery> iter = query.iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		
		assertEquals(10, i);
	}
	

	
	public void testIterPaginateStatefulPreviousPageFromScratch() {
		Discovery[] discs = new Discovery[10];
		for(int i=0; i<10; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		Iterable<Discovery> iter = query.previousPage().iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(0, i);

		it = iter.iterator();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("name");
		Iterable<Discovery> iter = query.iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);

		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		iter = query.previousPage().iter();
		it = iter.iterator();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).stateful().order("id");
		Iterable<Discovery> iter = query.iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);	
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);	
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);	
		
		iter = query.previousPage().iter();
		i=5;
		it = iter.iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);	
		
		iter = query.previousPage().iter();
		i=0;
		it = iter.iterator();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		Iterable<Discovery> iter = query.iter();
		Iterator<Discovery> it = iter.iterator();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.iter();
		Iterator<Discovery> it = iter.iterator();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> res = query.iter(50);
		Iterator<Discovery> it = res.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		res = query.iter(50,50);
		it = res.iterator();
		i=100;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	

		res = query.iter(50,100);
		it = res.iterator();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.paginate(50).iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		iter = query.iter(50,50);
		it = iter.iterator();
		i=50;
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.iter(50);
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
				
		iter = query.paginate(50).iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	
	
		iter = query.iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	

		iter = query.nextPage().iter();
		it = iter.iterator();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.paginate(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		res = query.fetch(50);
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.paginate(50).fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}

		res = query.fetch(50);
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		Iterable<Discovery> res2 = query.iter(50);
		Iterator<Discovery> it2 = res2.iterator();
		int i=50;
		while(it2.hasNext()){
			Discovery disc = it2.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	

		res = query.paginate(25).fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		res2 = query.nextPage().iter();
		it2 = res2.iterator();
		i=125;
		while(it2.hasNext()){
			Discovery disc = it2.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
		
		res = query.previousPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+125], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());
		
		res = query.previousPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+125], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(25, res.size());
		for(i=0; i<25; i++){
			assertEquals(discs[i+25], res.get(i));
		}
		
		res = query.previousPage().fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		Iterable<Discovery> res2 = query.iter(50);
		Iterator<Discovery> it2 = res2.iterator();
		int i=50;
		while(it2.hasNext()){
			Discovery disc = it2.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	
		
		res = query.fetch(50);
		assertEquals(50, res.size());
		for(i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		res2 = query.iter(50);
		it2 = res2.iterator();
		i=150;
		while(it2.hasNext()){
			Discovery disc = it2.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(200, i);	

	}
	
	public void testFetchIterLotsOfEntitiesStatefulMixed3(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		Iterable<Discovery> res2 = query.iter();
		Iterator<Discovery> it2 = res2.iterator();
		int i=50;
		while(it2.hasNext()){
			Discovery disc = it2.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
	
	public void testSearchSingle() {
		Discovery4Search[] discs = new Discovery4Search[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		List<Discovery4Search> res = query.fetch();
				
		assertEquals(1, res.size());
		assertEquals(discs[5], res.get(0));
	}
	
	public void testSearchSingleKeysOnly() {
		Discovery4Search[] discs = new Discovery4Search[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		List<Discovery4Search> res = query.fetchKeys();
				
		assertEquals(1, res.size());
		assertEquals(discs[5].id, res.get(0).id);
		assertTrue(res.get(0).isOnlyIdFilled());
	}
	
	public void testSearchSingleTwice() {
		Discovery4Search[] discs = new Discovery4Search[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		List<Discovery4Search> res = query.fetch();
				
		assertEquals(1, res.size());
		assertEquals(discs[5], res.get(0));

		query = 
			pm.createQuery(Discovery4Search.class).search("Disc_48", "name");
		
		res = query.fetch();
				
		assertEquals(1, res.size());
		assertEquals(discs[48], res.get(0));

	}

	public void testSearchSingleCount() {
		Discovery4Search[] discs = new Discovery4Search[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery4Search> query = 
			pm.createQuery(Discovery4Search.class).search("Disc_5", "name");
		
		int res = query.count();
				
		assertEquals(1, res);
	}
	
	public void testBatchInsert() {
		Object[] discs = new Discovery[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		int nb = pm.insert(discs);
		assertEquals(discs.length, nb);
		
		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
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
		int nb = pm.insert(discs);
		assertEquals(discs.size(), nb);
		
		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
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
		pm.insert(discs);

		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.length, res.size());
		
		int nb = pm.delete(discs);
		assertEquals(discs.length, nb);

		res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(0, res.size());
	}
	
	public void testBatchDeleteList() {
		List<Discovery> discs = new ArrayList<Discovery>();
		for(int i=0; i<100; i++){
			Discovery disc = new Discovery("Disc_"+i, LongAutoID_CURIE);
			discs.add(disc);
		}
		pm.insert(discs);

		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.size(), res.size());
		
		int nb = pm.delete(discs);
		assertEquals(discs.size(), nb);
		
		res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(0, res.size());
	}
	
	
	public void testBatchDeleteByKeys() {
		int nb = pm.deleteByKeys(PersonStringID.class, "TESLA", "CURIE");
		assertEquals(2, nb);
		
		List<PersonStringID> res = 
			pm.createQuery(PersonStringID.class).fetch();
		
		assertEquals(1, res.size());
		assertEquals(StringID_EINSTEIN, res.get(0));
	}
	
	public void testBatchDeleteByKeysList() {
		int nb = pm.deleteByKeys(PersonStringID.class, new ArrayList<String>(){
			private static final long serialVersionUID = 1L;
			{add("TESLA"); add( "CURIE");}
		});
		assertEquals(2, nb);
		
		List<PersonStringID> res = 
			pm.createQuery(PersonStringID.class).fetch();
		
		assertEquals(1, res.size());
		assertEquals(StringID_EINSTEIN, res.get(0));
	}
	
	public void testBatchGet() {
		Discovery[] discs = new Discovery[100];
		
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		Discovery[] discs2Get = new Discovery[100];
		for(int i=0; i<100; i++){
			discs2Get[i] = new Discovery();
			discs2Get[i].id = discs[i].id;
		}
		
		int nb = pm.get((Object[])discs2Get);
		assertEquals(discs.length, nb);
		
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
		pm.insert((Object[])discs);

		List<Discovery> discs2Get = new ArrayList<Discovery>();
		for(int i=0; i<100; i++){
			Discovery disc = new Discovery();
			disc.id = discs[i].id;
			discs2Get.add(disc);
		}
		
		int nb = pm.get(discs2Get);
		assertEquals(nb, discs.length);
		int i=0;
		for(Discovery disc:discs2Get){
			assertEquals(discs[i++], disc);
		}		
	}
	
	public void testBatchGetByKeys() {
		List<PersonStringID> res = pm.getByKeys(PersonStringID.class, "TESLA", "CURIE");
		
		assertEquals(2, res.size());
		assertEquals(StringID_TESLA, res.get(0));
		assertEquals(StringID_CURIE, res.get(1));
	}
	
	public void testBatchGetByKeysList() {
		Discovery[] discs = new Discovery[100];
		
		for(int i=0; i<100; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);

		List<Long> discsKeys = new ArrayList<Long>();
		for(int i=0; i<100; i++){
			discsKeys.add(discs[i].id);
		}
		
		List<Discovery> discs2Get = pm.getByKeys(Discovery.class, discsKeys);
		assertEquals(discs.length, discs2Get.size());
		int i=0;
		for(Discovery disc:discs2Get){
			assertEquals(discs[i++], disc);
		}		
	}
	
	public void testBatchGetByKeysNonExisting() {
		List<PersonStringID> res = pm.getByKeys(PersonStringID.class, "TESLA", "CURIE", "CHBOING");
		
		assertEquals(3, res.size());
		assertEquals(StringID_TESLA, res.get(0));
		assertEquals(StringID_CURIE, res.get(1));
		assertNull(res.get(2));
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

	private PersonUUID getByKeyPersonUUID(String id) {
		return pm.getByKey(PersonUUID.class, id);
	}

	private PersonLongAutoID getByKeyPersonLongAutoID(Long id) {
		return pm.getByKey(PersonLongAutoID.class, id);
	}
	
	private PersonLongManualID getByKeyPersonLongManualID(Long id) {
		return pm.getByKey(PersonLongManualID.class, id);
	}

	private PersonStringID getByKeyPersonStringID(String id) {
		return pm.getByKey(PersonStringID.class, id);
	}
	
	public void testLimitStateless(){
		Discovery[] discs = new Discovery[150];
		for(int i=0; i<150; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateless().order("id");
		List<Discovery> res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.paginate(50).fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		res = query.paginate(50).fetch(25);
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		List<Discovery> res = query.offset(50).fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.offset(50).fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		List<Discovery> res = query.offset(50).limit(50).fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.offset(50).limit(50).fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		List<Discovery> res = query.paginate(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		res = query.offset(50).fetch();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		res = query.offset(50).limit(50).fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id").stateful();
		List<Discovery> res = query.paginate(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		res = query.offset(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+150], res.get(i));
		}
		
		res = query.offset(50).limit(25).fetch();
		assertEquals(25, res.size());
		for(int i=0; i<25; i++){
			assertEquals(discs[i+250], res.get(i));
		}
		try {
			res = query.previousPage().fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		List<Discovery> res = query.limit(50).offset(12).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		res = query.offset(13).limit(30).fetch();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+13], res.get(i));
		}
		
		res = query.offset(10).limit(30).fetch(15);
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		res = query.paginate(6).fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6], res.get(i));
		}
		
		res = query.offset(10).fetch(10);
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		try {
			res = query.nextPage().fetch();
		}catch(SienaException ex){
			res = query.paginate(8).fetch();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i], res.get(i));
			}
			
			res = query.nextPage().fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id").stateful();
		List<Discovery> res = query.limit(50).offset(12).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		res = query.offset(13).limit(30).fetch();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		res = query.offset(10).limit(30).fetch(15);
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+115], res.get(i));
		}
		
		res = query.paginate(6).fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+130], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+142], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136], res.get(i));
		}
		
		res = query.offset(10).fetch(10);
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+146], res.get(i));
		}
		
		try {
			res = query.nextPage().fetch();
		}catch(SienaException ex){
			res = query.paginate(8).fetch();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+156], res.get(i));
			}
			
			res = query.nextPage().fetch();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.paginate(8).fetch();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().paginate(5).order("id");
		List<Discovery> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.paginate(8).fetch();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateless().order("id");
		List<Discovery> res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.fetchKeys(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(50).fetchKeys();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.fetchKeys(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(50).fetchKeys(25);
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		List<Discovery> res = query.offset(50).fetchKeys();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.offset(50).fetchKeys();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		List<Discovery> res = query.offset(50).limit(50).fetchKeys();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		List<Discovery> res = query.offset(50).limit(50).fetchKeys();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		List<Discovery> res = query.paginate(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.offset(50).fetchKeys();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(50).limit(50).fetchKeys();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id").stateful();
		List<Discovery> res = query.paginate(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.offset(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+150].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(50).limit(25).fetchKeys();
		assertEquals(25, res.size());
		for(int i=0; i<25; i++){
			assertEquals(discs[i+250].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		try {
			res = query.previousPage().fetch();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		List<Discovery> res = query.limit(50).offset(12).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(13).limit(30).fetchKeys();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+13].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(10).limit(30).fetchKeys(15);
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+10].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(6).fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+12].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(10).fetchKeys(10);
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+10].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		try {
			res = query.nextPage().fetchKeys();
		}catch(SienaException ex){
			res = query.paginate(8).fetch();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i].id, res.get(i).id);
			}
			
			res = query.nextPage().fetchKeys();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id").stateful();
		List<Discovery> res = query.limit(50).offset(12).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(13).limit(30).fetchKeys();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+75].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(10).limit(30).fetchKeys(15);
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+115].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(6).fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+130].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+142].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(10).fetchKeys(10);
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+146].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		try {
			res = query.nextPage().fetchKeys();
		}catch(SienaException ex){
			res = query.paginate(8).fetchKeys();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+156].id, res.get(i).id);
				assertTrue(res.get(i).isOnlyIdFilled());
			}
			
			res = query.nextPage().fetchKeys();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		List<Discovery> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}		
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(8).fetchKeys();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().paginate(5).order("id");
		List<Discovery> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}		
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(8).fetchKeys();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i+5].id, res.get(i).id);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateless().order("id");
		Iterable<Discovery> iter = query.limit(50).iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
		
		iter = query.limit(50).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
		
		iter = query.iter(50);
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		iter = query.paginate(50).iter();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.limit(50).iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
		
		iter = query.iter(50);
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);
		
		iter = query.paginate(50).iter(25);
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		Iterable<Discovery> iter = query.offset(50).iter();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.offset(50).iter();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		Iterable<Discovery> iter = query.offset(50).limit(50).iter();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.offset(50).limit(50).iter();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		Iterable<Discovery> iter = query.paginate(50).iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		iter = query.limit(50).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);

		iter = query.offset(50).iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
		
		iter = query.offset(50).limit(50).iter();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.paginate(50).iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		iter = query.limit(50).iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		iter = query.offset(50).iter();
		it = iter.iterator();
		i=150;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(200, i);
		
		iter = query.offset(50).limit(25).iter();
		it = iter.iterator();
		i=250;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(275, i);

		try {
			iter = query.previousPage().iter();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		Iterable<Discovery> iter = query.limit(50).offset(12).iter();
		Iterator<Discovery> it = iter.iterator();
		int i=12;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(62, i);
		
		iter = query.offset(13).limit(30).iter();
		it = iter.iterator();
		i=13;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(43, i);
		
		iter = query.offset(10).limit(30).iter(15);
		it = iter.iterator();
		i=10;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(25, i);

		iter = query.paginate(6).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(6, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=6;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(12, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=12;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(18, i);
		
		iter = query.previousPage().iter();
		it = iter.iterator();
		i=6;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(12, i);
		
		iter = query.offset(10).iter(10);
		it = iter.iterator();
		i=10;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(20, i);
		
		try {
			iter = query.nextPage().iter();
		}catch(SienaException ex){
			iter = query.paginate(8).iter();
			it = iter.iterator();
			i=0;
			while(it.hasNext()){
				Discovery disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(8, i);
			
			iter = query.nextPage().iter();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.limit(50).offset(12).iter();
		Iterator<Discovery> it = iter.iterator();
		int i=12;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(62, i);
		
		iter = query.offset(13).limit(30).iter();
		it = iter.iterator();
		i=75;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(105, i);
		
		iter = query.offset(10).limit(30).iter(15);
		it = iter.iterator();
		i=115;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(130, i);

		iter = query.paginate(6).iter();
		it = iter.iterator();
		i=130;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(136, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=136;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(142, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=142;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(148, i);
		
		iter = query.previousPage().iter();
		it = iter.iterator();
		i=136;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(142, i);
		
		iter = query.offset(10).iter(10);
		it = iter.iterator();
		i=146;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(156, i);
		
		try {
			iter = query.nextPage().iter();
		}catch(SienaException ex){
			iter = query.paginate(8).iter();
			it = iter.iterator();
			i=156;
			while(it.hasNext()){
				Discovery disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(164, i);
			
			iter = query.nextPage().iter();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).paginate(5).order("id");
		Iterable<Discovery> iter = query.iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		iter = query.paginate(8).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(8, i);
		
		iter = query.nextPage().iter();
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
		pm.insert((Object[])discs);

		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().paginate(5).order("id");
		Iterable<Discovery> iter = query.iter();
		Iterator<Discovery> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		iter = query.paginate(8).iter();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(13, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=13;
		while(it.hasNext()){
			Discovery disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);
	}
	
	public void testFetchStringAutoInc() {
		PersonStringAutoIncID person = new PersonStringAutoIncID("TEST1", "TEST2", "TEST3", "TEST4", 123);
		
		pm.insert(person);
		
		List<PersonStringAutoIncID> l = pm.getByKeys(PersonStringAutoIncID.class, "TEST1");
		assertEquals(person, l.get(0));
	}
	
	public void testDumpQueryOption() {
		Query<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class);
		
		QueryOption opt = query.option(QueryOptionPage.ID);
		Json dump = opt.dump();
		String str = JsonSerializer.serialize(dump).toString();
		assertNotNull(str);
		assertEquals("{\"value\": {\"pageType\": \"TEMPORARY\", \"state\": \"PASSIVE\", \"pageSize\": 0, \"type\": 1}, \"type\": \""+QueryOptionPage.class.getName()+"\"}", str);
	}
	
	public void testRestoreQueryOption() {
		QueryOption optRestored = (QueryOption)JsonSerializer.deserialize(QueryOption.class, Json.loads(
			"{\"type\":\""+QueryOptionPage.class.getName()+"\", \"value\": {\"pageType\": \"TEMPORARY\", \"state\": \"PASSIVE\", \"pageSize\": 0, \"type\": 1} }"
		));
		Query<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class);
		
		QueryOption opt = query.option(QueryOptionPage.ID);
		
		assertEquals(opt, optRestored);
	}
	
	public void testDumpRestoreQueryFilterSimple() {
		Query<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class).filter("firstName", "abcde");
		QueryFilterSimple qf = (QueryFilterSimple)query.getFilters().get(0);
		String str = JsonSerializer.serialize(qf).toString();
		assertNotNull(str);
		
		QueryFilterSimple qfRes = (QueryFilterSimple)JsonSerializer.deserialize(QueryFilter.class, Json.loads(str));
		assertNotNull(qfRes);
		assertEquals(qf.operator, qfRes.operator);
		assertEquals(qf.value, qfRes.value);
		assertEquals(qf.field.getName(), qfRes.field.getName());
	}
	
	public void testDumpRestoreQueryFilterSearch() {
		Query<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class).search("test", "firstName", "lastName");
		QueryFilterSearch qf = (QueryFilterSearch)query.getFilters().get(0);
		String str = JsonSerializer.serialize(qf).toString();
		assertNotNull(str);
		
		QueryFilterSearch qfRes = (QueryFilterSearch)JsonSerializer.deserialize(QueryFilter.class, Json.loads(str));
		assertNotNull(qfRes);
		assertEquals(qf.match, qfRes.match);
		for(int i=0; i<qfRes.fields.length; i++){
			assertEquals(qf.fields[i], qfRes.fields[i]);
		}
	}
	
	public void testDumpRestoreQueryOrder() {
		Query<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class).order("firstName");
		QueryOrder qo = (QueryOrder)query.getOrders().get(0);
		String str = JsonSerializer.serialize(qo).toString();
		assertNotNull(str);
		
		QueryOrder qoRes = (QueryOrder)JsonSerializer.deserialize(QueryOrder.class, Json.loads(str));
		assertNotNull(qoRes);
		assertEquals(qo.ascending, qoRes.ascending);
		assertEquals(qo.field.getName(), qoRes.field.getName());
	}
	
	public void testDumpRestoreQueryJoin() {
		Query<Discovery> query = pm.createQuery(Discovery.class).join("discoverer", "firstName");
		QueryJoin qj = (QueryJoin)query.getJoins().get(0);
		String str = JsonSerializer.serialize(qj).toString();
		assertNotNull(str);
		
		QueryJoin qjRes = (QueryJoin)JsonSerializer.deserialize(QueryJoin.class, Json.loads(str));
		assertNotNull(qjRes);
		assertEquals(qj.field.getName(), qjRes.field.getName());
		for(int i=0; i<qjRes.sortFields.length; i++){
			assertEquals(qj.sortFields[i], qjRes.sortFields[i]);
		}
	}
	
	public void testDumpRestoreQueryData() {
		Query<Discovery> query = 
			pm.createQuery(Discovery.class)
				.filter("name", "test").order("name").join("discoverer", "firstName");
		String str = JsonSerializer.serialize(query).toString();
		assertNotNull(str);
		
		Query<Discovery> qjRes = (Query<Discovery>)JsonSerializer.deserialize(BaseQuery.class, Json.loads(str));
		assertNotNull(qjRes);
		for(int i=0; i<qjRes.getFilters().size(); i++){
			assertEquals(query.getFilters().get(i), qjRes.getFilters().get(i));
		}
		for(int i=0; i<qjRes.getJoins().size(); i++){
			assertEquals(query.getJoins().get(i), qjRes.getJoins().get(i));
		}
		for(int i=0; i<qjRes.getOrders().size(); i++){
			assertEquals(query.getOrders().get(i), qjRes.getOrders().get(i));
		}
		for(int i=0; i<qjRes.getSearches().size(); i++){
			assertEquals(query.getSearches().get(i), qjRes.getSearches().get(i));
		}
	}
	
	public void testIterPerPageStateless(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		Iterable<Discovery> iter = query.iterPerPage(50);
		int i=0;
		for(Discovery disc: iter){
			assertEquals(discs[i++], disc);
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateless2(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		Iterable<Discovery> iter = query.iterPerPage(50);
		Iterator<Discovery> it = iter.iterator();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).order("id");
		Iterable<Discovery> iter = query.offset(25).iterPerPage(50);
		Iterator<Discovery> it = iter.iterator();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.iterPerPage(50);
		int i=0;
		for(Discovery disc: iter){
			assertEquals(discs[i++], disc);
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateful2(){
		Discovery[] discs = new Discovery[500];
		for(int i=0; i<500; i++){
			discs[i] = new Discovery("Disc_"+i, LongAutoID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.iterPerPage(50);
		Iterator<Discovery> it = iter.iterator();
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
		pm.insert((Object[])discs);
		
		Query<Discovery> query = pm.createQuery(Discovery.class).stateful().order("id");
		Iterable<Discovery> iter = query.offset(25).iterPerPage(50);
		Iterator<Discovery> it = iter.iterator();
		int i=25;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
    public void testInsertObjectWithNullJoinObject() {
        Discovery4Join model = new Discovery4Join();
        model.discovererJoined = null; // explicitly set the join object to null

        pm.insert(model);

        Query<Discovery4Join> query = pm.createQuery(Discovery4Join.class).filter("id", model.id);
        Discovery4Join modelFromDatabase = pm.get(query);
        assertNull(modelFromDatabase.discovererJoined);
    }
    
    public void testInsertObjectWithDoubleNullJoinObject() {
        Discovery4Join2 model = new Discovery4Join2();
        model.discovererJoined = null; // explicitly set the join object to null
        model.discovererJoined2 = null; // explicitly set the join object to null

        pm.insert(model);

        Query<Discovery4Join2> query = pm.createQuery(Discovery4Join2.class).filter("id", model.id);
        Discovery4Join2 modelFromDatabase = pm.get(query);
        assertNull(modelFromDatabase.discovererJoined);
        assertNull(modelFromDatabase.discovererJoined2);
    }
    
	public void testJoinAnnotationDouble() {
		Discovery4Join2 radioactivity = new Discovery4Join2("Radioactivity", LongAutoID_CURIE, LongAutoID_TESLA);
		Discovery4Join2 relativity = new Discovery4Join2("Relativity", LongAutoID_EINSTEIN, LongAutoID_TESLA);
		Discovery4Join2 foo = new Discovery4Join2("Foo", LongAutoID_EINSTEIN, LongAutoID_EINSTEIN);
		Discovery4Join2 teslaCoil = new Discovery4Join2("Tesla Coil", LongAutoID_TESLA, LongAutoID_CURIE);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);
		
		List<Discovery4Join2> res = pm.createQuery(Discovery4Join2.class).fetch();
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
		pm.insert(discs);

		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.length, res.size());
		
		for(int i=0; i<100; i++){
			((Discovery)discs[i]).discoverer = LongAutoID_EINSTEIN;
		}
		
		int nb = pm.update(discs);
		assertEquals(discs.length, nb);
		res = 
			pm.createQuery(Discovery.class).fetch();
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
		int nb = pm.insert(discs);
		assertEquals(discs.size(), nb);

		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.size(), res.size());
		
		for(Discovery d: discs){
			d.discoverer = LongAutoID_EINSTEIN;
		}
		
		nb = pm.update(discs);
		assertEquals(discs.size(), nb);
		res = 
			pm.createQuery(Discovery.class).fetch();
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs.get(i++), disc);
		}
		
	}
	
	public void testGetByKeyNonExisting() {
		PersonLongAutoID pers = getByKeyPersonLongAutoID(12345678L);
		assertNull(pers);
	}
	
	public void testGetByKeyUUID() {
		PersonUUID curie = getByKeyPersonUUID(UUID_CURIE.id);
		assertEquals(UUID_CURIE, curie);
	}

	public void testGetByKeyLongAutoID() {
		PersonLongAutoID curie = getByKeyPersonLongAutoID(LongAutoID_CURIE.id);
		assertEquals(LongAutoID_CURIE, curie);
	}

	public void testGetByKeyLongManualID() {
		PersonLongManualID curie = getByKeyPersonLongManualID(LongManualID_CURIE.id);
		assertEquals(LongManualID_CURIE, curie);
	}

	public void testGetByKeyStringID() {
		PersonStringID curie = getByKeyPersonStringID(StringID_CURIE.id);
		assertEquals(StringID_CURIE, curie);
	}
	
	public void testSaveLongAutoID() {
		PersonLongAutoID maxwell = new PersonLongAutoID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell);
		assertNotNull(maxwell.id);

		List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(LongAutoID_TESLA, people.get(0));
		assertEquals(LongAutoID_CURIE, people.get(1));
		assertEquals(LongAutoID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.save(maxwell);
		assertNotNull(maxwell.id);
		
		people = queryPersonLongAutoIDOrderBy("n", 0, false).fetch();
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

		pm.save(maxwell);
		assertNotNull(maxwell.id);

		List<PersonUUID> people = queryPersonUUIDOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(UUID_TESLA, people.get(0));
		assertEquals(UUID_CURIE, people.get(1));
		assertEquals(UUID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.save(maxwell);
		assertNotNull(maxwell.id);
		
		people = queryPersonUUIDOrderBy("n", 0, false).fetch();
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

		pm.save(maxwell);
		assertEquals((Long)4L, maxwell.id);

		List<PersonLongManualID> people = queryPersonLongManualIDOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(LongManualID_TESLA, people.get(0));
		assertEquals(LongManualID_CURIE, people.get(1));
		assertEquals(LongManualID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.save(maxwell);
		assertEquals((Long)4L, maxwell.id);

		people = queryPersonLongManualIDOrderBy("n", 0, false).fetch();
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

		pm.save(maxwell);
		assertEquals(maxwell.id, "MAXWELL");

		List<PersonStringID> people = queryPersonStringIDOrderBy("n", 0, false).fetch();
		assertEquals(4, people.size());

		assertEquals(StringID_TESLA, people.get(0));
		assertEquals(StringID_CURIE, people.get(1));
		assertEquals(StringID_EINSTEIN, people.get(2));
		assertEquals(maxwell, people.get(3));

		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;
		
		pm.save(maxwell);
		assertEquals(maxwell.id, "MAXWELL");

		people = queryPersonStringIDOrderBy("n", 0, false).fetch();
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
		pm.save(discs);

		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.length, res.size());
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs[i++], disc);
		}
		
		for(i=0; i<100; i++){
			((Discovery)discs[i]).name += "UPD";
			((Discovery)discs[i]).discoverer = LongAutoID_EINSTEIN;
		}
		
		int nb = pm.save(discs);
		assertEquals(discs.length, nb);
		res = 
			pm.createQuery(Discovery.class).fetch();
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
		int nb = pm.insert(discs);
		assertEquals(discs.size(), nb);
		
		List<Discovery> res = 
			pm.createQuery(Discovery.class).fetch();
		
		assertEquals(discs.size(), res.size());
		int i=0;
		for(Discovery disc:res){
			assertEquals(discs.get(i++), disc);
		}
		
		for(i=0; i<100; i++){
			((Discovery)discs.get(i)).name += "UPD";
			((Discovery)discs.get(i)).discoverer = LongAutoID_EINSTEIN;
		}
		
		nb = pm.save(discs);
		assertEquals(discs.size(), nb);
		res = 
			pm.createQuery(Discovery.class).fetch();
		i=0;
		for(Discovery disc:res){
			assertEquals(discs.get(i++), disc);
		}
	}
	
	public void testPolymorphic() {
		PolymorphicModel<String> poly = new PolymorphicModel<String>("test");
		pm.insert(poly);
		
		PolymorphicModel poly2 = pm.getByKey(PolymorphicModel.class, poly.id);
		assertEquals(poly, poly2);
	}
	
	public void testPolymorphic2() {
		List<String> arr = new ArrayList<String>();
		arr.add("alpha");
		arr.add("beta");
		PolymorphicModel<List<String>> poly = new PolymorphicModel<List<String>>(arr);
		pm.insert(poly);
		
		PolymorphicModel<List<String>> poly2 = pm.getByKey(PolymorphicModel.class, poly.id);
		assertEquals(poly, poly2);
	}
	
	public void testEmbeddedModel() {
		EmbeddedModel embed = new EmbeddedModel();
		embed.id = "embed";
		embed.alpha = "test";
		embed.beta = 123;
		embed.setGamma(true);
		pm.insert(embed);
		
		EmbeddedModel embed2 = new EmbeddedModel();
		embed2.id = "embed2";
		embed2.alpha = "test2";
		embed2.beta = 1234;
		embed2.setGamma(true);
		pm.insert(embed2);
		
		EmbeddedContainerModel container = new EmbeddedContainerModel();
		container.id = "container";
		container.embed = embed;
		container.embeds = new ArrayList<EmbeddedModel>();
		container.embeds.add(embed);
		container.embeds.add(embed2);
		pm.insert(container);

		EmbeddedContainerModel afterContainer = pm.getByKey(EmbeddedContainerModel.class, container.id);
		assertNotNull(afterContainer);
		assertEquals(container.id, afterContainer.id);
		assertNotNull(afterContainer.embed);
		assertEquals(embed.id, afterContainer.embed.id);
		assertEquals(null, afterContainer.embed.alpha);
		assertEquals(embed.beta, afterContainer.embed.beta);
		int i=0;
		for(EmbeddedModel mod: afterContainer.embeds){
			assertEquals(container.embeds.get(i++).id, mod.id);
		}
		assertEquals(embed.isGamma(), afterContainer.embed.isGamma());
	}

	public void testNoColumn() {
		DiscoveryNoColumn radioactivity = new DiscoveryNoColumn("Radioactivity", LongAutoID_CURIE, LongAutoID_TESLA);
		DiscoveryNoColumn relativity = new DiscoveryNoColumn("Relativity", LongAutoID_EINSTEIN, LongAutoID_TESLA);
		DiscoveryNoColumn foo = new DiscoveryNoColumn("Foo", LongAutoID_EINSTEIN, LongAutoID_EINSTEIN);
		DiscoveryNoColumn teslaCoil = new DiscoveryNoColumn("Tesla Coil", LongAutoID_TESLA, LongAutoID_CURIE);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(foo);
		pm.insert(teslaCoil);
		
		List<DiscoveryNoColumn> res = pm.createQuery(DiscoveryNoColumn.class).fetch();
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
		pm.insert(mk1);

		MultipleKeys mk2 = new MultipleKeys();
		mk2.id1 = "bid1";
		mk2.id2 = "bid2";
		mk2.name = "second";
		mk2.parent = null;
		pm.insert(mk2);
		
		mk2.parent = mk1;
		pm.update(mk2);
		
		DiscoveryNoColumnMultipleKeys disc = new DiscoveryNoColumnMultipleKeys("disc1", mk1, mk2);
		pm.insert(disc);
		
		DiscoveryNoColumnMultipleKeys afterDisc = pm.getByKey(DiscoveryNoColumnMultipleKeys.class, disc.id);
		assertNotNull(afterDisc);
		assertEquals("disc1", afterDisc.name);
		assertEquals(mk1.id1, afterDisc.mk1.id1);
		assertEquals(mk1.id2, afterDisc.mk1.id2);
		assertEquals(mk2.id1, afterDisc.mk2.id1);
		assertEquals(mk2.id2, afterDisc.mk2.id2);
	}
	
	public void testLifeCycleGet(){
		PersistenceManagerLifeCycleWrapper pml = new PersistenceManagerLifeCycleWrapper(pm);
		
		DiscoveryLifeCycle before = new DiscoveryLifeCycle("Radioactivity", LongAutoID_CURIE);
		pm.insert(before);
		
		lifeCyclePhase = "";
		DiscoveryLifeCycle after = new DiscoveryLifeCycle();
		after.id = before.id;
		pml.get(after);
		
		assertEquals(LifeCyclePhase.PRE_FETCH.toString()+" "+LifeCyclePhase.POST_FETCH.toString()+" ", lifeCyclePhase);
	}
	
	public void testLifeCycleGetMultiAndLifeCycleInjection(){
		PersistenceManagerLifeCycleWrapper pml = new PersistenceManagerLifeCycleWrapper(pm);
		
		DiscoveryLifeCycleMulti before = new DiscoveryLifeCycleMulti("Radioactivity", LongAutoID_CURIE);
		pm.insert(before);
		
		lifeCyclePhase = "";
		DiscoveryLifeCycleMulti after = new DiscoveryLifeCycleMulti();
		after.id = before.id;
		pml.get(after);
		
		assertEquals(LifeCyclePhase.PRE_FETCH.toString()+" "+LifeCyclePhase.POST_FETCH.toString()+" ", lifeCyclePhase);
	}
	
	public void testLifeCycleInsert(){
		PersistenceManagerLifeCycleWrapper pml = new PersistenceManagerLifeCycleWrapper(pm);
		
		lifeCyclePhase = "";
		DiscoveryLifeCycle before = new DiscoveryLifeCycle("Radioactivity", LongAutoID_CURIE);
		pml.insert(before);
		
		assertEquals(LifeCyclePhase.PRE_INSERT.toString()+" "+LifeCyclePhase.POST_INSERT.toString()+" ", lifeCyclePhase);
	}
	
	public void testLifeCycleDelete(){
		PersistenceManagerLifeCycleWrapper pml = new PersistenceManagerLifeCycleWrapper(pm);
		
		lifeCyclePhase = "";
		DiscoveryLifeCycle before = new DiscoveryLifeCycle("Radioactivity", LongAutoID_CURIE);
		pm.insert(before);
		
		pml.delete(before);
		
		assertEquals(LifeCyclePhase.PRE_DELETE.toString()+" "+LifeCyclePhase.POST_DELETE.toString()+" ", lifeCyclePhase);
	}
	
	public void testLifeCycleUpdate(){
		PersistenceManagerLifeCycleWrapper pml = new PersistenceManagerLifeCycleWrapper(pm);
		
		DiscoveryLifeCycle before = new DiscoveryLifeCycle("Radioactivity", LongAutoID_CURIE);
		pm.insert(before);
		
		lifeCyclePhase = "";
		before.name = "Radioactivity_UPD";
		pml.update(before);
		
		assertEquals(LifeCyclePhase.PRE_UPDATE.toString()+" "+LifeCyclePhase.POST_UPDATE.toString()+" ", lifeCyclePhase);
	}
	
	public void testLifeCycleSave(){
		PersistenceManagerLifeCycleWrapper pml = new PersistenceManagerLifeCycleWrapper(pm);
		
		lifeCyclePhase = "";
		DiscoveryLifeCycle before = new DiscoveryLifeCycle("Radioactivity", LongAutoID_CURIE);
		pml.save(before);
		
		assertEquals(LifeCyclePhase.PRE_SAVE.toString()+" "+LifeCyclePhase.POST_SAVE.toString()+" ", lifeCyclePhase);
	}
	
	public void testSerializeEmbeddedModel() {
		EmbeddedModel embed = new EmbeddedModel();
		embed.id = "embed";
		embed.alpha = "test";
		embed.beta = 123;
		pm.insert(embed);

		EmbeddedSubModel subEmbed = new EmbeddedSubModel();
		subEmbed.id = "subembed";
		subEmbed.parent = embed;
		
		EmbeddedContainerModel container = new EmbeddedContainerModel();
		container.id = "container";
		container.embed = embed;
		pm.insert(container);
		
		EmbeddedContainerModel afterContainer = pm.getByKey(EmbeddedContainerModel.class, container.id);
		assertNotNull(afterContainer);
		assertEquals(container.id, afterContainer.id);
		assertNotNull(afterContainer.embed);
		assertEquals(embed.id, afterContainer.embed.id);
		assertEquals(null, afterContainer.embed.alpha);
		assertEquals(embed.beta, afterContainer.embed.beta);
		assertEquals(null, afterContainer.embed.subs);
	}
	
	public void testBigDecimal() {
		BigDecimalModel bigdec = 
			new BigDecimalModel(new BigDecimal("123456789.0123456890"));
		pm.insert(bigdec);
		
		BigDecimalModel bigdec2 = pm.getByKey(BigDecimalModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		bigdec = 
			new BigDecimalModel(
					new BigDecimal("999999999.9999999999"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		//-100.5
		bigdec = 
			new BigDecimalModel(new BigDecimal("-100.5000000000"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
	}
	
	public void testBigDecimalNoPrecision() {
		BigDecimalModelNoPrecision bigdec = 
			new BigDecimalModelNoPrecision(new BigDecimal("123456789.01"));
		pm.insert(bigdec);
		
		BigDecimalModelNoPrecision bigdec2 = pm.getByKey(BigDecimalModelNoPrecision.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		bigdec = 
			new BigDecimalModelNoPrecision(
					new BigDecimal("999999999.99"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalModelNoPrecision.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		//-100.5
		bigdec = 
			new BigDecimalModelNoPrecision(new BigDecimal("-100.50"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalModelNoPrecision.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
	}
	
	public void testBigDecimalString() {
		BigDecimalStringModel bigdec = 
			new BigDecimalStringModel(new BigDecimal("123456789.0123456890"));
		pm.insert(bigdec);
		
		BigDecimalStringModel bigdec2 = pm.getByKey(BigDecimalStringModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		bigdec = 
			new BigDecimalStringModel(
					new BigDecimal("999999999.9999999999"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalStringModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		//-100.5
		bigdec = 
			new BigDecimalStringModel(new BigDecimal("-100.5000000000"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalStringModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
	}
	
	public void testBigDecimalDouble() {
		BigDecimalDoubleModel bigdec = 
			new BigDecimalDoubleModel(new BigDecimal("123456789.012345"));
		pm.insert(bigdec);
		
		BigDecimalDoubleModel bigdec2 = pm.getByKey(BigDecimalDoubleModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		bigdec = 
			new BigDecimalDoubleModel(
					new BigDecimal("999999999.9999999999"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalDoubleModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		//-100.5
		bigdec = 
			new BigDecimalDoubleModel(new BigDecimal("-100.5000000000"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalDoubleModel.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
	}
	
	public void testTransactionUpdate() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount-=100L;
			pm.update(accFrom);
			accTo.amount+=100L;
			pm.update(accTo);
			pm.commitTransaction();
		}catch(SienaException e){
			pm.rollbackTransaction();
			fail();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(900L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1100L == accToAfter.amount);
	}
	
	public void testTransactionUpdateFailure() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount-=100L;
			pm.update(accFrom);
			accTo.amount+=100L;
			pm.update(accTo);
			throw new SienaException("test");
		}catch(SienaException e){
			pm.rollbackTransaction();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1000L == accToAfter.amount);
	}
	
	public void testTransactionInsert() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount=1000L;
			accTo.amount=100L;
			pm.insert(accFrom);
			pm.insert(accTo);
			pm.commitTransaction();
		}catch(SienaException e){
			pm.rollbackTransaction();
			fail();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(100L == accToAfter.amount);
	}
	
	public void testTransactionInsertFailure() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount=1000L;
			accTo.amount=100L;
			pm.insert(accFrom, accTo);
			throw new SienaException("test");
		}catch(SienaException e){
			pm.rollbackTransaction();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertNull(accFromAfter);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertNull(accToAfter);
	}
	
	public void testTransactionSave() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount-=100L;
			pm.save(accFrom);
			accTo.amount+=100L;
			pm.save(accTo);
			pm.commitTransaction();
		}catch(SienaException e){
			pm.rollbackTransaction();
			fail();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(900L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1100L == accToAfter.amount);
	}
	
	public void testTransactionSaveFailure() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount-=100L;
			pm.save(accFrom);
			accTo.amount+=100L;
			pm.save(accTo);
			throw new SienaException("test");
		}catch(SienaException e){
			pm.rollbackTransaction();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1000L == accToAfter.amount);
	}
	
	public void testTransactionDelete() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			pm.delete(accFrom);
			pm.delete(accTo);
			pm.commitTransaction();
		}catch(SienaException e){
			pm.rollbackTransaction();
			fail();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertNull(accFromAfter);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertNull(accToAfter);
	}
	
	public void testTransactionDeleteFailure() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(100L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			pm.delete(accFrom);
			pm.delete(accTo);
			throw new SienaException("test");
		}catch(SienaException e){
			pm.rollbackTransaction();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(100L == accToAfter.amount);
	}
	
	public void testTransactionInsertBatch() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount=1000L;
			accTo.amount=100L;
			pm.insert(accFrom, accTo);
			pm.commitTransaction();
		}catch(SienaException e){
			pm.rollbackTransaction();
			fail();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(100L == accToAfter.amount);
	}
	
	public void testTransactionInsertBatchFailure() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount=1000L;
			accTo.amount=100L;
			pm.insert(accFrom, accTo);
			throw new SienaException("test");
		}catch(SienaException e){
			pm.rollbackTransaction();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertNull(accFromAfter);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertNull(accToAfter);
	}
	
	public void testTransactionDeleteBatch() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);

		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			pm.delete(accFrom, accTo);
			pm.commitTransaction();
		}catch(SienaException e){
			pm.rollbackTransaction();
			fail();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertNull(accFromAfter);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertNull(accToAfter);
	}
	
	public void testTransactionDeleteBatchFailure() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(100L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			pm.delete(accFrom, accTo);
			throw new SienaException("test");
		}catch(SienaException e){
			pm.rollbackTransaction();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(100L == accToAfter.amount);
	}
	
	public void testTransactionUpdateBatch() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount-=100L;
			accTo.amount+=100L;
			pm.update(accFrom, accTo);
			pm.commitTransaction();
		}catch(SienaException e){
			pm.rollbackTransaction();
			fail();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(900L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1100L == accToAfter.amount);
	}
	
	public void testTransactionUpdateBatchFailure() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount-=100L;
			accTo.amount+=100L;
			pm.update(accFrom, accTo);
			throw new SienaException("test");
		}catch(SienaException e){
			pm.rollbackTransaction();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1000L == accToAfter.amount);
	}
	
	public void testTransactionSaveBatch() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount-=100L;
			accTo.amount+=100L;
			pm.save(accFrom, accTo);
			pm.commitTransaction();
		}catch(SienaException e){
			pm.rollbackTransaction();
			fail();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(900L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1100L == accToAfter.amount);
	}
	
	public void testTransactionSaveBatchFailure() {
		TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
		TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		pm.insert(accFrom, accTo);
	
		try {
			pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
			accFrom.amount-=100L;
			accTo.amount+=100L;
			pm.save(accFrom, accTo);
			throw new SienaException("test");
		}catch(SienaException e){
			pm.rollbackTransaction();
		}finally{
			pm.closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1000L == accToAfter.amount);
	}
}
