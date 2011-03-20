package siena.base.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.Query;
import siena.base.test.model.DiscoveryModel;
import siena.base.test.model.PersonLongAutoIDModel;
import siena.base.test.model.SampleModel;
import siena.base.test.model.SampleModel2;
import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;
import siena.core.batch.Batch;

public abstract class BaseModelTest extends TestCase {
	
	protected PersistenceManager pm;

	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	
	private static PersonLongAutoIDModel PERSON_LONGAUTOID_TESLA = new PersonLongAutoIDModel("Nikola", "Tesla", "Smiljam", 1);		
	private static PersonLongAutoIDModel PERSON_LONGAUTOID_CURIE = new PersonLongAutoIDModel("Marie", "Curie", "Warsaw", 2);
	private static PersonLongAutoIDModel PERSON_LONGAUTOID_EINSTEIN = new PersonLongAutoIDModel("Albert", "Einstein", "Ulm", 3);
	
	private static DiscoveryModel[] discs = new DiscoveryModel[200];
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(PersonLongAutoIDModel.class);
		classes.add(DiscoveryModel.class);
		classes.add(SampleModel.class);
		classes.add(SampleModel2.class);
		pm = createPersistenceManager(classes);
		PersistenceManagerFactory.install(pm, classes);
			
		for (Class<?> clazz : classes) {
			pm.createQuery(clazz).delete();			
		}
		
		Batch<PersonLongAutoIDModel> batch = PersonLongAutoIDModel.batch();
		batch.insert(PERSON_LONGAUTOID_TESLA, PERSON_LONGAUTOID_CURIE, PERSON_LONGAUTOID_EINSTEIN);

		for(int i=0; i<200;i++){
			discs[i] = new DiscoveryModel("Disc_"+i, PERSON_LONGAUTOID_CURIE);
		}
		Batch<DiscoveryModel> batch2 = DiscoveryModel.batch();
		batch2.insert(discs);
	}

	public void testGet() {
		PersonLongAutoIDModel person = PersonLongAutoIDModel.all().filter("lastName", "Tesla").get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
	}
	
	public void testFetch() {
		List<PersonLongAutoIDModel> persons = PersonLongAutoIDModel.all().fetch();
		assertEquals(3, persons.size());
		assertEquals(PERSON_LONGAUTOID_TESLA, persons.get(0));
		assertEquals(PERSON_LONGAUTOID_CURIE, persons.get(1));
		assertEquals(PERSON_LONGAUTOID_EINSTEIN, persons.get(2));
	}
	
	public void testFetchAsync() {
		QueryAsync<PersonLongAutoIDModel> q = PersonLongAutoIDModel.all().async();
		SienaFuture<List<PersonLongAutoIDModel>> future = q.fetch();
		List<PersonLongAutoIDModel> persons = future.get();
		assertEquals(3, persons.size());
		assertEquals(PERSON_LONGAUTOID_TESLA, persons.get(0));
		assertEquals(PERSON_LONGAUTOID_CURIE, persons.get(1));
		assertEquals(PERSON_LONGAUTOID_EINSTEIN, persons.get(2));
	}
	
	public void testFetchAsync2Models() {
		QueryAsync<PersonLongAutoIDModel> q = PersonLongAutoIDModel.all().async();
		QueryAsync<DiscoveryModel> q2 = DiscoveryModel.all().async();
		SienaFuture<List<PersonLongAutoIDModel>> future = q.fetch();
		SienaFuture<List<DiscoveryModel>> future2 = q2.fetch();
		List<PersonLongAutoIDModel> persons = future.get();
		assertEquals(3, persons.size());
		assertEquals(PERSON_LONGAUTOID_TESLA, persons.get(0));
		assertEquals(PERSON_LONGAUTOID_CURIE, persons.get(1));
		assertEquals(PERSON_LONGAUTOID_EINSTEIN, persons.get(2));
		
		List<DiscoveryModel> res = future2.get();
		assertEquals(200, res.size());
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchAsyncAndGetAndResetAsync2Models() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().async();
		QueryAsync<PersonLongAutoIDModel> qp = PersonLongAutoIDModel.all().async();
		SienaFuture<List<DiscoveryModel>> futured = qd.fetch();
		SienaFuture<PersonLongAutoIDModel> futurep1 = qp.filter("lastName", "Tesla").get();
		SienaFuture<PersonLongAutoIDModel> futurep2 = qp.resetData().filter("lastName", "Curie").get();
		
		List<DiscoveryModel> res = futured.get();
		assertEquals(200, res.size());
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		PersonLongAutoIDModel person = futurep1.get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
		PersonLongAutoIDModel person2 = futurep2.get();
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
	}
	
	public void testFetchPaginateSyncAndGetAndResetAsync2Models() {
		Query<DiscoveryModel> qd = DiscoveryModel.all();
		QueryAsync<PersonLongAutoIDModel> qp = PersonLongAutoIDModel.all().async();
		SienaFuture<PersonLongAutoIDModel> futurep1 = qp.filter("lastName", "Tesla").get();
		SienaFuture<PersonLongAutoIDModel> futurep2 = qp.resetData().filter("lastName", "Curie").get();
		
		List<DiscoveryModel> res = qd.fetch();
		assertEquals(200, res.size());
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		PersonLongAutoIDModel person = futurep1.get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
		PersonLongAutoIDModel person2 = futurep2.get();
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
	}
	
	public void testFetchAsyncAndGetAndResetSync2Models() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().async();
		Query<PersonLongAutoIDModel> qp = PersonLongAutoIDModel.all();
		SienaFuture<List<DiscoveryModel>> futured = qd.fetch();
		
		PersonLongAutoIDModel person = qp.filter("lastName", "Tesla").get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
		PersonLongAutoIDModel person2 = qp.resetData().filter("lastName", "Curie").get();		
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
		
		List<DiscoveryModel> res = futured.get();
		assertEquals(200, res.size());
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
	}
	
	public void testFetchPaginateAsyncAndGetAndResetAsync2Models() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().async();
		QueryAsync<PersonLongAutoIDModel> qp = PersonLongAutoIDModel.all().async();
		SienaFuture<List<DiscoveryModel>> futured = qd.paginate(50).fetch();

		SienaFuture<PersonLongAutoIDModel> futurep1 = qp.filter("lastName", "Tesla").get();
		PersonLongAutoIDModel person = futurep1.get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
		
		List<DiscoveryModel> res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		futured = qd.nextPage().fetch();
		
		SienaFuture<PersonLongAutoIDModel> futurep2 = qp.resetData().filter("lastName", "Curie").get();
		PersonLongAutoIDModel person2 = futurep2.get();
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
		
		res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testFetchPaginateAsyncStatefulAndGetAndResetAsync2Models() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().async().stateful();
		QueryAsync<PersonLongAutoIDModel> qp = PersonLongAutoIDModel.all().async();
		SienaFuture<List<DiscoveryModel>> futured = qd.paginate(50).fetch();

		SienaFuture<PersonLongAutoIDModel> futurep1 = qp.filter("lastName", "Tesla").get();
		PersonLongAutoIDModel person = futurep1.get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
		
		List<DiscoveryModel> res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		futured = qd.nextPage().fetch();
		
		SienaFuture<PersonLongAutoIDModel> futurep2 = qp.resetData().filter("lastName", "Curie").get();
		PersonLongAutoIDModel person2 = futurep2.get();
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
		
		res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulAsyncAndGetAndResetAsync2Models() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().stateful().async();
		QueryAsync<PersonLongAutoIDModel> qp = PersonLongAutoIDModel.all().async();
		SienaFuture<List<DiscoveryModel>> futured = qd.paginate(50).fetch();

		SienaFuture<PersonLongAutoIDModel> futurep1 = qp.filter("lastName", "Tesla").get();
		PersonLongAutoIDModel person = futurep1.get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
		
		List<DiscoveryModel> res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		futured = qd.nextPage().fetch();
		
		SienaFuture<PersonLongAutoIDModel> futurep2 = qp.resetData().filter("lastName", "Curie").get();
		PersonLongAutoIDModel person2 = futurep2.get();
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
		
		res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testFetchPaginateStatefulAsyncAndGetAndResetSync2Models() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().stateful().async();
		Query<PersonLongAutoIDModel> qp = PersonLongAutoIDModel.all();
		SienaFuture<List<DiscoveryModel>> futured = qd.paginate(50).fetch();

		PersonLongAutoIDModel person = qp.filter("lastName", "Tesla").get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
		
		List<DiscoveryModel> res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		futured = qd.nextPage().fetch();
		
		PersonLongAutoIDModel person2 = qp.resetData().filter("lastName", "Curie").get();
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
		
		res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
	}
	
	public void testFetchPaginateAsync2Sync2AsyncAndGetAndResetSync2Models() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().async();
		Query<PersonLongAutoIDModel> qp = PersonLongAutoIDModel.all();
		SienaFuture<List<DiscoveryModel>> futured = qd.paginate(50).fetch();

		PersonLongAutoIDModel person = qp.filter("lastName", "Tesla").get();
		assertEquals(PERSON_LONGAUTOID_TESLA, person);
		
		List<DiscoveryModel> res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}

		PersonLongAutoIDModel person2 = qp.resetData().filter("lastName", "Curie").get();
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
		
		res = qd.sync().nextPage().fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}

		PersonLongAutoIDModel person3 = qp.resetData().filter("lastName", "Einstein").get();
		assertEquals(PERSON_LONGAUTOID_EINSTEIN, person3);

		futured = qd.nextPage().fetch();
		res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		person2 = qp.resetData().filter("lastName", "Curie").get();
		assertEquals(PERSON_LONGAUTOID_CURIE, person2);
		
		res = qd.sync().nextPage().fetch();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+150], res.get(i));
		}
		
		futured = qd.nextPage().fetch();
		res = futured.get();
		assertEquals(0, res.size());

		futured = qd.nextPage().fetch();
		res = futured.get();
		assertEquals(0, res.size());
		
		person3 = qp.resetData().filter("lastName", "Einstein").get();
			assertEquals(PERSON_LONGAUTOID_EINSTEIN, person3);
			
		res = qd.sync().previousPage().fetch();
		assertEquals(50, res.size()); 
		for(int i=0; i<50; i++){
			assertEquals(discs[i+150], res.get(i));
		}
		
		futured = qd.previousPage().fetch();
		res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i+100], res.get(i));
		}
		
		res = qd.sync().previousPage().fetch();
		assertEquals(50, res.size()); 
		for(int i=0; i<50; i++){
			assertEquals(discs[i+50], res.get(i));
		}
		
		futured = qd.previousPage().fetch();
		res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = qd.sync().previousPage().fetch();
		assertEquals(0, res.size()); 
		
		futured = qd.nextPage().fetch();
		res = futured.get();
		assertEquals(50, res.size());
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = qd.sync().fetch();
		assertEquals(50, res.size()); 
		for(int i=0; i<50; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = qd.sync().previousPage().fetch();
		assertEquals(0, res.size()); 
		
		futured = qd.previousPage().fetch();
		res = futured.get();
		assertEquals(0, res.size()); 
	}
	
	
	public void testFetchPaginateStatefulUpdateData() {
		Query<DiscoveryModel> qd = DiscoveryModel.all().stateful();
		List<DiscoveryModel> res = qd.paginate(200).fetch();

		assertEquals(200, res.size()); 
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = qd.nextPage().fetch();
		assertEquals(0, res.size()); 
		
		DiscoveryModel disc = new DiscoveryModel("Disc_201", PERSON_LONGAUTOID_CURIE);
		disc.insert();
		
		res = qd.fetch();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
		
		res = qd.nextPage().fetch();
		assertEquals(0, res.size()); 
		
		res = qd.previousPage().fetch();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
	}
	
	public void testFetchPaginateStatelessUpdateData() {
		Query<DiscoveryModel> qd = DiscoveryModel.all();
		List<DiscoveryModel> res = qd.paginate(200).fetch();

		assertEquals(200, res.size()); 
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		res = qd.nextPage().fetch();
		assertEquals(0, res.size()); 
		
		DiscoveryModel disc = new DiscoveryModel("Disc_201", PERSON_LONGAUTOID_CURIE);
		disc.insert();
		
		res = qd.fetch();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
		
		res = qd.nextPage().fetch();
		assertEquals(0, res.size()); 
		
		res = qd.previousPage().fetch();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
	}
	
	public void testFetchPaginateStatefulAsyncUpdateData() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().stateful().async();
		SienaFuture<List<DiscoveryModel>> future = qd.paginate(200).fetch();
		
		List<DiscoveryModel> res = future.get();
		assertEquals(200, res.size()); 
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = qd.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size()); 
		
		DiscoveryModel disc = new DiscoveryModel("Disc_201", PERSON_LONGAUTOID_CURIE);
		disc.insert();
		
		future = qd.fetch();
		res = future.get();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
		
		future = qd.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size()); 
		
		future = qd.previousPage().fetch();
		res = future.get();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
	}

	public void testFetchPaginateStatelessAsyncUpdateData() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().async();
		SienaFuture<List<DiscoveryModel>> future = qd.paginate(200).fetch();

		List<DiscoveryModel> res = future.get();
		assertEquals(200, res.size()); 
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		future = qd.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size()); 
		
		DiscoveryModel disc = new DiscoveryModel("Disc_201", PERSON_LONGAUTOID_CURIE);
		disc.insert();
		
		future = qd.fetch();
		res = future.get();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
		
		future = qd.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size()); 
		
		future = qd.previousPage().fetch();
		res = future.get();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
	}
	
	public void testFetchPaginateStatefulRealAsyncUpdateData() {
		QueryAsync<DiscoveryModel> qd = DiscoveryModel.all().stateful().async();
		SienaFuture<List<DiscoveryModel>> future = qd.paginate(200).fetch();
		DiscoveryModel disc = new DiscoveryModel("Disc_201", PERSON_LONGAUTOID_CURIE);
		SienaFuture<Void> futureDisc = disc.async().insert();
		
		List<DiscoveryModel> res = future.get();
		assertEquals(200, res.size()); 
		for(int i=0; i<200; i++){
			assertEquals(discs[i], res.get(i));
		}
		
		futureDisc.get();
		
		SienaFuture<List<DiscoveryModel>> future2 = qd.nextPage().fetch();
		res = future2.get();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
		
		future = qd.nextPage().fetch();
		res = future.get();
		assertEquals(0, res.size()); 
		
		future = qd.previousPage().fetch();
		res = future.get();
		assertEquals(1, res.size()); 
		assertEquals(disc, res.get(0));
	}
	
	
	public void testInsert() {
		SampleModel mod = new SampleModel();
		mod.clazz = String.class;
		mod.foobar = "FOOBAR";
		mod.publicField = "PUBLIC_FIELD";
		mod.setPrivateField("PRIVATE_FIELD");
		mod.type = SampleModel.Type.FOO;
		
		mod.insert();
		
		List<SampleModel> res = SampleModel.all().fetch();
		for(SampleModel m: res){
			assertEquals(mod, m);
		}
	}
	public void testInsertAsync() {
		SampleModel mod = new SampleModel();
		mod.clazz = String.class;
		mod.foobar = "FOOBAR";
		mod.publicField = "PUBLIC_FIELD";
		mod.setPrivateField("PRIVATE_FIELD");
		mod.type = SampleModel.Type.FOO;
		
		SienaFuture<Void> future = mod.async().insert();
		future.get();
		
		List<SampleModel> res = SampleModel.all().fetch();
		for(SampleModel m: res){
			assertEquals(mod, m);
		}
	}
	public void testInsertMany() {
		SampleModel mods[] = new SampleModel[100];
		for(int i=0; i<100; i++){
			SampleModel mod = new SampleModel();
			mod.clazz = String.class;
			mod.foobar = "FOOBAR";
			mod.publicField = "PUBLIC_FIELD";
			mod.setPrivateField("PRIVATE_FIELD");
			mod.type = SampleModel.Type.FOO;
			mods[i] = mod;
			
			mods[i].insert();
		}
					
		List<SampleModel> res = SampleModel.all().fetch();
		assertEquals(100, res.size()); 
		for(int i=0; i<100; i++){
			assertEquals(mods[i], res.get(i));
		}
	}
	
	public void testInsertManyAsync() {
		SampleModel mods[] = new SampleModel[100];
		for(int i=0; i<100; i++){
			SampleModel mod = new SampleModel();
			mod.clazz = String.class;
			mod.foobar = "FOOBAR";
			mod.publicField = "PUBLIC_FIELD";
			mod.setPrivateField("PRIVATE_FIELD");
			mod.type = SampleModel.Type.FOO;
			mods[i] = mod;
			
			mods[i].async().insert().get();
		}
					
		List<SampleModel> res = SampleModel.all().fetch();
		assertEquals(100, res.size()); 
		for(int i=0; i<100; i++){
			assertEquals(mods[i], res.get(i));
		}
	}
	
	public void testInsertAutoQuery() {
		SampleModel mod = new SampleModel();
		mod.clazz = String.class;
		mod.foobar = "FOOBAR";
		mod.publicField = "PUBLIC_FIELD";
		mod.setPrivateField("PRIVATE_FIELD");
		mod.type = SampleModel.Type.FOO;
		
		mod.insert();
		
		SampleModel2 mod2 = new SampleModel2();
		mod2.relationship = mod;
		mod2.insert();
		
		List<SampleModel> res = SampleModel.all().fetch();
		for(SampleModel m: res){
			assertEquals(mod, m);
			List<SampleModel2> res2 = mod.links.fetch();
			assertEquals(1, res2.size());
			assertEquals(mod2, res2.get(0));
		}
	}
	
	public void testInsertAutoQueryAsyncFetchSync() {
		SampleModel mod = new SampleModel();
		mod.clazz = String.class;
		mod.foobar = "FOOBAR";
		mod.publicField = "PUBLIC_FIELD";
		mod.setPrivateField("PRIVATE_FIELD");
		mod.type = SampleModel.Type.FOO;
		
		mod.async().insert().get();
		
		SampleModel2 mod2 = new SampleModel2();
		mod2.relationship = mod;
		mod2.async().insert().get();
		
		List<SampleModel> res = SampleModel.all().fetch();
		for(SampleModel m: res){
			assertEquals(mod, m);
			List<SampleModel2> res2 = mod.links.fetch();
			assertEquals(1, res2.size());
			assertEquals(mod2, res2.get(0));
		}
	}
	
	public void testInsertAutoQueryAsyncFetchAsync() {
		SampleModel mod = new SampleModel();
		mod.clazz = String.class;
		mod.foobar = "FOOBAR";
		mod.publicField = "PUBLIC_FIELD";
		mod.setPrivateField("PRIVATE_FIELD");
		mod.type = SampleModel.Type.FOO;
		
		mod.async().insert().get();
		
		SampleModel2 mod2 = new SampleModel2();
		mod2.relationship = mod;
		mod2.async().insert().get();
		
		SienaFuture<List<SampleModel>> future = SampleModel.all().async().fetch();
		List<SampleModel> res = future.get();
		for(SampleModel m: res){
			assertEquals(mod, m);
			List<SampleModel2> res2 = mod.links.fetch();
			assertEquals(1, res2.size());
			assertEquals(mod2, res2.get(0));
		}
	}
	
	public void testInsertAutoQueryAsyncFetchAsyncQueryAsync() {
		SampleModel mod = new SampleModel();
		mod.clazz = String.class;
		mod.foobar = "FOOBAR";
		mod.publicField = "PUBLIC_FIELD";
		mod.setPrivateField("PRIVATE_FIELD");
		mod.type = SampleModel.Type.FOO;
		
		mod.async().insert().get();
		
		SampleModel2 mod2 = new SampleModel2();
		mod2.relationship = mod;
		mod2.async().insert().get();
		
		SienaFuture<List<SampleModel>> future = SampleModel.all().async().fetch();
		List<SampleModel> res = future.get();
		for(SampleModel m: res){
			assertEquals(mod, m);
			SienaFuture<List<SampleModel2>> future2 = mod.links.async().fetch();
			List<SampleModel2> res2 = future2.get();
			assertEquals(1, res2.size());
			assertEquals(mod2, res2.get(0));
		}
	}
	
	
	public void testInsertAutoQueryMany() {
		SampleModel mod = new SampleModel();
		mod.clazz = String.class;
		mod.foobar = "FOOBAR";
		mod.publicField = "PUBLIC_FIELD";
		mod.setPrivateField("PRIVATE_FIELD");
		mod.type = SampleModel.Type.FOO;
		
		mod.insert();
		
		SampleModel2 mods2[] = new SampleModel2[100];
		for(int i=0; i<100; i++){
			SampleModel2 mod2 = new SampleModel2();
			mod2.relationship = mod;
			mods2[i] = mod2;
			mod2.insert();			
		}
		
		List<SampleModel> res = SampleModel.all().fetch();
		for(SampleModel m: res){
			assertEquals(mod, m);
			List<SampleModel2> res2 = mod.links.fetch();
			assertEquals(100, res2.size());
			for(int i=0; i<100; i++){
				assertEquals(mods2[i], res2.get(i));
			}
		}
	}
	
	public void testInsertBatchAsync() {
		SampleModel mods[] = new SampleModel[100];
		for(int i=0; i<100; i++){
			SampleModel mod = new SampleModel();
			mod.clazz = String.class;
			mod.foobar = "FOOBAR";
			mod.publicField = "PUBLIC_FIELD";
			mod.setPrivateField("PRIVATE_FIELD");
			mod.type = SampleModel.Type.FOO;
			mods[i] = mod;
			
			mods[i].async().insert().get();
		}
					
		List<SampleModel> res = SampleModel.all().fetch();
		assertEquals(100, res.size()); 
		for(int i=0; i<100; i++){
			assertEquals(mods[i], res.get(i));
		}
	}
}
