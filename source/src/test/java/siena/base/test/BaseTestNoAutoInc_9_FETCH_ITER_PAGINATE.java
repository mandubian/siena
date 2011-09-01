package siena.base.test;

import java.util.Iterator;
import java.util.List;

import siena.Query;
import siena.SienaException;
import siena.base.test.model.DiscoveryStringId;

public abstract class BaseTestNoAutoInc_9_FETCH_ITER_PAGINATE extends BaseTestNoAutoInc_BASE {
	
	public void testLimitStateless(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateless().order("name");
		List<DiscoveryStringId> res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.paginate(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testLimitStateful(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		List<DiscoveryStringId> res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.fetch(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		res = query.paginate(50).fetch(25);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}
	}

	public void testOffsetStateless(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		List<DiscoveryStringId> res = query.offset(50).fetch();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}

	public void testOffsetStateful(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		List<DiscoveryStringId> res = query.offset(50).fetch();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testOffsetLimitStateless(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		List<DiscoveryStringId> res = query.offset(50).limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testOffsetLimitStateful(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		List<DiscoveryStringId> res = query.offset(50).limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}


	public void testOffsetLimitStatelessPaginate(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		List<DiscoveryStringId> res = query.paginate(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		res = query.offset(50).fetch();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		res = query.offset(50).limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

	}
	
	public void testOffsetLimitStatefulPaginate(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name").stateful();
		List<DiscoveryStringId> res = query.paginate(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		res = query.limit(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		res = query.offset(50).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+150], res.get(i));
		}
		
		res = query.offset(50).limit(25).fetch();
		assertEquals(25, res.size());
		for(int i=0; i<25; i++){
			assertEquals(discs[i+250], res.get(i));
		}
		try {
			res = query.previousPage().fetch();
		}catch(SienaException ex){
			return;
		}
		fail();
	}
	
	public void testOffsetLimitStatelessPaginate2(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		List<DiscoveryStringId> res = query.limit(50).offset(12).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		res = query.offset(13).limit(30).fetch();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+13], res.get(i));
		}
		
		res = query.offset(10).limit(30).fetch(15);
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		res = query.paginate(6).fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6], res.get(i));
		}
		
		res = query.offset(10).fetch(10);
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		try {
			res = query.nextPage().fetch();
		}catch(SienaException ex){
			res = query.paginate(8).fetch();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i], res.get(i));
			}
			
			res = query.nextPage().fetch();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+8], res.get(i));
			}
			
			return;
		}
		fail();
		
	}
	
	public void testOffsetLimitStatefulPaginate2(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name").stateful();
		List<DiscoveryStringId> res = query.limit(50).offset(12).fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12], res.get(i));
		}
		
		res = query.offset(13).limit(30).fetch();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+75], res.get(i));
		}
		
		res = query.offset(10).limit(30).fetch(15);
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+115], res.get(i));
		}
		
		res = query.paginate(6).fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+130], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+142], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136], res.get(i));
		}
		
		res = query.offset(10).fetch(10);
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+146], res.get(i));
		}
		
		try {
			res = query.nextPage().fetch();
		}catch(SienaException ex){
			res = query.paginate(8).fetch();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+156], res.get(i));
			}
			
			res = query.nextPage().fetch();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+164], res.get(i));
			}
		}
	}
	
	public void testFetchPaginateStatelessTwice() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		List<DiscoveryStringId> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.paginate(8).fetch();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(7, res.size());
		for(int i=0; i<7; i++){
			assertEquals(discs[i+8], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulTwice() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().paginate(5).order("name");
		List<DiscoveryStringId> res = query.fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}		
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.paginate(8).fetch();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(2, res.size());
		for(int i=0; i<2; i++){
			assertEquals(discs[i+13], res.get(i));
		}
	}
	
	
	public void testLimitStatelessKeys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateless().order("name");
		List<DiscoveryStringId> res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.fetchKeys(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testLimitStatefulKeys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		List<DiscoveryStringId> res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.fetchKeys(50);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(50).fetchKeys(25);
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testOffsetStatelessKeys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		List<DiscoveryStringId> res = query.offset(50).fetchKeys();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}

	public void testOffsetStatefulKeys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		List<DiscoveryStringId> res = query.offset(50).fetchKeys();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testOffsetLimitStatelessKeys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		List<DiscoveryStringId> res = query.offset(50).limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}

	public void testOffsetLimitStatefulKeys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		List<DiscoveryStringId> res = query.offset(50).limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}

	
	public void testOffsetLimitStatelessPaginateKeys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		List<DiscoveryStringId> res = query.paginate(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.offset(50).fetchKeys();
		assertEquals(100, res.size());
		for(int i=0; i<100; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(50).limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

	}
	
	public void testOffsetLimitStatefulPaginateKeys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name").stateful();
		List<DiscoveryStringId> res = query.paginate(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.limit(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}

		res = query.offset(50).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+150].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(50).limit(25).fetchKeys();
		assertEquals(25, res.size());
		for(int i=0; i<25; i++){
			assertEquals(discs[i+250].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		try {
			res = query.previousPage().fetch();
		}catch(SienaException ex){
			return;
		}
		fail();
	}
	
	public void testOffsetLimitStatelessPaginate2Keys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		List<DiscoveryStringId> res = query.limit(50).offset(12).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(13).limit(30).fetchKeys();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+13].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(10).limit(30).fetchKeys(15);
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+10].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(6).fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+12].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+6].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(10).fetchKeys(10);
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+10].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		try {
			res = query.nextPage().fetchKeys();
		}catch(SienaException ex){
			res = query.paginate(8).fetch();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i].name, res.get(i).name);
			}
			
			res = query.nextPage().fetchKeys();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+8].name, res.get(i).name);
			}
		}
		
	}
	
	public void testOffsetLimitStatefulPaginate2Keys(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name").stateful();
		List<DiscoveryStringId> res = query.limit(50).offset(12).fetchKeys();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+12].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(13).limit(30).fetchKeys();
		assertEquals(30, res.size());
		for(int i=0; i<30; i++){
			assertEquals(discs[i+75].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(10).limit(30).fetchKeys(15);
		assertEquals(15, res.size());
		for(int i=0; i<15; i++){
			assertEquals(discs[i+115].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(6).fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+130].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+142].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(6, res.size());
		for(int i=0; i<6; i++){
			assertEquals(discs[i+136].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.offset(10).fetchKeys(10);
		assertEquals(10, res.size());
		for(int i=0; i<10; i++){
			assertEquals(discs[i+146].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		try {
			res = query.nextPage().fetchKeys();
		}catch(SienaException ex){
			res = query.paginate(8).fetchKeys();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+156].name, res.get(i).name);
				assertTrue(res.get(i).isOnlyIdFilled());
			}
			
			res = query.nextPage().fetchKeys();
			assertEquals(8, res.size());
			for(int i=0; i<8; i++){
				assertEquals(discs[i+164].name, res.get(i).name);
				assertTrue(res.get(i).isOnlyIdFilled());
			}
		}
	}
	
	public void testFetchPaginateStatelessTwiceKeys() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		List<DiscoveryStringId> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}		
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(8).fetchKeys();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(7, res.size());
		for(int i=0; i<7; i++){
			assertEquals(discs[i+8].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchPaginateStatefulTwiceKeys() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().paginate(5).order("name");
		List<DiscoveryStringId> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}		
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.paginate(8).fetchKeys();
		assertEquals(8, res.size());
		for(int i=0; i<8; i++){
			assertEquals(discs[i+5].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(2, res.size());
		for(int i=0; i<2; i++){
			assertEquals(discs[i+13].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testLimitStatelessIter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateless().order("name");
		Iterable<DiscoveryStringId> iter = query.limit(50).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
		
		iter = query.limit(50).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	
		
		iter = query.iter(50);
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);	

		iter = query.paginate(50).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
	}
	
	public void testLimitStatefulIter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.limit(50).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
		
		iter = query.iter(50);
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);
		
		iter = query.paginate(50).iter(25);
		it = iter.iterator();
		i=100;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
	}
	
	public void testOffsetStatelessIter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		Iterable<DiscoveryStringId> iter = query.offset(50).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
	}
	
	public void testOffsetStatefulIter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.offset(50).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
	}
	
	public void testOffsetLimitStatelessIter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		Iterable<DiscoveryStringId> iter = query.offset(50).limit(50).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);
	}
	
	public void testOffsetLimitStatefulIter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.offset(50).limit(50).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);
	}
	
	public void testOffsetLimitStatelessPaginateIter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[150];
		for(int i=0; i<150; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		Iterable<DiscoveryStringId> iter = query.paginate(50).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		iter = query.limit(50).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(50, i);

		iter = query.offset(50).iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(150, i);
		
		iter = query.offset(50).limit(50).iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

	}
	
	public void testOffsetLimitStatefulPaginateIter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
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
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		iter = query.limit(50).iter();
		it = iter.iterator();
		i=50;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(100, i);

		iter = query.offset(50).iter();
		it = iter.iterator();
		i=150;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(200, i);
		
		iter = query.offset(50).limit(25).iter();
		it = iter.iterator();
		i=250;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(275, i);

		try {
			iter = query.previousPage().iter();
		}catch(SienaException ex){
			return;
		}
		fail();
	}
	
	public void testOffsetLimitStatelessPaginate2Iter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		Iterable<DiscoveryStringId> iter = query.limit(50).offset(12).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=12;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(62, i);
		
		iter = query.offset(13).limit(30).iter();
		it = iter.iterator();
		i=13;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(43, i);
		
		iter = query.offset(10).limit(30).iter(15);
		it = iter.iterator();
		i=10;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(25, i);

		iter = query.paginate(6).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(6, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=6;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(12, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=12;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(18, i);
		
		iter = query.previousPage().iter();
		it = iter.iterator();
		i=6;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(12, i);
		
		iter = query.offset(10).iter(10);
		it = iter.iterator();
		i=10;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(20, i);
		
		try {
			iter = query.nextPage().iter();
		}catch(SienaException ex){
			iter = query.paginate(8).iter();
			it = iter.iterator();
			i=0;
			while(it.hasNext()){
				DiscoveryStringId disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(8, i);
			
			iter = query.nextPage().iter();
			it = iter.iterator();
			i=8;
			while(it.hasNext()){
				DiscoveryStringId disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(16, i);
			
			return;
		}
		fail();
		
	}
	
	public void testOffsetLimitStatefulPaginate2Iter(){
		DiscoveryStringId[] discs = new DiscoveryStringId[300];
		for(int i=0; i<300; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.limit(50).offset(12).iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=12;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(62, i);
		
		iter = query.offset(13).limit(30).iter();
		it = iter.iterator();
		i=75;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(105, i);
		
		iter = query.offset(10).limit(30).iter(15);
		it = iter.iterator();
		i=115;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(130, i);

		iter = query.paginate(6).iter();
		it = iter.iterator();
		i=130;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(136, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=136;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(142, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=142;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(148, i);
		
		iter = query.previousPage().iter();
		it = iter.iterator();
		i=136;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(142, i);
		
		iter = query.offset(10).iter(10);
		it = iter.iterator();
		i=146;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(156, i);
		
		try {
			iter = query.nextPage().iter();
		}catch(SienaException ex){
			iter = query.paginate(8).iter();
			it = iter.iterator();
			i=156;
			while(it.hasNext()){
				DiscoveryStringId disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(164, i);
			
			iter = query.nextPage().iter();
			it = iter.iterator();
			i=164;
			while(it.hasNext()){
				DiscoveryStringId disc = it.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(172, i);
			
			return;
		}
		fail();
		
	}
	
	public void testFetchPaginateStatelessTwiceIter() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		Iterable<DiscoveryStringId> iter = query.iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		iter = query.paginate(8).iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(8, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=8;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);
	}
	
	public void testFetchPaginateStatefulTwiceIter() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().paginate(5).order("name");
		Iterable<DiscoveryStringId> iter = query.iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		iter = query.paginate(8).iter();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(13, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		i=13;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);
	}
	
	
	
	
	public void testIterPerPageStateless(){
		DiscoveryStringId[] discs = new DiscoveryStringId[500];
		for(int i=0; i<500; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		Iterable<DiscoveryStringId> iter = query.iterPerPage(50);
		int i=0;
		for(DiscoveryStringId disc: iter){
			assertEquals(discs[i++], disc);
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateless2(){
		DiscoveryStringId[] discs = new DiscoveryStringId[500];
		for(int i=0; i<500; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		Iterable<DiscoveryStringId> iter = query.iterPerPage(50);
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateless3(){
		DiscoveryStringId[] discs = new DiscoveryStringId[500];
		for(int i=0; i<500; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).order("name");
		Iterable<DiscoveryStringId> iter = query.offset(25).iterPerPage(50);
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=25;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateful(){
		DiscoveryStringId[] discs = new DiscoveryStringId[500];
		for(int i=0; i<500; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.iterPerPage(50);
		int i=0;
		for(DiscoveryStringId disc: iter){
			assertEquals(discs[i++], disc);
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStateful2(){
		DiscoveryStringId[] discs = new DiscoveryStringId[500];
		for(int i=0; i<500; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.iterPerPage(50);
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
	public void testIterPerPageStatefull3(){
		DiscoveryStringId[] discs = new DiscoveryStringId[500];
		for(int i=0; i<500; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);
		
		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.offset(25).iterPerPage(50);
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=25;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(500, i);	
	}
	
}
