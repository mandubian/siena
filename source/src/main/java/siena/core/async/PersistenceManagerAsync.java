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
package siena.core.async;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.BaseQueryData;
import siena.PersistenceManager;
import siena.SienaException;
import siena.core.batch.BatchAsync;

/**
 * This is the core interface to implement on <code>Siena</code>.
 * <code>PersistenceManagerFactory</code> will instanciate implementations of
 * this interface when required. Most of the methods of this interface will be
 * called indirectly from the <code>Model</code> class.
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 * 
 */
public interface PersistenceManagerAsync {

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
	 * Method for obtaining <code>QueryAsync</code> implementations.
	 * 
	 * @param clazz
	 *            The object types that will be queried.
	 * @return A <code>QueryAsync</code> object that lets make queries of objects of
	 *         the given type.
	 */
	<T> QueryAsync<T> createQuery(Class<T> clazz);
	<T> QueryAsync<T> createQuery(BaseQueryData<T> query);

	/**
	 * Method for obtaining <code>BatchAsync</code> implementations.
	 * 
	 * @return A <code>BatchAsync</code> object that lets make batch operations
	 */
	<T> BatchAsync<T> createBatch(Class<T> clazz);
	
	/**
	 * Method for obtaining <code>Sync PersistenceManager</code> implementations.
	 */
	PersistenceManager sync();


	/**
	 * This method fills all the fields of the given object using its primary key value
	 * to extract the entity from DB in an asynchronous mode.
	 * 
	 * @param obj The object that contains the primary key values, and also the
	 *            object where the information will be loaded into.
	 * @throws SienaException if the object couldn't be inserted.
	 */
	SienaFuture<Void> get(Object obj);

	/**
	 * Inserts objects in a batch mode into the database using asynchronous mode. 
	 * 
	 * @param objects An array of objects to be inserted into the database
	 * @throws SienaException if the object couldn't be inserted.
	 */
	SienaFuture<Integer> insert(Object... objects);

	/**
	 * Inserts objects in a batch mode into the database using asynchronous mode. 
	 * 
	 * @param objects Any iterable of objects to be interated and inserted into the database
	 * @throws SienaException if the object couldn't be inserted.
	 */
	SienaFuture<Integer> insert(Iterable<?> objects);

	/**
	 * Inserts an object into the database. Any generated primary key will be
	 * loaded into the given object. 
	 * 
	 * @param obj The object that will be inserted into the database
	 * @throws SienaException if the object couldn't be inserted.
	 */
	SienaFuture<Void> insert(Object obj);

	/**
	 * Deletes objects in a batch mode from the database.
	 * The objects must be filled at least with the primary keys.
	 * 
	 * @param objects An array of objects to be deleted from the database
	 * @throws SienaException if the object couldn't be deleted.
	 */
	SienaFuture<Integer> delete(Object... models);

	/**
	 * Deletes objects in a batch mode from the database.
	 * The objects must be filled at least with the primary keys.
	 * 
	 * @param objects an iterable of objects to be deleted from the database
	 * @throws SienaException if the object couldn't be deleted.
	 */
	SienaFuture<Integer> delete(Iterable<?> models);

	/**
	 * Deletes objects in a batch mode from the database only using their keys.
	 * Here one don't provide full objects but simply the primary key values 
	 * identifying those objects
	 * 
	 * @param keys an array of keys to be deleted from the database
	 * @throws SienaException if the object couldn't be deleted.
	 */
	<T> SienaFuture<Integer> deleteByKeys(Class<T> clazz, Object... keys);

	/**
	 * Deletes objects in a batch mode from the database only using their keys.
	 * Here one don't provide full objects but simply the primary key values 
	 * identifying those objects
	 * 
	 * @param keys an iterable of keys to be deleted from the database
	 * @throws SienaException if the object couldn't be deleted.
	 */
	<T> SienaFuture<Integer> deleteByKeys(Class<T> clazz, Iterable<?> keys);

	/**
	 * Deletes the given object from the database. The object must contain at
	 * least the primary key values.
	 * 
	 * @param obj The object that will be deleted. The object is deleted from
	 *            the database but the instance object still lives in the Java
	 *            Virtual Machine.
	 * @throws SienaException if the object couldn't be deleted.
	 */
	SienaFuture<Void> delete(Object obj);

	/**
	 * Updates the values of an object from the database. If the object couldn't
	 * be updated a <code>SienaException</code> must be thrown.
	 * 
	 * @param obj The object that will be updated.
	 */
	SienaFuture<Void> update(Object obj);


	/**
	 * gets objects (by keys or entities) in a batch mode from the database.
	 * Any generated primary key will be loaded into the given object. If the
	 * object couldn't be inserted a <code>SienaException</code> must be thrown.
	 * 
	 * @param objects
	 *            The objects that will be deleted from the database
	 */
	SienaFuture<Integer> get(Object... models);

	<T> SienaFuture<Integer> get(Iterable<T> models);

	<T> SienaFuture<List<T>> getByKeys(Class<T> clazz, Object... keys);

	<T> SienaFuture<List<T>> getByKeys(Class<T> clazz, Iterable<?> keys);
	
	SienaFuture<Integer> update(Object... objects);
	<T> SienaFuture<Integer> update(Iterable<T> objects);
	
	/**
	 * begins a transaction
	 * 
	 */
	SienaFuture<Void> beginTransaction(int isolationLevel);

	/**
	 * commits a transaction
	 * 
	 */
	SienaFuture<Void> commitTransaction();

	/**
	 * rollbacks a transaction
	 * 
	 */
	SienaFuture<Void> rollbackTransaction();

	/**
	 * closes database connection
	 * 
	 */
	SienaFuture<Void> closeConnection();

	// Methods needed by Query class

	<T> SienaFuture<T> get(QueryAsync<T> query);

	<T> SienaFuture<List<T>> fetch(QueryAsync<T> query);

	<T> SienaFuture<List<T>> fetch(QueryAsync<T> query, int limit);

	<T> SienaFuture<List<T>> fetch(QueryAsync<T> query, int limit, Object offset);

	<T> SienaFuture<Integer> count(QueryAsync<T> query);

	<T> SienaFuture<Integer> delete(QueryAsync<T> query);

	<T> SienaFuture<List<T>> fetchKeys(QueryAsync<T> query);

	<T> SienaFuture<List<T>> fetchKeys(QueryAsync<T> query, int limit);

	<T> SienaFuture<List<T>> fetchKeys(QueryAsync<T> query, int limit, Object offset);

	<T> SienaFuture<Iterable<T>> iter(QueryAsync<T> query);

	<T> SienaFuture<Iterable<T>> iter(QueryAsync<T> query, int limit);

	<T> SienaFuture<Iterable<T>> iter(QueryAsync<T> query, int limit, Object offset);

	<T> void release(QueryAsync<T> query);
	<T> void nextPage(QueryAsync<T> query);
	<T> void previousPage(QueryAsync<T> query);
	<T> SienaFuture<Integer> update(QueryAsync<T> query, Map<String, ?> fieldValues);


	String[] supportedOperators();

}
