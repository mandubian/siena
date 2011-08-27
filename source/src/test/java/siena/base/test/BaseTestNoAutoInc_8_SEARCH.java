package siena.base.test;

import java.util.List;

import siena.Query;
import siena.base.test.model.Discovery4SearchStringId;

public abstract class BaseTestNoAutoInc_8_SEARCH extends BaseTestNoAutoInc_BASE {

	public void testSearchSingle() {
		Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery4SearchStringId> query = 
			pm.createQuery(Discovery4SearchStringId.class).search("Disc_005", "name");
		
		List<Discovery4SearchStringId> res = query.fetch();
				
		assertEquals(1, res.size());
		assertEquals(discs[5], res.get(0));
	}
	
	public void testSearchSingleKeysOnly() {
		Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery4SearchStringId> query = 
			pm.createQuery(Discovery4SearchStringId.class).search("Disc_005", "name");
		
		List<Discovery4SearchStringId> res = query.fetchKeys();
				
		assertEquals(1, res.size());
		assertEquals(discs[5].name, res.get(0).name);
		assertTrue(res.get(0).isOnlyIdFilled());
	}
	
	public void testSearchSingleTwice() {
		Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery4SearchStringId> query = 
			pm.createQuery(Discovery4SearchStringId.class).search("Disc_005", "name");
		
		List<Discovery4SearchStringId> res = query.fetch();
				
		assertEquals(1, res.size());
		assertEquals(discs[5], res.get(0));

		query = 
			pm.createQuery(Discovery4SearchStringId.class).search("Disc_048", "name");
		
		res = query.fetch();
				
		assertEquals(1, res.size());
		assertEquals(discs[48], res.get(0));

	}

	public void testSearchSingleCount() {
		Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
		for(int i=0; i<100; i++){
			discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<Discovery4SearchStringId> query = 
			pm.createQuery(Discovery4SearchStringId.class).search("Disc_005", "name");
		
		int res = query.count();
				
		assertEquals(1, res);
	}
	
	public void testSearchMultipleBegin() {
		if(supportsSearchStart()){
			Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
			for(int i=0; i<100; i++){
				discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
	
			Query<Discovery4SearchStringId> query = 
				pm.createQuery(Discovery4SearchStringId.class).search("Disc_01%", "name").order("name");
			
			List<Discovery4SearchStringId> res = query.fetch();
					
			assertEquals(10, res.size());
			for(int i=0; i<10; i++){
				assertEquals(discs[i+10], res.get(i));
			}
		}
	}
	
	public void testSearchMultipleEnd() {
		if(supportsSearchEnd()){
			Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
			for(int i=0; i<100; i++){
				discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
	
			Query<Discovery4SearchStringId> query = 
				pm.createQuery(Discovery4SearchStringId.class).search("%_005", "name").order("name");
			
			List<Discovery4SearchStringId> res = query.fetch();
					
			assertEquals(1, res.size());
			assertEquals(discs[5], res.get(0));
		}
	}
	
	public void testSearchMultipleBeginEnd() {
		if(supportsSearchStart() && supportsSearchEnd()){
			Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
			for(int i=0; i<100; i++){
				discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
	
			Query<Discovery4SearchStringId> query = 
				pm.createQuery(Discovery4SearchStringId.class).search("%_01%", "name").order("name");
			
			List<Discovery4SearchStringId> res = query.fetch();
					
			assertEquals(10, res.size());
			for(int i=0; i<10; i++){
				assertEquals(discs[i+10], res.get(i));
			}
		}
	}
	
	public void testSearchMultipleMultipleWords() {
		if(supportsSearchStart() && supportsSearchEnd()){
			Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
			for(int i=0; i<100; i++){
				discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
	
			Query<Discovery4SearchStringId> query = 
				pm.createQuery(Discovery4SearchStringId.class).search("Disc_005 Disc_010 Disc_020", "name").order("name");
			
			List<Discovery4SearchStringId> res = query.fetch();
					
			assertEquals(3, res.size());
			assertEquals(discs[5], res.get(0));
			assertEquals(discs[10], res.get(1));
			assertEquals(discs[20], res.get(2));
		}
	}
	
	public void testSearchMultipleFieldsMultipleWords() {
		if(supportsSearchStart() && supportsSearchEnd()){
			Discovery4SearchStringId[] discs = new Discovery4SearchStringId[100];
			for(int i=0; i<100; i++){
				discs[i] = new Discovery4SearchStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
	
			Query<Discovery4SearchStringId> query = 
				pm.createQuery(Discovery4SearchStringId.class).search("Disc_005 Disc_010 Disc_020", "name").order("name");
			
			List<Discovery4SearchStringId> res = query.fetch();
					
			assertEquals(3, res.size());
			assertEquals(discs[5], res.get(0));
			assertEquals(discs[10], res.get(1));
			assertEquals(discs[20], res.get(2));
		}
	}
}
