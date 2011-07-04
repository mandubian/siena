package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.core.One;
import siena.core.Owned;

public class RelatedSimpleOwnedParent extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	@Owned(mappedBy="owner")
	public One<RelatedSimpleOwnedChild> child;
	
	public RelatedSimpleOwnedParent() {
	}

	public RelatedSimpleOwnedParent(String name) {
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
		RelatedSimpleOwnedParent other = (RelatedSimpleOwnedParent) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	public static Query<RelatedSimpleOwnedParent> all() {
		return Model.all(RelatedSimpleOwnedParent.class);
	}
}
