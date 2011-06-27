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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import siena.core.options.QueryOption;

/**
 * The Siena interface for storing query data.
 * it extends Serializable so that all queries are serializable
 *
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public interface QueryData<T> extends Serializable{
    /**
     * finds an option from its identifier
     * 
     * @param option the option identifier
     * @return the found QueryOption
     */
    QueryOption option(int option);	
	
    /**
     * retrieves all options
     * 
     * @return the map of <Integer, QueryOption>
     */
    Map<Integer, QueryOption> options();	
        
	List<QueryFilter> getFilters();
	List<QueryOrder> getOrders();
	List<QueryFilterSearch> getSearches();
	List<QueryJoin> getJoins();
	List<QueryAggregated> getAggregatees();

	Class<T> getQueriedClass();

}
