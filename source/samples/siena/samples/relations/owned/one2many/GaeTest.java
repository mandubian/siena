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
package siena.samples.relations.owned.one2many;

import java.util.ArrayList;
import java.util.List;

import siena.Model;
import siena.samples.GaeSamplesTest;

/**
 * @author pascal
 *
 */
public class GaeTest extends GaeSamplesTest{
	public void createClasses(List<Class<?>> classes){
		classes.add(Flea.class);
		classes.add(Dog.class);
	}

	public void test() {
		// creates dog
		Dog pluto = new Dog("Pluto");
		// infects dog with fleas
		List<Flea> fleas = new ArrayList<Flea>();		
		for(int i=0; i<10; i++){
			Flea flea = new Flea("pupuce"+i);
			fleas.add(flea);
			pluto.fleas.asList().add(flea);
		}
		//saves dog + fleas
		pluto.save();
		
		// retrieves master
		Dog plutobis = Dog.getByName("Pluto");
		// verifies the fleas suck the blood from pluto
		for(int i=0; i<plutobis.fleas.asList().size(); i++){
			Flea flea = plutobis.fleas.asList().get(i);
			assertEquals(fleas.get(i).id, flea.id);
			assertEquals(fleas.get(i).name, flea.name);
			assertEquals(fleas.get(i).bloodsource.id, flea.bloodsource.id);
		}
	}
	
	public void test2() {
		// inserts dog
		Dog pluto = new Dog("Pluto");
		pluto.insert();

		// infects dog with fleas
		List<Flea> fleas = new ArrayList<Flea>();		
		for(int i=0; i<10; i++){
			Flea flea = new Flea("pupuce"+i);
			// associates the flea to its dog
			flea.bloodsource = pluto;
			flea.insert();
			fleas.add(flea);
		}

		// retrieves all the fleas from the dog
		// gets the dog
		Dog plutobis = Dog.getByName("Pluto");
		// here the fleas are not yet fetched from datastore
		// ... later ...
		// gets the little bloodsuckers
		// verifies the fleas suck the blood from pluto
		for(int i=0; i<plutobis.fleas.asList().size(); i++){
			Flea flea = plutobis.fleas.asList().get(i);
			assertEquals(fleas.get(i).id, flea.id);
			assertEquals(fleas.get(i).name, flea.name);
			assertEquals(fleas.get(i).bloodsource.id, flea.bloodsource.id);
		}
	}

	public void test3() {
		// creates dog
		Dog pluto = new Dog("Pluto");
		// infects dog with fleas
		List<Flea> fleas = new ArrayList<Flea>();		
		for(int i=0; i<10; i++){
			Flea flea = new Flea("pupuce"+i);
			fleas.add(flea);
			pluto.fleas.asList().add(flea);
		}
		//saves dog + fleas
		pluto.save();
		
		// retrieves master
		Dog plutobis = Dog.getByName("Pluto");
		// verifies the fleas suck the blood from pluto
		for(int i=0; i<plutobis.fleas.asList().size(); i++){
			Flea flea = plutobis.fleas.asList().get(i);
			assertEquals(fleas.get(i).id, flea.id);
			assertEquals(fleas.get(i).name, flea.name);
			assertEquals(fleas.get(i).bloodsource.id, flea.bloodsource.id);
		}
		
		// removes/adds a few flea for the poor dog 
		plutobis.fleas.asList().remove(5);
		plutobis.fleas.asList().remove(7);
		Flea flea10 = new Flea("pupuce10");
		plutobis.fleas.asList().add(flea10);

		// updates the dog
		plutobis.update();
		plutobis = Dog.getByName("Pluto");
		// verifies the fleas suck the blood from pluto
		assertEquals(9, plutobis.fleas.asList().size());
		for(int i=0; i<plutobis.fleas.asList().size(); i++){
			int j = i;
			if(i>=5) j=i+1;
			if(i>=7) j=i+2;
			Flea flearef;
			if(i<8) flearef = fleas.get(j);
			else flearef = flea10;
			Flea flea = plutobis.fleas.asList().get(i);
			assertEquals(flearef.id , flea.id);
			assertEquals(flearef.name, flea.name);
			assertEquals(flearef.bloodsource.id, flea.bloodsource.id);
		}
		
		// verifies flea5/8 are no more associated to pluto
		// verifies flea10 is now associated to pluto
		Flea flea5 = Flea.getByName("pupuce5");
		assertNull(flea5.bloodsource);
		Flea flea8 = Flea.getByName("pupuce8");
		assertNull(flea8.bloodsource);
		Flea flea10bis = Flea.getByName("pupuce10");
		assertEquals(pluto.id, flea10bis.bloodsource.id);
	}
	
	public void test4() {
		// creates dog
		Dog pluto = new Dog("Pluto");
		// infects dog with fleas
		List<Flea> fleas = new ArrayList<Flea>();		
		for(int i=0; i<10; i++){
			Flea flea = new Flea("pupuce"+i);
			fleas.add(flea);
			pluto.fleas.asList().add(flea);
		}
		//saves dog + fleas
		pluto.save();
		
		pluto = Dog.getByName("Pluto");
		Flea flea = pluto.fleas.asList().get(5);
		
		// modifies one flea
		flea.name = flea.name + "_UPD";
		
		// updates the dog but not the flea
		pluto.update();
		
		// retrieves again the dog
		pluto = Dog.getByName("Pluto");
		// retrieves the same flea
		Flea fleabis = pluto.fleas.asList().get(5);
		// verifies it was not updated
		assertNotSame(fleabis.name, flea.name);
		
		// now update the flea
		flea.update();
		
		// retrieves again the dog
		pluto = Dog.getByName("Pluto");
		// retrieves the same flea
		fleabis = pluto.fleas.asList().get(5);
		// verifies it was updated
		assertEquals(fleabis.name, flea.name);
	}
	
	public void test5() {
		// creates dog
		Dog pluto = new Dog("Pluto");
		// infects dog with fleas
		List<Flea> fleas = new ArrayList<Flea>();		
		for(int i=0; i<10; i++){
			Flea flea = new Flea("pupuce"+i);
			fleas.add(flea);
		}
		//saves dog + fleas
		pluto.fleas.asList().addAll(fleas);
		pluto.save();
		
		// fetches the list a first time
		fleas = pluto.fleas.asList();
		
		// updates flea n°5
		Flea flea = Model.getByKey(Flea.class, pluto.fleas.asList().get(5).id);		
		flea.name = flea.name + "_UPD";
		flea.update();
		
		// synchronizes the list (it doesn't re-fetch if already done) 
		pluto.fleas.asList().sync();
		// verifies the updated flea is not synchronized
		Flea fleabis = pluto.fleas.asList().get(5);
		// verifies it was updated
		assertFalse(fleabis.name.equals(flea.name));
		
		// forces synchronize the list (it re-fetches the fleas) 
		pluto.fleas.asList().forceSync();
		// retrieves the same flea
		fleabis = pluto.fleas.asList().get(5);
		// verifies it was updated
		assertEquals(fleabis.name, flea.name);
	}
	
	public void test6() {
		// creates dog
		Dog pluto = new Dog("Pluto");
		// infects dog with fleas having several times the same name
		Flea alpha1 = new Flea("alpha");
		Flea alpha2 = new Flea("alpha");
		Flea beta1 = new Flea("beta");
		Flea beta2 = new Flea("beta");
		
		//saves dog + fleas
		pluto.fleas.asList().addAll(alpha1, alpha2, beta1, beta2);
		pluto.save();
		
		// retrieves again the dog
		pluto = Dog.getByName("Pluto");
		
		// fetches alpha fleas
		List<Flea> alphas = pluto.fleas.asQuery().filter("name", "alpha").order("id").fetch();
		assertEquals(alpha1.name, alphas.get(0).name);
		assertEquals(alpha1.id, alphas.get(0).id);
		assertEquals(alpha2.name, alphas.get(1).name);
		assertEquals(alpha2.id, alphas.get(1).id);
	
		// interates on beta fleas
		Iterable<Flea> betas = pluto.fleas.asQuery().filter("name", "beta").order("id").iter();
		int i=0;
		for(Flea beta:betas){
			if(i==0) {
				assertEquals(beta1.name, beta.name);
				assertEquals(beta1.id, beta.id);
			}
			else if(i==1) {
				assertEquals(beta2.name, beta.name);
				assertEquals(beta2.id, beta.id);
			}
			i++;
		}
}
}
