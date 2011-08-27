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

package siena.samples.relations.referenced.unowned.one2one;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Query;
import siena.Table;
import siena.core.batch.Batch;

@Table("sample_unowned_onetoone_cat")
public class Cat extends Model{

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Column("name") @Max(100)
	public String name;

	public static Query<Cat> all() {
		return Model.all(Cat.class);
	}
	
	public static Batch<Cat> batch() {
		return Model.batch(Cat.class);
	}
	
	public Cat() {
	}

	public Cat(String name) {
		this.name = name;
	}

	public String toString() {
		return "id: "+id+", name: "+name;
	}
}
