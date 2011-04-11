package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Join;
import siena.Max;
import siena.Table;

@Table("discoveries_join2")
public class Discovery4Join2 {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	public String name;
	
	@Column("discoverer_joined")
	@Join
	public PersonLongAutoID discovererJoined;

	@Column("discoverer_joined2")
	@Join
	public PersonLongAutoID discovererJoined2;
	
	public Discovery4Join2(String name, PersonLongAutoID discovererJoined, PersonLongAutoID discovererJoined2) {
		this.name = name;
		this.discovererJoined = discovererJoined;
		this.discovererJoined2 = discovererJoined2;
	}
	
	public Discovery4Join2() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		Discovery4Join2 other = (Discovery4Join2) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discovererJoined != null && other.discovererJoined.equals(discovererJoined))
			return true;
		if(other.discovererJoined2 != null && other.discovererJoined2.equals(discovererJoined2))
			return true;
		
		return false;
	}
		
}
