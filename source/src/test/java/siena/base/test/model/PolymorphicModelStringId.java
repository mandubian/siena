/*
 * Copyright 2011 Pascal <pascal.voitot@mandubian.org>
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
package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Table;
import siena.core.Polymorphic;

@Table("polymorphic_stringid")
public class PolymorphicModelStringId<T> {

	@Id(Generator.NONE)
	public String id;
	
	@Polymorphic
	public T payload;

	public PolymorphicModelStringId() {
	}

	public PolymorphicModelStringId(String id, T payload) {
		this.id = id;
		this.payload = payload;
	}

	public String toString() {
		return "id: "+id+", payload: "+payload;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());		
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
		PolymorphicModelStringId other = (PolymorphicModelStringId) obj;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} 
		if(!payload.equals(other.payload))
			return false;
		return true;
	}

	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.payload == null
		) return true;
		return false;
	}
}
