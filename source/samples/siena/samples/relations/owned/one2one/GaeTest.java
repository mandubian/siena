/*
 * Copyright 2011 Pascal Voitot <pascal.voitot.dev@gmail.com>
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
package siena.samples.relations.owned.one2one;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManagerFactory;
import siena.gae.GaePersistenceManager;
import siena.samples.GaeSamplesTest;
import siena.samples.relations.referenced.unowned.one2one2ways.Planet;
import siena.samples.relations.referenced.unowned.one2one2ways.Satellite;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * @author pascal
 *
 */
public class GaeTest extends GaeSamplesTest{
	public void createClasses(List<Class<?>> classes){
		classes.add(Person.class);
		classes.add(Dog.class);
	}

	public void test() {
		Dog pluto = new Dog("Pluto");
		
		Person tom = new Person("Tom");
		tom.dog.set(pluto);
		tom.save();
		
		Person tombis = Person.getByName("Tom");
		Dog plutobis = tombis.dog.get();
		assertEquals(tom.id, tombis.id);
		assertEquals(tom.name, tombis.name);
		assertEquals(pluto.id, plutobis.id);
		assertEquals(pluto.name, plutobis.name);
		assertEquals(tom.id, plutobis.master.id);
		assertEquals(tom.name, plutobis.master.name);
	}

}
