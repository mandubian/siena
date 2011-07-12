/**
 * 
 */
package siena.core;

/**
 * @author pascal
 *
 */
public class Relation {
	public RelationMode mode;
	public Object 		target;
	public Object		discriminator;	
	
	public Relation(){
	}
	
	public Relation(RelationMode mode){
		this.mode = mode;
	}
	
	public Relation(RelationMode mode, Object target){
		this.mode = mode;
		this.target = target;		
	}

	public Relation(RelationMode mode, Object target, Object discriminator){
		this.mode = mode;
		this.target = target;		
		this.discriminator = discriminator;
	}
}
