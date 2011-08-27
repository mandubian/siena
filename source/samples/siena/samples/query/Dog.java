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

package siena.samples.query;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Query;
import siena.Table;
import siena.core.batch.Batch;

@Table("sample_unowned_onetoone_dog")
public class Dog extends Model{

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	public String name;
	
	public Person master;

	public static Query<Dog> all() {
		return Model.all(Dog.class);
	}
	
	public static Batch<Dog> batch() {
		return Model.batch(Dog.class);
	}
	
	public Dog() {
	}

	public Dog(String name) {
		this.name = name;
	}

	public String toString() {
		return "id: "+id+", name: "+name + " - master:"+master;
	}
}
