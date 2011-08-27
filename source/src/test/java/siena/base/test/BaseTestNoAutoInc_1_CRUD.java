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

public abstract class BaseTestNoAutoInc_1_CRUD extends BaseTestNoAutoInc_BASE {
	
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
		if(supportsAutoincrement()){
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
		}else {
			try {
				PersonLongAutoID maxwell = new PersonLongAutoID();
				maxwell.firstName = "James Clerk";
				maxwell.lastName = "Maxwell";
				maxwell.city = "Edinburgh";
				maxwell.n = 4;
		
				pm.insert(maxwell);
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
		}
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
		if(supportsAutoincrement()){
			PersonLongAutoID curie = getPersonLongAutoID(LongAutoID_CURIE.id);
			assertEquals(LongAutoID_CURIE, curie);
		}else {
			try {
				PersonLongAutoID curie = getPersonLongAutoID(LongAutoID_CURIE.id);
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
		}
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
		if(supportsAutoincrement()){
			PersonLongAutoID curie = getPersonLongAutoID(LongAutoID_CURIE.id);
			curie.lastName = "Sklodowska–Curie";
			pm.update(curie);
			PersonLongAutoID curie2 = getPersonLongAutoID(LongAutoID_CURIE.id);
			assertEquals(curie2, curie);
		}else {
			try {
				PersonLongAutoID curie = getPersonLongAutoID(LongAutoID_CURIE.id);
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
		}
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

	
}
