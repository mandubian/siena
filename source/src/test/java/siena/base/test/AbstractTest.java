package siena.base.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;

public abstract class AbstractTest extends TestCase {
	
	protected PersistenceManager pm;

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		init();
		createClasses(classes);
		pm = createPersistenceManager(classes);
		
		PersistenceManagerFactory.install(pm, classes);
		
		postInit();
	}

}
