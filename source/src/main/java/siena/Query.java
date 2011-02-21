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

/**
 * The Siena interface for performing queries.
 *
 * @author gimenete
 *
 */

public interface Query<T> {
	
	List<QueryFilter> getFilters();
	List<QueryOrder> getOrders();
	List<QuerySearch> getSearches();
	List<QueryJoin> getJoins();
	
	Query<T> filter(String fieldName, Object value);
	Query<T> order(String fieldName);
	Query<T> search(String match, boolean inBooleanMode, String index);
	Query<T> join(String field, String... sortFields);

	T get();
	List<T> fetch();
	List<T> fetch(int limit);
	List<T> fetch(int limit, Object offset);

	int count();
	
	int delete();

	List<T> fetchKeys();
	List<T> fetchKeys(int limit);
	List<T> fetchKeys(int limit, Object offset);

	Iterable<T> iter();
	Iterable<T> iter(int limit);
	Iterable<T> iter(int limit, Object offset);
		
	Query<T> clone();
	
	Class<T> getQueriedClass();

	Object raw(String request);
	
	Query<T> paginate(int size);
    Query<T> offset(int offset);

    /* 
     * sets options on the query. 
     * By default, this function is not used but expert users could trigger some specific options such as the REUSABLE
     */
    Query<T> customize(QueryOption... options);
    
    /* 
     * retrieves an option by its type
     */
    QueryOption option(QueryOption.Type option);	
	
    /* 
     * retrieves all options
     */
    Map<QueryOption.Type, QueryOption> options();	
    
    /**
     * triggers ON the query reuse mechanism for advanced users
     */
    Query<T> reuse();

    /**
     * release all resources of a query and triggers OFF the query reuse
     */
    Query<T> release();
    
	@Deprecated
	int count(int limit);
	@Deprecated
	int count(int limit, Object offset);
	
	@Deprecated
	Object nextOffset();

	@Deprecated
	void setNextOffset(Object nextOffset);
}
