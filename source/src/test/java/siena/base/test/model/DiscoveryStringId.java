package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Table;

@Table("discoveries_stringid")
public class DiscoveryStringId {

	@Max(100)
	@Id(Generator.NONE)
	public String name;
	
	@Column("discoverer")
	public PersonStringID discoverer;

	public DiscoveryStringId(String name, PersonStringID discoverer) {
		this.name = name;
		this.discoverer = discoverer;
	}
	
	public DiscoveryStringId() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		DiscoveryStringId other = (DiscoveryStringId) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discoverer != null && other.discoverer.equals(discoverer))
			return true;
		
		return false;
	}
	
	public boolean isOnlyIdFilled() {
		if(this.name != null && this.discoverer == null
		) return true;
		return false;
	}
	
	public String toString() {
		return "Discovery [ name:"+name+" - discoverer:"+discoverer+" ]";
	}
}
