package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.core.Many;
import siena.core.Related;

public class RelatedParentModel extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	@Related(as = "owner")
	public AggregateChildModel child;
	
	@Related(as="owner")
	public Many<RelatedChildModel> children;
	
	public RelatedParentModel() {
	}

	public RelatedParentModel(String name) {
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
		RelatedParentModel other = (RelatedParentModel) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		if (!child.equals(other.child)) {
			return false;
		}
		return true;
	}
	
	public static Query<RelatedParentModel> all() {
		return Model.all(RelatedParentModel.class);
	}
}
