package siena.base.test;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Join;
import siena.Max;
import siena.Table;

@Table("discoveries")
public class Discovery4Join {

	@Id(Generator.UUID) @Max(36)
	public String id;
	
	@Max(100)
	public String name;
	
	@Column("discoverer_joined")
	@Join
	public Person discovererJoined;

	@Column("discoverer_not_joined")
	public Person discovererNotJoined;
	
	public Discovery4Join(String name, Person discovererJoined, Person discovererNotJoined) {
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
