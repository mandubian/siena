package siena.base.test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.Model;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.Query;
import siena.base.test.model.RelatedManyChild;
import siena.base.test.model.RelatedSeveralQueryChild;
import siena.base.test.model.RelatedSeveralQueryNoAsChild;
import siena.base.test.model.RelatedManyParent;
import siena.base.test.model.RelatedSeveralQueryParent;
import siena.base.test.model.RelatedSeveralQueryNoAsParent;
import siena.base.test.model.RelatedSimpleOwnedParent;
import siena.base.test.model.RelatedSimpleOwnedChild;
import siena.base.test.model.RelatedSimpleReferencedChild;
import siena.base.test.model.RelatedSimpleReferencedParent;

public abstract class BaseRelatedTest extends TestCase {
	
	protected PersistenceManager pm;

	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(RelatedSimpleReferencedParent.class);
		classes.add(RelatedSimpleReferencedChild.class);
		
		pm = createPersistenceManager(classes);
		PersistenceManagerFactory.install(pm, classes);
			
		for (Class<?> clazz : classes) {
			if(!Modifier.isAbstract(clazz.getModifiers())){
				pm.createQuery(clazz).delete();			
			}
		}

	}

	public void testRelatedSimpleReference() {

		RelatedSimpleReferencedChild adam1 = new RelatedSimpleReferencedChild("adam1");
		adam1.insert();

		RelatedSimpleReferencedParent god = new RelatedSimpleReferencedParent("god");
		god.child = adam1;				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
		RelatedSimpleReferencedParent godbis = Model.getByKey(RelatedSimpleReferencedParent.class, god.id);
		RelatedSimpleReferencedChild adam1bis = Model.getByKey(RelatedSimpleReferencedChild.class, godbis.child.id);
		
		assertEquals(god, godbis);
		assertEquals(adam1, adam1bis);
	}
		
	public void testRelatedSimpleOwned() {
		RelatedSimpleOwnedChild adam1 = new RelatedSimpleOwnedChild("adam1");
		
		RelatedSimpleOwnedParent god = new RelatedSimpleOwnedParent("god");
		god.child.set(adam1);
		god.insert();

		assertNotNull(god.id);
		assertEquals(god.id, adam1.owner.id);
		
		RelatedSimpleOwnedParent godbis = Model.getByKey(RelatedSimpleOwnedParent.class, god.id);
		
		assertEquals(god, godbis);
		assertEquals(adam1, godbis.child.get());
	}
	
	public void testRelatedSimpleOwnedNull() {
		RelatedSimpleOwnedChild adam1 = new RelatedSimpleOwnedChild("adam1");
		
		RelatedSimpleOwnedParent god = new RelatedSimpleOwnedParent("god");
		god.child.set(null);
		god.insert();

		assertNotNull(god.id);
		assertNull(adam1.owner);
		
		RelatedSimpleOwnedParent godbis = Model.getByKey(RelatedSimpleOwnedParent.class, god.id);
		
		assertEquals(god, godbis);
		assertNull(godbis.child.get());
	}
	
	public void testRelatedSimpleOwnedUpdate() {
		RelatedSimpleOwnedChild adam1 = new RelatedSimpleOwnedChild("adam1");
		RelatedSimpleOwnedChild adam2 = new RelatedSimpleOwnedChild("adam2");
		
		RelatedSimpleOwnedParent god = new RelatedSimpleOwnedParent("god");
		god.child.set(adam1);
		god.insert();

		assertNotNull(god.id);
		assertEquals(god.id, adam1.owner.id);
		
		RelatedSimpleOwnedParent godbis = Model.getByKey(RelatedSimpleOwnedParent.class, god.id);
		
		god.child.set(adam2);
		god.update();

		RelatedSimpleOwnedParent godbis2 = Model.getByKey(RelatedSimpleOwnedParent.class, godbis.id);

		assertEquals(god, godbis2);
		assertEquals(adam2, godbis2.child.get());
	}
	
	public void testRelatedSeveralQuery() {
		RelatedSeveralQueryParent god = new RelatedSeveralQueryParent("god");
		god.insert();

		RelatedSeveralQueryChild adam1 = new RelatedSeveralQueryChild("adam1");
		adam1.owner = god;
		adam1.insert();
		
		RelatedSeveralQueryChild adam2 = new RelatedSeveralQueryChild("adam2");
		adam2.owner = god;
		adam2.insert();


		assertNotNull(god.id);
		assertEquals(god.id, adam1.owner.id);
		assertEquals(god.id, adam2.owner.id);
		
		RelatedSeveralQueryParent godbis = Model.getByKey(RelatedSeveralQueryParent.class, god.id);
		List<RelatedSeveralQueryChild> children = godbis.children.fetch();
		
		assertEquals(god, godbis);
		assertEquals(adam1, children.get(0));
		assertEquals(adam2, children.get(1));
	}
	
	public void testRelatedSeveralQueryLotsPaginate() {
		RelatedSeveralQueryParent god = new RelatedSeveralQueryParent("god");
		god.insert();

		List<RelatedSeveralQueryChild> adams = new ArrayList<RelatedSeveralQueryChild>();
		for(int i=0; i<100; i++){
			RelatedSeveralQueryChild adam = new RelatedSeveralQueryChild("adam"+i);
			adam.owner = god;
			adam.insert();
			adams.add(adam);
		}
			
		RelatedSeveralQueryParent godbis = Model.getByKey(RelatedSeveralQueryParent.class, god.id);
		assertEquals(god, godbis);
		Query<RelatedSeveralQueryChild> q = godbis.children.paginate(10);
		List<RelatedSeveralQueryChild> children = q.fetch();
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i), children.get(i));
		}
		
		children = q.nextPage().fetch();
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i+10), children.get(i));
		}

		children = q.nextPage().fetch();
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i+20), children.get(i));
		}

		children = q.previousPage().fetch();
		for(int i=0; i<10; i++){
			assertEquals(adams.get(i+10), children.get(i));
		}
	}
	
	public void testRelatedSeveralQueryNoAs() {
		RelatedSeveralQueryNoAsParent god = new RelatedSeveralQueryNoAsParent("god");
		god.insert();

		RelatedSeveralQueryNoAsChild adam1 = new RelatedSeveralQueryNoAsChild("adam1");
		adam1.owner = god;
		adam1.insert();
		
		RelatedSeveralQueryNoAsChild adam2 = new RelatedSeveralQueryNoAsChild("adam2");
		adam2.owner = god;
		adam2.insert();


		assertNotNull(god.id);
		assertEquals(god.id, adam1.owner.id);
		assertEquals(god.id, adam2.owner.id);
		
		RelatedSeveralQueryNoAsParent godbis = Model.getByKey(RelatedSeveralQueryNoAsParent.class, god.id);
		List<RelatedSeveralQueryNoAsChild> children = godbis.children.fetch();
		
		assertEquals(god, godbis);
		assertEquals(adam1, children.get(0));
		assertEquals(adam2, children.get(1));
	}
	
	public void testRelatedManyOldInsertWay() {
		RelatedManyParent god = new RelatedManyParent("god");
		god.insert();

		RelatedManyChild adam1 = new RelatedManyChild("adam1");
		adam1.owner = god;
		adam1.insert();
		
		RelatedManyChild adam2 = new RelatedManyChild("adam2");
		adam2.owner = god;
		adam2.insert();

		assertNotNull(god.id);
		assertEquals(god.id, adam1.owner.id);
		assertEquals(god.id, adam2.owner.id);
		
		// tries to forcesync on god
		god.children.asList().forceSync();
		assertEquals(adam1, god.children.asList().get(0));
		assertEquals(adam2, god.children.asList().get(1));
		
		RelatedManyParent godbis = Model.getByKey(RelatedManyParent.class, god.id);
		List<RelatedManyChild> children = godbis.children.asQuery().fetch();
		
		assertEquals(god, godbis);
		assertEquals(adam1, children.get(0));
		assertEquals(adam2, children.get(1));
		
		children = godbis.children.asList();
		assertEquals(adam1, children.get(0));
		assertEquals(adam2, children.get(1));
	}
	
	public void testRelatedManyCascadeInsert() {
		RelatedManyParent god = new RelatedManyParent("god");
		RelatedManyChild adam1 = new RelatedManyChild("adam1");
		RelatedManyChild adam2 = new RelatedManyChild("adam2");
		god.children.asList().add(adam1);
		god.children.asList().add(adam2);
		god.insert();

		assertNotNull(god.id);
		assertNotNull(adam1.id);
		assertNotNull(adam2.id);
		assertEquals(god.id, adam1.owner.id);
		assertEquals(god.id, adam2.owner.id);
		
		RelatedManyParent godbis = Model.getByKey(RelatedManyParent.class, god.id);
		List<RelatedManyChild> children = godbis.children.asQuery().fetch();
		
		assertEquals(god, godbis);
		assertEquals(adam1, children.get(0));
		assertEquals(adam2, children.get(1));
		
		children = godbis.children.asList();
		assertEquals(adam1, children.get(0));
		assertEquals(adam2, children.get(1));
	}
	
	public void testRelatedManyCascadeInsertFetch() {
		RelatedManyParent god = new RelatedManyParent("god");
		RelatedManyChild adam1 = new RelatedManyChild("adam1");
		RelatedManyChild adam2 = new RelatedManyChild("adam2");
		god.children.asList().add(adam1);
		god.children.asList().add(adam2);
		god.insert();

		assertNotNull(god.id);
		assertNotNull(adam1.id);
		assertNotNull(adam2.id);
		assertEquals(god.id, adam1.owner.id);
		assertEquals(god.id, adam2.owner.id);
		
		RelatedManyParent godbis = Model.all(RelatedManyParent.class).filter("name", god.name).get();
		List<RelatedManyChild> children = godbis.children.asQuery().fetch();
		
		assertEquals(god, godbis);
		assertEquals(adam1, children.get(0));
		assertEquals(adam2, children.get(1));
		
		children = godbis.children.asList();
		assertEquals(adam1, children.get(0));
		assertEquals(adam2, children.get(1));
	}
	
	public void testRelatedManyCascadeInsertMany() {
		RelatedManyParent god = new RelatedManyParent("god");		
		List<RelatedManyChild> adams = new ArrayList<RelatedManyChild>();
		for(int i=0; i<100; i++){
			RelatedManyChild adam = new RelatedManyChild("adam"+i);
			god.children.asList().add(adam);
			adams.add(adam);
		}
		god.insert();

		assertNotNull(god.id);
		for(int i=0; i<100; i++){
			assertNotNull(adams.get(i).id);
			assertEquals(god.id, adams.get(i).owner.id);
		}
		
		RelatedManyParent godbis = Model.all(RelatedManyParent.class).filter("name", god.name).get();
		
		List<RelatedManyChild> children = godbis.children.asList();
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i), children.get(i));
		}
		
		children = godbis.children.asQuery().fetch();
		
		assertEquals(god, godbis);
		for(int i=0; i<100; i++){
			assertEquals(adams.get(i), children.get(i));
		}

	}
	
	public void testRelatedManyCascadeUpdateMany() {
		RelatedManyParent god = new RelatedManyParent("god");		
		List<RelatedManyChild> adams = new ArrayList<RelatedManyChild>();
		for(int i=0; i<100; i++){
			RelatedManyChild adam = new RelatedManyChild("adam"+i);
			god.children.asList().add(adam);
			adams.add(adam);
		}
		god.insert();

		assertNotNull(god.id);
		for(int i=0; i<100; i++){
			assertNotNull(adams.get(i).id);
			assertEquals(god.id, adams.get(i).owner.id);
		}
		
		// update 
		adams.get(57).name = "adam57_modified";
		adams.get(57).update();
		
		RelatedManyParent godbis = Model.all(RelatedManyParent.class).filter("name", god.name).get();
		
		List<RelatedManyChild> children = godbis.children.asList();
		for(int i=0; i<100; i++){
			if(i!=57){
				assertEquals(adams.get(i), children.get(i));
			}else {
				assertEquals("adam57_modified", children.get(i).name);
			}
		}
		
		RelatedManyChild child57 = godbis.children.asQuery().filter("id", adams.get(57).id).get();		
		assertEquals("adam57_modified", child57.name);

	}
	
	public void testRelatedManyCascadeUpdateManyRemove() {
		RelatedManyParent god = new RelatedManyParent("god");		
		List<RelatedManyChild> adams = new ArrayList<RelatedManyChild>();
		for(int i=0; i<100; i++){
			RelatedManyChild adam = new RelatedManyChild("adam"+i);
			god.children.asList().add(adam);
			adams.add(adam);
		}
		god.insert();

		assertNotNull(god.id);
		for(int i=0; i<100; i++){
			assertNotNull(adams.get(i).id);
			assertEquals(god.id, adams.get(i).owner.id);
		}
		
		// update 
		god.children.asList().remove(57);
		god.update();
		
		RelatedManyParent godbis = Model.all(RelatedManyParent.class).filter("name", god.name).get();
		
		List<RelatedManyChild> children = godbis.children.asList();
		for(int i=0; i<99; i++){
			if(i<57){
				assertEquals(adams.get(i), children.get(i));
			}else {
				assertEquals(adams.get(i+1), children.get(i));
			}
		}
		
		RelatedManyChild child57 = RelatedManyChild.all().filter("id", adams.get(57).id).get();		
		assertNull(child57.owner);

	}
}
