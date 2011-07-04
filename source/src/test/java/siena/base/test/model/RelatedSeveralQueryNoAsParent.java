package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.core.Owned;

public class RelatedSeveralQueryNoAsParent extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	@Owned
	public Query<RelatedSeveralQueryNoAsChild> children;
	
	public RelatedSeveralQueryNoAsParent() {
	}

	public RelatedSeveralQueryNoAsParent(String name) {
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
		RelatedSeveralQueryNoAsParent other = (RelatedSeveralQueryNoAsParent) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	public static Query<RelatedSeveralQueryNoAsParent> all() {
		return Model.all(RelatedSeveralQueryNoAsParent.class);
	}
}
