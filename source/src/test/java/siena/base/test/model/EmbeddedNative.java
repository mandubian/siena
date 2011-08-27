package siena.base.test.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import siena.embed.Embedded;
import siena.embed.EmbeddedMap;


public class EmbeddedNative{
    public String 	alpha;
    
    public short	beta;
    
    private boolean isGamma;
    
    public Long		delta;
    
    public List<String>		eta;
    
    public enum MyEnum {
    	ONE, TWO, THREE
    }
    
    public MyEnum		myEnum;
    
    public BigDecimal	big;
    
    @EmbeddedMap
    public static class SubEmbed implements Serializable {
    	public String str;
    	public Long   l;
    	
    	public String toString() {
    		return "str:"+str+" - l:"+l;
    	}
    }
    
    @Embedded
    public SubEmbed jsonEmbed;
    
    @Embedded(mode=Embedded.Mode.SERIALIZE_JAVA)
    public SubEmbed javaEmbed;
    
    @Embedded(mode=Embedded.Mode.NATIVE)
    public SubEmbed nativeEmbed;
    
    public boolean isGamma() {
    	return isGamma;
    }
    
    public void setGamma(boolean isGamma){
    	this.isGamma = isGamma;
    }
    
    public String toString() {
    	return "alpha:" + alpha + " - beta:" + beta + " - isGamma:"+isGamma+ " - delta:"+delta 
    		+" - eta:"+eta+" - myEnum:"+myEnum + " - big:"+big + " - jsonEmbed:"+jsonEmbed 
    		+ " - javaEmbed:"+javaEmbed;
    }
}
