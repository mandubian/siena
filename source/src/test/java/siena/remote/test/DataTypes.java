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

import java.util.Date;

import siena.Id;
import siena.Model;

public class DataTypes extends Model {
	
	@Id
	public Long id;
	
	public byte typeByte;
	public short typeShort;
	public int typeInt;
	public long typeLong;
	public float typeFloat;
	public double typeDouble;
	
	public String typeString;
	public Date typeDate;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + typeByte;
		result = prime * result
				+ ((typeDate == null) ? 0 : typeDate.hashCode());
		long temp;
		temp = Double.doubleToLongBits(typeDouble);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Float.floatToIntBits(typeFloat);
		result = prime * result + typeInt;
		result = prime * result + (int) (typeLong ^ (typeLong >>> 32));
		result = prime * result + typeShort;
		result = prime * result
				+ ((typeString == null) ? 0 : typeString.hashCode());
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
		DataTypes other = (DataTypes) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (typeByte != other.typeByte)
			return false;
		if (typeDate == null) {
			if (other.typeDate != null)
				return false;
		} else if (!typeDate.equals(other.typeDate))
			return false;
		if (Double.doubleToLongBits(typeDouble) != Double
				.doubleToLongBits(other.typeDouble))
			return false;
		if (Float.floatToIntBits(typeFloat) != Float
				.floatToIntBits(other.typeFloat))
			return false;
		if (typeInt != other.typeInt)
			return false;
		if (typeLong != other.typeLong)
			return false;
		if (typeShort != other.typeShort)
			return false;
		if (typeString == null) {
			if (other.typeString != null)
				return false;
		} else if (!typeString.equals(other.typeString))
			return false;
		return true;
	}

}
