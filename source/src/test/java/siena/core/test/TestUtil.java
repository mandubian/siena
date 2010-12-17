/*
 * Copyright 2009 Alberto Gimeno <gimenete at gmail.com>
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
package siena.core.test;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;
import siena.Util;

public class TestUtil extends TestCase {
	
	public void testJoin() {
		assertEquals("", Util.join(new ArrayList<String>(), ", "));
		assertEquals("foo", Util.join(Arrays.asList("foo"), ", "));
		assertEquals("foo, bar", Util.join(Arrays.asList("foo", "bar"), ", "));
	}
	
	public void testSha1() {
		assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", Util.sha1(""));
		assertEquals("5de5f7ed4762f3e6555f479d98a75696170c1eb1", Util.sha1("siena"));
		assertEquals("622758a4191a450e0d94ad07b6d9d8ef67ffc485", Util.sha1("ma\u00f1o"));
	}

}
