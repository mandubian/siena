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
package siena.core.batch;

import java.util.List;


/**
 * The Siena interface for performing batches.
 *
 * @author mandubian
 *
 */
public interface Batch<T> {
	int get(T... models);
	int get(Iterable<T> models);	
	List<T> getByKeys(Object... keys);
	List<T> getByKeys(Iterable<?> keys);
	
	int insert(T... models);
	int insert(Iterable<T> models);
	int update(T... models);
	int update(Iterable<T> models);
	int delete(T... models);
	int delete(Iterable<T> models);
	int deleteByKeys(Object... keys);
	int deleteByKeys(Iterable<?> keys);
	
	BatchAsync<T> async();
}
