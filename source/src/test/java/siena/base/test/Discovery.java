package siena.base.test;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Table;

@Table("discoveries")
public class Discovery {

	@Id(Generator.UUID) @Max(36)
	public String id;
	
	@Max(100)
	public String name;
	
	@Column("discoverer")
	public Person discoverer;

	public Discovery(String name, Person discoverer) {
		this.name = name;
		this.discoverer = discoverer;
	}
	
	public Discovery() {
	}

}
