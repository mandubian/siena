package siena.base.test.model;

import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.embed.EmbedIgnore;
import siena.embed.EmbeddedMap;

@Table("embedded_models")
@EmbeddedMap
public class EmbeddedModel{
    @Id(Generator.NONE)
    public String id;
    
    @EmbedIgnore
    public String 	alpha;
    
    public short	beta;
    
    @Filter("parent")
    public siena.Query<EmbeddedSubModel> subs;           
    
    public String toString() {
    	return id + " " + alpha + " " + beta;
    }
}
