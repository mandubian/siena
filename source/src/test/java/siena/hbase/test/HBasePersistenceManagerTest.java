package siena.hbase.test;

import java.util.List;

import junit.framework.TestCase;
import siena.hbase.HBaseDdlGenerator;
import siena.hbase.HBasePersistenceManager;

public class HBasePersistenceManagerTest extends TestCase {
	
	public void testSimple() {
		assertTrue(true);
	}
	
//	public void testCreate() {
//		HBasePersistenceManager pm = new HBasePersistenceManager();
//		Person person = new Person("1", "Nikola", "Tesla", "Smiljam");
//		pm.insert(person);
//	}
//	
//	public void testQuery() {
//		HBasePersistenceManager pm = new HBasePersistenceManager();
//		
//		Person person = new Person("1", "Nikola", "Tesla", "Smiljam");
//		pm.insert(person);
//		
//		List<Person> people = pm.createQuery(Person.class).fetch();
//		assertEquals(1, people.size());
//		assertEquals(person, people.get(0));
//	}
//	
//	public void testGet() {
//		HBasePersistenceManager pm = new HBasePersistenceManager();
//		
//		Person person = new Person("1", "Nikola", "Tesla", "Smiljam");
//		pm.insert(person);
//		
//		Person p = new Person();
//		p.id = "1";
//		pm.get(p);
//		assertEquals(person, p);
//	}
//	
//	public void testDelete() {
//		HBasePersistenceManager pm = new HBasePersistenceManager();
//		
//		Person person = new Person("1", "Nikola", "Tesla", "Smiljam");
//		pm.insert(person);
//
//		List<Person> people = pm.createQuery(Person.class).fetch();
//		assertEquals(1, people.size());
//		
//		pm.delete(person);
//
//		people = pm.createQuery(Person.class).fetch();
//		assertTrue(people.isEmpty());
//	}
//	
//	public void testUpdate() {
//		HBasePersistenceManager pm = new HBasePersistenceManager();
//		
//		Person person = new Person("1", "Nikola", "Tesla", "Smiljam");
//		pm.insert(person);
//
//		List<Person> people = pm.createQuery(Person.class).fetch();
//		assertEquals(1, people.size());
//		
//		person.lastName = "xxx";
//		pm.update(person);
//
//		people = pm.createQuery(Person.class).fetch();
//		assertEquals(1, people.size());
//
//		Person p = new Person();
//		p.id = "1";
//		pm.get(p);
//		assertEquals(person, p);
//	}
//	
//	@Override
//	protected void setUp() throws Exception {
//		super.setUp();
//		
//		HBaseDdlGenerator generator = new HBaseDdlGenerator();
//		generator.addTable(Person.class);
//		generator.dropTables();
//		
//		generator.updateSchema();
//	}

}
