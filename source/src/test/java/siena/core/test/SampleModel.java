package siena.core.test;

import siena.Column;
import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class SampleModel extends Model {
	
	public static String FOO = "FOO";
	
	enum Type {
		FOO, BAR
	}
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	@Id
	public String key;

	@Column("private_field")
	private String privateField;
	public String publicField;
	
	@Column({"p_id", "p_key"})
	public SampleModel relationship;
	
	/* fields ignored by ClassInfo */
	@Filter("relationship")
	public Query<SampleModel> query;
	public Class<?> clazz;
	public transient String foobar;
	
	public void setPrivateField(String privateField) {
		this.privateField = privateField;
	}
	
	public String getPrivateField() {
		return privateField;
	}

}
