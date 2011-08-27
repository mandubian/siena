package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class RelatedSeveralQueryChild extends Model {
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public RelatedSeveralQueryParent owner;
	
	public String name;
	
	public RelatedSeveralQueryChild(){
	}

	public RelatedSeveralQueryChild(String name){
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
		RelatedSeveralQueryChild other = (RelatedSeveralQueryChild) obj;
		if (id == null && other.id!=null || !id.equals(other.id)) {
			return false;
		}
		if (name == null && other.name!=null || !name.equals(other.name)) {
			return false;
		}
		if (owner == null && other.owner!=null || owner != null && !owner.id.equals(other.owner!=null?other.owner.id:null)) {
			return false;
		}
		return true;
	}
	
	public static Query<RelatedSeveralQueryChild> all(){
		return Model.all(RelatedSeveralQueryChild.class);
	}
	
	public String toString(){
		return "id:"+id+ " - name:"+name+ " - owner:"+owner;
	}
}
