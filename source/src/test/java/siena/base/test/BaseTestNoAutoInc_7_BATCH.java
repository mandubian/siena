package siena.base.test;

import java.util.ArrayList;
import java.util.List;

import siena.base.test.model.DiscoveryStringId;
import siena.base.test.model.PersonStringID;

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
		for(int i=0; i<100; i++){
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
	
	
	
}
