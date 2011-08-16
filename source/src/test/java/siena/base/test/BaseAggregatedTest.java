package siena.base.test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import siena.Model;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.base.test.model.AggregateChildManualModel;
import siena.base.test.model.AggregateChildModel;
import siena.base.test.model.AggregateParentManualModel;
import siena.base.test.model.AggregateParentModel;

public abstract class BaseAggregatedTest extends TestCase {
	
	protected PersistenceManager pm;

	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(AggregateChildModel.class);
		classes.add(AggregateParentModel.class);
		
		pm = createPersistenceManager(classes);
		PersistenceManagerFactory.install(pm, classes);
			
		for (Class<?> clazz : classes) {
			if(!Modifier.isAbstract(clazz.getModifiers())){
				pm.createQuery(clazz).delete();			
			}
		}

	}
	
	public void testAggregateMostSimple() {
		AggregateParentModel god = new AggregateParentModel("god");
		god.insert();
		assertNotNull(god.id);
		
		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		adam1.aggregate(god, "child");
		adam1.insert();
		
		assertNotNull(adam1.id);
		assertNotNull(adam1.getRelation());

		AggregateParentModel god1 = 
			Model.getByKey(AggregateParentModel.class, god.id);
		assertNotNull(god1);
		assertEquals(adam1, god1.child.get());
		
		adam1.name = "adam1_UPD";
		adam1.update();
		
		assertEquals(adam1, god1.child.forceSync().get());
		
		adam1.name = "adam1_UPD2";
		adam1.save();
		
		assertEquals(adam1, god1.child.forceSync().get());
		
		AggregateChildModel adam2 = new AggregateChildModel("adam2");
		adam2.aggregate(god, "child");
		adam2.save();
		
		assertEquals(adam1, god1.child.forceSync().get());
		
		god1.child.set(adam2);
		god1.update();

		AggregateParentModel god2 = 
			Model.getByKey(AggregateParentModel.class, god1.id);
		assertNotNull(god2);
		assertEquals(adam2, god2.child.get());
		
		AggregateChildModel adam3 = new AggregateChildModel("adam3");
		adam3.aggregate(god, "child");
		adam3.save();
		
		god.child.set(adam3);
		god.update();
		
		AggregateParentModel god3 = 
			Model.getByKey(AggregateParentModel.class, god.id);
		assertNotNull(god3);
		assertEquals(adam2, god3.child.get());
		
		god.child.forceSync().set(adam3);
		god.update();
		
		god3 = 
			Model.getByKey(AggregateParentModel.class, god.id);
		assertNotNull(god3);
		assertEquals(adam3, god3.child.get());
		
		adam3.delete();
		assertNull(god3.child.forceSync().get());
	}

	public void testAggregateMostSimpleMultiple() {
		AggregateParentModel god = new AggregateParentModel("god");
		god.insert();
		assertNotNull(god.id);

		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		adam1.aggregate(god, "child");
		adam1.insert();
		
		AggregateChildModel adam2 = new AggregateChildModel("adam2");	
		adam2.aggregate(god, "children");
		adam2.insert();

		AggregateChildModel eve = new AggregateChildModel("eve");
		eve.aggregate(god, "children");
		eve.insert();

		AggregateChildModel bob = new AggregateChildModel("bob");
		bob.aggregate(god, "children");
		bob.insert();

		AggregateParentModel god1 = 
			Model.getByKey(AggregateParentModel.class, god.id);
		
		assertNotNull(god1);
		assertEquals(adam1, god1.child.get());
		List<AggregateChildModel> children = god1.children.asQuery().fetch();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));
		
		eve.delete();
		children = god1.children.asList().forceSync();
		assertEquals(adam2, children.get(0));
		assertEquals(bob, children.get(1));
	}
	
	public void testAggregate() {
		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		AggregateChildModel adam2 = new AggregateChildModel("adam2");	
		AggregateChildModel eve = new AggregateChildModel("eve");
		AggregateChildModel bob = new AggregateChildModel("bob");

		AggregateParentModel god = new AggregateParentModel("god");
		god.child.set(adam1);
		god.children.asList().addAll(adam2, eve, bob);				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		AggregateParentModel god1 = 
			Model.getByKey(AggregateParentModel.class, god.id);
		
		assertNotNull(god1);
		assertEquals(adam1, god1.child.get());
		List<AggregateChildModel> children = god1.children.asQuery().fetch();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));
		
		// get aggregated one2one
		AggregateChildModel adamAfter2 = AggregateChildModel.all().aggregated(god, "child").get();
		assertEquals(adam1, adamAfter2);
		
		// get aggregated one2many
		children = AggregateChildModel.all().aggregated(god, "children").fetch();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));

		AggregateParentModel god2 = AggregateParentModel.all().filter("name", "god").get();
		assertEquals(adam1, god2.child.get());
		children = god2.children.asList();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));
	}
	
	public void testAggregateUpdate() {
		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		AggregateChildModel adam2 = new AggregateChildModel("adam2");	
		AggregateChildModel eve = new AggregateChildModel("eve");
		AggregateChildModel bob = new AggregateChildModel("bob");

		AggregateParentModel god = new AggregateParentModel("god");
		god.child.set(adam1);
		god.children.asList().addAll(Arrays.asList(adam2, eve, bob));
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		god.name = "goddy";
		adam1.name = "adammy";
		adam1.save();
		bob.name = "bobby";
		bob.save();
		eve.name = "evvy";
		eve.save();
		
		god.update();
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "goddy").get();
		assertEquals(god, godbis);
		List<AggregateChildModel> children = godbis.children.asList();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));
		
		god.children.asList().remove(eve);
		god.update();
		
		godbis = AggregateParentModel.all().filter("name", "goddy").get();
		assertEquals(god, godbis);
		children = godbis.children.asList();
		assertEquals(adam2, children.get(0));
		assertEquals(bob, children.get(1));
	}
	
	public void testAggregateSave() {
		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		AggregateChildModel adam2 = new AggregateChildModel("adam2");	
		AggregateChildModel eve = new AggregateChildModel("eve");
		AggregateChildModel bob = new AggregateChildModel("bob");

		AggregateParentModel god = new AggregateParentModel("god");
		god.child.set(adam1);
		god.children.asList().addAll(Arrays.asList(adam2, eve, bob));
				
		god.save();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		AggregateParentModel god1 = 
			Model.getByKey(AggregateParentModel.class, god.id);
		
		assertNotNull(god1);
		assertEquals(adam1, god1.child.get());
		List<AggregateChildModel> children = god1.children.asQuery().fetch();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));

		god.name = "goddy";
		adam1.name = "adammy";
		bob.name = "bobby";
		eve.name = "evvy";
		
		Model.batch(AggregateChildModel.class).update(adam1, bob, eve);
		
		god.save();
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "goddy").get();
		assertNotNull(godbis);
		assertEquals(god.id, godbis.id);
		assertEquals(god.name, godbis.name);
		children = godbis.children.asList();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));
		
		god.children.asList().remove(eve);
		god.save();
		
		godbis = AggregateParentModel.all().filter("name", "goddy").get();
		assertEquals(god, godbis);
		children = godbis.children.asList();
		assertEquals(adam2, children.get(0));
		assertEquals(bob, children.get(1));
	}
	
	public void testAggregateDelete() {
		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		AggregateChildModel adam2 = new AggregateChildModel("adam2");	
		AggregateChildModel eve = new AggregateChildModel("eve");
		AggregateChildModel bob = new AggregateChildModel("bob");

		AggregateParentModel god = new AggregateParentModel("god");
		god.child.set(adam1);
		god.children.asList().addAll(Arrays.asList(adam2, eve, bob));
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		god.delete();
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		assertNull(godbis);
		
		List<AggregateChildModel> children = Model.batch(AggregateChildModel.class).getByKeys(adam1.id, adam2.id, eve.id, bob.id);
		assertEquals(4, children.size());
		assertEquals(null, children.get(0));
		assertEquals(null, children.get(1));
		assertEquals(null, children.get(2));
		assertEquals(null, children.get(3));
	}
	
	public void testAggregateDeleteChildOfChildren() {
		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		AggregateChildModel adam2 = new AggregateChildModel("adam2");	
		AggregateChildModel eve = new AggregateChildModel("eve");
		AggregateChildModel bob = new AggregateChildModel("bob");

		AggregateParentModel god = new AggregateParentModel("god");
		god.child.set(adam1);
		god.children.asList().addAll(Arrays.asList(adam2, eve, bob));
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		god.children.asList().remove(eve);
		god.update();
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		assertEquals(god, godbis);
		List<AggregateChildModel> children = godbis.children.asList();
		assertEquals(adam2, children.get(0));
		assertEquals(bob, children.get(1));
	}
	
	public void testAggregateAddChild() {
		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		AggregateChildModel adam2 = new AggregateChildModel("adam2");	
		AggregateChildModel eve = new AggregateChildModel("eve");
		AggregateChildModel bob = new AggregateChildModel("bob");
		AggregateChildModel eve2 = new AggregateChildModel("eve2");

		AggregateParentModel god = new AggregateParentModel("god");
		god.child.set(adam1);
		god.children.asList().addAll(Arrays.asList(adam2, eve, bob));
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		god.children.asList().add(eve2);
		god.update();
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		assertEquals(god, godbis);
		List<AggregateChildModel> children = godbis.children.asList();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));
		assertEquals(eve2, children.get(3));
	}
	
	public void testAggregateListQuerysFetch() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel("eve");

		AggregateParentModel god = new AggregateParentModel("god");
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		List<AggregateChildModel> children = godbis.children.asQuery().fetch();
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i), children.get(i));			
		}
		
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i), godbis.children.asList().get(i));			
		}
		
		Iterator<AggregateChildModel> it = godbis.children.asList().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(adams.get(i++), it.next());	
		}
		assertEquals(100, i);
	}
	
	public void testAggregateListQuerysFetchLimit() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		List<AggregateChildModel> children_0_10 = godbis.children.asQuery().fetch(10);
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i), children_0_10.get(i));			
		}
		
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i), godbis.children.asList().get(i));			
		}
		
		Iterator<AggregateChildModel> it = godbis.children.asList().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(adams.get(i++), it.next());	
		}
		assertEquals(100, i);
	}
	
	public void testAggregateListQuerysFetchLimitOffset() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		List<AggregateChildModel> children_26_36 = godbis.children.asQuery().fetch(10, 26);
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i+26), children_26_36.get(i));			
		}
		
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i), godbis.children.asList().get(i));			
		}
		
		Iterator<AggregateChildModel> it = godbis.children.asList().iterator();
		int i=0;
		while(it.hasNext()){
			assertEquals(adams.get(i++), it.next());	
		}
		assertEquals(100, i);
	}
	
	public void testAggregateListQuerysFetchKeys() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		List<AggregateChildModel> children = godbis.children.asQuery().fetchKeys();
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i).id, children.get(i).id);			
			assertTrue(children.get(i).name == null);			
		}
		
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i), godbis.children.asList().get(i));			
		}
		
		Iterator<AggregateChildModel> it = godbis.children.asList().iterator();
		int i=0;
		while(it.hasNext()){
			AggregateChildModel child = it.next();
			assertEquals(adams.get(i++), child);	
		}
		assertEquals(100, i);
	}
	
	public void testAggregateListQuerysFetchKeysLimit() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		List<AggregateChildModel> children = godbis.children.asQuery().fetchKeys(10);
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i).id, children.get(i).id);			
			assertTrue(children.get(i).name == null);			
		}
		
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i).id, godbis.children.asList().get(i).id);			
		}
		
		Iterator<AggregateChildModel> it = godbis.children.asList().iterator();
		int i=0;
		while(it.hasNext()){
			AggregateChildModel child = it.next();
			assertEquals(adams.get(i++).id, child.id);	
		}
		assertEquals(100, i);
	}
	
	public void testAggregateListQuerysFetchKeysLimitOffset() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		List<AggregateChildModel> children = godbis.children.asQuery().fetchKeys(10, 26);
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i+26).id, children.get(i).id);			
			assertTrue(children.get(i).name == null);			
		}
		
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i).id, godbis.children.asList().get(i).id);			
		}
		
		Iterator<AggregateChildModel> it = godbis.children.asList().iterator();
		int i=0;
		while(it.hasNext()){
			AggregateChildModel child = it.next();
			assertEquals(adams.get(i++).id, child.id);	
		}
		assertEquals(100, i);
	}
	
	public void testAggregateListQueryDelete() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		int nb = godbis.children.asQuery().delete();
		assertEquals(100, nb);			
		assertTrue( godbis.children.asList().isEmpty());			
		
		assertTrue( godbis.children.asQuery().fetch().isEmpty());			
		
		List<AggregateChildModel> children = AggregateChildModel.all().aggregated(god, "children").fetch();
		assertTrue(children.isEmpty());
	}
	
	public void testAggregateListQueryGet() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		assertEquals( adams.get(0), godbis.children.asQuery().get());			
		assertEquals( adams.get(0), godbis.children.asList().get(0));			
		assertEquals( adams.get(0), godbis.children.asQuery().get());			

	}
	
	public void testAggregateListQueryCount() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		// get aggregated one2many
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		assertEquals( adams.size(), godbis.children.asQuery().count());			
		assertEquals( adams.size(), godbis.children.asList().size());			
		assertEquals( adams.size(), godbis.children.asQuery().count());			

	}
	
	public void testAggregateListQueryFilter() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		AggregateChildModel adam47 = godbis.children.asQuery().filter("name", "adam47").get();
		assertEquals( adams.get(47), adam47);		

		List<AggregateChildModel> adamsbis = 
			godbis.children.asQuery().filter("name>", "adam47").filter("name<", "adam5").fetch();
		assertEquals( adams.get(48), adamsbis.get(0));		
		assertEquals( adams.get(49), adamsbis.get(1));		
	}
	
	public void testAggregateListQueryOrder() {
		List<AggregateChildModel> adams = new ArrayList<AggregateChildModel>();
		for(int i=0; i<100; i++){
			AggregateChildModel adam = new AggregateChildModel();
			adam.name = "adam"+i;
			adams.add(adam);
		}
		
		AggregateChildModel eve = new AggregateChildModel();
		eve.name = "eve";

		AggregateParentModel god = new AggregateParentModel();
		god.name = "god";
		god.child.set(eve);
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.get().id);
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();

		List<AggregateChildModel> adamsbis = 
			godbis.children.asQuery().filter("name>=", "adam41").filter("name<=", "adam49").order("-name").fetch();
		assertEquals( adams.get(49), adamsbis.get(0));		
		assertEquals( adams.get(48), adamsbis.get(1));		
		assertEquals( adams.get(47), adamsbis.get(2));		
		assertEquals( adams.get(46), adamsbis.get(3));		
		assertEquals( adams.get(45), adamsbis.get(4));		
		assertEquals( adams.get(44), adamsbis.get(5));		
		assertEquals( adams.get(43), adamsbis.get(6));		
		assertEquals( adams.get(42), adamsbis.get(7));		
		assertEquals( adams.get(41), adamsbis.get(8));		
	}
	
	public void testAggregateMostSimpleManual() {
		AggregateParentManualModel god = new AggregateParentManualModel("god");
		god.insert();
		assertNotNull(god.id);
		
		AggregateChildManualModel adam1 = new AggregateChildManualModel(123L, "adam1");
		adam1.aggregate(god, "child");
		adam1.insert();
		
		AggregateChildManualModel adam2 = new AggregateChildManualModel(0L, "adam2");	
		adam2.aggregate(god, "children");
		adam2.insert();

		AggregateChildManualModel eve = new AggregateChildManualModel(1L, "eve");
		eve.aggregate(god, "children");
		eve.insert();

		AggregateChildManualModel bob = new AggregateChildManualModel(2L, "bob");
		bob.aggregate(god, "children");
		bob.insert();

		
		AggregateChildManualModel adam1After = AggregateChildManualModel.all().aggregated(god, "child").get();
		assertEquals(adam1, adam1After);

		List<AggregateChildManualModel> children = AggregateChildManualModel.all().aggregated(god, "children").fetch();		
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));
		
		AggregateChildManualModel adam1After2 = AggregateChildManualModel.all().aggregated(god, "child").filter("id", 123L).get();		
		assertEquals(adam1, adam1After2);

		AggregateChildManualModel eveAfter = AggregateChildManualModel.all().aggregated(god, "children").filter("id", 1L).get();		
		assertEquals(eve, eveAfter);
		
		eveAfter.name+="_UPD";
		eveAfter.update();
		AggregateChildManualModel eveAfter2 = AggregateChildManualModel.all().aggregated(god, "children").filter("id", 1L).get();
		assertEquals(eveAfter, eveAfter2);
		
		eveAfter2.name+="_UPD2";
		eveAfter2.save();
		AggregateChildManualModel eveAfter3 = AggregateChildManualModel.all().aggregated(god, "children").filter("id", 1L).get();
		assertEquals(eveAfter2, eveAfter3);
	}
	
	public void testAggregateDeleteParentFromQuery() {
		AggregateParentManualModel god = new AggregateParentManualModel("god");
		god.insert();
		assertNotNull(god.id);
		
		AggregateChildManualModel adam1 = new AggregateChildManualModel(123L, "adam1");
		adam1.aggregate(god, "child");
		adam1.insert();
		
		AggregateChildManualModel adam2 = new AggregateChildManualModel(0L, "adam2");	
		adam2.aggregate(god, "children");
		adam2.insert();

		AggregateChildManualModel eve = new AggregateChildManualModel(1L, "eve");
		eve.aggregate(god, "children");
		eve.insert();

		AggregateChildManualModel bob = new AggregateChildManualModel(2L, "bob");
		bob.aggregate(god, "children");
		bob.insert();
		
		int nb = AggregateParentManualModel.all().filter("name", "god").delete();
		assertEquals(5, nb);
		
		assertEquals(0, AggregateChildManualModel.all().aggregated(god, "children").count());
		assertEquals(0, AggregateChildManualModel.all().aggregated(god, "child").count());
		
	}
}
