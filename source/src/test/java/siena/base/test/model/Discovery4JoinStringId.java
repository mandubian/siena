package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Join;
import siena.Max;
import siena.Table;

@Table("discoveries_join_stringid")
public class Discovery4JoinStringId {
	
	@Max(100)
	@Id(Generator.NONE)
	public String name;
	
	@Column("discoverer_joined")
	@Join
	public PersonStringID discovererJoined;

	@Column("discoverer_not_joined")
	public PersonStringID discovererNotJoined;
	
	public Discovery4JoinStringId(String name, PersonStringID discovererJoined, PersonStringID discovererNotJoined) {
		this.name = name;
		this.discovererJoined = discovererJoined;
		this.discovererNotJoined = discovererNotJoined;
	}
	
	public Discovery4JoinStringId() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		Discovery4JoinStringId other = (Discovery4JoinStringId) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discovererJoined != null && other.discovererJoined.equals(discovererJoined))
			return true;
		if(other.discovererNotJoined != null && other.discovererNotJoined.equals(discovererNotJoined))
			return true;
		
		return false;
	}
		
}
