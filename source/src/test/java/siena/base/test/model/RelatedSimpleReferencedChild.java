package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class RelatedSimpleReferencedChild extends Model {
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	public String name;
	
	public RelatedSimpleReferencedChild(){
	}

	public RelatedSimpleReferencedChild(String name){
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
		RelatedSimpleReferencedChild other = (RelatedSimpleReferencedChild) obj;
		if (id == null && other.id!=null || !id.equals(other.id)) {
			return false;
		}
		if (name == null && other.name!=null || !name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	public static Query<RelatedSimpleReferencedChild> all(){
		return Model.all(RelatedSimpleReferencedChild.class);
	}
	
	public String toString(){
		return "id:"+id+ " - name:"+name;
	}
}
