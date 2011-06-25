package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.core.Aggregated;
import siena.core.ListQuery;

public class AggregateParentModel extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	@Aggregated
	public AggregateChildModel child;
	
	/* fields ignored by ClassInfo */
	//@Aggregated
	//public ListQuery<AggregateChildModel> children;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AggregateParentModel other = (AggregateParentModel) obj;
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
	
	public static Query<AggregateParentModel> all() {
		return Model.all(AggregateParentModel.class);
	}
}
