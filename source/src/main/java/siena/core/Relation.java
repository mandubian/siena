/**
 * 
 */
package siena.core;

/**
 * @author pascal
 *
 */
public class Relation {
	public enum Type {
		AGGREGATION
	}
	
	public Type 	type;
	public Object 	target;
	public Object	discriminator;	
	
	public Relation(){
	}
	
	public Relation(Type type){
		this.type = type;
	}
	
	public Relation(Type type, Object target){
		this.type = type;
		this.target = target;		
	}

	public Relation(Type type, Object target, Object discriminator){
		this.type = type;
		this.target = target;		
		this.discriminator = discriminator;
	}
}
