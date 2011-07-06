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

import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Table;

@Table("string_list")
public class StringListModel extends Model{

	@Id(Generator.NONE)
	@Max(36)
	public String id;
	
	public List<String> friends;
	
	public StringListModel() {
	}

	public StringListModel(String id) {
		this.id = id;
	}

	public String toString() {
		return "id: "+id+", friends: "+friends;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringListModel other = (StringListModel) obj;
		boolean b = true;
		
		int i=0;
		for(String friend: friends){
			if(!friend.equals(other.friends.get(i++))) b=false;
		}
		if(!b) return false;
		
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.friends == null
		) return true;
		return false;
	}
}
