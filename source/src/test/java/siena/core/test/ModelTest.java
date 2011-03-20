package siena.core.test;

import siena.base.test.model.SampleModelMultipleKeys;
import junit.framework.TestCase;

public class ModelTest extends TestCase {
	
	public void testEqualsAndHashCode() {
		SampleModelMultipleKeys a = new SampleModelMultipleKeys();
		SampleModelMultipleKeys b = new SampleModelMultipleKeys();
		
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
