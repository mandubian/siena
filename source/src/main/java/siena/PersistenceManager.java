/*
 * Copyright 2008 Alberto Gimeno <gimenete at gmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package siena;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.core.ListQuery;
import siena.core.async.PersistenceManagerAsync;
import siena.core.batch.Batch;

/**
 * This is the core interface to implement on <code>Siena</code>.
 * <code>PersistenceManagerFactory</code> will instanciate implementations of
 * this interface when required. Most of the methods of this interface will be
 * called indirectly from the <code>Model</code> class.
 * 
 * @author gimenete
 * 
 */
public interface PersistenceManager {

	/**
	 * When <code>PersistenceManagerFactory</code> is called it loads the
	 * <code>siena.properties</code> file. The only information that
	 * <code>PersistenceManagerFactory</code> needs is the
	 * <code>implementation</code> parameter. That parameter indicates the
	 * qualified class name of the <code>PersistenceManager</code>
	 * implementation that will be used.
	 * 
	 * The entire <code>Properties</code> object then is passed to this method:
	 * the <code>init()</code> method. Implementations would use this object to
	 * set up configuration parameters such as the database connection.
	 * 
	 * @param p
	 *            The content of the <code>siena.properties</code> file.
	 */
	void init(Properties p);

	/**
	 * Method for obtaining <code>Query</code> implementations.
	 * 
	 * @param clazz
	 *            The object types that will be queried.
	 * @return A <code>Query</code> object that lets make queries of objects of
	 *         the given type.
	 */
	<T> Query<T> createQuery(Class<T> clazz);
	<T> Query<T> createQuery(BaseQueryData<T> query);

	/**
	 * Method for obtaining <code>Batch</code> implementations.
	 * 
	 * @return A <code>Batch</code> object that lets make batch operations
	 */
	<T> Batch<T> createBatch(Class<T> clazz);
 
	/**
	 * Method for obtaining <code>AggregatorQuery</code> implementations.
	 * 
	 * @return A <code>AggregatorQuery</code> object that lets make batch operations
	 */
	<T> ListQuery<T> createListQuery(Class<T> clazz);
	
	/**
	 * This method fills all the fields of the given object using its primary key value
	 * to extract the entity from DB.
	 * 
	 * @param obj The object that contains the primary key values, and also the
	 *            object where the information will be loaded into.
	 */
	void get(Object obj);

	/**
	 * Inserts an object into the database. Any generated primary key will be
	 * loaded into the given object. If the object couldn't be inserted a
	 * <code>SienaException</code> must be thrown.
	 * 
	 * @param obj
	 *            The object that will be inserted into the database
	 */
	void insert(Object obj);
	
	/**
	 * Deletes the given object from the database. The object must contain at
	 * least the primary key values. If the object couldn't be deleted for any
	 * reason including there is no record with the given primary key values,
	 * this method must throw a <code>SienaException</code>.
	 * 
	 * @param obj
	 *            The object that will be deleted. The object is deleted from
	 *            the database but the instance object still lives in the Java
	 *            Virtual Machine.
	 */
	void delete(Object obj);

	/**
	 * Updates the values of an object from the database. If the object couldn't
	 * be updated a <code>SienaException</code> must be thrown.
	 * 
	 * @param obj
	 *            The object that will be updated.
	 */
	void update(Object obj);

	
	/**
	 * save means insertOrUpdate: if the value exists, it updates, if not, it inserts.
	 * If the object couldn't be inserted or updated a <code>SienaException</code> must be thrown.
	 * 
	 * @param obj
	 *            The object that will be updated.
	 */
	void save(Object obj);
	int save(Object... objects);
	int save(Iterable<?> objects);

	/**
	 * Inserts objects in a batch mode into the database. Any generated primary
	 * key will be loaded into the given object. If the object couldn't be
	 * inserted a <code>SienaException</code> must be thrown.
	 * 
	 * @param objects
	 *            The objects that will be inserted into the database
	 */
	int insert(Object... objects);

	int insert(Iterable<?> objects);



	/**
	 * Deletes objects (by keys or entities) in a batch mode from the database.
	 * Any generated primary key will be loaded into the given object. If the
	 * object couldn't be inserted a <code>SienaException</code> must be thrown.
	 * 
	 * @param objects
	 *            The objects that will be deleted from the database
	 */
	int delete(Object... models);

	int delete(Iterable<?> models);

	<T> int deleteByKeys(Class<T> clazz, Object... keys);

	<T> int deleteByKeys(Class<T> clazz, Iterable<?> keys);


	/**
	 * gets objects (by keys or entities) in a batch mode from the database.
	 * Any generated primary key will be loaded into the given object. If the
	 * object couldn't be inserted a <code>SienaException</code> must be thrown.
	 * 
	 * @param objects
	 *            The objects that will be deleted from the database
	 */
	int get(Object... models);

	<T> int get(Iterable<T> models);

	<T> T getByKey(Class<T> clazz, Object key);

	<T> List<T> getByKeys(Class<T> clazz, Object... keys);

	<T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys);
	
	<T> int update(Object... models);
	<T> int update(Iterable<T> models);

	
	void beginTransaction(int isolationLevel);
	void beginTransaction();

	void commitTransaction();

	void rollbackTransaction();

	void closeConnection();
	
	

	// Methods needed by Query class
	<T> T get(Query<T> query);
	<T> int delete(Query<T> query);
	<T> int update(Query<T> query, Map<String, ?> fieldValues);
	<T> int count(Query<T> query);

	<T> List<T> fetch(Query<T> query);
	<T> List<T> fetch(Query<T> query, int limit);
	<T> List<T> fetch(Query<T> query, int limit, Object offset);

	<T> List<T> fetchKeys(Query<T> query);
	<T> List<T> fetchKeys(Query<T> query, int limit);
	<T> List<T> fetchKeys(Query<T> query, int limit, Object offset);

	<T> Iterable<T> iter(Query<T> query);
	<T> Iterable<T> iter(Query<T> query, int limit);
	<T> Iterable<T> iter(Query<T> query, int limit, Object offset);
	<T> Iterable<T> iterPerPage(Query<T> query, int pageSize);

	<T> void release(Query<T> query);
	<T> void paginate(Query<T> query);
	<T> void nextPage(Query<T> query);
	<T> void previousPage(Query<T> query);

	<T> PersistenceManagerAsync async();

	String[] supportedOperators();

	@Deprecated
	<T> int count(Query<T> query, int limit);
	@Deprecated
	<T> int count(Query<T> query, int limit, Object offset);
}
