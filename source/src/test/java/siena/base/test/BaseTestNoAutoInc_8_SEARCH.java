package siena.base.test;

import java.util.List;

import siena.Query;
import siena.base.test.model.Discovery4Search2StringId;
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
			Discovery4Search2StringId[] discs = new Discovery4Search2StringId[100];
			for(int i=0; i<100; i++){
				discs[i] = new Discovery4Search2StringId("Disc_"+String.format("%03d", i), "Body_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
	
			Query<Discovery4Search2StringId> query = 
				pm.createQuery(Discovery4Search2StringId.class).search("Disc_005 Disc_010 Disc_020 Body_058 Body_068 Body_078", "name", "body").order("name");
			
			List<Discovery4Search2StringId> res = query.fetch();
					
			assertEquals(6, res.size());
			assertEquals(discs[5], res.get(0));
			assertEquals(discs[10], res.get(1));
			assertEquals(discs[20], res.get(2));
			assertEquals(discs[58], res.get(3));
			assertEquals(discs[68], res.get(4));
			assertEquals(discs[78], res.get(5));
		}
	}
	
	public void testSearchMultipleFieldsMultipleWordsFilter() {
		if(supportsSearchStart() && supportsSearchEnd()){
			Discovery4Search2StringId[] discs = new Discovery4Search2StringId[100];
			for(int i=0; i<100; i++){
				discs[i] = new Discovery4Search2StringId("Disc_"+String.format("%03d", i), "Body_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
	
			Query<Discovery4Search2StringId> query = 
				pm.createQuery(Discovery4Search2StringId.class).search("Disc_005 Disc_010 Disc_058 Body_058 Body_068 Body_078", "name", "body").filter("name", "Disc_058").order("name");
			
			List<Discovery4Search2StringId> res = query.fetch();
					
			assertEquals(1, res.size());
			assertEquals(discs[58], res.get(0));
		}
	}
	
	public void testSearchMultipleFieldsMultipleWordsFilterBefore() {
		if(supportsSearchStart() && supportsSearchEnd()){
			Discovery4Search2StringId[] discs = new Discovery4Search2StringId[100];
			for(int i=0; i<100; i++){
				discs[i] = new Discovery4Search2StringId("Disc_"+String.format("%03d", i), "Body_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
	
			Query<Discovery4Search2StringId> query = 
				pm.createQuery(Discovery4Search2StringId.class).filter("name", "Disc_058").search("Disc_005 Disc_010 Disc_058 Body_058 Body_068 Body_078", "name", "body").order("name");
			
			List<Discovery4Search2StringId> res = query.fetch();
					
			assertEquals(1, res.size());
			assertEquals(discs[58], res.get(0));
		}
	}
}
