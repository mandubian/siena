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
import siena.mongodb.MongoPersistenceManager;
import siena.sdb.SdbPersistenceManager;

public abstract class BaseTestNoAutoInc_0_STARTER_STRINGID extends BaseTestNoAutoInc_BASE {
	@Override
	public void postInit() {
		pm.createQuery(PersonStringID.class).delete();
		pm.option(MongoPersistenceManager.WRITECONCERN_SAFE);
	}

	public void testInsertGetStringId() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);
		
		PersonStringID maxwellbis = new PersonStringID();
		maxwellbis.id = "MAXWELL";
		maxwellbis.firstName = "James Clerk";
		maxwellbis.lastName = "Maxwell";
		maxwellbis.city = "Edinburgh";
		maxwellbis.n = 4;
		pm.get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
	}
	
	public void testDeleteStringId() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);
		
		pm.delete(maxwell);
		
		try {
			PersonStringID maxwellbis = new PersonStringID();
			maxwellbis.id = "MAXWELL";
			maxwellbis.firstName = "James Clerk";
			maxwellbis.lastName = "Maxwell";
			maxwellbis.city = "Edinburgh";
			maxwellbis.n = 4;
			pm.get(maxwellbis);
		}catch(SienaException ex){
			assertTrue(ex.getMessage().contains("not found"));
		}
	}
	
	public void testDeleteQueryNoFilterNoSortStringId() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertNotNull(maxwell.id);
		
		Model.all(PersonStringID.class).delete();
		
		try {
			PersonStringID maxwellbis = new PersonStringID();
			maxwellbis.id = "MAXWELL";
			maxwellbis.firstName = "James Clerk";
			maxwellbis.lastName = "Maxwell";
			maxwellbis.city = "Edinburgh";
			maxwellbis.n = 4;
			pm.get(maxwellbis);
		}catch(SienaException ex){
			assertTrue(ex.getMessage().contains("not found"));
		}
	}
	
	public void testUpdateStringId() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
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
		
		PersonStringID maxwellbis = new PersonStringID();
		maxwellbis.id = "MAXWELL";
		maxwellbis.id = maxwell.id;
		// needs to set the integer n or it searches value 0
		maxwellbis.n = maxwell.n;
		pm.get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
	}

	public void testSaveBSON() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell);
		assertNotNull(maxwell.id);
		
		PersonStringID maxwellbis = new PersonStringID();
		maxwellbis.id = "MAXWELL";
		// needs to set the integer n or it searches value 0
		maxwellbis.n = maxwell.n;
		pm.get(maxwellbis);
		
		assertEquals(maxwell, maxwellbis);
		
		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 8;
		
		pm.save(maxwell);
		
		PersonStringID maxwellter = new PersonStringID();
		maxwellter.id = "MAXWELL";
		// needs to set the integer n or it searches value 0
		maxwellter.n = maxwell.n;
		pm.get(maxwellter);
		
		assertEquals(maxwell, maxwellter);
	}
	
	public void testGetByKeyStringId() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.save(maxwell);
		assertNotNull(maxwell.id);
		
		PersonStringID maxwellbis = pm.getByKey(PersonStringID.class, maxwell.id);
		assertEquals(maxwell, maxwellbis);
	}
	
	public void testCountStringId() {
		for(int i=0; i<10; i++){
			PersonStringID person = new PersonStringID();
			person.id = "PERSON"+i;
			person.firstName = "firstName"+i;
			person.lastName = "lastName"+i;
			person.city = "city"+i;
			person.n = i;
			pm.insert(person);
			assertNotNull(person.id);
		}
		
		assertEquals(10, pm.count(Model.all(PersonStringID.class)));
	}
}
