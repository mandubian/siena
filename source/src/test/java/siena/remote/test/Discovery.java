/*
 * Copyright 2009 Alberto Gimeno <gimenete at gmail.com>
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
package siena.remote.test;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.Table;

@Table("discoverings")
public class Discovery extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public long id;
	public String name;
	@Column("discoverer_id")
	public Person discoverer;

	public Discovery() {
	}

	public Discovery(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public static Discovery get(long id) {
		Discovery d = new Discovery();
		d.id = id;
		d.get();
		return d;
	}

	public static Query<Discovery> all() {
		return Model.all(Discovery.class);
	}

	public String toString() {
		return "id: "+id+", name: "+name;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int) (id ^ (id >>> 32));
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Discovery other = (Discovery) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
