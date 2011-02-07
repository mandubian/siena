package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Join;
import siena.Max;
import siena.Table;

@Table("discoveries4Join")
public class Discovery4Join {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	public String name;
	
	@Column("discoverer_joined")
	@Join
	public PersonLongAutoID discovererJoined;

	@Column("discoverer_not_joined")
	public PersonLongAutoID discovererNotJoined;
	
	public Discovery4Join(String name, PersonLongAutoID discovererJoined, PersonLongAutoID discovererNotJoined) {
		this.name = name;
		this.discovererJoined = discovererJoined;
		this.discovererNotJoined = discovererNotJoined;
	}
	
	public Discovery4Join() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		Discovery4Join other = (Discovery4Join) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discovererJoined != null && other.discovererJoined.equals(discovererJoined))
			return true;
		if(other.discovererNotJoined != null && other.discovererNotJoined.equals(discovererNotJoined))
			return true;
		
		return false;
	}
		
}
