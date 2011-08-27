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

public abstract class BaseTestNoAutoInc_3_ITER extends BaseTestNoAutoInc_BASE {
	

	
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
		if(supportsAutoincrement()){
			Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter();
	
			assertNotNull(people);
	
			PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_TESLA, LongAutoID_CURIE, LongAutoID_EINSTEIN };
	
			int i = 0;
			for (PersonLongAutoID person : people) {
				assertEquals(array[i], person);
				i++;
			}
		}else {
			try {
				Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter();
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
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
		if(supportsAutoincrement()){
			Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter(2);
	
			assertNotNull(people);
	
			PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_TESLA, LongAutoID_CURIE };
	
			int i = 0;
			for (PersonLongAutoID person : people) {
				assertEquals(array[i], person);
				i++;
			}
		}else {
			try {
				Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter(2);				
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
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
		if(supportsAutoincrement()) {
			Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter(2, 1);
	
			assertNotNull(people);
	
			PersonLongAutoID[] array = new PersonLongAutoID[] { LongAutoID_CURIE, LongAutoID_EINSTEIN };
	
			int i = 0;
			for (PersonLongAutoID person : people) {
				assertEquals(array[i], person);
				i++;
			}
		}else {
			try {
				Iterable<PersonLongAutoID> people = pm.createQuery(PersonLongAutoID.class).order("n").iter(2, 1);
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
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

	
	public void testIterLotsOfEntitiesStateless(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		Iterable<DiscoveryStringId> iter = query.iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
	
	public void testIterLotsOfEntitiesStateful(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
	
	public void testIterLotsOfEntitiesStatefulMixed(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> res = query.iter(50);
		Iterator<DiscoveryStringId> it = res.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		res = query.iter(50,50);
		it = res.iterator();
		i=100;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	

		res = query.iter(50,100);
		it = res.iterator();
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
	
	public void testIterLotsOfEntitiesStatefulMixed2(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.paginate(50).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		iter = query.iter(50,50);
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	

	}

	public void testIterLotsOfEntitiesStatefulMixed3(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.iter(50);
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
				
		iter = query.paginate(50).iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	
	
		iter = query.iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);	

		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);	
	}
}
