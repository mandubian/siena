package siena.base.test;

import java.util.Iterator;
import java.util.List;

import siena.Query;
import siena.base.test.model.DiscoveryStringId;
import siena.sdb.SdbPersistenceManager;

public abstract class BaseTestNoAutoInc_5_PAGINATE extends BaseTestNoAutoInc_BASE {
	public void postInit() {
		//pm.createQuery(PersonStringID.class).delete();
		pm.createQuery(DiscoveryStringId.class).delete();

		//pm.insert(StringID_TESLA, StringID_CURIE, StringID_EINSTEIN);
		
		pm.option(SdbPersistenceManager.CONSISTENT_READ);

	}		

	public void testFetchPaginateStatelessNextPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
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
	}
	
	public void testFetchPaginateStatelessNextPageToEnd() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
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
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());

		res = query.nextPage().fetch();
		assertEquals(0, res.size());

		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatelessPreviousPageFromScratch() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		List<DiscoveryStringId> res = query.previousPage().fetch();
		assertEquals(0, res.size());

		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(0, res.size());
		
		res = query.previousPage().fetch();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatelessPreviousPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		List<DiscoveryStringId> res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateStatelessSeveralTimes() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
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
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	
	public void testFetchPaginateStatefulNextPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
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
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatefulNextPageToEnd() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name").stateful();
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
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());

		res = query.nextPage().fetch();
		assertEquals(0, res.size());

		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.nextPage().fetch();
		assertEquals(0, res.size());
	}	
	
	public void testFetchPaginateStatefulPreviousPageFromScratch() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
		List<DiscoveryStringId> res = query.previousPage().fetch();
		assertEquals(0, res.size());

		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(0, res.size());
		
		res = query.previousPage().fetch();
		assertEquals(0, res.size());
	}
	
	public void testFetchPaginateStatefulPreviousPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
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
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulPreviouPageSeveralTimes() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
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
		
		res = query.nextPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5], res.get(i));
		}
		
		res = query.previousPage().fetch();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchKeysPaginateStatelessNextPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
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
	}
	
	public void testFetchKeysPaginateStatelessPreviousPageFromScratch() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		List<DiscoveryStringId> res = query.previousPage().fetchKeys();
		assertEquals(0, res.size());

		res = query.previousPage().fetchKeys();
		assertEquals(0, res.size());
	}
	
	public void testFetchKeysPaginateStatelessPreviousPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		List<DiscoveryStringId> res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatelessPreviouPageSeveralTimes() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
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
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatefulNextPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
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
	}

	
	public void testFetchKeysPaginateStatefulPreviousPageFromScratch() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
		List<DiscoveryStringId> res = query.previousPage().fetchKeys();
		assertEquals(0, res.size());

		res = query.previousPage().fetchKeys();
		assertEquals(0, res.size());
	}
	
	public void testFetchKeysPaginateStatefulPreviousPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
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
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].name, res.get(i).name);
			assertTrue(res.get(i).isOnlyIdFilled());
		}
	}
	
	public void testFetchKeysPaginateStatefulSeveralTimes() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
		List<DiscoveryStringId> res = query.fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].name, res.get(i).name);
		}		
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].name, res.get(i).name);
		}
		
		res = query.nextPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+10].name, res.get(i).name);
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i+5].name, res.get(i).name);
		}
		
		res = query.previousPage().fetchKeys();
		assertEquals(5, res.size());
		for(int i=0; i<5; i++){
			assertEquals(discs[i].name, res.get(i).name);
		}
	}
	
	public void testIterPaginateStatelessNextPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		try {
			Iterable<DiscoveryStringId> res = query.iter();
			Iterator<DiscoveryStringId> it = res.iterator();
			int i=0;
			while(it.hasNext()){
				assertEquals(discs[i++], it.next());
			}
			assertEquals(5, i);

			res = query.nextPage().iter();
			it = res.iterator();
			while(it.hasNext()){
				assertEquals(discs[i++], it.next());
			}
			assertEquals(10, i);
		}finally {
			query.release();
		}
	}

	public void testIterPaginateStatelessPreviousPageFromScratch() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		Iterable<DiscoveryStringId> iter = query.previousPage().iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(0, i);

		iter = query.previousPage().iter();
		i=0;
		it = iter.iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(0, i);

	}
	
	public void testIterPaginateStatelessPreviousPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		Iterable<DiscoveryStringId> iter = query.nextPage().iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=5;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(10, i);

		iter = query.previousPage().iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(5, i);

	}
	
	public void testIterPaginateStatelessPreviouPageSeveralTimes() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).order("name");
		Iterable<DiscoveryStringId> iter = query.iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(5, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		assertEquals(10, i);
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(15, i);
	
		iter = query.previousPage().iter();
		it = iter.iterator();
		i=5;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(10, i);

		iter = query.previousPage().iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}
		assertEquals(5, i);

	}
	
	public void testIterPaginateStatefulNextPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			assertEquals(discs[i++], it.next());
		}	
		
		assertEquals(10, i);
	}
	

	
	public void testIterPaginateStatefulPreviousPageFromScratch() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
		Iterable<DiscoveryStringId> iter = query.previousPage().iter();
		Iterator<DiscoveryStringId> it = iter.iterator();
		int i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(0, i);

		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(0, i);
	}
	
	public void testIterPaginateStatefulPreviousPage() {
		DiscoveryStringId[] discs = new DiscoveryStringId[10];
		for(int i=0; i<10; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
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
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);
		
		iter = query.previousPage().iter();
		it = iter.iterator();
		i=0;
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
	}
	
	
	public void testIterPaginateStatefulPreviouPageSeveralTimes() {
		DiscoveryStringId[] discs = new DiscoveryStringId[15];
		for(int i=0; i<15; i++){
			discs[i] = new DiscoveryStringId("Disc_"+String.format("%02d", i), StringID_CURIE);
		}
		pm.insert((Object[])discs);

		Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).paginate(5).stateful().order("name");
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
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);	
		
		iter = query.nextPage().iter();
		it = iter.iterator();
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(15, i);	
		
		iter = query.previousPage().iter();
		i=5;
		it = iter.iterator();
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(10, i);	
		
		iter = query.previousPage().iter();
		i=0;
		it = iter.iterator();
		while(it.hasNext()){
			DiscoveryStringId disc = it.next();
			assertEquals(discs[i++], disc);
		}	
		assertEquals(5, i);
	}
}
