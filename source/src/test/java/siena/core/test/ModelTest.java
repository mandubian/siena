package siena.core.test;

import junit.framework.TestCase;

public class ModelTest extends TestCase {
	
	public void testEqualsAndHashCode() {
		SampleModel a = new SampleModel();
		SampleModel b = new SampleModel();
		
		a.id = 1l;
		b.id = 1l;
		
		a.key = "foo";
		b.key = "foo";
		
		assertFalse(a.equals(null));
		assertFalse(a.equals("xxx"));
		
		assertFalse(a == b);
		assertTrue(a.equals(b));
		assertEquals(a.hashCode(), b.hashCode());
		
		b.id = 2L;
		assertFalse(a.equals(b));
		assertNotSame(a.hashCode(), b.hashCode());
	}

}
