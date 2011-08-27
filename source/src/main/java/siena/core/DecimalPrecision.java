/*
 * Copyright 2011 pascal VOitot <pascal.voitot@mandubian.org>
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
package siena.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DecimalPrecision {
	public enum StorageType {
		NATIVE,	// NATIVE means it uses native DB storage for bigdecimal: for SQL: SQL type DECIMAL - for GAE:String 
		DOUBLE, // stored as a double
		STRING	// stored as a string (not padded with 0 yet)
	}
	
	StorageType storageType() default StorageType.NATIVE;
	int size() default 19;
	int scale() default 2;
}
