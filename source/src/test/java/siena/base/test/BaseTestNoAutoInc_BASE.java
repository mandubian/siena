package siena.base.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import siena.Query;
import siena.base.test.model.DataTypes;
import siena.base.test.model.Discovery4Search2StringId;
import siena.base.test.model.DiscoveryStringId;
import siena.base.test.model.PersonBsonID;
import siena.base.test.model.PersonLongAutoID;
import siena.base.test.model.PersonLongManualID;
import siena.base.test.model.PersonStringID;
import siena.base.test.model.PersonUUID;
import siena.sdb.SdbPersistenceManager;

public abstract class BaseTestNoAutoInc_BASE extends AbstractTest {
	
	protected static PersonUUID UUID_TESLA = new PersonUUID("Nikola", "Tesla", "Smiljam", 1);
	protected static PersonUUID UUID_CURIE = new PersonUUID("Marie", "Curie", "Warsaw", 2);
	protected static PersonUUID UUID_EINSTEIN = new PersonUUID("Albert", "Einstein", "Ulm", 3);
	
	protected static PersonLongManualID LongManualID_TESLA = new PersonLongManualID(1L, "Nikola", "Tesla", "Smiljam", 1);
	protected static PersonLongManualID LongManualID_CURIE = new PersonLongManualID(2L, "Marie", "Curie", "Warsaw", 2);
	protected static PersonLongManualID LongManualID_EINSTEIN = new PersonLongManualID(3L, "Albert", "Einstein", "Ulm", 3);
	
	protected static PersonStringID StringID_TESLA = new PersonStringID("TESLA", "Nikola", "Tesla", "Smiljam", 1);
	protected static PersonStringID StringID_CURIE = new PersonStringID("CURIE", "Marie", "Curie", "Warsaw", 2);
	protected static PersonStringID StringID_EINSTEIN = new PersonStringID("EINSTEIN", "Albert", "Einstein", "Ulm", 3);

	protected static PersonLongAutoID LongAutoID_TESLA = new PersonLongAutoID("Nikola", "Tesla", "Smiljam", 1);
	protected static PersonLongAutoID LongAutoID_CURIE = new PersonLongAutoID("Marie", "Curie", "Warsaw", 2);
	protected static PersonLongAutoID LongAutoID_EINSTEIN = new PersonLongAutoID("Albert", "Einstein", "Ulm", 3);
	
	public static String lifeCyclePhase = "";
	
	public void createClasses(List<Class<?>> classes) {
		classes.add(PersonBsonID.class);
		//classes.add(PersonUUID.class);
		//classes.add(PersonLongAutoID.class);
		//classes.add(PersonLongManualID.class);
		//classes.add(PersonStringAutoIncID.class);
		//classes.add(DiscoveryStringId.class);
		//classes.add(DataTypes.class);
		/*classes.add(Discovery4Join.class);
		classes.add(Discovery4Join2.class);
		classes.add(DiscoveryPrivate.class);
		classes.add(Discovery4Search.class);
		classes.add(Discovery4Search2.class);
		classes.add(DataTypes.class);
		classes.add(PolymorphicModel.class);
		classes.add(EmbeddedModel.class);
		classes.add(EmbeddedSubModel.class);
		classes.add(ContainerModel.class);
		classes.add(DiscoveryNoColumn.class);
		classes.add(DiscoveryNoColumnMultipleKeys.class);
		classes.add(DiscoveryLifeCycle.class);
		classes.add(DiscoveryLifeCycleMulti.class);
		classes.add(BigDecimalModel.class);
		classes.add(BigDecimalModelNoPrecision.class);
		classes.add(BigDecimalStringModel.class);
		classes.add(BigDecimalDoubleModel.class);
		classes.add(TransactionAccountFrom.class);
		classes.add(TransactionAccountTo.class);*/
	}

	public void postInit() {
		/*for (Class<?> clazz : classes) {
			if(!Modifier.isAbstract(clazz.getModifiers())){
				pm.createQuery(clazz).delete();			
			}
		}*/

		//pm.createQuery(PersonBson.class).delete();
		
		//pm.insert(UUID_TESLA, UUID_CURIE, UUID_EINSTEIN);
		//pm.insert(LongManualID_TESLA, LongManualID_CURIE, LongManualID_EINSTEIN);
		//pm.insert(StringID_TESLA, StringID_CURIE, StringID_EINSTEIN);
		
		/*pm.insert(UUID_TESLA, UUID_CURIE, UUID_EINSTEIN,
				LongManualID_TESLA, LongManualID_CURIE, LongManualID_EINSTEIN,
				StringID_TESLA, StringID_CURIE, StringID_EINSTEIN);*/

		//pm.option(SdbPersistenceManager.CONSISTENT_READ);

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
		return query.order(desc ? "-"+order : order);
	}

	protected Query<PersonLongAutoID> queryPersonLongAutoIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonLongAutoID> query = pm.createQuery(PersonLongAutoID.class);
		return query.order(desc ? "-"+order : order);
	}
	
	protected Query<PersonLongManualID> queryPersonLongManualIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonLongManualID> query = pm.createQuery(PersonLongManualID.class);
		return query.order(desc ? "-"+order : order);
	}
	
	protected Query<PersonStringID> queryPersonStringIDOrderBy(String order, Object value, boolean desc) {
		Query<PersonStringID> query = pm.createQuery(PersonStringID.class);
		return query.order(desc ? "-"+order : order);
	}
	
	protected PersonUUID getPersonUUID(String id) {
		PersonUUID p = new PersonUUID();
		p.id = id;
		pm.get(p);
		return p;
	}
	
	protected PersonLongAutoID getPersonLongAutoID(Long id) {
		PersonLongAutoID p = new PersonLongAutoID();
		p.id = id;
		pm.get(p);
		return p;
	}
	
	protected PersonLongManualID getPersonLongManualID(Long id) {
		PersonLongManualID p = new PersonLongManualID();
		p.id = id;
		pm.get(p);
		return p;
	}

	protected PersonStringID getPersonStringID(String id) {
		PersonStringID p = new PersonStringID();
		p.id = id;
		pm.get(p);
		return p;
	}

	protected PersonUUID getByKeyPersonUUID(String id) {
		return pm.getByKey(PersonUUID.class, id);
	}

	protected PersonLongAutoID getByKeyPersonLongAutoID(Long id) {
		return pm.getByKey(PersonLongAutoID.class, id);
	}
	
	protected PersonLongManualID getByKeyPersonLongManualID(Long id) {
		return pm.getByKey(PersonLongManualID.class, id);
	}

	protected PersonStringID getByKeyPersonStringID(String id) {
		return pm.getByKey(PersonStringID.class, id);
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
		
}
