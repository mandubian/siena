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

import siena.core.async.SienaFuture;


/**
 * The Siena interface for performing batches asynchronously.
 *
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public interface BatchAsync<T> {
	SienaFuture<Integer> insert(T... models);
	SienaFuture<Integer> insert(Iterable<T> models);
	SienaFuture<Integer> update(T... models);
	SienaFuture<Integer> update(Iterable<T> models);
	SienaFuture<Integer> delete(T... models);
	SienaFuture<Integer> delete(Iterable<T> models);
	SienaFuture<Integer> deleteByKeys(Object... keys);
	SienaFuture<Integer> deleteByKeys(Iterable<?> keys);
	Batch<T> sync();
}
