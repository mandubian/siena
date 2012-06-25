package siena.mongodb;

import java.lang.reflect.Field;
import java.util.List;

import siena.ClassInfo;
import siena.Index;
import siena.Model;
import siena.PersistenceManager;
import siena.Unique;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * A simple generator to help process Siena annotated indexes with Mongo. This generator can be called on each start up since the indexes will only be created if necessary.
 * 
 * @author pjarrell
 */
public class MongoDBIndexGeneratorImpl implements MongoDBIndexGenerator {

	private final MongoDBPersistenceManager persistenceManager;

	/**
	 * Create the generator using the Mongo {@link PersistenceManager}.
	 * 
	 * @param persistenceManager
	 *            is the persistence manager to use to generate indexes
	 */
	public MongoDBIndexGeneratorImpl(final MongoDBPersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	/**
	 * Generate Mongo indexes using the annotations {@link Index} and {@link Unique} from the model classes provided.
	 * 
	 * @param clazzes
	 *            is a list of Siena {@link Model} classes
	 */
	@SuppressWarnings("rawtypes")
	public void generate(final List<Class> clazzes) {
		if (clazzes != null) {
			for (final Class<?> clazz : clazzes) {
				if (ClassInfo.isModel(clazz)) {
					final ClassInfo info = ClassInfo.getClassInfo(clazz);
					final DBCollection collection = persistenceManager.getDatabase().getCollection(info.tableName);
					for (final Field field : info.updateFields) {

						final Index index = field.getAnnotation(Index.class);
						if (index != null) {
							final String[] names = index.value();
							final DBObject newIndex = new BasicDBObject();
							for (final String name : names) {
								if (newIndex.containsField(name) == false) {
									newIndex.put(name, 1);
								}
							}
							collection.ensureIndex(newIndex);
						}

						final Unique unique = field.getAnnotation(Unique.class);
						if (unique != null) {
							final String[] names = unique.value();
							final DBObject newIndex = new BasicDBObject();
							for (final String name : names) {
								if (newIndex.containsField(name) == false) {
									newIndex.put(name, 1);
								}
							}
							final DBObject uniqueIndex = new BasicDBObject();
							uniqueIndex.put("unique", true);
							collection.ensureIndex(newIndex, uniqueIndex);
						}

					}
				}
			}
		}
	}

	/**
	 * Gets the persistence manager being used by this generator.
	 * 
	 * @return the persistenceManager
	 */
	protected MongoDBPersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

}
