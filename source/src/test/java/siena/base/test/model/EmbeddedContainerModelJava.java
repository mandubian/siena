package siena.base.test.model;

import java.util.List;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.embed.Embedded;
import siena.embed.Embedded.Mode;
import siena.embed.EmbeddedList;

@Table("container_models_java")
@EmbeddedList
public class EmbeddedContainerModelJava{
	@Id(Generator.NONE)
    public String id;
    
    @Embedded(mode=Mode.SERIALIZE_JAVA)
    @Column("embed")
    public EmbeddedModel embed;
    
    @Embedded(mode=Mode.SERIALIZE_JAVA)
    @Column("embeds")
    public List<EmbeddedModel> embeds;
    
    public String toString() {
    	return "id:"+id + " - embed:" + embed + " - embeds:"+embeds;
    }
}
