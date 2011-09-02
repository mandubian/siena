package siena.base.test;

import java.util.ArrayList;
import java.util.List;

import siena.SienaRestrictedApiException;
import siena.base.test.model.Discovery4Search2StringId;
import siena.base.test.model.DiscoveryStringId;
import siena.base.test.model.PersonLongAutoID;
import siena.base.test.model.PersonLongManualID;
import siena.base.test.model.PersonStringID;
import siena.base.test.model.PersonUUID;

public abstract class BaseTestNoAutoInc_7_BATCH extends BaseTestNoAutoInc_BASE {

	public void testBatchInsert() {
		Object[] discs = new DiscoveryStringId[100];
		for(int i=0; i<100; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		int nb = pm.insert(discs);
		assertEquals(discs.length, nb);
		
		List<DiscoveryStringId> res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
		assertEquals(discs.length, res.size());
		int i=0;
		for(DiscoveryStringId disc:res){
			assertEquals(discs[i++], disc);
		}
	}
	public void testBatchInsertList() {
		List<DiscoveryStringId> discs = new ArrayList<DiscoveryStringId>();
		for(int i=0; i<100; i++){
			discs.add(new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE));
		}
		int nb = pm.insert(discs);
		assertEquals(discs.size(), nb);
		
		List<DiscoveryStringId> res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
		assertEquals(discs.size(), res.size());
		int i=0;
		for(DiscoveryStringId disc:res){
			assertEquals(discs.get(i++), disc);
		}
	}
	
	public void testBatchDelete() {
		Object[] discs = new DiscoveryStringId[100];
		for(int i=0; i<100; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert(discs);

		List<DiscoveryStringId> res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
		assertEquals(discs.length, res.size());
		
		int nb = pm.delete(discs);
		assertEquals(discs.length, nb);

		res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
		assertEquals(0, res.size());
	}
	
	public void testBatchDeleteList() {
		List<DiscoveryStringId> discs = new ArrayList<DiscoveryStringId>();
		for(int i=0; i<59; i++){
			DiscoveryStringId disc = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			discs.add(disc);
		}
		pm.insert(discs);

		List<DiscoveryStringId> res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
		assertEquals(discs.size(), res.size());
		
		int nb = pm.delete(discs);
		assertEquals(discs.size(), nb);
		
		res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
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
	
	public void testBatchDeleteByKeysLots() {
		List<DiscoveryStringId> discs = new ArrayList<DiscoveryStringId>();
		List<String> keys = new ArrayList<String>();
		for(int i=0; i<59; i++){
			DiscoveryStringId disc = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			discs.add(disc);
			keys.add(disc.name);
		}
		pm.insert(discs);

		int nb = 
			pm.deleteByKeys(DiscoveryStringId.class, keys);
		
		assertEquals(discs.size(), nb);
		
		List<DiscoveryStringId> res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
		assertEquals(0, res.size());
	}
	
	public void testBatchGet() {
		DiscoveryStringId[] discs = new DiscoveryStringId[100];
		
		for(int i=0; i<100; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		DiscoveryStringId[] discs2Get = new DiscoveryStringId[100];
		for(int i=0; i<100; i++){
			discs2Get[i] = new DiscoveryStringId();
			discs2Get[i].name = discs[i].name;
		}
		
		int nb = pm.get((Object[])discs2Get);
		assertEquals(discs.length, nb);
		
		assertEquals(discs.length, discs2Get.length);
		for(int i=0; i<discs.length; i++){
			assertEquals(discs[i], discs2Get[i]);
		}		
	}
	
	public void testBatchGetList() {
		DiscoveryStringId[] discs = new DiscoveryStringId[100];
		
		for(int i=0; i<100; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		List<DiscoveryStringId> discs2Get = new ArrayList<DiscoveryStringId>();
		for(int i=0; i<100; i++){
			DiscoveryStringId disc = new DiscoveryStringId();
			disc.name = discs[i].name;
			discs2Get.add(disc);
		}
		
		int nb = pm.get(discs2Get);
		assertEquals(nb, discs.length);
		int i=0;
		for(DiscoveryStringId disc:discs2Get){
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
		DiscoveryStringId[] discs = new DiscoveryStringId[100];
		
		for(int i=0; i<100; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		List<String> discsKeys = new ArrayList<String>();
		for(int i=0; i<100; i++){
			discsKeys.add(discs[i].name);
		}
		
		List<DiscoveryStringId> discs2Get = pm.getByKeys(DiscoveryStringId.class, discsKeys);
		assertEquals(discs.length, discs2Get.size());
		int i=0;
		for(DiscoveryStringId disc:discs2Get){
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
	
	
	public void testBatchUpdate() {
		Object[] discs = new DiscoveryStringId[100];
		for(int i=0; i<100; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert(discs);

		List<DiscoveryStringId> res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
		assertEquals(discs.length, res.size());
		
		for(int i=0; i<100; i++){
			((DiscoveryStringId)discs[i]).discoverer = StringID_EINSTEIN;
		}
		
		int nb = pm.update(discs);
		assertEquals(discs.length, nb);
		res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		int i=0;
		for(DiscoveryStringId disc:res){
			assertEquals(discs[i++], disc);
		}
		
	}
	
	public void testBatchUpdateList() {
		List<DiscoveryStringId> discs = new ArrayList<DiscoveryStringId>();
		for(int i=0; i<100; i++){
			discs.add(new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE));
		}
		int nb = pm.insert(discs);
		assertEquals(discs.size(), nb);

		List<DiscoveryStringId> res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		
		assertEquals(discs.size(), res.size());
		
		for(DiscoveryStringId d: discs){
			d.discoverer = StringID_EINSTEIN;
		}
		
		nb = pm.update(discs);
		assertEquals(discs.size(), nb);
		res = 
			pm.createQuery(DiscoveryStringId.class).fetch();
		int i=0;
		for(DiscoveryStringId disc:res){
			assertEquals(discs.get(i++), disc);
		}
		
	}
	
	public void testGetByKeyNonExisting() {
		if(supportsAutoincrement()){
			PersonLongAutoID pers = getByKeyPersonLongAutoID(12345678L);
			assertNull(pers);
		}else {
			try {
				PersonLongAutoID pers = getByKeyPersonLongAutoID(12345678L);
			}catch(SienaRestrictedApiException ex){
				return;				
			}
			fail();
		}
	}
	
	public void testGetByKeyUUID() {
		PersonUUID curie = getByKeyPersonUUID(UUID_CURIE.id);
		assertEquals(UUID_CURIE, curie);
	}

	public void testGetByKeyLongAutoID() {
		if(supportsAutoincrement()){
			PersonLongAutoID curie = getByKeyPersonLongAutoID(LongAutoID_CURIE.id);
			assertEquals(LongAutoID_CURIE, curie);
		}else {
			try {
				PersonLongAutoID curie = getByKeyPersonLongAutoID(LongAutoID_CURIE.id);
			}catch(SienaRestrictedApiException ex){
				return;
			}
			fail();
		}
	}

	public void testGetByKeyLongManualID() {
		PersonLongManualID curie = getByKeyPersonLongManualID(LongManualID_CURIE.id);
		assertEquals(LongManualID_CURIE, curie);
	}

	public void testGetByKeyStringID() {
		PersonStringID curie = getByKeyPersonStringID(StringID_CURIE.id);
		assertEquals(StringID_CURIE, curie);
	}
	

	public void testBatchSave() {
		Object[] discs = new Discovery4Search2StringId[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4Search2StringId(
					"Disc_"+String.format("%03d", i), 
					"Body_"+String.format("%03d", i),
					StringID_CURIE);
		}
		pm.save(discs);

		List<Discovery4Search2StringId> res = 
			pm.createQuery(Discovery4Search2StringId.class).fetch();
		
		assertEquals(discs.length, res.size());
		int i=0;
		for(Discovery4Search2StringId disc:res){
			assertEquals(discs[i++], disc);
		}
		
		for(i=0; i<100; i++){
			((Discovery4Search2StringId)discs[i]).body += "UPD";
			((Discovery4Search2StringId)discs[i]).discoverer = StringID_EINSTEIN;
		}
		
		int nb = pm.save(discs);
		assertEquals(discs.length, nb);
		res = 
			pm.createQuery(Discovery4Search2StringId.class).fetch();
		i=0;
		for(Discovery4Search2StringId disc:res){
			assertEquals(discs[i++], disc);
		}
		
	}
	
	
	public void testBatchSaveList() {
		List<Discovery4Search2StringId> discs = new ArrayList<Discovery4Search2StringId>();
		for(int i=0; i<100; i++){
			discs.add(new Discovery4Search2StringId(
					"Disc_"+String.format("%03d", i), "Body_"+String.format("%03d", i), StringID_CURIE));
		}
		int nb = pm.insert(discs);
		assertEquals(discs.size(), nb);
		
		List<Discovery4Search2StringId> res = 
			pm.createQuery(Discovery4Search2StringId.class).fetch();
		
		assertEquals(discs.size(), res.size());
		int i=0;
		for(Discovery4Search2StringId disc:res){
			assertEquals(discs.get(i++), disc);
		}
		
		for(i=0; i<100; i++){
			((Discovery4Search2StringId)discs.get(i)).body += "UPD";
			((Discovery4Search2StringId)discs.get(i)).discoverer = StringID_EINSTEIN;
		}
		
		nb = pm.save(discs);
		assertEquals(discs.size(), nb);
		res = 
			pm.createQuery(Discovery4Search2StringId.class).fetch();
		i=0;
		for(Discovery4Search2StringId disc:res){
			assertEquals(discs.get(i++), disc);
		}
	}
}
