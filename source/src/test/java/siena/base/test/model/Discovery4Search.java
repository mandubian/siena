package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Max;
import siena.Table;

@Table("discoveries_search")
public class Discovery4Search {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	@Index("name")
	public String name;
	
	@Column("discoverer")
	public PersonLongAutoID discoverer;

	public Discovery4Search(String name, PersonLongAutoID discoverer) {
		this.name = name;
		this.discoverer = discoverer;
	}
	
	public Discovery4Search() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		Discovery4Search other = (Discovery4Search) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discoverer != null && other.discoverer.equals(discoverer))
			return true;
		
		return false;
	}
	
	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.name == null
			&& this.discoverer == null
		) return true;
		return false;
	}
}
