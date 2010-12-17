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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.dom4j.Document;

import siena.remote.Connector;
import siena.remote.RemoteStub;
import siena.remote.Serializer;

public class MockConnector implements Connector, Serializer {
	
	public Document request;
	public Document response;
	public static String key;
	
	public MockConnector() {
	}
	
	public void configure(Properties properties) {
		key = properties.getProperty("key");
	}

	public void close() throws IOException {
	}

	public void connect() throws IOException {
	}

	public InputStream getInputStream() throws IOException {
		return null;
	}

	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	public Document deserialize(InputStream in) throws IOException {
		RemoteStub stub = new RemoteStub(null, this.getClass().getClassLoader());
		stub.setKey(key);
		return stub.process(request);
	}

	public void serialize(Document document, OutputStream out)
			throws IOException {
		request = document;
	}

}