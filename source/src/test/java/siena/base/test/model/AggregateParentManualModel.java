package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.core.Aggregated;
import siena.core.Many;
import siena.core.One;

public class AggregateParentManualModel extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	@Aggregated
	public One<AggregateChildManualModel> child;
	
	@Aggregated
	public Many<AggregateChildManualModel> children;
	
	public AggregateParentManualModel() {
	}

	public AggregateParentManualModel(String name) {
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
		AggregateParentManualModel other = (AggregateParentManualModel) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		if (child.get()==null) {
			if(other.child.get() != null) {
				return false;
			}
		}else {
			 if(!child.get().equals(other.child.get())) {
				 return false;
			 }
		}
		
		return true;
	}
	
	public static Query<AggregateParentManualModel> all() {
		return Model.all(AggregateParentManualModel.class);
	}
}
