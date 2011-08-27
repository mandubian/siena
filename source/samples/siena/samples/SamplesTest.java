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


import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import siena.PersistenceManager;
import siena.PersistenceManagerFactory;

/**
 * @author pascal
 *
 */
public abstract class SamplesTest extends TestCase{
	protected PersistenceManager pm;

	List<Class<?>> classes = new ArrayList<Class<?>>();

	public abstract void init();
	public abstract void createClasses(List<Class<?>> classes);
	public abstract PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		init();
		createClasses(classes);
		pm = createPersistenceManager(classes);
		
		PersistenceManagerFactory.install(pm, classes);
	}

}
