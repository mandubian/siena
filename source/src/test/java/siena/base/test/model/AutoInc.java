package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Max;

public class AutoInc {
	
	@Id(Generator.AUTO_INCREMENT)
	public long id;
	
	@Max(100)
	public String name;

}
