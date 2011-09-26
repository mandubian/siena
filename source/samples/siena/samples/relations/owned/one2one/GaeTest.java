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
		classes.add(Dog.class);
		classes.add(Person.class);
	}

	public void test() {
		// creates dog
		Dog pluto = new Dog("Pluto");
		// creates master
		Person tom = new Person("Tom");
		// associates dog to master
		tom.dog.set(pluto);
		// saves both
		tom.save();
		
		// retrieves master
		Person tombis = Person.getByName("Tom");
		// gets dog (it is also extracted from the DB there)
		Dog plutobis = tombis.dog.get();
		assertEquals(tom.id, tombis.id);
		assertEquals(tom.name, tombis.name);
		assertEquals(pluto.id, plutobis.id);
		assertEquals(pluto.name, plutobis.name);
		assertEquals(tom.id, plutobis.master.id);
		assertEquals(tom.name, plutobis.master.name);
		
		// updates dog data
		pluto.name = "Pluto_2";
		pluto.update();
		
		// proves the One<T> can't be aware of the update of the dog
		Dog plutoAfter = tombis.dog.get();
		assertEquals(pluto.id, plutoAfter.id);		
		// Still Pluto and not Pluto2
		assertEquals("Pluto", plutoAfter.name);
		assertEquals(tom.id, plutoAfter.master.id);
		assertEquals(tom.name, plutoAfter.master.name);
		
		// forces the resync of the dog to get latest version
		plutoAfter = tombis.dog.forceSync().get();
		assertEquals(pluto.id, plutoAfter.id);
		assertEquals("Pluto_2", plutoAfter.name);
		assertEquals(tom.id, plutoAfter.master.id);
		
		// creates another dog
		Dog medor = new Dog("Medor");
		// changes Tom's dog
		tom.dog.set(medor);
		// saves both
		tom.update();
		
		// resync the dog
		Dog medorAfter = tombis.dog.forceSync().get();
		assertEquals(medor.id, medorAfter.id);
		assertEquals(medor.name, medorAfter.name);
		assertEquals(tom.id, medorAfter.master.id);
		assertEquals(tom.name, medorAfter.master.name);
		
		// retrieves again the dog from the master
		tombis = Person.getByName("Tom");
		medorAfter = tombis.dog.get();
		assertEquals(medor.id, medorAfter.id);
		assertEquals(medor.name, medorAfter.name);
		assertEquals(tom.id, medorAfter.master.id);
		assertEquals(tom.name, medorAfter.master.name);
	}

}
