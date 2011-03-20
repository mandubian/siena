package siena.core.test;

import java.lang.reflect.Field;
import java.util.List;

import junit.framework.TestCase;
import siena.ClassInfo;
import siena.base.test.model.SampleModelMultipleKeys;

public class ClassInfoTest extends TestCase {
	
	public void testAcceptedFields() {
		ClassInfo info = ClassInfo.getClassInfo(SampleModelMultipleKeys.class);
		
		assertNotNull(info);
		
		List<Field> fields = null;
		
		fields = info.allFields;
		assertEquals(5, info.allFields.size());
		assertEquals("id", fields.get(0).getName());
		assertEquals("key", fields.get(1).getName());
		assertEquals("privateField", fields.get(2).getName());
		assertEquals("publicField", fields.get(3).getName());
		assertEquals("relationship", fields.get(4).getName());

		fields = info.generatedKeys;
		assertEquals(1, fields.size());
		assertEquals("id", fields.get(0).getName());
		
		fields = info.insertFields;
		assertEquals(4, fields.size());
		assertEquals("key", fields.get(0).getName());
		assertEquals("privateField", fields.get(1).getName());
		assertEquals("publicField", fields.get(2).getName());
		assertEquals("relationship", fields.get(3).getName());
		
		fields = info.updateFields;
		assertEquals(3, fields.size());
		assertEquals("privateField", fields.get(0).getName());
		assertEquals("publicField", fields.get(1).getName());
		assertEquals("relationship", fields.get(2).getName());
		
		String[] columns = ClassInfo.getColumnNames(fields.get(2));
		assertNotNull(columns);
		assertEquals(2, columns.length);
		assertEquals("p_id", columns[0]);
		assertEquals("p_key", columns[1]);
		
		fields = info.keys;
		assertEquals(2, fields.size());
		assertEquals("id", fields.get(0).getName());
		assertEquals("key", fields.get(1).getName());
		
		assertEquals("SampleModelMultipleKeys", info.tableName);
	}

}
