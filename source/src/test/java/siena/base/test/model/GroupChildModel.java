package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;

public class GroupChildModel extends Model {
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public SampleModel parent;	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupChildModel other = (GroupChildModel) obj;
		if (id == null || !id.equals(other.id)) {
			return false;
		}
		if (parent == null || !parent.id.equals(other.parent.id)) {
			return false;
		}
		return true;
	}
}
