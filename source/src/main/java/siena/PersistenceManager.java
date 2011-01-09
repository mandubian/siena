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
import java.util.Properties;

/**
 * This is the core interface to implement on <code>Siena</code>.
 * <code>PersistenceManagerFactory</code> will instanciate implementations
 * of this interface when required.
 * Most of the methods of this interface will be called indirectly from the
 * <code>Model</code> class.
 *
 * @author gimenete
 *
 */
public interface PersistenceManager {

	/**
	 * When <code>PersistenceManagerFactory</code> is called it loads
	 * the <code>siena.properties</code> file. The only information
	 * that <code>PersistenceManagerFactory</code> needs is the
	 * <code>implementation</code> parameter. That parameter indicates
	 * the qualified class name of the <code>PersistenceManager</code>
	 * implementation that will be used.
	 *
	 * The entire <code>Properties</code> object then is passed
	 * to this method: the <code>init()</code> method. Implementations
	 * would use this object to set up configuration parameters such
	 * as the database connection.
	 *
	 * @param p The content of the <code>siena.properties</code> file.
	 */
	public void init(Properties p);
	
	/**
	 * Method for obtaining <code>Query</code> implementations.
	 *
	 * @param clazz The object types that will be queried.
	 * @return A <code>Query</code> object that lets make queries of objects of the
	 * given type.
	 */
	public <T> Query<T> createQuery(Class<T> clazz);

	/**
	 * This method must read all the information of the given object.
	 * Typically the object will contain only the primary key values.
	 *
	 * @param obj The object that contains the primary key values, and
	 * also the object where the information will be loaded into.
	 */
	public void get(Object obj);

	/**
	 * Deletes the given object from the database. The object must
	 * contain at least the primary key values. If the object
	 * couldn't be deleted for any reason including there is no
	 * record with the given primary key values, this method must
	 * throw a <code>SienaException</code>.
	 *
	 * @param obj The object that will be deleted. The object
	 * is deleted from the database but the instance object still
	 * lives in the Java Virtual Machine.
	 */
	public void delete(Object obj);

	/**
	 * Inserts an object into the database. Any generated primary
	 * key will be loaded into the given object. If the object
	 * couldn't be inserted a <code>SienaException</code>
	 * must be thrown.
	 *
	 * @param obj The object that will be inserted into the database
	 */
	public void insert(Object obj);

	/**
	 * Updates the values of an object from the database. If the object
	 * couldn't be updated a <code>SienaException</code>
	 * must be thrown.
	 *
	 * @param obj The object that will be updated.
	 */
	public void update(Object obj);

	public void beginTransaction(int isolationLevel);

	// methods for transactions
	
	public void commitTransaction();

	public void rollbackTransaction();

	public void closeConnection();
	
	// Methods needed by Query class

	public <T> T get(Query<T> query);

	public <T> List<T> fetch(Query<T> query);

	public <T> List<T> fetch(Query<T> query, int limit);

	public <T> List<T> fetch(Query<T> query, int limit, Object offset);

	public <T> int count(Query<T> query);

	@Deprecated
	public <T> int count(Query<T> query, int limit);

	@Deprecated
	public <T> int count(Query<T> query, int limit, Object offset);
	
	public <T> int delete(Query<T> query);

	public <T> List<T> fetchKeys(Query<T> query);
	public <T> List<T> fetchKeys(Query<T> query, int limit);
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset);
	
	public <T> Iterable<T> iter(Query<T> query);
	public <T> Iterable<T> iter(Query<T> query, int limit);
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset);

	public <T> Iterable<T> iter(Query<T> query, String field);
	public <T> Iterable<T> iter(Query<T> query, String field, int limit);
	public <T> Iterable<T> iter(Query<T> query, String field, int limit, Object offset);
	
	public String[] supportedOperators();

}
