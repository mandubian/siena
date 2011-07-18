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
package siena.samples.relations.referenced.unowned.one2one2ways;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManagerFactory;
import siena.gae.GaePersistenceManager;
import siena.samples.GaeSamplesTest;
import siena.samples.SamplesTest;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;


/**
 * @author pascal
 *
 */
public class GaeTest extends GaeSamplesTest{
	public void createClasses(List<Class<?>> classes){
		classes.add(Planet.class);
		classes.add(Satellite.class);
	}

	public void test() {
		// First create the satellite without referencing the planet
		Satellite moon = new Satellite("Moon");
		moon.save();
		// Second create the planet and link it to the satellite
		Planet earth = new Planet("Earth");
		earth.sat = moon;
		earth.save();
		// Finally link the satellite to the planet
		moon.planet = earth;
		moon.update();
		
		Planet earthbis = Planet.getByName("Earth");
		// To fully fill the sat field, get it explicitly
		earthbis.sat.get();
		assertEquals(earth.id, earthbis.id);
		assertEquals(earth.name, earthbis.name);
		assertEquals(moon.id, earthbis.sat.id);
		assertEquals(moon.name, earthbis.sat.name);

		Satellite moonbis = Satellite.getByName("Moon");
		// To fully fill the planet field, get it explicitly
		moonbis.planet.get();
		assertEquals(moon.id, moonbis.id);
		assertEquals(moon.name, moonbis.name);
		assertEquals(earth.id, moonbis.planet.id);
		assertEquals(earth.name, moonbis.planet.name);
	}

}
