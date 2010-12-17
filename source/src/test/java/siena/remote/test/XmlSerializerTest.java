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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import siena.remote.XmlSerializer;

public class XmlSerializerTest extends TestCase {
	
	public void testXmlSerializer() throws Exception {
		XmlSerializer serializer = new XmlSerializer();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Document document = DocumentHelper.createDocument();
		document.addElement("root");
		serializer.serialize(document, baos);
		
		Document result = serializer.deserialize(new ByteArrayInputStream(baos.toByteArray()));
		
		assertEquals(result.getRootElement().getName(),
				document.getRootElement().getName());
	}
	
	public void testDocumentException() {
		XmlSerializer serializer = new XmlSerializer();
		try {
			serializer.deserialize(new ByteArrayInputStream("[hello]".getBytes()));
		} catch(Exception e) {
			return;
		}
		fail("deserialize() should have been failed due to a DocumentException");
	}

}
