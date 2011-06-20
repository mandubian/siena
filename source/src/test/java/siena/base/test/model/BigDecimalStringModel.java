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

import java.math.BigDecimal;

import siena.Generator;
import siena.Id;
import siena.Table;
import siena.core.DecimalPrecision;
import siena.core.DecimalPrecision.StorageType;

@Table("big_decimal_string")
public class BigDecimalStringModel {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@DecimalPrecision(storateType=StorageType.STRING, size=21, scale=10)
	public BigDecimal big;

	public BigDecimalStringModel() {
	}

	public BigDecimalStringModel(BigDecimal big) {
		this.big = big;
	}

	public String toString() {
		return "id: "+id+", big: "+big;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((big == null) ? 0 : big.hashCode());		
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
		BigDecimalStringModel other = (BigDecimalStringModel) obj;
		if (big == null) {
			if (other.big != null)
				return false;
		} 
		if(!big.equals(other.big))
			return false;
		return true;
	}

	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.big == null
		) return true;
		return false;
	}
}
