package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class RelatedSimpleReferencedParent extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	public RelatedSimpleReferencedChild child;
	
	public RelatedSimpleReferencedParent() {
	}

	public RelatedSimpleReferencedParent(String name) {
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
		RelatedSimpleReferencedParent other = (RelatedSimpleReferencedParent) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		if (child==null && other.child != null || !child.id.equals(other.child!=null?other.child.id:null)) {
			return false;
		}
		return true;
	}
	
	public static Query<RelatedSimpleReferencedParent> all() {
		return Model.all(RelatedSimpleReferencedParent.class);
	}
}
