package siena.base.test.model;

import java.util.List;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.embed.Embedded;
import siena.embed.Embedded.Mode;
import siena.embed.EmbeddedList;

@Table("container_models_native")
@EmbeddedList
public class EmbeddedContainerModelNative{
	@Id(Generator.NONE)
    public String id;
    
    @Column("embed")
    @Embedded(mode=Mode.NATIVE)
    public EmbeddedModel embed;
    
    public String toString() {
    	return "id:"+id + " - embed:" + embed;
    }
}
