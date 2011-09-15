package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Max;
import siena.Table;

@Table("discoveries_search_stringid")
public class Discovery4SearchStringId {
	
	@Id(Generator.NONE)
	@Max(100)
	@Index("name")
	public String name;
	
	@Column("discoverer")
	public PersonStringID discoverer;

	public Discovery4SearchStringId(String name, PersonStringID discoverer) {
		this.name = name;
		this.discoverer = discoverer;
	}
	
	public Discovery4SearchStringId() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		Discovery4SearchStringId other = (Discovery4SearchStringId) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discoverer != null && other.discoverer.equals(discoverer))
			return true;
		
		return false;
	}
	
	public boolean isOnlyIdFilled() {
		if(this.name != null && 
				this.discoverer == null
		) return true;
		return false;
	}
	
	public String toString(){
		return "name:"+this.name+" - discoverer:"+discoverer;
	}
}
