package siena.mongodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.mongodb.model.SienaMongoDbTestModel;

/**
 * Test class for MongoDBPersistenceManager
 * 
 * Before you run test cases, be sure run the mongod process in your localhost(127.0.0.1)
 * and db name "test" doesn't have any important data.
 * 
 * @author T.hagikura
 *
 */
public class MongoDBPersistenceManagerTest {

	private static PersistenceManager pm;
	
	public static PersistenceManager createPersistenceManager(final List<Class<?>> classes) throws Exception {
		final Properties p = new Properties();
		p.setProperty("siena.mongodb.hostnames", "127.0.0.1");
		p.setProperty("siena.mongodb.databaseName", "test");

		final MongoDBPersistenceManager pm = new MongoDBPersistenceManager();
		pm.init(p);

		return pm;
	}
	
	@Test
	public void testInsertAndDelete() {
		SienaMongoDbTestModel model = createTestInstance();
		pm.insert(model);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().fetch(); 
		
		assertEquals(1, models.size());
		SienaMongoDbTestModel registered = models.get(0);
		assertTrue(model.equals(registered));
		
		registered.delete();
		models = SienaMongoDbTestModel.all().fetch();
		
		assertEquals(0, models.size());
	}
	
	@Test
	public void testSaveAndDelete() {
		SienaMongoDbTestModel model = createTestInstance();
		pm.save(model);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().fetch(); 
		assertEquals(1, models.size());
		
		SienaMongoDbTestModel registered = models.get(0);
		assertTrue(model.equals(registered));
		
		registered.stringField = "updatedBySave";
		pm.save(registered); // will be updated because same _id object is already inserted.
		
		models = SienaMongoDbTestModel.all().fetch(); 
		assertEquals(1, models.size());
		
		SienaMongoDbTestModel afterSave = models.get(0);
		assertTrue(registered.equals(afterSave));
		
		afterSave.delete();
		models = SienaMongoDbTestModel.all().fetch();
		
		assertEquals(0, models.size());
	}
	
	@Test
	public void testUpdate() {
		SienaMongoDbTestModel model = createTestInstance();
		pm.insert(model);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().fetch(); 
		
		assertEquals(1, models.size());
		SienaMongoDbTestModel registered = models.get(0);
		assertTrue(model.equals(registered));
		
		Integer changedInt = model.integerField + 10;
		String changedStr = "changed";
		registered.integerField = changedInt;
		registered.stringField = changedStr;
		
		registered.update();
		
		models = SienaMongoDbTestModel.all().fetch();
		assertEquals(1, models.size());
		SienaMongoDbTestModel updatedModel = models.get(0);
		assertTrue(registered.equals(updatedModel));
		
		updatedModel.delete();
		models = SienaMongoDbTestModel.all().fetch();
		assertEquals(0, models.size());
	}
	
	@Test
	public void testInsertIterable() {
		Collection<SienaMongoDbTestModel> modelsToInsert = new ArrayList<SienaMongoDbTestModel>();
		SienaMongoDbTestModel model = createTestInstance();
		SienaMongoDbTestModel model2 = createTestInstance();
		modelsToInsert.add(model);
		modelsToInsert.add(model2);
		int insertCount = pm.insert(modelsToInsert);
		
		assertEquals(2, insertCount);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().fetch(); 
		assertEquals(2, models.size());
		
		int sameModelInsertCount = pm.insert(modelsToInsert); // can not be inserted because same _id objects exists
		assertEquals(0, sameModelInsertCount);
		models = SienaMongoDbTestModel.all().fetch(); 
		assertEquals(2, models.size());
		
		deleteExistingModel();
	}
	
	@Test
	public void testDeleteIterable() {
		Collection<SienaMongoDbTestModel> modelsToInsert = new ArrayList<SienaMongoDbTestModel>();
		SienaMongoDbTestModel model = createTestInstance();
		SienaMongoDbTestModel model2 = createTestInstance();
		modelsToInsert.add(model);
		modelsToInsert.add(model2);
		int insertCount = pm.insert(modelsToInsert);
		assertEquals(2, insertCount);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().fetch(); 
		assertEquals(2, models.size());
		
		int deleteCount = pm.delete(modelsToInsert); 
		assertEquals(2, deleteCount);
		models = SienaMongoDbTestModel.all().fetch(); 
		assertEquals(0, models.size());
		
		deleteExistingModel();
	}
	
	@Test
	public void testUpdateIterable() {
		Collection<SienaMongoDbTestModel> modelsToInsert = new ArrayList<SienaMongoDbTestModel>();
		SienaMongoDbTestModel model = createTestInstance();
		SienaMongoDbTestModel model2 = createTestInstance();
		modelsToInsert.add(model);
		modelsToInsert.add(model2);
		int insertCount = pm.insert(modelsToInsert);
		assertEquals(2, insertCount);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().fetch(); 
		assertEquals(2, models.size());
		
		model.stringField = "updateField1";
		model2.stringField = "updateField2";
		int updateCount = pm.update(modelsToInsert); 
		assertEquals(2, updateCount);
		models = SienaMongoDbTestModel.all().order("stringField").fetch(); 
		assertEquals(2, models.size());
		SienaMongoDbTestModel registered1 = models.get(0);
		SienaMongoDbTestModel registered2 = models.get(1);
		assertTrue(model.equals(registered1));
		assertTrue(model2.equals(registered2));
		
		deleteExistingModel();
	}
	
	@Test
	public void testStringFilter() {
		SienaMongoDbTestModel model = createTestInstance();
		pm.insert(model);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().filter("stringField", "dummyString").fetch();
		assertEquals(0, models.size());
		
		models = SienaMongoDbTestModel.all().filter("stringField", TEST_STRING).fetch();
		assertEquals(1, models.size());
		
		models = SienaMongoDbTestModel.all().filter("stringField !=", TEST_STRING).fetch();
		assertEquals(0, models.size());
		
		deleteExistingModel();
	}
	
	@Test
	public void testIntegerFilter() {
		SienaMongoDbTestModel model = createTestInstance();
		pm.insert(model);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().filter("integerField", 0).fetch();
		assertEquals(0, models.size());
		
		models = SienaMongoDbTestModel.all().filter("integerField", TEST_INTEGER).fetch();
		assertEquals(1, models.size());
	
		models = SienaMongoDbTestModel.all().filter("integerField !=", TEST_INTEGER).fetch();
		assertEquals(0, models.size());
	
		models = SienaMongoDbTestModel.all().filter("integerField >", TEST_INTEGER).fetch();
		assertEquals(0, models.size());
	
		models = SienaMongoDbTestModel.all().filter("integerField <", TEST_INTEGER).fetch();
		assertEquals(0, models.size());
	
		models = SienaMongoDbTestModel.all().filter("integerField >", TEST_INTEGER - 1).fetch();
		assertEquals(1, models.size());
	
		models = SienaMongoDbTestModel.all().filter("integerField <", TEST_INTEGER + 1).fetch();
		assertEquals(1, models.size());
		
		models = SienaMongoDbTestModel.all().filter("integerField >=", TEST_INTEGER).fetch();
		assertEquals(1, models.size());
	
		models = SienaMongoDbTestModel.all().filter("integerField <=", TEST_INTEGER).fetch();
		assertEquals(1, models.size());
	
		models = SienaMongoDbTestModel.all().filter("integerField >=", TEST_INTEGER + 1).fetch();
		assertEquals(0, models.size());
	
		models = SienaMongoDbTestModel.all().filter("integerField <=", TEST_INTEGER - 1).fetch();
		assertEquals(0, models.size());
	
		deleteExistingModel();
	}
	
	@Test
	public void testDateFilter() {
		SienaMongoDbTestModel model = createTestInstance();
		pm.insert(model);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().filter("dateField", new Date()).fetch();
		assertEquals(0, models.size());
		
		models = SienaMongoDbTestModel.all().filter("dateField", TEST_DATE).fetch();
		assertEquals(1, models.size());
	
		models = SienaMongoDbTestModel.all().filter("dateField !=", TEST_DATE).fetch();
		assertEquals(0, models.size());
	
		models = SienaMongoDbTestModel.all().filter("dateField >", TEST_DATE).fetch();
		assertEquals(0, models.size());
	
		models = SienaMongoDbTestModel.all().filter("dateField <", TEST_DATE).fetch();
		assertEquals(0, models.size());
	
		Calendar nextDateCal = new GregorianCalendar();
		nextDateCal.setTime(TEST_DATE);
		nextDateCal.add(Calendar.DATE, 1);
		Date nextDate = nextDateCal.getTime();
	
		models = SienaMongoDbTestModel.all().filter("dateField <", nextDate).fetch();
		assertEquals(1, models.size());
		
		models = SienaMongoDbTestModel.all().filter("dateField >=", TEST_DATE).fetch();
		assertEquals(1, models.size());
	
		models = SienaMongoDbTestModel.all().filter("dateField <=", TEST_DATE).fetch();
		assertEquals(1, models.size());
	
		models = SienaMongoDbTestModel.all().filter("dateField >=", nextDate).fetch();
		assertEquals(0, models.size());
	
		deleteExistingModel();
	}
	
	@Test
	public void testOrder() {
		Collection<SienaMongoDbTestModel> modelsToInsert = new ArrayList<SienaMongoDbTestModel>();
		SienaMongoDbTestModel model = createTestInstance();
		SienaMongoDbTestModel model2 = createTestInstance();
		model.stringField = "updateField1";
		model2.stringField = "updateField2";
		modelsToInsert.add(model);
		modelsToInsert.add(model2);
		int insertCount = pm.insert(modelsToInsert);
		assertEquals(2, insertCount);
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().fetch(); 
		assertEquals(2, models.size());
		
		// get models in ascending order by stringField
		models = SienaMongoDbTestModel.all().order("stringField").fetch(); 
		assertEquals(2, models.size());
		SienaMongoDbTestModel registered1 = models.get(0);
		SienaMongoDbTestModel registered2 = models.get(1);
		assertTrue(model.equals(registered1));
		assertTrue(model2.equals(registered2));
		
		// get models in descending order by stringField
		models = SienaMongoDbTestModel.all().order("-stringField").fetch(); 
		assertEquals(2, models.size());
		registered1 = models.get(0);
		registered2 = models.get(1);
		assertTrue(model2.equals(registered1));
		assertTrue(model.equals(registered2));
		
		deleteExistingModel();
	}
	
	@BeforeClass
	public static void setUpOnce() throws Exception {
		pm = installPersistenceManager();
		SienaMongoDbTestModel.deleteAll();
	}
	
	@AfterClass
	public static void tearDownOnce() {
		SienaMongoDbTestModel.deleteAll();
	}

	private static final Date TEST_DATE = new Date();
	private static final String TEST_STRING = "testStr";
	private static final Integer TEST_INTEGER = Integer.valueOf(10);
	private static final Long TEST_LONG = Long.valueOf(20L);
	private static final Double TEST_DOUBLE = Double.valueOf(20.02);
	
	private static void deleteExistingModel() {
		SienaMongoDbTestModel.deleteAll();
		List<SienaMongoDbTestModel> models = SienaMongoDbTestModel.all().fetch();
		assertEquals(0, models.size());
	}
	
	private static SienaMongoDbTestModel createTestInstance() {
		SienaMongoDbTestModel model = new SienaMongoDbTestModel();
		model.dateField = TEST_DATE;
		model.doubleField = TEST_DOUBLE;
		model.stringField = TEST_STRING;
		model.integerField = TEST_INTEGER;
		model.longField = TEST_LONG;
		return model;
	}
	
	private static PersistenceManager installPersistenceManager() throws Exception {
		PersistenceManager pm = createPersistenceManager(null);
		PersistenceManagerFactory.install(pm, SienaMongoDbTestModel.class);
		return pm;
	}
}
