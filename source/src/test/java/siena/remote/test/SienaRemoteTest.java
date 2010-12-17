/*
 * Copyright 2009 Alberto Gimeno <gimenete at gmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package siena.remote.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import siena.PersistenceManagerFactory;
import siena.SienaException;
import siena.remote.Common;
import siena.remote.RemotePersistenceManager;

public class SienaRemoteTest extends TestCase {

	private static Person TESLA;
	private static Person CURIE;
	private static Person EINSTEIN;

	private static Discovery RADIOACTIVITY;
	private static Discovery RELATIVITY;
	private static Discovery TESLA_COIL;
	
	private ClassLoader cl = this.getClass().getClassLoader();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		PersistenceManagerFactory.install(new MockPersistenceManager(), Person.class);
		
		TESLA = new Person(1, "Nikola", "Tesla", "Smiljam");
		CURIE = new Person(2, "Marie", "Curie", "Warsaw");
		EINSTEIN = new Person(3, "Albert", "Einstein", "Ulm");
		
		RADIOACTIVITY = new Discovery(1, "Radioactivity");
		RELATIVITY = new Discovery(2, "Relativity");
		TESLA_COIL = new Discovery(3, "Tesla Coil");
		
		RADIOACTIVITY.discoverer = CURIE;
		RELATIVITY.discoverer = EINSTEIN;
		TESLA_COIL.discoverer = TESLA;
	}
	
	public void testFillAndParseIds() throws IOException {
		Element root = createSimpleDocument();
		Common.fillRequestElement(TESLA, root, true);
		
		Person person = (Person) Common.parseEntity(root, cl);

		assertEquals(person.id, TESLA.id);
		assertNull(person.firstName);
		assertNull(person.lastName);
		assertNull(person.city);
	}

	public void testFillAndParse() throws IOException {
		Element root = createSimpleDocument();
		Common.fillRequestElement(TESLA, root, false);
		
		Person person = (Person) Common.parseEntity(root, cl);

		assertEquals(TESLA, person);
	}

	public void testFillAndParseDataTypesEmpty() throws IOException {
		Element root = createSimpleDocument();
		
		DataTypes original = new DataTypes();
		
		Common.fillRequestElement(original, root, false);
		
		DataTypes parsed = (DataTypes) Common.parseEntity(root, cl);

		assertEquals(original, parsed);
	}

	public void testFillAndParseDataTypesFull() throws IOException {
		Element root = createSimpleDocument();
		
		DataTypes original = new DataTypes();
		original.typeByte = 1;
		original.typeShort = 2;
		original.typeInt = 3;
		original.typeLong = 4;
		original.typeFloat = 5;
		original.typeDouble = 6;
		original.typeString = "hello";
		original.typeDate = new Date();
		
		Common.fillRequestElement(original, root, false);
		
		DataTypes parsed = (DataTypes) Common.parseEntity(root, cl);
		
		assertEquals(original, parsed);
	}

	public void testFillAndParseDataTypesEmptyString() throws IOException {
		Element root = createSimpleDocument();
		
		DataTypes original = new DataTypes();
		original.typeByte = 1;
		original.typeShort = 2;
		original.typeInt = 3;
		original.typeLong = 4;
		original.typeFloat = 5;
		original.typeDouble = 6;
		original.typeString = "";
		original.typeDate = new Date();
		
		Common.fillRequestElement(original, root, false);
		
		DataTypes parsed = (DataTypes) Common.parseEntity(root, cl);
		
		assertEquals(original, parsed);
	}

	public void testFillAndParseDataTypesNullString() throws IOException {
		Element root = createSimpleDocument();
		
		DataTypes original = new DataTypes();
		original.typeByte = 1;
		original.typeShort = 2;
		original.typeInt = 3;
		original.typeLong = 4;
		original.typeFloat = 5;
		original.typeDouble = 6;
		original.typeString = null;
		original.typeDate = new Date();
		
		Common.fillRequestElement(original, root, false);
		
		DataTypes parsed = (DataTypes) Common.parseEntity(root, cl);
		
		assertEquals(original, parsed);
	}

	public void testFillAndParseRelationship() throws IOException {
		Element root = createSimpleDocument();
		Common.fillRequestElement(RADIOACTIVITY, root, false);
		
		Discovery parsed = (Discovery) Common.parseEntity(root, cl);
		
		assertEquals(RADIOACTIVITY.id, parsed.id);
		assertEquals(RADIOACTIVITY.name, parsed.name);
		assertNotNull(parsed.discoverer);
		assertEquals(RADIOACTIVITY.discoverer.id, parsed.discoverer.id);
		assertNull(parsed.discoverer.firstName);
		assertNull(parsed.discoverer.lastName);
		assertNull(parsed.discoverer.city);
	}
	
	public void testRemote() {
		MockPersistenceManager mock = new MockPersistenceManager();
		PersistenceManagerFactory.install(mock, Person.class);
		
		RemotePersistenceManager remote = new RemotePersistenceManager();
		Properties properties = new Properties();
		properties.setProperty("connector", MockConnector.class.getName());
		properties.setProperty("serializer", MockConnector.class.getName());
		remote.init(properties);
		
		remote.insert(TESLA);
		assertEquals("insert", mock.action);
		assertEquals(TESLA, mock.object);
		
		remote.update(TESLA);
		assertEquals("update", mock.action);
		assertEquals(TESLA, mock.object);
		
		Person expected = new Person();
		expected.id = TESLA.id;
		
		remote.delete(TESLA);
		assertEquals("delete", mock.action);
		assertEquals(expected, mock.object);
		
		remote.get(TESLA);
		assertEquals("get", mock.action);
		assertEquals(expected, mock.object);
		
		remote.createQuery(Person.class).fetch();
		assertEquals(0, mock.lastQuery.filters.size());
		assertEquals(0, mock.lastQuery.orders.size());
		
		remote.createQuery(Person.class).order("firstName").order("lastName").fetch();
		assertEquals(0, mock.lastQuery.filters.size());
		assertEquals(Arrays.asList("firstName", "lastName"), mock.lastQuery.orders);
		
		remote.createQuery(Person.class)
			.filter("city", "Ulm")
			.filter("firstName", "Albert")
			.filter("lastName", null)
			.order("firstName")
			.order("lastName")
			.fetch();
		assertEquals(3, mock.lastQuery.filters.size());
		assertEquals("city", ((Object[])mock.lastQuery.filters.get(0))[0]);
		assertEquals("Ulm", ((Object[])mock.lastQuery.filters.get(0))[1]);
		assertEquals("firstName", ((Object[])mock.lastQuery.filters.get(1))[0]);
		assertEquals("Albert", ((Object[])mock.lastQuery.filters.get(1))[1]);
		assertEquals("lastName", ((Object[])mock.lastQuery.filters.get(2))[0]);
		assertEquals(null, ((Object[])mock.lastQuery.filters.get(2))[1]);
		assertEquals(Arrays.asList("firstName", "lastName"), mock.lastQuery.orders);
	}
	
	public void testSecurity() {
		MockPersistenceManager mock = new MockPersistenceManager();
		PersistenceManagerFactory.install(mock, Person.class);
		
		RemotePersistenceManager remote = new RemotePersistenceManager();
		Properties properties = new Properties();
		properties.setProperty("connector", MockConnector.class.getName());
		properties.setProperty("serializer", MockConnector.class.getName());
		properties.setProperty("key", "siena");
		remote.init(properties);
		
		remote.createQuery(Person.class).fetch();
	}
	
	public void testFailSecurity() {
		MockPersistenceManager mock = new MockPersistenceManager();
		PersistenceManagerFactory.install(mock, Person.class);
		
		RemotePersistenceManager remote = new RemotePersistenceManager();
		Properties properties = new Properties();
		properties.setProperty("connector", MockConnector.class.getName());
		properties.setProperty("serializer", MockConnector.class.getName());
		properties.setProperty("key", "siena");
		remote.init(properties);
		
		MockConnector.key = "1234";
		
		try {
			remote.createQuery(Person.class).fetch();
		} catch(SienaException e) {
			return;
		}
		fail("It should have failed due to invalid hash");
	}
	
	private Element createSimpleDocument(String rootName) {
		return DocumentHelper.createDocument().addElement(rootName);
	}
	
	private Element createSimpleDocument() {
		return createSimpleDocument("root");
	}

}
