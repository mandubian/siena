package siena.mongodb;

import java.util.List;

/**
 * A generator to process Siena annotated indexes in Mongo.
 * 
 * @author pjarrell
 */
public interface MongoDBIndexGenerator {

	/**
	 * Generate indexes from the supplied model classes.
	 * 
	 * @param clazzes
	 *            is a list of model classes to process
	 */
	@SuppressWarnings("rawtypes")
	void generate(final List<Class> clazzes);

}
