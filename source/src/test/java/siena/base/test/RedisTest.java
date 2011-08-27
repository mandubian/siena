/**
 * 
 */
package siena.base.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManager;
import siena.SienaException;
import siena.base.test.model.PersonStringID;
import siena.redis.RedisPersistenceManager;


/**
 * @author Pascal Voitot <pascal.voitot@mandubian.org>
 *
 */
public class RedisTest extends TestCase {
	private static RedisPersistenceManager pm;

	public PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception {
		if(pm==null){
			pm = new RedisPersistenceManager();
			pm.init(null);
		}
		return pm;
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(PersonStringID.class);
		
		createPersistenceManager(classes);
	}
	
	public void testInsertPersonStringID() {
		PersonStringID maxwell = new PersonStringID();
		maxwell.id = "MAXWELL";
		maxwell.firstName = "James Clerk";
		maxwell.lastName = "Maxwell";
		maxwell.city = "Edinburgh";
		maxwell.n = 4;

		pm.insert(maxwell);
		assertEquals(maxwell.id, "MAXWELL");

		PersonStringID maxwellbis = new PersonStringID();
		maxwellbis.id = "MAXWELL";
		pm.get(maxwellbis);

		assertEquals(maxwell, maxwellbis);

		maxwell.firstName = "James Clerk UPD";
		maxwell.lastName = "Maxwell UPD";
		maxwell.city = "Edinburgh UPD";
		maxwell.n = 5;

		pm.update(maxwell);

		maxwellbis = new PersonStringID();
		maxwellbis.id = "MAXWELL";
		pm.get(maxwellbis);

		assertEquals(maxwell, maxwellbis);

		pm.delete(maxwell);
                
		try {
                    pm.get(maxwell);
		} catch(SienaException ex){
                    return;
		}
		fail();
	}
}
