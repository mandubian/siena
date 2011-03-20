package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Query;
import siena.Table;
import siena.core.batch.Batch;

@Table("discoveries_model")
public class DiscoveryModel extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	public String name;
	
	@Column("discoverer")
	public PersonLongAutoIDModel discoverer;

	public DiscoveryModel(String name, PersonLongAutoIDModel discoverer) {
		this.name = name;
		this.discoverer = discoverer;
	}
	
	public static Query<DiscoveryModel> all() {
		return Model.all(DiscoveryModel.class);
	}
	
	public static Batch<DiscoveryModel> batch() {
		return Model.batch(DiscoveryModel.class);
	}
	
	public DiscoveryModel() {
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		DiscoveryModel other = (DiscoveryModel) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discoverer != null && other.discoverer.equals(discoverer))
			return true;
		
		return false;
	}
	
	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.name == null
			&& this.discoverer == null
		) return true;
		return false;
	}
	
	public String toString() {
		return "Discovery [ id:"+id+" - name:"+name+" - discoverer:"+discoverer+" ]";
	}
}
