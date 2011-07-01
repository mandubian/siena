package siena.base.test.model;

import siena.Generator;
import siena.Id;
import siena.Table;
import siena.embed.Embedded;
import siena.embed.Embedded.Mode;
import siena.embed.EmbeddedList;

@Table("container_models")
@EmbeddedList
public class EmbeddedContainerNative{
	@Id(Generator.NONE)
    public String id;
    
    public String normal;
    
	@Embedded(mode=Mode.NATIVE)
    public EmbeddedNative embed;
    
    public String toString() {
    	return "id:"+id + " - embed:" + embed;
    }
}
