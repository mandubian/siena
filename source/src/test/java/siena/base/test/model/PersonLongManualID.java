/*
 * Copyright 2008-2010 Alberto Gimeno <gimenete at gmail.com>
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

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Table;

@Table("people_long_manual")
public class PersonLongManualID {

	@Id(Generator.NONE)
	public Long id;
	
	@Column("first_name") @Max(100)
	public String firstName;
	
	@Column("last_name") @Max(100)
	public String lastName;
	
	@Max(100)
	public String city;
	
	public int n;

	public PersonLongManualID() {
	}

	public PersonLongManualID(Long id, String firstName, String lastName, String city, int n) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.city = city;
		this.n = n;
	}

	public String toString() {
		return "id: "+id+", firstName: "+firstName+", lastName: "+lastName+", city: "+city+", n:"+n;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result
				+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + n;
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
		PersonLongManualID other = (PersonLongManualID) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (n != other.n)
			return false;
		return true;
	}

	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.firstName == null
			&& this.lastName == null
			&& this.city == null
			&& this.n == 0
		) return true;
		return false;
	}
}
