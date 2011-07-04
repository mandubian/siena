package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.core.Owned;

public class RelatedSeveralQueryParent extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	@Owned(mappedBy="owner")
	public Query<RelatedSeveralQueryChild> children;
	
	public RelatedSeveralQueryParent() {
	}

	public RelatedSeveralQueryParent(String name) {
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
		RelatedSeveralQueryParent other = (RelatedSeveralQueryParent) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	public static Query<RelatedSeveralQueryParent> all() {
		return Model.all(RelatedSeveralQueryParent.class);
	}
}
