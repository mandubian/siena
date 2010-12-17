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
package siena.remote;

import static siena.remote.Common.fillRequestElement;
import static siena.remote.Common.parseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import siena.ClassInfo;
import siena.Model;
import siena.Query;
import siena.SienaException;
import siena.Util;

public class RemoteStub {
	
	private ClassLoader classLoader;
	private Serializer serializer;
	private String key;
	
	public RemoteStub(Serializer serializer, ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.serializer = serializer;
	}

	@SuppressWarnings("unchecked")
	public Document process(Document doc) {
		try {
			Element root = doc.getRootElement();
			if(key != null) {
				// TODO: cutom exception message if time is null
				long time = Long.parseLong(root.attributeValue("time"));
				String hash = root.attributeValue("hash");
				if(!Util.sha1(time+key).equals(hash)) {
					throw new SienaException("Invalid hash");
				}
				long diff = Math.abs(time - System.currentTimeMillis());
				if(diff > 10000) {
					throw new SienaException("Invalid time");
				}
			}
			String action = root.getName();
			if("insert".equals(action)) {
				Model obj = parseEntity(root, classLoader);
				obj.insert();
				return simpleResponse(obj, true);
			} else if("update".equals(action)) {
				parseEntity(root, classLoader).update();
			} else if("delete".equals(action)) {
				parseEntity(root, classLoader).delete();
			} else if("get".equals(action)) {
				Model obj = parseEntity(root, classLoader);
				obj.get();
				return simpleResponse(obj, false);
			} else if("query".equals(action)) {
				// TODO: convert document to QueryBase
				
				String clazzName = root.attributeValue("class");
				Class<? extends Model> clazz = (Class<? extends Model>) Common.classForName(clazzName, classLoader);
				Query<? extends Model> query = Model.all(clazz);

				List<Element> list = root.elements();
				for (Element element : list) {
					String name = element.getName();
					String fieldName = element.attributeValue("field");
					if("filter".equals(name)) {
						// TODO: operator attribute
						Field field = clazz.getField(fieldName);
						Object value = null;
						if(element.hasContent()) {
							if(ClassInfo.isModel(field.getType())) {
								value = Common.parseEntity(element, classLoader);
							} else {
								value = Util.fromString(field.getType(), element.getText());
							}
						}
						query.filter(fieldName, value);
					} else if("order".equals(name)) {
						// TODO: ascending attribute
						query.order(fieldName);
					}
				}

				String limit  = root.attributeValue("limit");
				String offset = root.attributeValue("offset");
				
				List<? extends Model> result = null;
				if(limit != null && offset != null) {
					result = query.fetch(Integer.parseInt(limit), Integer.parseInt(offset));
				} else if(limit != null) {
					result = query.fetch(Integer.parseInt(limit));
				} else {
					result = query.fetch();
				}
				
				Document response = DocumentHelper.createDocument();
				Element r = response.addElement("result");
				for (Model obj : result) {
					Element object = r.addElement("object");
					fillRequestElement(obj, object, false);
				}
				// TODO add nextOffset to response
				return response;
			}
		} catch(Throwable e) {
			return error(e);
		}
		return newDocument("ok");
	}
	
	private Document newDocument(String root) {
		return DocumentHelper.createDocument().addElement(root).getDocument();
	}
	
	private Document simpleResponse(Model obj, boolean ids) {
		Document response = newDocument("object");
		fillRequestElement(obj, response.getRootElement(), ids);
		return response;
	}
	
	private Document error(Throwable e) {
		Document response = DocumentHelper.createDocument();
		Element root = response.addElement("error");
		root.addAttribute("class", e.getClass().getName());
		root.setText(e.getMessage());
		return response;
	}
	
	public void execute(InputStream in, OutputStream out) throws IOException {
		Document request = serializer.deserialize(in);
		Document response = process(request);
		serializer.serialize(response, out);
	}

	public void setKey(String key) {
		this.key = key;
	}

}
