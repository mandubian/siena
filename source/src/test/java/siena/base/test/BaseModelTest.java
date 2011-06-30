package siena.base.test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import siena.Model;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.Query;
import siena.SienaException;
import siena.base.test.model.AggregateChildModel;
import siena.base.test.model.AggregateParentModel;
import siena.base.test.model.DiscoveryModel;
import siena.base.test.model.PersonLongAutoIDAbstract;
import siena.base.test.model.PersonLongAutoIDExtended;
import siena.base.test.model.PersonLongAutoIDExtended2;
import siena.base.test.model.PersonLongAutoIDExtended2.MyEnum;
import siena.base.test.model.PersonLongAutoIDExtendedAbstract;
import siena.base.test.model.PersonLongAutoIDExtendedFilter;
import siena.base.test.model.PersonLongAutoIDModel;
import siena.base.test.model.SampleModel;
import siena.base.test.model.SampleModel2;
import siena.base.test.model.TransactionAccountFrom;
import siena.base.test.model.TransactionAccountFromModel;
import siena.base.test.model.TransactionAccountTo;
import siena.base.test.model.TransactionAccountToModel;
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
		classes.add(PersonLongAutoIDExtended.class);
		classes.add(PersonLongAutoIDExtended2.class);
		classes.add(PersonLongAutoIDAbstract.class);
		classes.add(PersonLongAutoIDExtendedAbstract.class);
		classes.add(PersonLongAutoIDExtendedFilter.class);
		classes.add(TransactionAccountFromModel.class);
		classes.add(TransactionAccountToModel.class);
//		classes.add(AggregateChildModel.class);
//		classes.add(AggregateParentModel.class);

		pm = createPersistenceManager(classes);
		PersistenceManagerFactory.install(pm, classes);
			
		for (Class<?> clazz : classes) {
			if(!Modifier.isAbstract(clazz.getModifiers())){
				pm.createQuery(clazz).delete();			
			}
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
	
	public void testSimpleInheritance() {
		PersonLongAutoIDExtended bob = 
			new PersonLongAutoIDExtended("Bob", "Doe", "Oklahoma", 1, "the_dog1");
		PersonLongAutoIDExtended ben = 
			new PersonLongAutoIDExtended("Ben", "Smith", "Wichita", 2, "the_dog2");
		PersonLongAutoIDExtended john = 
			new PersonLongAutoIDExtended("John", "Wells", "Buffalo", 3, "the_dog3");
		
		final PersonLongAutoIDExtended.Image img1 = new PersonLongAutoIDExtended.Image();
		img1.filename = "test.file";
		img1.title = "title";
		final PersonLongAutoIDExtended.Image img2 = new PersonLongAutoIDExtended.Image();
		img2.filename = "test.file";
		img2.title = "title";
		final PersonLongAutoIDExtended.Image img3 = new PersonLongAutoIDExtended.Image();
		img3.filename = "test.file";
		img3.title = "title";
		
		List<PersonLongAutoIDExtended.Image> imgList = new ArrayList<PersonLongAutoIDExtended.Image>() 
		{{
			add(img1);
			add(img2);
			add(img3);			
		}};
		
		Map<String, PersonLongAutoIDExtended.Image> imgMap = 
			new HashMap<String, PersonLongAutoIDExtended.Image>() 
		{{
			put("img1", img1);
			put("img2", img2);
			put("img3", img3);			
		}};
				
		bob.boss = john;
		bob.profileImage = img1;
		bob.otherImages = imgList;
		bob.stillImages = imgMap;
		
		ben.boss = john;
		ben.profileImage = img2;
		ben.otherImages = imgList;
		ben.stillImages = imgMap;
		
		john.profileImage = img3;
		john.otherImages = imgList;
		john.stillImages = imgMap;
		
		pm.save(john);
		pm.save(bob);
		pm.save(ben);
				
		PersonLongAutoIDExtended bob1 = 
			pm.getByKey(PersonLongAutoIDExtended.class, bob.id);
		PersonLongAutoIDExtended ben1 = 
			pm.getByKey(PersonLongAutoIDExtended.class, ben.id);
		PersonLongAutoIDExtended john1 = 
			pm.getByKey(PersonLongAutoIDExtended.class, john.id);
		
		assertEquals(bob, bob1);
		assertEquals(john.id, bob1.boss.id);
		assertEquals(ben, ben1);
		assertEquals(john.id, ben1.boss.id);
		assertEquals(john, john1);
		List<PersonLongAutoIDExtended> emps = john1.employees.order("id").fetch();
		assertEquals(bob, emps.get(0));
		assertEquals(ben, emps.get(1));
	}

	public void testDoubleInheritance() {
		PersonLongAutoIDExtended2 bob = 
			new PersonLongAutoIDExtended2("Bob", "Doe", "Oklahoma", 1, "the_dog1", MyEnum.VAL1);
		pm.save(bob);
		
		PersonLongAutoIDExtended2 bob1 = 
			pm.getByKey(PersonLongAutoIDExtended2.class, bob.id);
		
		assertEquals(bob, bob1);

	}
	
	public void testAbstractInheritance() {
		PersonLongAutoIDExtendedAbstract bob = 
			new PersonLongAutoIDExtendedAbstract("Bob", "Doe", "Oklahoma", 1, "the_dog1");
		pm.save(bob);
		
		PersonLongAutoIDExtendedAbstract bob1 = 
			pm.getByKey(PersonLongAutoIDExtendedAbstract.class, bob.id);
		
		assertEquals(bob, bob1);

	}
	
	public void testFilterInheritance() {
		PersonLongAutoIDExtendedFilter bob = 
			new PersonLongAutoIDExtendedFilter("Bob", "Doe", "Oklahoma", 1, "the_dog1");
		pm.save(bob);
		
		PersonLongAutoIDExtendedFilter bob1 = 
			pm.getByKey(PersonLongAutoIDExtendedFilter.class, bob.id);
		
		assertNotSame(bob, bob1);
		assertEquals(bob.firstName, bob1.firstName);
		assertEquals(bob.lastName, bob1.lastName);
		assertEquals(bob.dogName, bob1.dogName);
		assertEquals(bob.boss, bob1.boss);
		assertEquals(bob.profileImage, bob1.profileImage);
		assertEquals(bob.stillImages, bob1.stillImages);
		assertEquals(bob.otherImages, bob1.otherImages);
		assertTrue(bob1.city == null);
		assertTrue(bob1.n == 0);
	}

	public void testTransactionSave() {
		TransactionAccountFromModel accFrom = new TransactionAccountFromModel(1000L);
		TransactionAccountToModel accTo = new TransactionAccountToModel(1000L);
		
		accFrom.insert();
		accTo.insert();
	
		try {
			accFrom.getPersistenceManager().beginTransaction();
			accFrom.amount-=100L;
			accFrom.save();
			accTo.amount+=100L;
			accTo.save();
			accFrom.getPersistenceManager().commitTransaction();
		}catch(SienaException e){
			accFrom.getPersistenceManager().rollbackTransaction();
			fail();
		}finally{
			accFrom.getPersistenceManager().closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(900L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1100L == accToAfter.amount);
	}
	
	public void testTransactionSaveFailure() {
		TransactionAccountFromModel accFrom = new TransactionAccountFromModel(1000L);
		TransactionAccountToModel accTo = new TransactionAccountToModel(1000L);
		accFrom.insert();
		accTo.insert();
	
		try {
			accFrom.getPersistenceManager().beginTransaction();
			accFrom.amount-=100L;
			accFrom.save();
			accTo.amount+=100L;
			accTo.save();
			throw new SienaException("test");
		}catch(SienaException e){
			accFrom.getPersistenceManager().rollbackTransaction();
		}finally{
			accFrom.getPersistenceManager().closeConnection();
		}
		
		TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
		assertTrue(1000L == accFromAfter.amount);
		TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
		assertTrue(1000L == accToAfter.amount);
	}
	
	public void testAggregate() {
		AggregateChildModel adam1 = new AggregateChildModel("adam1");
		AggregateChildModel adam2 = new AggregateChildModel("adam2");	
		AggregateChildModel eve = new AggregateChildModel("eve");
		AggregateChildModel bob = new AggregateChildModel("bob");

		AggregateParentModel god = new AggregateParentModel("god");
		god.child = adam1;
		god.children.asList().addAll(adam2, eve, bob);				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
		AggregateParentModel god1 = 
			Model.getByKey(AggregateParentModel.class, god.id);
		
		assertNotNull(god1);
		assertEquals(adam1, god1.child);
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
		assertEquals(adam1, god2.child);
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
		god.child = adam1;
		god.children.asList().addAll(Arrays.asList(adam2, eve, bob));
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
		god.name = "goddy";
		adam1.name = "adammy";
		bob.name = "bobby";
		eve.name = "evvy";
		
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
		god.child = adam1;
		god.children.asList().addAll(Arrays.asList(adam2, eve, bob));
				
		god.save();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
		AggregateParentModel god1 = 
			Model.getByKey(AggregateParentModel.class, god.id);
		
		assertNotNull(god1);
		assertEquals(adam1, god1.child);
		List<AggregateChildModel> children = god1.children.asQuery().fetch();
		assertEquals(adam2, children.get(0));
		assertEquals(eve, children.get(1));
		assertEquals(bob, children.get(2));

		god.name = "goddy";
		adam1.name = "adammy";
		bob.name = "bobby";
		eve.name = "evvy";
		
		god.save();
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "goddy").get();
		assertEquals(god, godbis);
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
		god.child = adam1;
		god.children.asList().addAll(Arrays.asList(adam2, eve, bob));
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
		god.delete();
		
		AggregateParentModel godbis = AggregateParentModel.all().filter("name", "god").get();
		assertNull(godbis);
		
		List<AggregateChildModel> children = Model.batch(AggregateChildModel.class).getByKeys(adam1.id, adam2.id, eve.id, bob.id);
		assertTrue(children.isEmpty());
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
		god.child = eve;
		god.children.asList().addAll(adams);
				
		god.insert();
		
		assertNotNull(god.id);
		assertNotNull(god.child.id);
		
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
}
