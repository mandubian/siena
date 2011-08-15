package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class AggregateChildManualModel extends Model {
	@Id(Generator.NONE)
	public Long id;

	public String name;
	
	public AggregateChildManualModel(){
	}

	public AggregateChildManualModel(Long id, String name){
		this.id = id;
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
		AggregateChildManualModel other = (AggregateChildManualModel) obj;
		if ((id == null && other.id != null) || !id.equals(other.id)) {
			return false;
		}
		if (name == null && other.name != null){
			return false;
		}
		if(!name.equals(other.name)){
			return false;
		}
		return true;
	}
	
	public static Query<AggregateChildManualModel> all(){
		return Model.all(AggregateChildManualModel.class);
	}
	
	public String toString(){
		return "id:"+id+ " - name:"+name;
	}
}
