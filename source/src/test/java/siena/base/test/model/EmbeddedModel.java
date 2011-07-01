package siena.base.test.model;

import java.io.Serializable;

import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.embed.EmbedIgnore;
import siena.embed.EmbeddedMap;

@Table("embedded_models")
@EmbeddedMap
public class EmbeddedModel implements Serializable{
 	private static transient final long serialVersionUID = 2590813183461026436L;

	@Id(Generator.NONE)
    public String id;
    
    @EmbedIgnore
    public String 	alpha;
    
    public short	beta;
    
    private boolean isGamma;
    
    @Filter("parent")
    public siena.Query<EmbeddedSubModel> subs;           
    
    public boolean isGamma() {
    	return isGamma;
    }
    
    public void setGamma(boolean isGamma){
    	this.isGamma = isGamma;
    }
    
    public String toString() {
    	return "id:"+id + " - alpha:" + alpha + " - beta:" + beta + " - isGamma:"+isGamma;
    }
}
