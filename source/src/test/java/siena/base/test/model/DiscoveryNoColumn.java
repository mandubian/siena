package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Join;
import siena.Max;
import siena.Table;

@Table("discoveries_join_nocol")
public class DiscoveryNoColumn {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	public String name;
	
	@Join
	public PersonLongAutoID discovererJoined;

	public PersonLongAutoID discovererNotJoined;
	
	public DiscoveryNoColumn(String name, PersonLongAutoID discovererJoined, PersonLongAutoID discovererNotJoined) {
		this.name = name;
		this.discovererJoined = discovererJoined;
		this.discovererNotJoined = discovererNotJoined;
	}
	
	public DiscoveryNoColumn() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		DiscoveryNoColumn other = (DiscoveryNoColumn) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discovererJoined != null && other.discovererJoined.equals(discovererJoined))
			return true;
		if(other.discovererNotJoined != null && other.discovererNotJoined.equals(discovererNotJoined))
			return true;
		
		return false;
	}
		
}
