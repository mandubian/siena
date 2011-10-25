package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Model;

public class EnumTest extends Model {

	public static enum UserRole {
		SUPPORT, ADMIN, STAFF
	};

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Column("user_role")
	public UserRole role;
	
	@Override
	public boolean equals(Object that){
		EnumTest t = (EnumTest)that;
		if(this.role == t.role) return true;
		return false;
	}
}
