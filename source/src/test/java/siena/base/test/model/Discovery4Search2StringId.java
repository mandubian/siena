package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Index;
import siena.Max;
import siena.Table;

@Table("discoveries_search2_stringid")
public class Discovery4Search2StringId {

	@Id(Generator.NONE)
	@Max(100)
	@Index("discoveries_search2_name")
	public String name;
	
	@Max(100)
	@Index("discoveries_search2_body")
	public String body;
	
	@Column("discoverer")
	public PersonStringID discoverer;

	public Discovery4Search2StringId(String name, String body, PersonStringID discoverer) {
		this.name = name;
		this.body = body;
		this.discoverer = discoverer;
	}
	
	public Discovery4Search2StringId() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		Discovery4Search2StringId other = (Discovery4Search2StringId) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.body != null && other.body.equals(body))
			return true;
		if(other.discoverer != null && other.discoverer.equals(discoverer))
			return true;
		
		return false;
	}
	
	public boolean isOnlyIdFilled() {
		if(this.name != null
			&& this.body == null
			&& this.discoverer == null
		) return true;
		return false;
	}
}
