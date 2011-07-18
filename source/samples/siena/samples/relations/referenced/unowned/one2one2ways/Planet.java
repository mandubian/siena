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

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.Table;
import siena.core.Referenced;
import siena.core.batch.Batch;

@Table("sample_unowned_one2one2ways_planet")
public class Planet extends Model{

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	public String name;
	
	// The relation to the cat
	// REMIND THAT THIS ANNOTATION CAN BE SKIPPED
	@Referenced
	public Satellite sat;
	
	public static Query<Planet> all() {
		return Model.all(Planet.class);
	}
	
	public static Batch<Planet> batch() {
		return Model.batch(Planet.class);
	}
	
	public static Planet getByName(String name){
		return all().filter("name", name).get();
	}
	
	public Planet() {
	}

	public Planet(String name) {
		this.name = name;
	}

	public String toString() {
		return "id: "+id+", name: "+name+"satellite:"+sat;
	}

}
