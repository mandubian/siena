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

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.Table;
import siena.core.batch.Batch;

@Table("sample_unowned_onetoone_flea")
public class Flea extends Model{

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	public String name;
	
	public Dog bloodsource;

	public static Query<Flea> all() {
		return Model.all(Flea.class);
	}
	
	public static Batch<Flea> batch() {
		return Model.batch(Flea.class);
	}
	
	public Flea() {
	}

	public Flea(String name) {
		this.name = name;
	}

	public String toString() {
		return "id: "+id+", name: "+name + " - bloodsource:"+bloodsource;
	}
	
	public static Flea getByName(String name){
		return all().filter("name", name).get();
	}
}
