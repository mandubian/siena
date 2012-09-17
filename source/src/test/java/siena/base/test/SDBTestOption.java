package siena.base.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;
import siena.sdb.SdbPersistenceManager;
import siena.core.options.PmOption;
import siena.core.options.PmOptionStickiness;
import siena.sdb.PmOptionSdbReadConsistency;

public abstract class SDBTestOption extends TestCase {
	
	protected SdbPersistenceManager sdb;

	List<Class<?>> classes = new ArrayList<Class<?>>();

	public abstract void init();
	public abstract void createClasses(List<Class<?>> classes);
	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	public abstract void postInit();

	public abstract boolean supportsAutoincrement();
	public abstract boolean supportsMultipleKeys();
	public abstract boolean supportsDeleteException();
	public abstract boolean supportsSearchStart();
	public abstract boolean supportsSearchEnd();
	public abstract boolean supportsTransaction();
	public abstract boolean supportsListStore();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		sdb = new SdbPersistenceManager();
		sdb.init(SimpleDBConfig.getSienaAWSProperties());
	}
	
	public void testNonSticky()
	{
		PmOption opt;
		opt = sdb.option(PmOptionSdbReadConsistency.ID);
		assertTrue(opt == null);
		
		sdb.option(SdbPersistenceManager.CONSISTENT_READ);
		opt = sdb.option(PmOptionSdbReadConsistency.ID);
		assertTrue(opt != null);
		assertTrue(opt instanceof PmOptionSdbReadConsistency);
		
		assertEquals(SdbPersistenceManager.CONSISTENT_READ, opt);
	
	}
	
	public void testLocalPriority()
	{
		PmOption opt;
		
		sdb.option(SdbPersistenceManager.NOT_CONSISTENT_READ, PmOptionStickiness.NOT_STICKY);
		sdb.option(SdbPersistenceManager.CONSISTENT_READ, PmOptionStickiness.STICKY);
		opt = sdb.option(PmOptionSdbReadConsistency.ID);
		assertTrue(opt != null);
		assertTrue(opt instanceof PmOptionSdbReadConsistency);
		
		assertEquals(SdbPersistenceManager.NOT_CONSISTENT_READ, opt);
	
	}
	
	public void testThread() throws java.lang.InterruptedException
	{
		sdb.option(SdbPersistenceManager.CONSISTENT_READ, PmOptionStickiness.STICKY);

	    MyOptionRunner r = new MyOptionRunner(sdb, true);
		Thread t = new Thread(r);
		t.start();
		t.wait(1000);
	}
	
	private class MyOptionRunner implements Runnable
	{
		public MyOptionRunner(SdbPersistenceManager sdb, boolean isReadConsistent)
		{
			m_sdb = sdb;
			this.isReadConsistent = isReadConsistent;
		}
		
		public void run() {
			
			assertEquals(isReadConsistent,  m_sdb.isReadConsistent());
		}
		
		private SdbPersistenceManager m_sdb;
		private boolean isReadConsistent;
	}

}
