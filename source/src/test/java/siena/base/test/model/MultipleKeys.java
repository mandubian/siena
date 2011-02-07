package siena.base.test.model;

import siena.Column;
import siena.Id;
import siena.Max;
import siena.Table;

@Table("multiple_keys")
public class MultipleKeys {
	
	@Id @Max(100)
	public String id1;
	
	@Id @Max(100)
	public String id2;
	
	@Max(100)
	public String name;
	
	@Column({"parent_id1", "parent_id2"})
	public MultipleKeys parent;

}
