package siena.base.test.model;

import siena.Column;
import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.Ignore;

public class SampleModelMultipleKeys extends Model {
	
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
	public SampleModelMultipleKeys relationship;
	
	/* fields ignored by ClassInfo */
	@Filter("relationship")
	public Query<SampleModelMultipleKeys> query;
	public Class<?> clazz;
	public transient String foobar;
	@Ignore
	public String ignoredData;
	
	public void setPrivateField(String privateField) {
		this.privateField = privateField;
	}
	
	public String getPrivateField() {
		return privateField;
	}

}
