package siena.base.test;

import java.util.Iterator;
import java.util.List;

import siena.Query;
import siena.base.test.model.DiscoveryStringId;

public abstract class BaseTestNoAutoInc_6_FETCH_ITER extends BaseTestNoAutoInc_BASE {

	public void testFetchIterLotsOfEntitiesStatefulMixed(){
			DiscoveryStringId[] discs = new DiscoveryStringId[150];
			for(int i=0; i<150; i++){
				discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
			
			Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
			List<DiscoveryStringId> res = query.fetch(50);
			assertEquals(50, res.size());
			for(int i=0; i<50; i++){
				assertEquals(discs[i], res.get(i));
			}
			
			Iterable<DiscoveryStringId> res2 = query.iter(50);
			Iterator<DiscoveryStringId> it2 = res2.iterator();
			int i=50;
			while(it2.hasNext()){
				DiscoveryStringId disc = it2.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(100, i);	
	
			res = query.paginate(25).fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+100], res.get(i));
			}
			
			res2 = query.nextPage().iter();
			it2 = res2.iterator();
			i=125;
			while(it2.hasNext()){
				DiscoveryStringId disc = it2.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(150, i);	
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+100], res.get(i));
			}
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+75], res.get(i));
			}
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+50], res.get(i));
			}
			
			res = query.nextPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+75], res.get(i));
			}
			
			res = query.nextPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+100], res.get(i));
			}
			
			res = query.nextPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+125], res.get(i));
			}
			
			res = query.nextPage().fetch();
			assertEquals(0, res.size());
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+125], res.get(i));
			}
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+100], res.get(i));
			}
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+75], res.get(i));
			}
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+50], res.get(i));
			}
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i+25], res.get(i));
			}
			
			res = query.previousPage().fetch();
			assertEquals(25, res.size());
			for(i=0; i<25; i++){
				assertEquals(discs[i], res.get(i));
			}
		}
		
		public void testFetchIterLotsOfEntitiesStatefulMixed2(){
			DiscoveryStringId[] discs = new DiscoveryStringId[200];
			for(int i=0; i<200; i++){
				discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
			
			Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
			List<DiscoveryStringId> res = query.fetch(50);
			assertEquals(50, res.size());
			for(int i=0; i<50; i++){
				assertEquals(discs[i], res.get(i));
			}
	
			Iterable<DiscoveryStringId> res2 = query.iter(50);
			Iterator<DiscoveryStringId> it2 = res2.iterator();
			int i=50;
			while(it2.hasNext()){
				DiscoveryStringId disc = it2.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(100, i);	
			
			res = query.fetch(50);
			assertEquals(50, res.size());
			for(i=0; i<50; i++){
				assertEquals(discs[i+100], res.get(i));
			}
			
			res2 = query.iter(50);
			it2 = res2.iterator();
			i=150;
			while(it2.hasNext()){
				DiscoveryStringId disc = it2.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(200, i);	
	
		}
		
		public void testFetchIterLotsOfEntitiesStatefulMixed3(){
			DiscoveryStringId[] discs = new DiscoveryStringId[150];
			for(int i=0; i<150; i++){
				discs[i] = new DiscoveryStringId("Disc_"+String.format("%03d", i), StringID_CURIE);
			}
			pm.insert((Object[])discs);
			
			Query<DiscoveryStringId> query = pm.createQuery(DiscoveryStringId.class).stateful().order("name");
			List<DiscoveryStringId> res = query.fetch(50);
			assertEquals(50, res.size());
			for(int i=0; i<50; i++){
				assertEquals(discs[i], res.get(i));
			}
			
			Iterable<DiscoveryStringId> res2 = query.iter();
			Iterator<DiscoveryStringId> it2 = res2.iterator();
			int i=50;
			while(it2.hasNext()){
				DiscoveryStringId disc = it2.next();
				assertEquals(discs[i++], disc);
			}	
			assertEquals(150, i);	
		}
	

	
	
}
