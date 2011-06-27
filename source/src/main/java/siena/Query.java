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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import siena.core.async.QueryAsync;
import siena.core.options.QueryOption;

/**
 * The Siena interface for performing queries.
 *
 * @author gimenete
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */

public interface Query<T> extends QueryData<T>{
	Query<T> filter(String fieldName, Object value);
	Query<T> order(String fieldName);
	Query<T> join(String field, String... sortFields);	
	Query<T> search(String match, String... fields);
	Query<T> search(String match, QueryOption opt, String... fields);

	Query<T> aggregated(Object aggregator, String fieldName);	

	T get();
	int delete();
	int update(Map<String, ?> fieldValues);
	int count();

	T getByKey(Object key);
	
	List<T> fetch();
	List<T> fetch(int limit);
	List<T> fetch(int limit, Object offset);

	List<T> fetchKeys();
	List<T> fetchKeys(int limit);
	List<T> fetchKeys(int limit, Object offset);

	Iterable<T> iter();
	Iterable<T> iter(int limit);
	Iterable<T> iter(int limit, Object offset);
	
	Iterable<T> iterPerPage(int limit);
		
	Query<T> copy();
	
	/**
	 * sets the limit number of entities to fetch
	 * 
	 * @param limit the limit size
	 * @return the query
	 */
	Query<T> limit(int limit);

	/**
	 * sets the offset from which to fetch the data from the current place
	 * 
	 * @param offset the offset
	 * @return the query
	 */
	Query<T> offset(Object offset);

	
	/**
	 * initializes the automatic pagination mechanism
	 * 
	 * @param size the page size
	 * @return the query
	 */
	Query<T> paginate(int size);
	
	/**
	 * when automatic pagination has been initialized, goes to next page
	 * 
	 * @return the query
	 */
	Query<T> nextPage();
	
	/**
	 * when automatic pagination has been initialized, goes to previous page
	 * 
	 * @return the query
	 */
	Query<T> previousPage();

    /* 
     * sets options on the query. 
     * By default, this function is not used but expert users could trigger some specific options such as the REUSABLE
     */
    Query<T> customize(QueryOption... options);
       
    /**
     * triggers ON the query reuse mechanism for advanced users
     * 
     * @return the query
     */
    Query<T> stateful();
    
    /**
     * triggers ON the query reuse mechanism for advanced users
     * 
     * @return the query
     */
    Query<T> stateless();

    /**
     * releases all resources of a query and triggers OFF the query reuse
     * 
     * @return the query
     */    
    Query<T> release();
    Query<T> resetData();
    
    /**
     * dumps a query to a safe String
     * 
     * @return the safe String representation
     */
    String dump(QueryOption... options);
    void dump(OutputStream os, QueryOption... options);

    /**
     * restores a query from a safe String
     * 
     * @return the restored Query
     */
    Query<T> restore(String dump, QueryOption... options);
    Query<T> restore(InputStream dump, QueryOption... options);

    /**
     * accesses the asynchronous mechanism
     */
    QueryAsync<T> async();
    
	PersistenceManager getPersistenceManager();

    /*
     * 
     * DEPRECATED APIs
     * 
     */
	@Deprecated
	int count(int limit);
	@Deprecated
	int count(int limit, Object offset);
	
	@Deprecated
	Object nextOffset();

	@Deprecated
	void setNextOffset(Object nextOffset);	

	@Deprecated
	Query<T> search(String match, boolean inBooleanMode, String index);
}
