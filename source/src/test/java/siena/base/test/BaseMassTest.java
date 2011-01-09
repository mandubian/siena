package siena.base.test;

import static siena.Json.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManager;

public abstract class BaseMassTest extends TestCase {
	
	private PersistenceManager pm;

	protected static final int NB_ENTITIES = 10000;
	
	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	
	public void testMassFetch() {		
		List<MassEntity> ents = pm.createQuery(MassEntity.class).fetch();
		assertNotNull(ents);
		assertEquals(NB_ENTITIES, ents.size());
	}

	public void testMassDelete() {
		int res = pm.createQuery(MassEntity.class).delete();
		assertEquals(NB_ENTITIES, res);
		
		List<MassEntity> ents = pm.createQuery(MassEntity.class).fetch();
		assertNotNull(ents);
		assertEquals(0, ents.size());
	}

	public void testMassIter() {
		Iterable<MassEntity> it = pm.createQuery(MassEntity.class).iter();

		assertNotNull(it);
		int i=0;
		for(MassEntity ent: it){
			assertNotNull(ent);
			i++;
		}
		
		assertEquals(NB_ENTITIES, i);
	}
	
	protected void massInsert() {
		// massive data injection
		int _int = 0;
		short _short = 0;
		byte _byte = 0;
		long _long = 0;
		float _float = 0f;
		double _double = 0d;
		char[] longstr = new char[501];
		Arrays.fill(longstr, 'x');
				
		for(int i=0; i<NB_ENTITIES; i++){
			MassEntity ent = new MassEntity();
			ent.typeByte = _byte; // incremented later in function
			ent.typeShort = _short++;
			ent.typeInt = _int++;
			ent.typeLong = _long++;
			ent.typeFloat = _float++;
			ent.typeDouble = _double++;
			ent.typeDate = new Date();
			ent.typeString = "str_"+i;
			ent.typeLargeString = new String(longstr);
			ent.typeJson = map().put("foo_"+i, "bar_"+i);
			ent.addresses = new ArrayList<Address>();
			ent.addresses.add(new Address("addr_"+i+"_0", "town_"+i+"_0"));
			ent.addresses.add(new Address("addr_"+i+"_1", "town_"+i+"_1"));
			ent.contacts = new HashMap<String, Contact>();
			ent.contacts.put("id_"+i, 
					new Contact("Somebody_"+i, Arrays.asList("foo_"+i, "bar_"+i)));
			
			// Blob
			byte[] blob = new byte[5000];
			Arrays.fill(blob, _byte++);
			ent.typeBlob = blob;
			
			pm.insert(ent);			
		}
	}
	
	protected void massDelete() {
		pm.createQuery(MassEntity.class).delete();
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(MassEntity.class);
		pm = createPersistenceManager(classes);
		massInsert();			
	}

    @Override
    public void tearDown() throws Exception {
    	massDelete();			

        super.tearDown();
    }
}
