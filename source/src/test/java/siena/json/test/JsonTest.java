package siena.json.test;

import static siena.Json.list;
import static siena.Json.map;
import static siena.Json.sortedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import siena.Json;

public class JsonTest extends TestCase {
	
	public void testBuildEmptyList() {
		Json json = Json.list();

		assertTrue(json.isList());
		assertTrue(json.isEmpty());
		
		assertFalse(json.isBoolean());
		assertFalse(json.isMap());
		assertFalse(json.isNumber());
		assertFalse(json.isString());
		
		assertEquals(0, json.size());
		assertEquals("[]", json.toString());
	}
	
	public void testBuildEmptyMap() {
		Json json = Json.map();

		assertTrue(json.isMap());
		assertTrue(json.isEmpty());
		
		assertFalse(json.isBoolean());
		assertFalse(json.isList());
		assertFalse(json.isNumber());
		assertFalse(json.isString());
		
		assertEquals(0, json.size());
		assertEquals("{}", json.toString());
	}
	
	public void testBuildString() {
		Json json = new Json("foobar");

		assertTrue(json.isString());
		
		assertFalse(json.isBoolean());
		assertFalse(json.isList());
		assertFalse(json.isMap());
		assertFalse(json.isEmpty());
		
		assertEquals(1, json.size());
		assertEquals("\"foobar\"", json.toString());
	}
	
	public void testBuildComplex() {
		Json json = Json.list(1, 2, 3, 4, true, false, null, Json.map().put("foo", 1).put("bar", 2));

		assertEquals(1, json.at(0).asInt());
		assertEquals(2, json.at(1).asInt());
		assertEquals(3, json.at(2).asInt());
		assertEquals(4, json.at(3).asInt());
		assertTrue(json.at(4).asBoolean());
		assertFalse(json.at(5).asBoolean());
		assertTrue(json.at(6).isNull());
		assertTrue(json.at(7).isMap());
		assertEquals(2, json.at(7).size());
		assertEquals(1, json.at(7).get("foo").asInt());
		assertEquals(2, json.at(7).get("bar").asInt());
	}
	
	public void testParseList() {
		Json json = Json.loads("[true, false, null, 1234, \"foobar\"]");
		
		assertTrue(json.isList());
		assertEquals(5, json.size());
		assertTrue(json.at(0).isBoolean());
		assertTrue(json.at(0).asBoolean());
		assertTrue(json.at(1).isBoolean());
		assertFalse(json.at(1).asBoolean());
		assertTrue(json.at(2).isNull());
		assertTrue(json.at(3).isNumber());
		assertEquals(1234, json.at(3).asInt());
		assertTrue(json.at(4).isString());
		assertEquals("foobar", json.at(4).asString());
	}
	
	public void testEscapeCharacters() {
		Json json = Json.loads("[\"\\b\\f\\n\\r\\t\\\"\\/\\u0041\"]");
		
		assertTrue(json.isList());
		assertEquals(1, json.size());
		assertTrue(json.at(0).isString());
		
		assertEquals("[\"\\b\\f\\n\\r\\t\\\"\\/A\"]", json.toString());
	}
	
	public void testParseMap() {
		Json json = Json.loads("{\"foo\": 1234, \"bar\": true}");
		
		assertTrue(json.isMap());
		assertEquals(2, json.size());
		
		Json foo = json.get("foo");
		assertTrue(foo.isNumber());
		assertEquals(1234, foo.asInt());
		
		Json bar = json.get("bar");
		assertTrue(bar.isBoolean());
		assertEquals(true, bar.asBoolean());
	}
	
	public void testParseComplex() {
		Json json = null;
		
		json = Json.loads("{}");
		assertEquals("{}", json.toString());
		
		json = Json.loads("[{}]");
		assertEquals("[{}]", json.toString());
		
		json = Json.loads("{}");
		assertEquals("{}", json.toString());
		
		json = Json.loads("[{}, {}, {}]");
		assertEquals("[{}, {}, {}]", json.toString());
		
		json = Json.loads("{\"foo\": 1234, \"bar\": [1, 2, 3, [{}], true, false, null, {}]}");
		
		assertTrue(json.isMap());
		assertEquals(2, json.size());
		assertEquals(1234, json.get("foo").asInt());
		
		Json list = json.get("bar");
		assertTrue(list.isList());
		assertEquals(8, list.size());
		assertEquals(1, list.at(0).asInt());
		assertEquals(2, list.at(1).asInt());
		assertEquals(3, list.at(2).asInt());
		
		assertTrue(list.at(3).isList());
		assertEquals(1, list.at(3).size());
		assertTrue(list.at(3).at(0).isMap());
		assertTrue(list.at(3).at(0).isEmpty());
		
		assertTrue(list.at(4).asBoolean());
		assertFalse(list.at(5).asBoolean());
		assertTrue(list.at(6).isNull());
		assertTrue(list.at(7).isMap());
		assertTrue(list.at(7).isEmpty());
	}
	
	public void testPutAll() {
		Json map = Json.map().put("foo", 1).put("bar", 2);
		
		Json other = Json.map().put("baz", 3);
		other.putAll(map);
		
		assertEquals(3, other.size());
		assertEquals(3, other.get("baz").asInt());
		assertEquals(2, other.get("bar").asInt());
		assertEquals(1, other.get("foo").asInt());
	}
	
	public void testArray() {
		Json list = new Json(new Object[]{1, 2, 3});
		assertEquals(3, list.size());
		assertEquals(1, list.at(0).asInt());
		assertEquals(2, list.at(1).asInt());
		assertEquals(3, list.at(2).asInt());
		assertEquals("[1, 2, 3]", list.toString());
	}
	
	public void testCollection() {
		Json list = new Json(Arrays.asList(new Object[]{1, 2, 3}));
		assertEquals(3, list.size());
		assertEquals(1, list.at(0).asInt());
		assertEquals(2, list.at(1).asInt());
		assertEquals(3, list.at(2).asInt());
		assertEquals("[1, 2, 3]", list.toString());
	}
	
	public void testMap() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("foo", 1);
		
		Json map = new Json(m);
		assertTrue(map.isMap());
		assertEquals(1, map.size());
		assertEquals(1, map.get("foo").asInt());
		assertEquals("{\"foo\": 1}", map.toString());
	}
	
	public void testPutComplex() {
		List<Integer> list1 = Arrays.asList(new Integer[]{1, 2, 3});
		List<String>  list2 = Arrays.asList(new String[]{"foo", "bar", "baz"});
		
		List<List<?>> objects = new ArrayList<List<?>>();;
		objects.add(list1);
		objects.add(list2);
		
		Json map = Json.map().put("foo", objects);
		
		Json list = map.get("foo");
		assertEquals(2, list.size());
		
		assertEquals(3, list.at(0).size());
		assertEquals(1, list.at(0).at(0).asInt());
		assertEquals(2, list.at(0).at(1).asInt());
		assertEquals(3, list.at(0).at(2).asInt());

		assertEquals(3, list.at(1).size());
		assertEquals("foo", list.at(1).at(0).str());
		assertEquals("bar", list.at(1).at(1).str());
		assertEquals("baz", list.at(1).at(2).str());
	}
	
	public void testAddAll() {
		Json json = Json.list(1, 2, 3);
		json.addAll(Json.list(4, 5, 6));
		
		assertEquals(6, json.size());
	}
	
	public void testAddAt() {
		Json json = Json.list(1, 3);
		json.addAt(1, 2);
		
		assertEquals(3, json.size());
		assertEquals(1, json.at(0).asInt());
		assertEquals(2, json.at(1).asInt());
		assertEquals(3, json.at(2).asInt());
	}
	
	public void testRemoveAt() {
		Json json = Json.list(1, 2, 3);
		json.removeAt(1);

		assertEquals(2, json.size());
		assertEquals(1, json.at(0).asInt());
		assertEquals(3, json.at(1).asInt());
	}
	
	public void testIndexOf() {
		Json json = Json.list(1, 2, 3, "hello", true, false, null);

		assertEquals(0, json.indexOf(1));
		assertEquals(1, json.indexOf(2));
		assertEquals(2, json.indexOf(3));
		assertEquals(3, json.indexOf("hello"));
		assertEquals(4, json.indexOf(true));
		assertEquals(5, json.indexOf(false));
		assertEquals(6, json.indexOf(null));
	}
	
	public void testRemoveKey() {
		Json json = Json.map().put("foo", "bar");
		json.remove("foo");
		assertTrue(json.isEmpty());
	}
	
	public void testSortedMap() {
		Json json = Json.sortedMap().put("1", 1).put("400", 400).put("3", 3).put("2", 2);
		
		Iterator<String> keys = json.keys().iterator();
		assertEquals("1", keys.next());
		assertEquals("2", keys.next());
		assertEquals("3", keys.next());
		assertEquals("400", keys.next());
	}
	
	public void testSortedMap2() {
		Json json = Json.sortedMap().put("2009-01", 1).put("2008-10", 400);
		Iterator<String> keys = json.keys().iterator();
		assertEquals("2008-10", keys.next());
		assertEquals("2009-01", keys.next());
	}
	
	public void testContains() {
		Json json = Json.list(1, 2, 3);
		assertTrue(json.contains(1));
		assertTrue(json.contains(2));
		assertTrue(json.contains(3));
		assertFalse(json.contains(4));
	}
	
	public void testFormatAndParseNumbers() {
		Json json = Json.loads("[1.0, 2.0, 3, 4, 5, 6.1, 1.0e12, 2e12]");
		String s = json.toString();
		assertTrue(s.contains("1.0"));
		assertTrue(s.contains("2.0"));
		assertFalse(s.contains("3.0"));
		assertFalse(s.contains("4.0"));
		assertFalse(s.contains("5.0"));
		assertTrue(s.contains("6.1"));
		assertTrue(s.contains("1.0E12"));
		assertTrue(s.contains("2.0E12"));
	}
	
	public void testRemoveAll() {
		Json list = Json.list(1, 2, 3);
		list.removeAll(Json.list(1, 2));
		assertEquals(1, list.size());
		assertEquals(3, list.at(0).asInt());
		
		Json map = Json.map().put("foo", 1).put("bar", 2);
		map.removeAll(Json.map().put("foo", 1));
		assertEquals(1, map.size());
		assertEquals(2, map.get("bar").asInt());
	}
	
	public void testSumIntegers() {
		Json map1 = Json.map().put("a", 1);
		Json map2 = Json.map().put("a", 2).put("b", 5);
		
		map1.sumIntegers(map2);

		assertEquals(3, map1.get("a").asInt());
		assertEquals(5, map1.get("b").asInt());
	}
	
	public void testSumDoubles() {
		Json map1 = Json.map().put("a", 1.5);
		Json map2 = Json.map().put("a", 2.5).put("b", 5);
		
		map1.sumReals(map2);

		assertEquals(4, map1.get("a").asInt());
		assertEquals(5, map1.get("b").asInt());
	}
	
	public void sumInteger() {
		Json map1 = Json.map().put("a", 1);
		map1.sumInteger("a", 2);
		assertEquals(3, map1.get("a").asInt());
		
		map1 = Json.map();
		map1.sumInteger("a", 2);
		assertEquals(2, map1.get("a").asInt());
	}
	
	public void sumReal() {
		Json map1 = Json.map().put("a", 1.5);
		map1.sumReal("a", 2.5);
		assertEquals(4, map1.get("a").asInt());
		
		map1 = Json.map();
		map1.sumReal("a", 2.5);
		assertEquals(2.5, map1.get("a").asInt());
	}
	
	public void testPutDefault() {
		Json map = Json.map();
		map.putDefault("a", 2);
		assertEquals(2, map.get("a").asInt());
		map.putDefault("a", 3);
		assertEquals(2, map.get("a").asInt());
	}
	
	public void testEqualsTo() {
		Json a = map();
		Json b = map();
		assertTrue(a.equalsTo(b));

		a = list();
		b = list();
		assertTrue(a.equalsTo(b));

		a = list("foo", "bar");
		b = list("foo", "bar");
		assertTrue(a.equalsTo(b));
		
		a = map().put("foo", "bar");
		b = map().put("foo", "bar");
		assertTrue(a.equalsTo(b));
		
		a = map().put("foo", list("bar"));
		b = map().put("foo", list("bar"));
		assertTrue(a.equalsTo(b));
		
		a = map();
		b = sortedMap();
		assertTrue(a.equalsTo(b));
		
		a = new Json(true);
		b = new Json(true);
		assertTrue(a.equalsTo(b));
		
		a = new Json(1);
		b = new Json(1l);
		assertTrue(a.equalsTo(b));
		
		a = new Json(1.0);
		b = new Json(1l);
		assertTrue(a.equalsTo(b));
		
		a = new Json(null);
		b = new Json(null);
		assertTrue(a.equalsTo(b));

		a = map();
		b = list();
		assertFalse(a.equalsTo(b));

		a = list();
		b = map();
		assertFalse(a.equalsTo(b));
		
		a = list(1, 2, 3, 4);
		b = list(1, 2, 3);
		assertFalse(a.equalsTo(b));
		
		a = map().put("foo", 1);
		b = map().put("bar", 1);
		assertFalse(a.equalsTo(b));
		
		a = new Json(1);
		b = new Json(null);
		assertFalse(a.equalsTo(b));
		
		a = list(1, 3);
		b = list(1, 2);
		assertFalse(a.equalsTo(b));

		a = map();
		b = map().put("1", 2);
		assertFalse(a.equalsTo(b));
		
		a = map();
		b = a;
		assertTrue(a.equalsTo(b));
	}
	
	public void testFind() {
		Json data = map().put("foo", list(1, 2, list(1, 2, map().put("bar", 1))));
		Json result = data.find("foo", 2, 2, "bar");
		assertNotNull(result);
		assertEquals(1, result.asInt());
		
		// try to call at() in a map
		result = data.find(1);
		assertNull(result);
		
		// try to call get() in a list
		result = data.find("foo", "bar");
		assertNull(result);
		
		// key not found in map
		result = data.find("bar", "baz");
		assertNull(result);
		
		// out of bounds
		result = data.find("foo", 3);
		assertNull(result);
	}
	
	public void testEnumerations() {
		Json data = list(Gender.MALE);
		assertEquals("[\"MALE\"]", data.toString());
		assertEquals(Gender.MALE.name(), data.at(0).str());
	}
	
	public enum Gender { MALE, FEMALE };

}
