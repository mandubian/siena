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

import java.util.List;
import java.util.Map;

import siena.Column;
import siena.Filter;
import siena.Index;
import siena.Max;
import siena.Table;
import siena.embed.Embedded;
import siena.embed.EmbeddedMap;

@Table("people_long_auto_extended_abstract")
public class PersonLongAutoIDExtendedAbstract extends PersonLongAutoIDAbstract {

	@Max(100)
	public String dogName;
	
    @Column("boss") @Index("boss_index")
    public PersonLongAutoIDExtendedAbstract boss;
    
    @Filter("boss")
    public siena.Query<PersonLongAutoIDExtendedAbstract> employees;
           
    @Embedded
    public Image profileImage;
    
    @Embedded
    public List<Image> otherImages;

    @Embedded
    public Map<String, Image> stillImages;
    
    @EmbeddedMap
    public static class Image {
            public String filename;
            public String title;
    }

	public PersonLongAutoIDExtendedAbstract() {
	}

	public PersonLongAutoIDExtendedAbstract(String firstName, String lastName, String city, int n, String dogName) {
		super(firstName, lastName, city, n);
		
		this.dogName = dogName;
	}

	public String toString() {
		return super.toString() + " - dogName:"+dogName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dogName == null) ? 0 : dogName.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)) return false;

		PersonLongAutoIDExtendedAbstract other = (PersonLongAutoIDExtendedAbstract) obj;
		
		if (dogName == null) {
			if (other.dogName != null)
				return false;
		} else if (!dogName.equals(other.dogName))
			return false;
		
		return true;
	}

	public boolean isOnlyIdFilled() {
		if(super.isOnlyIdFilled() && this.firstName == null) return true;
		return false;
	}
}
