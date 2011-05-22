package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Table;

@Table("discoveries_join_nocolmultk")
public class DiscoveryNoColumnMultipleKeys {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	public String name;
	
	public MultipleKeys mk1;

	public MultipleKeys mk2;
	
	public DiscoveryNoColumnMultipleKeys(String name, MultipleKeys mk1, MultipleKeys mk2) {
		this.name = name;
		this.mk1 = mk1;
		this.mk2 = mk2;
	}
	
	public DiscoveryNoColumnMultipleKeys() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		DiscoveryNoColumnMultipleKeys other = (DiscoveryNoColumnMultipleKeys) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.mk1 != null && other.mk1.equals(mk1))
			return true;
		if(other.mk2 != null && other.mk2.equals(mk2))
			return true;
		
		return false;
	}
		
}
