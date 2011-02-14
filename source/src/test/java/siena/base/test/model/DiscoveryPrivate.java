package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Table;

@Table("discoveries_private")
public class DiscoveryPrivate {

	@Id(Generator.NONE)
	private Long id;
	
	@Max(100)
	private String name;
	
	@Column("discoverer")
	private PersonLongAutoID discoverer;

	public DiscoveryPrivate(Long id, String name, PersonLongAutoID discoverer) {
		this.id = id;
		this.name = name;
		this.discoverer = discoverer;
	}
	
	public DiscoveryPrivate() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		DiscoveryPrivate other = (DiscoveryPrivate) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discoverer != null && other.discoverer.equals(discoverer))
			return true;
		
		return false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PersonLongAutoID getDiscoverer() {
		return discoverer;
	}

	public void setDiscoverer(PersonLongAutoID discoverer) {
		this.discoverer = discoverer;
	}
	
	
}
