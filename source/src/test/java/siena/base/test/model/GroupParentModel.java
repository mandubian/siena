package siena.base.test.model;

import siena.Column;
import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Query;

public class GroupParentModel extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public GroupChildModel ownedChild;
	
	/* fields ignored by ClassInfo */
	@Filter("parent")
	public Query<GroupChildModel> links;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupParentModel other = (GroupParentModel) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
	
	public static Query<GroupParentModel> all() {
		return Model.all(GroupParentModel.class);
	}
}
