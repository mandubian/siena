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
import siena.Model;
import siena.Query;
import siena.Table;

@Table("transaction_from")
public class TransactionAccountFromModel extends Model{

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	public Long amount;

	public TransactionAccountFromModel() {
	}

	public TransactionAccountFromModel(Long amount) {
		this.amount = amount;
	}

	public Query<TransactionAccountFromModel> all() {
		return Model.all(TransactionAccountFromModel.class);
	}
	
	public String toString() {
		return "id: "+id+", amount: "+amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());		
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
		TransactionAccountFromModel other = (TransactionAccountFromModel) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} 
		if(!amount.equals(other.amount))
			return false;
		return true;
	}

	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.amount == null
		) return true;
		return false;
	}
}
