package siena.base.test;

import static siena.Json.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import siena.Query;
import siena.SienaRestrictedApiException;
import siena.base.test.model.Address;
import siena.base.test.model.AutoInc;
import siena.base.test.model.Contact;
import siena.base.test.model.DataTypes;
import siena.base.test.model.DataTypes.EnumLong;
import siena.base.test.model.Discovery4JoinStringId;
import siena.base.test.model.DiscoveryPrivate;
import siena.base.test.model.DiscoveryStringId;
import siena.base.test.model.MultipleKeys;
import siena.base.test.model.PersonLongAutoID;
import siena.base.test.model.PersonLongManualID;
import siena.base.test.model.PersonStringAutoIncID;
import siena.base.test.model.PersonStringID;
import siena.base.test.model.PersonUUID;
import siena.sdb.SdbPersistenceManager;

public abstract class BaseTestNoAutoInc_2_FETCH extends BaseTestNoAutoInc_BASE {
	
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
		if(supportsAutoincrement()){
			List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", false).fetchKeys();
			assertEquals(0, people.size());
		}else {
			try {
				List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", false).fetchKeys();				
			}catch(SienaRestrictedApiException ex){
				return;
			}
			
			fail();
		}
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
		if(supportsAutoincrement()){
			List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", true).fetchKeys();

			assertEquals(0, people.size());
		}else {
			try {
				List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", true).fetchKeys();
			}catch(SienaRestrictedApiException ex){
				return;
			}
			
			fail();
		}
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
		if(supportsAutoincrement()){
			PersonLongAutoID person = pm.createQuery(PersonLongAutoID.class).filter("id", LongAutoID_EINSTEIN.id).get();
			assertNotNull(person);
			assertEquals(LongAutoID_EINSTEIN, person);
		}else {
			try {
				PersonLongAutoID person = pm.createQuery(PersonLongAutoID.class).filter("id", LongAutoID_EINSTEIN.id).get();
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
		}
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
		if(supportsAutoincrement()){
			List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id!=", LongAutoID_EINSTEIN.id).order("id").fetch();
			assertEquals(0, people.size());
		}else {
			try {
				List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id!=", LongAutoID_EINSTEIN.id).order("id").fetch();
			}catch(SienaRestrictedApiException ex){
				return;
			}
			
			fail();
		}
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
		if(supportsAutoincrement()){
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
		}else {
			try {
				@SuppressWarnings("serial")
				List<PersonLongAutoID> people = 
					pm.createQuery(PersonLongAutoID.class)
						.filter("id IN", new ArrayList<Long>(){{ 
							add(LongAutoID_TESLA.id);
							add(LongAutoID_CURIE.id);
						}})
						.fetch();
			}catch(SienaRestrictedApiException ex){
				return;
			}
	
			fail();
		}
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
		if(supportsAutoincrement()){
			List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id<", LongAutoID_EINSTEIN.id).order("id").fetch();

			assertNotNull(people);
			assertEquals(2, people.size());

			assertEquals(LongAutoID_TESLA, people.get(0));
			assertEquals(LongAutoID_CURIE, people.get(1));
		}else {
			try {
				List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id<", LongAutoID_EINSTEIN.id).order("id").fetch();
			}catch(SienaRestrictedApiException ex){
				return;
			}
		}
		fail();
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
		if(supportsAutoincrement()){
			List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id<=", LongAutoID_EINSTEIN.id).order("id").fetch();

			assertNotNull(people);
			assertEquals(3, people.size());

			assertEquals(LongAutoID_TESLA, people.get(0));
			assertEquals(LongAutoID_CURIE, people.get(1));
			assertEquals(LongAutoID_EINSTEIN, people.get(2));
		} else {
			try {
				List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id<=", LongAutoID_EINSTEIN.id).order("id").fetch();
			}catch(SienaRestrictedApiException ex){
				return;
			}
		}
		fail();
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
		if(supportsAutoincrement()){
			List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id>", LongAutoID_TESLA.id).order("id").fetch();

			assertNotNull(people);
			assertEquals(2, people.size());

			assertEquals(LongAutoID_CURIE, people.get(0));
			assertEquals(LongAutoID_EINSTEIN, people.get(1));
		}else {
			try {
				List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id>", LongAutoID_TESLA.id).order("id").fetch();
			}catch(SienaRestrictedApiException ex){
				return;
			}
	
			fail();
		}
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
		if(supportsAutoincrement()){
			List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id>", LongAutoID_TESLA.id).order("id").fetch();

			assertNotNull(people);
			assertEquals(2, people.size());

			assertEquals(LongAutoID_CURIE, people.get(0));
			assertEquals(LongAutoID_EINSTEIN, people.get(1));
		}else {
			try {
				List<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).filter("id>=", LongAutoID_CURIE.id).order("id").fetch();
			}catch(SienaRestrictedApiException ex){
				return;
			}

			fail();
		}
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
		if(supportsAutoincrement()){
			assertEquals(2, pm.createQuery(PersonLongAutoID.class).filter("id<", LongAutoID_EINSTEIN.id).count());
		}else {
			try {
				assertEquals(2, pm.createQuery(PersonLongAutoID.class).filter("id<", LongAutoID_EINSTEIN.id).count());
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
		}
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
		if(supportsAutoincrement()) {
			List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", 0, false).fetch(1);
	
			assertNotNull(people);
			assertEquals(1, people.size());
	
			assertEquals(LongAutoID_TESLA, people.get(0));
		}else {
			try {
				List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", 0, false).fetch(1);
			}catch(SienaRestrictedApiException ex){
				return;
			}
			
			fail();
		}
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
/*	
	@Deprecated
	public void testCountLimit() {
		assertEquals(1, pm.createQuery(PersonUUID.class).filter("n<", 3).count(1));
	}
*/
	public void testFetchLimitReal() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+i, StringID_CURIE);
			pm.insert(discs[i]);
		}

		List<DiscoveryStringId> res = pm.createQuery(DiscoveryStringId.class).order("name").fetch(3);
		assertNotNull(res);
		assertEquals(3, res.size());
		
		assertEquals(discs[0], res.get(0));
		assertEquals(discs[1], res.get(1));
		assertEquals(discs[2], res.get(2));
	}
	

	public void testFetchLimitOffsetReal() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+i, StringID_CURIE);
			pm.insert(discs[i]);
		}

		List<DiscoveryStringId> res = pm.createQuery(DiscoveryStringId.class).order("name").fetch(3, 5);
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

	public void testOrderLongAutoId() {
		if(supportsAutoincrement()){
			List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", false).fetch();
			
			assertNotNull(people);
			assertEquals(3, people.size());
			
			PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_TESLA, LongAutoID_CURIE, LongAutoID_EINSTEIN };
	
			int i = 0;
			for (PersonLongAutoID person : people) {
				assertEquals(array[i], person);
				i++;
			}
		}else {
			try{
				List<PersonLongAutoID> people = queryPersonLongAutoIDOrderBy("id", "", false).fetch();
			}catch(SienaRestrictedApiException ex) {
				return;
			}
			
			fail();
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
	
	public void testFetchLotsOfEntitiesStatefulMixed(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		List<DiscoveryStringId> res = query.paginate(50).fetch();
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
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		List<DiscoveryStringId> res = query.fetch(50);
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
}
