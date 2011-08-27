package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.core.Many;
import siena.core.Owned;

public class RelatedManyParent extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	@Owned(mappedBy="owner")
	public Many<RelatedManyChild> children;
	
	public RelatedManyParent() {
	}

	public RelatedManyParent(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelatedManyParent other = (RelatedManyParent) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	public static Query<RelatedManyParent> all() {
		return Model.all(RelatedManyParent.class);
	}
}
