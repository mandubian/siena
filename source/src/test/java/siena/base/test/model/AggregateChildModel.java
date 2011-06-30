package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class AggregateChildModel extends Model {
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	public AggregateChildModel(){
	}

	public AggregateChildModel(String name){
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
		AggregateChildModel other = (AggregateChildModel) obj;
		if (id == null || !id.equals(other.id)) {
			return false;
		}
		if (name == null || !name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	public static Query<AggregateChildModel> all(){
		return Model.all(AggregateChildModel.class);
	}
	
	public String toString(){
		return "id:"+id+ " - name:"+name;
	}
}
