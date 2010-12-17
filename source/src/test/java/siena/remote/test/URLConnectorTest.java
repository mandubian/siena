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
package siena.remote.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import junit.framework.TestCase;
import siena.SienaException;
import siena.remote.URLConnector;

public class URLConnectorTest extends TestCase {
	
	public void testURLConnector() throws Exception {
		URLConnector connector = new URLConnector();
		
		Properties properties = new Properties();
		properties.setProperty("backend", "http://example.com/");
		connector.configure(properties);
		
		connector.connect();
		
		OutputStream out = connector.getOutputStream();
		out.close();
		
		InputStream in = connector.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] buff = new byte[1024];
		int read;
		do {
			read = in.read(buff);
			if(read == -1) break;
			baos.write(buff, 0, read);
		} while(true);
		
		String s = new String(baos.toByteArray());
		assertTrue(s.indexOf("http://www.rfc-editor.org/rfc/rfc2606.txt") > 0);
		
		connector.close();
	}
	
	public void testMalformedURL() {
		URLConnector connector = new URLConnector();
		
		Properties properties = new Properties();
		properties.setProperty("backend", "hello");
		try {
			connector.configure(properties);
		} catch(SienaException e) {
			return;
		}
		fail("configure() should have been failed due to a MalformedURLException");
	}

}
