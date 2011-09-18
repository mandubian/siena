package siena.base.test;

import static siena.Json.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.appengine.tools.info.SupportInfo;

import siena.Model;
import siena.Query;
import siena.SienaException;
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
import siena.base.test.model.PersonBsonID;
import siena.base.test.model.PersonLongAutoID;
import siena.base.test.model.PersonLongManualID;
import siena.base.test.model.PersonStringAutoIncID;
import siena.base.test.model.PersonStringID;
import siena.base.test.model.PersonUUID;
import siena.sdb.SdbPersistenceManager;

public abstract class BaseTestNoAutoInc_0_STARTER_BSONID extends BaseTestNoAutoInc_BASE {
	@Override
	public void postInit() {
		pm.createQuery(PersonBsonID.class).delete();
	}

	public void testInsertGetBSON() {
		PersonBsonID maxwell = new PersonBsonID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);
		
		PersonBsonID maxwellbis = new PersonBsonID();
		maxwellbis.firstName = "James Clerk";
		maxwellbis.lastName = "Maxwell";
		maxwellbis.city = "Edinburgh";
		maxwellbis.n = 4;
		pm.get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
	}
	
	public void testDeleteBSON() {
		PersonBsonID maxwell = new PersonBsonID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);
		
		pm.delete(maxwell);
		
		try {
			PersonBsonID maxwellbis = new PersonBsonID();
			maxwellbis.firstName = "James Clerk";
			maxwellbis.lastName = "Maxwell";
			maxwellbis.city = "Edinburgh";
			maxwellbis.n = 4;
			pm.get(maxwellbis);
		}catch(SienaException ex){
			assertTrue(ex.getMessage().contains("not found"));
		}
	}

	public void testDeleteQueryNoFilterNoSortBSON() {
		PersonBsonID maxwell = new PersonBsonID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);
		
		Model.all(PersonBsonID.class).delete();
		
		try {
			PersonBsonID maxwellbis = new PersonBsonID();
			maxwellbis.firstName = "James Clerk";
			maxwellbis.lastName = "Maxwell";
			maxwellbis.city = "Edinburgh";
			maxwellbis.n = 4;
			pm.get(maxwellbis);
		}catch(SienaException ex){
			assertTrue(ex.getMessage().contains("not found"));
		}
	}
	
	public void testUpdateBSON() {
		PersonBsonID maxwell = new PersonBsonID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 8;
		
		pm.update(maxwell);
		
		PersonBsonID maxwellbis = new PersonBsonID();
		maxwellbis.id = maxwell.id;
		// needs to set the integer n or it searches value 0
		maxwellbis.n = maxwell.n;
		pm.get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
	}

	public void testSaveBSON() {
		PersonBsonID maxwell = new PersonBsonID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell);
		assertNotNull(maxwell.id);
		
		PersonBsonID maxwellbis = new PersonBsonID();
		maxwellbis.id = maxwell.id;
		// needs to set the integer n or it searches value 0
		maxwellbis.n = maxwell.n;
		pm.get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 8;
		
		pm.save(maxwell);
		
		PersonBsonID maxwellter = new PersonBsonID();
		maxwellter.id = maxwell.id;
		// needs to set the integer n or it searches value 0
		maxwellter.n = maxwell.n;
		pm.get(maxwellter);
		
		assertEquals(maxwell, maxwellter);
	}
	
	public void testGetByKeyBSON() {
		PersonBsonID maxwell = new PersonBsonID();
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell);
		assertNotNull(maxwell.id);
		
		PersonBsonID maxwellbis = pm.getByKey(PersonBsonID.class, maxwell.id);
		assertEquals(maxwell, maxwellbis);
	}
		
	public void testCountBSON() {
		for(int i=0; i<10; i++){
			PersonBsonID person = new PersonBsonID();
			person.firstName = "firstName"+i;
			person.lastName = "lastName"+i;
			person.city = "city"+i;
			person.n = i;
			pm.insert(person);
			assertNotNull(person.id);
		}
		
		assertEquals(10, pm.count(Model.all(PersonBsonID.class)));
	}
	
	public void testInsertMultipleListBSON() {
		List<PersonBsonID> persons = new ArrayList<PersonBsonID>();
		
		for(int i=0; i<10; i++){
			PersonBsonID person = new PersonBsonID();
			person.firstName = "firstName"+i;
			person.lastName = "lastName"+i;
			person.city = "city"+i;
			person.n = i;
			persons.add(person);
		}
		
		int nb = pm.insert(persons);		
		assertEquals(10, nb);
	}
}
