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

/**
 * The Siena interface for performing queries.
 *
 * @author gimenete
 *
 */

public interface Query<T> {
	
	public List<QueryFilter> getFilters();

	public List<QueryOrder> getOrders();

	public List<QuerySearch> getSearches();

	public Query<T> filter(String fieldName, Object value);

	public Query<T> order(String fieldName);

	public Query<T> search(String match, boolean inBooleanMode, String index);

	public T get();

	public List<T> fetch();

	public List<T> fetch(int limit);

	public List<T> fetch(int limit, Object offset);

	public int count();

	@Deprecated
	public int count(int limit);

	@Deprecated
	public int count(int limit, Object offset);
	
	public Object nextOffset();
	
	public void setNextOffset(Object nextOffset);
	
	public int delete();

	public List<T> fetchKeys();

	public List<T> fetchKeys(int limit);

	public List<T> fetchKeys(int limit, Object offset);
	
	public Iterable<T> iter(String field, int max);
	
	public Query<T> clone();
	
	public Class<T> getQueriedClass();

}
