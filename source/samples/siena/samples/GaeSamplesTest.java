package siena.samples;
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


import java.util.List;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import siena.PersistenceManager;
import siena.gae.GaePersistenceManager;

/**
 * @author pascal
 *
 */
public abstract class GaeSamplesTest extends SamplesTest{
	private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
		
	public void init() {
		helper.setUp();
	}

	public PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception{
		PersistenceManager pm = new GaePersistenceManager();
		//PersistenceManagerFactory.install(pm, Discovery4GeneratorNone.class);
		pm.init(null);
		
		return pm;
	}
}
