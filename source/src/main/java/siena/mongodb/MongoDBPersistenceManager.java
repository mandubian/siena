package siena.mongodb;

import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Json;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSimple;
import siena.QueryOrder;
import siena.SienaException;
import siena.Util;
import siena.core.async.PersistenceManagerAsync;
import siena.embed.Embedded;
import siena.embed.JsonSerializer;
import siena.gae.Unindexed;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * Persistence Manager for working with a Mongo DB. Each Siena model will become it's own collection.
 * <p>
 * The following properties can be configured on Mongo database. A database name and at least one host name are required.
 * <ul>
 * <li><strong>siena.mongodb.hostnames</strong> - a comma-delimited list of host names (or IP addresses) with or without port numbers (i.e. 127.0.0.1:27000,192.168.0.1)</li>
 * <li><strong>siena.mongodb.databaseName</strong> - name of the database (will be created automatically if it doesn't already exist)</li>
 * <li><strong>siena.mongodb.userName</strong> - required if auth is enabled</li>
 * <li><strong>siena.mongodb.password</strong> - required if auth is enabled</li>
 * <li><strong>siena.mongodb.autoConnectRetry</strong></li>
 * <li><strong>siena.mongodb.connectionsPerHost</strong></li>
 * <li><strong>siena.mongodb.connectTimeout</strong></li>
 * <li><strong>siena.mongodb.maxWaitTime</strong></li>
 * <li><strong>siena.mongodb.socketTimeout</strong></li>
 * <li><strong>siena.mongodb.threadsAllowedToBlockForConnectionMultiplier</strong></li>
 * </ul>
 * <p>
 * The Query operators supported are:
 * <ul>
 * <li>=</li>
 * <li>!=</li>
 * <li>&lt;</li>
 * <li>&gt;</li>
 * <li>&lt;=</li>
 * <li>&gt;=</li>
 * <li>IN</li>
 * <li>NOT IN</li>
 * <li>MOD</li>
 * <li>ALL</li>
 * <li>EXISTS</li>
 * <li>TYPE</li>
 * <li>SIZE</li>
 * <li>OR</li>
 * <li>NOR</li>
 * </ul>
 * 
 * @author pjarrell
 * @author (modified by) T.hagikura
 */
public class MongoDBPersistenceManager extends AbstractPersistenceManager {

	private static final String ID_FIELD = "_id";
	private static final String PREFIX = "siena.mongodb.";
	private static final Log LOGGER = LogFactory.getLog(MongoDBPersistenceManager.class);
	private static final Map<String, String> operators = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("=", null);
			put("!=", "$ne");
			put("<", "$lt");
			put(">", "$gt");
			put("<=", "$lte");
			put(">=", "$gte");
			put(" IN", "$in");
			put(" NOT IN", "$nin");
			put(" MOD", "$mod");
			put(" ALL", "$all");
			put(" EXISTS", "$exists");
			put(" TYPE", "$type");
			put(" SIZE", "$size");
			put(" OR", "$or");
			put(" NOR", "$nor");
		}
	};
	private static String[] supportedOperators;

	static {
		supportedOperators = operators.keySet().toArray(new String[0]);
	}

	private Mongo mongo;
	private String databaseName;
	private String userName;
	private String password;

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#beginTransaction(int)
	 */
	@Override
	public void beginTransaction(final int isolationLevel) {
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#closeConnection()
	 */
	@Override
	public void closeConnection() {
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#commitTransaction()
	 */
	@Override
	public void commitTransaction() {
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#count(siena.Query)
	 */
	@Override
	public <T> int count(final Query<T> query) {
		return prepare(query, true).count();
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#delete(java.lang.Object)
	 */
	@Override
	public void delete(final Object obj) {
		final Class<?> clazz = obj.getClass();
		
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		Field idField = info.getIdField();
		Object idVal = Util.readField(obj, idField);
		if (idVal == null) {
			throw new SienaException("Object does not exist : " + obj);
		}
		
		final DBCollection collection = getDatabase().getCollection(ClassInfo.getClassInfo(clazz).tableName);
		final Object id = getId(obj);
		final DBObject query = new BasicDBObject(ID_FIELD, new ObjectId(id.toString()));
		// LOGGER.info("Query to delete (by object): " + query);
		collection.remove(query);
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#delete(siena.Query)
	 */
	@Override
	public <T> int delete(final Query<T> query) {
		int count = 0;
		final Class<?> clazz = query.getQueriedClass();
		final DBCollection collection = getDatabase().getCollection(ClassInfo.getClassInfo(clazz).tableName);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Query to delete: " + query);
		}
		final DBCursor cursor = prepare(query, true);
		while (cursor.hasNext()) {
			final DBObject entity = cursor.next();
			collection.remove(entity);
			count += 1;
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#fetch(siena.Query)
	 */
	@Override
	public <T> List<T> fetch(final Query<T> query) {
		return map(query, 0, prepare(query, false));
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#fetch(siena.Query, int)
	 */
	@Override
	public <T> List<T> fetch(final Query<T> query, final int limit) {
		return map(query, 0, prepare(query, false).limit(limit));
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#fetch(siena.Query, int, java.lang.Object)
	 */
	@Override
	public <T> List<T> fetch(final Query<T> query, final int limit, final Object offset) {
		return map(query, 0, prepare(query, false).skip((Integer) offset).limit(limit));
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#fetchKeys(siena.Query)
	 */
	@Override
	public <T> List<T> fetchKeys(final Query<T> query) {
		return map(query, 0, prepare(query, true));
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#fetchKeys(siena.Query, int)
	 */
	@Override
	public <T> List<T> fetchKeys(final Query<T> query, final int limit) {
		return map(query, 0, prepare(query, true).limit(limit));
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#fetchKeys(siena.Query, int, java.lang.Object)
	 */
	@Override
	public <T> List<T> fetchKeys(final Query<T> query, final int limit, final Object offset) {
		return map(query, 0, prepare(query, true).skip((Integer) offset).limit(limit));
	}

	/**
	 * Fill in the properties in the entity using the model object.
	 * 
	 * @param obj
	 *            is the model to use as the source
	 * @param entity
	 *            is the entity to store as the target
	 */
	protected void fillEntity(final Object obj, final DBObject entity) {
		final Class<?> clazz = obj.getClass();

		for (final Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			final String property = ClassInfo.getColumnNames(field)[0];
			Object value = readField(obj, field);
			final Class<?> fieldClass = field.getType();
			if (ClassInfo.isModel(fieldClass)) {
				if (value == null) {
					entity.put(property, null);
				}
				else {
					final Object id = getId(value);
					entity.put(property, id);
				}
			}
			else {
				if (value != null) {
					if (field.getType() == Json.class) {
						value = value.toString();
					}
					else if (field.getAnnotation(Embedded.class) != null) {
						value = JsonSerializer.serialize(value).toString();
					}
				}
				final Unindexed ui = field.getAnnotation(Unindexed.class);
				if (ui == null) {
					entity.put(property, value);
				}
				else {
					entity.put(property, value);
				}
			}
		}
	}

	/**
	 * Fill in the properties of the model from stored entity.
	 * 
	 * @param obj
	 *            is model to fill in as the target
	 * @param entity
	 *            is the entity used to fill the model as the source
	 */
	protected void fillModel(final Object obj, final DBObject entity) {
		final Class<?> clazz = obj.getClass();

		for (final Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			field.setAccessible(true);
			final String property = ClassInfo.getColumnNames(field)[0];
			try {
				final Class<?> fieldClass = field.getType();
				if (ClassInfo.isModel(fieldClass)) {
					final Object objectId = entity.get(property);
					if (objectId != null) {
						final Object value = fieldClass.newInstance();
						final Field id = ClassInfo.getIdField(fieldClass);
						setId(id, value, objectId);
						field.set(obj, value);
					}
				}
				else {
					setFromObject(obj, field, entity.get(property));
				}
			}
			catch (final Exception e) {
				throw new SienaException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#get(java.lang.Object)
	 */
	@Override
	public void get(final Object obj) {
		final Class<?> clazz = obj.getClass();
		final DBCollection collection = getDatabase().getCollection(ClassInfo.getClassInfo(clazz).tableName);
		Object id = getId(obj);
		try {
			id = new ObjectId(id.toString());
			final DBObject entity = collection.findOne(id);
			fillModel(obj, entity);
		}
		catch (final Exception e) {
			throw new SienaException(e);
		}
	}

	/**
	 * Gets the database specified. MongoDB will create the database if it doesn't already exist.
	 * 
	 * @return
	 */
	protected DB getDatabase() {
		if (mongo != null) {
			final DB db = mongo.getDB(databaseName);
			if (db != null && db.isAuthenticated() == false && userName != null && password != null) {
				if (db.authenticate(userName.trim(), password.trim().toCharArray()) == false) {
					throw new RuntimeException("Authentication failed using the configured credentials");
				}
			}
			return db;
		}
		else {
			throw new RuntimeException("MongoDB not initialized");
		}
	}

	/**
	 * Get the ID of the entity.
	 * 
	 * @param entity
	 *            is the stored entity with the ID
	 * @return
	 */
	protected Object getId(final DBObject entity) {
		return entity.get(ID_FIELD);
	}

	/**
	 * Get the ID of the model.
	 * 
	 * @param obj
	 *            is the the model with the ID
	 * @return
	 */
	protected Object getId(final Object obj) {
		try {
			final Field f = ClassInfo.getIdField(obj.getClass());
			return f.get(obj);
		}
		catch (final Exception e) {
			throw new SienaException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#init(java.util.Properties)
	 */
	@Override
	public void init(final Properties p) {
		final String hostNames = p.getProperty(PREFIX + "hostnames");
		final List<ServerAddress> addresses = new LinkedList<ServerAddress>();
		if (hostNames != null && hostNames.length() > 0) {
			final String[] hosts = hostNames.trim().split(",");
			for (final String host : hosts) {
				ServerAddress address = null;
				if (host.contains(":")) {
					final String[] parts = host.trim().split(":");
					if (parts.length == 2) {
						try {
							address = new ServerAddress(parts[0], Integer.valueOf(parts[1]));
						}
						catch (final Exception e) {
							throw new RuntimeException("Port number is not a valid number", e);
						}
					}
					else {
						throw new RuntimeException("Invalid host format.  The format should be \"server:port\"");
					}
				}
				else {
					try {
						address = new ServerAddress(host);
					}
					catch (final UnknownHostException e) {
						throw new RuntimeException("Unknown host name specified", e);
					}
				}
				addresses.add(address);
			}
		}

		final MongoOptions options = new MongoOptions();
		final String autoConnectRetry = p.getProperty(PREFIX + "autoConnectRetry");
		if (autoConnectRetry != null && autoConnectRetry.length() > 0) {
			options.autoConnectRetry = Boolean.valueOf(autoConnectRetry.trim());
		}
		final String connectionsPerHost = p.getProperty(PREFIX + "connectionsPerHost");
		if (connectionsPerHost != null && connectionsPerHost.length() > 0) {
			options.connectionsPerHost = Integer.valueOf(connectionsPerHost.trim());
		}
		final String connectTimeout = p.getProperty(PREFIX + "connectTimeout");
		if (connectTimeout != null && connectTimeout.length() > 0) {
			options.connectTimeout = Integer.valueOf(connectTimeout.trim());
		}
		final String maxWaitTime = p.getProperty(PREFIX + "maxWaitTime");
		if (maxWaitTime != null && maxWaitTime.length() > 0) {
			options.maxWaitTime = Integer.valueOf(maxWaitTime.trim());
		}
		final String socketTimeout = p.getProperty(PREFIX + "socketTimeout");
		if (socketTimeout != null && socketTimeout.length() > 0) {
			options.socketTimeout = Integer.valueOf(socketTimeout.trim());
		}
		final String threadsAllowedToBlockForConnectionMultiplier = p.getProperty(PREFIX + "threadsAllowedToBlockForConnectionMultiplier");
		if (threadsAllowedToBlockForConnectionMultiplier != null && threadsAllowedToBlockForConnectionMultiplier.length() > 0) {
			options.threadsAllowedToBlockForConnectionMultiplier = Integer.valueOf(threadsAllowedToBlockForConnectionMultiplier.trim());
		}
		if (p.containsKey(PREFIX + "databaseName")) {
			this.databaseName = p.getProperty(PREFIX + "databaseName");
		}
		if (p.containsKey(PREFIX + "userName")) {
			this.userName = p.getProperty(PREFIX + "userName");
		}
		if (p.containsKey(PREFIX + "password")) {
			this.password = p.getProperty(PREFIX + "password");
		}

		if (this.databaseName == null || this.databaseName.length() == 0) {
			throw new RuntimeException("Database name is required");
		}
		if (addresses.size() == 0) {
			throw new RuntimeException("At least one valid server address is required");
		}

		// Now create the MongoDB
		this.mongo = new Mongo(addresses, options);
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#insert(java.lang.Object)
	 */
	@Override
	public void insert(final Object obj) {
		final Class<?> clazz = obj.getClass();
		final DBCollection collection = getDatabase().getCollection(ClassInfo.getClassInfo(clazz).tableName);
		final DBObject entity = new BasicDBObject();
		
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		Field idField = info.getIdField();
		Object idVal = Util.readField(obj, idField);
		if (idVal != null) {
			throw new SienaException("Same key object already exists : " + obj);
		}
		
		fillEntity(obj, entity);
		collection.insert(entity);
		setId(ClassInfo.getIdField(clazz), obj, getId(entity));
	}

	/**
	 * Handles a query including setting the next offset on a query.
	 * 
	 * @param <T>
	 * @param query
	 *            is the query to execute
	 * @param offset
	 *            is the offset requested
	 * @param cursor
	 *            is the MongoDB cursor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> map(final Query<T> query, final int offset, final DBCursor cursor) {
		final Class<?> clazz = query.getQueriedClass();
		final List<T> result = (List<T>) mapEntities(cursor, clazz);
		query.setNextOffset(offset + result.size());
		return result;
	}

	/**
	 * Convert the stored entities into models.
	 * 
	 * @param <T>
	 * @param cursor
	 *            is the MongoDB cursor that resulted from an executed query
	 * @param clazz
	 *            is the model class to convert to
	 * @return a list of model objects
	 */
	protected <T> List<T> mapEntities(final DBCursor cursor, final Class<T> clazz) {
		final List<T> list = new LinkedList<T>();
		final Field id = ClassInfo.getIdField(clazz);
		while (cursor.hasNext()) {
			final DBObject entity = cursor.next();
			T obj;
			try {
				obj = clazz.newInstance();
				fillModel(obj, entity);
				list.add(obj);
				setId(id, obj, getId(entity));
			}
			catch (final SienaException e) {
				throw e;
			}
			catch (final Exception e) {
				throw new SienaException(e);
			}
		}
		return list;
	}

	/**
	 * Create a MongoDB query using the Siena query.
	 * 
	 * @param <T>
	 * @param query
	 *            is the Siena query
	 * @param keysOnly
	 *            determines whether or not just keys should be returned (lighter weight query)
	 * @return a MongoDB cursor
	 */
	protected <T> DBCursor prepare(final Query<T> query, final boolean keysOnly) {
		final Class<?> clazz = query.getQueriedClass();
		final DBCollection collection = getDatabase().getCollection(ClassInfo.getClassInfo(clazz).tableName);

		final BasicDBObject queryObject = new BasicDBObject();
		final List<QueryFilter> filters = query.getFilters();
		for (final QueryFilter filter : filters) {
			QueryFilterSimple simpleFilter = (QueryFilterSimple) filter;
			//final Field f = filter.field;
			final Field f = simpleFilter.field;
			final String propertyName = ClassInfo.getColumnNames(f)[0];
			//Object value = filter.value;
			Object value = simpleFilter.value;
			//final String op = operators.get(filter.operator);
			final String op = operators.get(simpleFilter.operator);

			if (value != null && ClassInfo.isModel(value.getClass())) {
				Object id = getId(value);
				if (op != null) {
					id = new BasicDBObject(op, id);
				}
				queryObject.put(propertyName, id);
			}
			else {
				if (ClassInfo.isId(f)) {
					value = new ObjectId(value.toString());
					if (op != null) {
						value = new BasicDBObject(op, value);
					}
					queryObject.put(ID_FIELD, value);
				}
				else {
					if (op != null) {
						value = new BasicDBObject(op, value);
					}
					queryObject.put(propertyName, value);
				}
			}
		}

		final BasicDBObject sort = new BasicDBObject();
		final List<QueryOrder> orders = query.getOrders();
		for (final QueryOrder order : orders) {
			final Field f = order.field;
			if (ClassInfo.isId(f)) {
				sort.put(ID_FIELD, 1);
			}
			else {
				sort.put(ClassInfo.getColumnNames(f)[0], order.ascending ? 1 : -1);
			}
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Query using: " + queryObject);
		}

		DBCursor cursor = null;
		if (keysOnly) {
			cursor = collection.find(queryObject, new BasicDBObject(ID_FIELD, 1));
		}
		else {
			cursor = collection.find(queryObject);
		}
		if (sort.isEmpty() == false) {
			cursor = cursor.sort(sort);
		}
		return cursor;
	}

	/**
	 * Get the value of a field property from a model object.
	 * 
	 * @param object
	 *            is the model object
	 * @param field
	 *            is the field to read
	 * @return
	 */
	private Object readField(final Object object, final Field field) {
		field.setAccessible(true);
		try {
			return field.get(object);
		}
		catch (final Exception e) {
			throw new SienaException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#rollbackTransaction()
	 */
	@Override
	public void rollbackTransaction() {
	}

	/**
	 * Set the field value on a model object.
	 * 
	 * @param object
	 *            is the model instance
	 * @param f
	 *            is the field on the model
	 * @param value
	 *            is the value to set on the model
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void setFromObject(final Object object, final Field f, final Object value) throws IllegalArgumentException, IllegalAccessException {
		Util.setFromObject(object, f, value);
	}

	/**
	 * Set the ID from an entity onto the appropriate ID field on the model.
	 * 
	 * @param f
	 *            is the field on the model
	 * @param obj
	 *            is the model instance
	 * @param id
	 *            is the id to set
	 */
	private void setId(final Field f, final Object obj, final Object id) {
		try {
			Object value = id;
			if (f.getType() == String.class) {
				value = value.toString();
			}
			f.setAccessible(true);
			f.set(obj, value);
		}
		catch (final Exception e) {
			throw new SienaException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#supportedOperators()
	 */
	@Override
	public String[] supportedOperators() {
		return supportedOperators;
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#update(java.lang.Object)
	 */
	@Override
	public void update(final Object obj) {
		try {
			final Class<?> clazz = obj.getClass();
			final DBCollection collection = getDatabase().getCollection(ClassInfo.getClassInfo(clazz).tableName);
			final DBObject entity = new BasicDBObject();
			fillEntity(obj, entity);
			final BasicDBObject queryObject = new BasicDBObject();
			queryObject.put(ID_FIELD, new ObjectId(getId(obj).toString()));
			collection.update(queryObject, entity);
		}
		catch (final SienaException e) {
			throw e;
		}
		catch (final Exception e) {
			throw new SienaException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see siena.PersistenceManager#save(java.lang.Object)
	 */
	@Override
	public void save(Object obj) {
		Class clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		Field idField = info.getIdField();
		Object idVal = Util.readField(obj, idField);
		if (idVal == null) {
			insert(obj);
		} else {
			update(obj);
		}
	}

	@Override
	public int save(Object... paramArrayOfObject) {
		return save(Arrays.asList(paramArrayOfObject));
	}

	@Override
	public int save(Iterable<?> paramIterable) {
		int result = 0;
		for (Iterator<?> it=paramIterable.iterator(); it.hasNext();) {
			try {
				save(it.next());
				result++;
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return result;	
	}

	@Override
	public int insert(Object... paramArrayOfObject) {
		return insert(Arrays.asList(paramArrayOfObject));
	}

	@Override
	public int insert(Iterable<?> paramIterable) {
		int result = 0;
		for (Iterator<?> it=paramIterable.iterator(); it.hasNext();) {
			try {
				insert(it.next());
				result++;
			} catch (Exception e) {
				LOGGER.warn(e);
			}
		}
		return result;
	}

	@Override
	public int delete(Object... paramArrayOfObject) {
		return delete(Arrays.asList(paramArrayOfObject));
	}

	@Override
	public int delete(Iterable<?> paramIterable) {
		int result = 0;
		for (Iterator<?> it=paramIterable.iterator(); it.hasNext();) {
			try {
				delete(it.next());
				result++;
			} catch (Exception e) {
				LOGGER.warn(e);
			}
		}
		return result;
	}

	@Override
	public <T> int deleteByKeys(Class<T> paramClass, Object... paramArrayOfObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> int deleteByKeys(Class<T> paramClass, Iterable<?> paramIterable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int get(Object... paramArrayOfObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> int get(Iterable<T> paramIterable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getByKey(Class<T> paramClass, Object paramObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<T> getByKeys(Class<T> paramClass, Object... paramArrayOfObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<T> getByKeys(Class<T> paramClass, Iterable<?> paramIterable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> int update(Object... paramArrayOfObject) {
		return update(Arrays.asList(paramArrayOfObject));
	}

	@Override
	public <T> int update(Iterable<T> paramIterable) {
		int result = 0;
		for (Iterator<?> it=paramIterable.iterator(); it.hasNext();) {
			try {
				update(it.next());
				result++;
			} catch (Exception e) {
				LOGGER.warn(e);
			}
		}
		return result;
	}

	@Override
	public void beginTransaction() {
		//do nothing
	}

	@Override
	public <T> int update(Query<T> paramQuery, Map<String, ?> paramMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Iterable<T> iter(Query<T> paramQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Iterable<T> iter(Query<T> paramQuery, int paramInt) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Iterable<T> iter(Query<T> paramQuery, int paramInt, Object paramObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void paginate(Query<T> paramQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void nextPage(Query<T> paramQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void previousPage(Query<T> paramQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> PersistenceManagerAsync async() {
		throw new UnsupportedOperationException();
	}
}
