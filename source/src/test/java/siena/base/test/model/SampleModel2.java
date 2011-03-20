package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Model;

public class SampleModel2 extends Model {
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	@Column("relationship")
	public SampleModel relationship;	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SampleModel2 other = (SampleModel2) obj;
		if (id == null || !id.equals(other.id)) {
			return false;
		}
		if (relationship == null || !relationship.id.equals(other.relationship.id)) {
			return false;
		}
		return true;
	}
}
