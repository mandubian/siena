package siena.base.test.model;

import siena.Column;
import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Query;

public class SampleModel extends Model {
	
	public static String FOO = "FOO";
	
	public enum Type {
		FOO, BAR
	}
		
	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	@Column("private_field")
	@Max(256)
	private String privateField;

	@Max(256)
	public String publicField;
	
	public Type type;
	
	/* fields ignored by ClassInfo */
	@Filter("relationship")
	public Query<SampleModel2> links;
	
	public Class<?> clazz;
	public transient String foobar;
	
	public void setPrivateField(String privateField) {
		this.privateField = privateField;
	}
	
	public String getPrivateField() {
		return privateField;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SampleModel other = (SampleModel) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		if (privateField == null || !privateField.equals(other.privateField)) {
			return false;
		}
		if (publicField == null || !publicField.equals(other.publicField)) {
			return false;
		}
		if (type == null || !type.equals(other.type)) {
			return false;
		}
		return true;
	}
	
	public static Query<SampleModel> all() {
		return Model.all(SampleModel.class);
	}
}
