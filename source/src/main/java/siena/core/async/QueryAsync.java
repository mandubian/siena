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

import siena.Query;
import siena.QueryData;
import siena.core.options.QueryOption;

/**
 * The Siena interface for performing asynchronous queries.
 *
 * @author mandubian <pascal.voitot@mandubian.org>

 */
public interface QueryAsync<T> extends QueryData<T>{
	
	QueryAsync<T> filter(String fieldName, Object value);
	QueryAsync<T> order(String fieldName);
	QueryAsync<T> join(String field, String... sortFields);	
	QueryAsync<T> aggregated(Object aggregator, String field);	
	QueryAsync<T> search(String match, String... fields);
	QueryAsync<T> search(String match, QueryOption opt, String... fields);
	
	SienaFuture<T> get();
	SienaFuture<T> getByKey(Object key);
	SienaFuture<Integer> delete();
	SienaFuture<Integer> update(Map<String, ?> fieldValues);
	SienaFuture<Integer> count();

	SienaFuture<List<T>> fetch();
	SienaFuture<List<T>> fetch(int limit);
	SienaFuture<List<T>> fetch(int limit, Object offset);

	SienaFuture<List<T>> fetchKeys();
	SienaFuture<List<T>> fetchKeys(int limit);
	SienaFuture<List<T>> fetchKeys(int limit, Object offset);

	SienaFuture<Iterable<T>> iter();
	SienaFuture<Iterable<T>> iter(int limit);
	SienaFuture<Iterable<T>> iter(int limit, Object offset);
	SienaFuture<Iterable<T>> iterPerPage(int limit);
		
	QueryAsync<T> clone();
	
	SienaFuture<Object> raw(String request);
	
	
	/**
	 * initializes the automatic pagination mechanism
	 * 
	 * @param size the page size
	 * @return the query
	 */
	QueryAsync<T> paginate(int size);
	
	/**
	 * sets the limit number of entities to fetch
	 * 
	 * @param limit the limit size
	 * @return the query
	 */
	QueryAsync<T> limit(int limit);

	/**
	 * sets the offset from which to fetch the data from the current place
	 * 
	 * @param offset the offset
	 * @return the query
	 */
	QueryAsync<T> offset(Object offset);

	/**
	 * when automatic pagination has been initialized, goes to next page
	 * 
	 * @return the query
	 */
	QueryAsync<T> nextPage();
	
	/**
	 * when automatic pagination has been initialized, goes to previous page
	 * 
	 * @return the query
	 */
	QueryAsync<T> previousPage();


    /* 
     * sets options on the query. 
     * By default, this function is not used but expert users could trigger some specific options such as the REUSABLE
     */
    QueryAsync<T> customize(QueryOption... options);
    
    
    /**
     * triggers ON the query stateful mechanism for advanced users
     * 
     * @return the query
     */
    QueryAsync<T> stateful();

    /**
     * triggers OFF the query stateful mechanism for advanced users
     * 
     * @return the query
     */
    QueryAsync<T> stateless();
    
    /**
     * releases all resources of a query and triggers OFF the query reuse
     * 
     * @return the query
     */    
    QueryAsync<T> release();
    QueryAsync<T> resetData();
    /**
     * dumps a query to a safe String
     * 
     * @return the safe String representation
     */
    String dump();

    /**
     * restores a query from a safe String
     * 
     * @return the restored Query
     */
    QueryAsync<T> restore(String dump);

    /**
     * accesses the synchronous mechanism
     */
    Query<T> sync();

	PersistenceManagerAsync getPersistenceManager();

}
