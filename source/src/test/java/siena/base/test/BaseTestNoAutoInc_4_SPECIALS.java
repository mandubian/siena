package siena.base.test;

import static siena.Json.map;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import siena.Model;
import siena.base.test.model.Address;
import siena.base.test.model.AutoInc;
import siena.base.test.model.BigDecimalDoubleModelStringId;
import siena.base.test.model.BigDecimalModel;
import siena.base.test.model.BigDecimalModelNoPrecisionStringId;
import siena.base.test.model.BigDecimalModelStringId;
import siena.base.test.model.BigDecimalStringModelStringId;
import siena.base.test.model.Contact;
import siena.base.test.model.DataTypes;
import siena.base.test.model.PersonStringID;
import siena.base.test.model.DataTypes.EnumLong;
import siena.base.test.model.DiscoveryStringId;
import siena.base.test.model.EnumTest;
import siena.base.test.model.MultipleKeys;
import siena.base.test.model.PersonUUID;
import siena.base.test.model.PolymorphicModel;
import siena.base.test.model.PolymorphicModelStringId;

public abstract class BaseTestNoAutoInc_4_SPECIALS extends BaseTestNoAutoInc_BASE {
	
	public void createClasses(List<Class<?>> classes) {
		classes.add(PersonStringID.class);
		classes.add(DiscoveryStringId.class);
		classes.add(PersonUUID.class);
		classes.add(DiscoveryStringId.class);
		classes.add(DataTypes.class);
		classes.add(BigDecimalModelStringId.class);
		classes.add(BigDecimalStringModelStringId.class);
		classes.add(BigDecimalModelNoPrecisionStringId.class);
		classes.add(BigDecimalDoubleModelStringId.class);
		classes.add(PolymorphicModel.class);
		classes.add(PolymorphicModelStringId.class);
		classes.add(EnumTest.class);
	}
	
	public void postInit() {
		for (Class<?> clazz : classes) {
			if(!Modifier.isAbstract(clazz.getModifiers())){
				pm.createQuery(clazz).delete();			
			}
		}
	}
	
	public void testGetObjectNotFound() {
		try {
			getPersonUUID("");
			fail();
		} catch(Exception e) {
			System.out.println("Everything is OK");
		}
		
		assertNull(pm.createQuery(PersonUUID.class).filter("firstName", "John").get());
	}
	
	public void testDeleteObjectNotFound() {
		try {
			PersonUUID p = new PersonUUID();
			pm.delete(p);
			fail();
		} catch(Exception e) {
			System.out.println("Everything is OK");
		}
	}
	
	public void testAutoincrement() {
		if(!supportsAutoincrement()) return;

		AutoInc first = new AutoInc();
		first.name = "first";
		pm.insert(first);
		assertTrue(first.id > 0);

		AutoInc second = new AutoInc();
		second.name = "second";
		pm.insert(second);
		assertTrue(second.id > 0);
		
		assertTrue(second.id > first.id);
	}
	
	public void testRelationship() {
		DiscoveryStringId radioactivity = new DiscoveryStringId("Radioactivity", StringID_CURIE);
		DiscoveryStringId relativity = new DiscoveryStringId("Relativity", StringID_EINSTEIN);
		DiscoveryStringId teslaCoil = new DiscoveryStringId("Tesla Coil", StringID_TESLA);
		
		pm.insert(radioactivity);
		pm.insert(relativity);
		pm.insert(teslaCoil);

		DiscoveryStringId relativity2 = pm.createQuery(DiscoveryStringId.class).filter("discoverer", StringID_EINSTEIN).get();
		assertTrue(relativity.name.equals(relativity2.name));

	}
	
	public void testMultipleKeys() {
		if(!supportsMultipleKeys()) return;
		
		MultipleKeys a = new MultipleKeys();
		a.id1 = "aid1";
		a.id2 = "aid2";
		a.name = "first";
		a.parent = null;
		pm.insert(a);

		MultipleKeys b = new MultipleKeys();
		b.id1 = "bid1";
		b.id2 = "bid2";
		b.name = "second";
		b.parent = null;
		pm.insert(b);
		
		b.parent = a;
		pm.update(b);
	}
	
	public void testDataTypesNull() {
		DataTypes dataTypes = new DataTypes();
		pm.insert(dataTypes);
		
		assertEqualsDataTypes(dataTypes, pm.createQuery(DataTypes.class).get());
	}
	
	public void testDataTypesNotNull() {
		char[] c = new char[501];
		Arrays.fill(c, 'x');
		
		DataTypes dataTypes = new DataTypes();
		dataTypes.typeByte = 1;
		dataTypes.typeShort = 2;
		dataTypes.typeInt = 3;
		dataTypes.typeLong = 4;
		dataTypes.typeFloat = 5;
		dataTypes.typeDouble = 6;
		dataTypes.typeDate = new Date();
		dataTypes.typeString = "hello";
		dataTypes.typeLargeString = new String(c);
		dataTypes.typeJson = map().put("foo", "bar");
		dataTypes.addresses = new ArrayList<Address>();
		dataTypes.addresses.add(new Address("Castellana", "Madrid"));
		dataTypes.addresses.add(new Address("Diagonal", "Barcelona"));
		dataTypes.contacts = new HashMap<String, Contact>();
		dataTypes.contacts.put("id1", new Contact("Somebody", Arrays.asList("foo", "bar")));
		
		dataTypes.shortShort = Short.MAX_VALUE;
		dataTypes.intInt = Integer.MAX_VALUE;
		dataTypes.longLong = Long.MAX_VALUE;
		dataTypes.boolBool = Boolean.TRUE;
		
		// Blob
		dataTypes.typeBlob = new byte[] { 
				(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
				(byte)0x10,	(byte)0X11, (byte)0xF0, (byte)0xF1, 
				(byte)0xF9,	(byte)0xFF };
		
		dataTypes.typeEnum = EnumLong.ALPHA;
		
		pm.insert(dataTypes);
		
		// to test that fields are read back correctly
		pm.createQuery(DataTypes.class).filter("id", dataTypes.id).get();
		
		DataTypes same = pm.createQuery(DataTypes.class).get();
		assertEqualsDataTypes(dataTypes, same);
	}
	
	public void testBigDecimal() {
		BigDecimalModelStringId bigdec = 
			new BigDecimalModelStringId("test", new BigDecimal("123456789.0123456890"));
		pm.insert(bigdec);
		
		BigDecimalModelStringId bigdec2 = pm.getByKey(BigDecimalModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		bigdec = 
			new BigDecimalModelStringId("test2",
					new BigDecimal("999999999.9999999999"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		//-100.5
		bigdec = 
			new BigDecimalModelStringId("test3", new BigDecimal("-100.5000000000"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
	}
	
	public void testBigDecimalNoPrecision() {
		BigDecimalModelNoPrecisionStringId bigdec = 
			new BigDecimalModelNoPrecisionStringId("test", new BigDecimal("123456789.01"));
		pm.insert(bigdec);
		
		BigDecimalModelNoPrecisionStringId bigdec2 = pm.getByKey(BigDecimalModelNoPrecisionStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		bigdec = 
			new BigDecimalModelNoPrecisionStringId(
					"test2", 
					new BigDecimal("999999999.99"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalModelNoPrecisionStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		//-100.5
		bigdec = 
			new BigDecimalModelNoPrecisionStringId("test3", new BigDecimal("-100.50"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalModelNoPrecisionStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
	}
	
	public void testBigDecimalString() {
		BigDecimalStringModelStringId bigdec = 
			new BigDecimalStringModelStringId("test", new BigDecimal("123456789.0123456890"));
		pm.insert(bigdec);
		
		BigDecimalStringModelStringId bigdec2 = pm.getByKey(BigDecimalStringModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		bigdec = 
			new BigDecimalStringModelStringId(
					"test2",
					new BigDecimal("999999999.9999999999"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalStringModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		//-100.5
		bigdec = 
			new BigDecimalStringModelStringId("test3", new BigDecimal("-100.5000000000"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalStringModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
	}
	
	public void testBigDecimalDouble() {
		BigDecimalDoubleModelStringId bigdec = 
			new BigDecimalDoubleModelStringId("test", new BigDecimal("123456789.012345"));
		pm.insert(bigdec);
		
		BigDecimalDoubleModelStringId bigdec2 = pm.getByKey(BigDecimalDoubleModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		bigdec = 
			new BigDecimalDoubleModelStringId(
					"test2",
					new BigDecimal("999999999.9999999999"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalDoubleModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
		
		//-100.5
		bigdec = 
			new BigDecimalDoubleModelStringId("test3", new BigDecimal("-100.5000000000"));
		pm.insert(bigdec);
		
		bigdec2 = pm.getByKey(BigDecimalDoubleModelStringId.class, bigdec.id);
		assertEquals(bigdec, bigdec2);
	}
	
	public void testPolymorphic() {
		PolymorphicModelStringId<String> poly = new PolymorphicModelStringId<String>("test", "test2");
		pm.insert(poly);
		
		PolymorphicModelStringId poly2 = pm.getByKey(PolymorphicModelStringId.class, poly.id);
		assertEquals(poly, poly2);
	}
	
	public void testPolymorphic2() {
		List<String> arr = new ArrayList<String>();
		arr.add("alpha");
		arr.add("beta");
		PolymorphicModelStringId<List<String>> poly = new PolymorphicModelStringId<List<String>>("test", arr);
		pm.insert(poly);
		
		PolymorphicModelStringId<List<String>> poly2 = pm.getByKey(PolymorphicModelStringId.class, poly.id);
		assertEquals(poly, poly2);
	}

	public void testEnum() {
		EnumTest e = new EnumTest();
		e.role = EnumTest.UserRole.ADMIN;
		e.insert();
		
		List<EnumTest> es = Model.all(EnumTest.class).filter("role", EnumTest.UserRole.ADMIN).fetch();
		assertTrue(!es.isEmpty());
		assertEquals(e, es.get(0));
	}
}
