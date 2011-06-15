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

import siena.Table;

@Table("people_long_auto_extended2")
public class PersonLongAutoIDExtended2 extends PersonLongAutoIDExtended {

	public MyEnum enumField;
     
	public static enum MyEnum{
     	VAL1,
     	VAL2,
     	VAL3
	};
     
	public PersonLongAutoIDExtended2() {
	}

	public PersonLongAutoIDExtended2(String firstName, String lastName, String city, int n, String dogName, MyEnum enumField) {
		super(firstName, lastName, city, n, dogName);
		
		this.enumField = enumField;
	}

	public String toString() {
		return super.toString() + " - enumField:"+enumField;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((enumField == null) ? 0 : enumField.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)) return false;

		PersonLongAutoIDExtended2 other = (PersonLongAutoIDExtended2) obj;
		
		if (enumField == null) {
			if (other.enumField != null)
				return false;
		} else if (!enumField.equals(other.enumField))
			return false;
		
		return true;
	}

	public boolean isOnlyIdFilled() {
		if(super.isOnlyIdFilled() && this.enumField == null) return true;
		return false;
	}
}
