package siena.base.test.model;

import java.util.List;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.embed.Embedded;
import siena.embed.EmbeddedList;

@Table("container_models")
@EmbeddedList
public class ContainerModel{
	@Id(Generator.NONE)
    public String id;
    
    @Embedded
    @Column("embed")
    public EmbeddedModel embed;
    
    @Embedded
    @Column("embeds")
    public List<EmbeddedModel> embeds;
    
    public String toString() {
    	return "id:"+id + " - embed:" + embed + " - embeds:"+embeds;
    }
}
