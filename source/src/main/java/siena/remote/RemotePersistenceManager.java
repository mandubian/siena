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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Model;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSimple;
import siena.QueryOrder;
import siena.SienaException;
import siena.Util;
import siena.core.async.PersistenceManagerAsync;

public class RemotePersistenceManager extends AbstractPersistenceManager {
	
	private Connector connector;
	private Serializer serializer;
	private String key;

	public void init(Properties p) {
		String connectorImpl = p.getProperty("connector");
		if(connectorImpl != null) {
			try {
				connector = (Connector) Class.forName(connectorImpl).newInstance();
			} catch (Exception e) {
				throw new SienaException("Error while instantiating connector: "+connectorImpl, e);
			}
		} else {
			connector = new URLConnector();
		}
		
		String serializerImpl = p.getProperty("serializer");
		if(serializerImpl != null) {
			try {
				serializer = (Serializer) Class.forName(serializerImpl).newInstance();
			} catch (Exception e) {
				throw new SienaException("Error while instantiating serializer: "+serializerImpl, e);
			}
		} else {
			serializer = new XmlSerializer();
		}
		
		key = p.getProperty("key");
		
		connector.configure(p);
	}

	public void delete(Object obj) {
		simpleRequest("delete", obj, true);
	}

	public void get(Object obj) {
		simpleRequest("get", obj, true);
	}

	public void insert(Object obj) {
		simpleRequest("insert", obj, false);
	}

	public void update(Object obj) {
		simpleRequest("update", obj, false);
	}
	
	protected Document createRequest(String name) {
		Document d = DocumentHelper.createDocument();
		Element root = d.addElement(name);
		if(key != null) {
			String time = Long.toString(System.currentTimeMillis());
			root.addAttribute("time", time);
			root.addAttribute("hash", Util.sha1(time+key));
		}
		return d;
	}
	
	private Document createRequest(String name, Object entity, boolean ids) {
		Document d = createRequest(name);
		Common.fillRequestElement(entity, d.getRootElement(), ids);
		return d;
	}
	
	private void simpleRequest(String name, Object entity, boolean ids) {
		Document request = createRequest(name, entity, ids);
		Document response = send(request);
		Element root = response.getRootElement();
		String rootName = root.getName();
		if("error".equals(rootName)) {
			throw new SienaException(root.attributeValue("class") + " " +root.getText());
		}
		if("object".equals(rootName)) {
			Common.parseEntity(entity, root, entity.getClass().getClassLoader());
		}
	}
	
	protected Document send(Document request) {
		try {
			connector.connect();
			serializer.serialize(request, connector.getOutputStream());
			Document response = serializer.deserialize(connector.getInputStream());
			connector.close();
			return response;
		} catch(IOException e) {
			throw new SienaException(e);
		}
	}

	public void rollbackTransaction() {
	}

	public void beginTransaction(int isolationLevel) {
	}

	public void closeConnection() {
	}

	public void commitTransaction() {
	}

	private <T> Document createRequest(Query<T> query) {
		Class<?> clazz = query.getQueriedClass();
		Document request = createRequest("query");
		request.getRootElement().addAttribute("class", clazz.getName());
		
		List<QueryFilter> filters = query.getFilters();
		for (QueryFilter filter : filters) {
			if(QueryFilterSimple.class.isAssignableFrom(filter.getClass())){
				QueryFilterSimple qf = (QueryFilterSimple)filter;

				Field field = qf.field;
				Object value = qf.value;
				
				Element filtr = request.getRootElement().addElement("filter");
				filtr.addAttribute("field", field.getName());
				filtr.addAttribute("operator", qf.operator);
				
				if(ClassInfo.isModel(value.getClass())) {
					Common.fillRequestElement((Model) value, filtr, true);
				} else {
					filtr.setText(Util.toString(field, value));
				}
			}
		}
		
		List<QueryOrder> orders = query.getOrders();
		for (QueryOrder order : orders) {
			Field field = order.field;
			request.getRootElement().addElement("order")
				.addAttribute("field", field.getName())
				.addAttribute("ascending", Boolean.toString(order.ascending));
		}
		return request;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> sendAndParse(Document request) {
		Document response = send(request);
		Element root = response.getRootElement();
		if("error".equals(root.getName())) {
			throw new SienaException(root.attributeValue("class") + " " +root.getText());
		}
		List<Element> result = response.getRootElement().elements("object");
		List<T> list = new ArrayList<T>(result.size());
		for (Element element : result) {
			list.add((T) Common.parseEntity(element, null));
		}
		return list;
	}

	@Override
	public <T> List<T> fetch(Query<T> query) {
		Document request = createRequest(query);
		return sendAndParse(request);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		Document request = createRequest(query);
		request.getRootElement().addAttribute("limit",  Integer.toString(limit));
		return sendAndParse(request);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		Document request = createRequest(query);
		request.getRootElement().addAttribute("limit",  Integer.toString(limit));
		request.getRootElement().addAttribute("offset", offset.toString());
		return sendAndParse(request);
	}

	@Override
	public <T> int count(Query<T> query) {
		return fetch(query).size(); // TODO: change this!
	}

	@Override
	public <T> int delete(Query<T> query) {
		throw new NotImplementedException();
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		throw new NotImplementedException();
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		throw new NotImplementedException();
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		throw new NotImplementedException();
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		// TODO!
		throw new NotImplementedException();

	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		// TODO!
		throw new NotImplementedException();
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		// TODO!
		throw new NotImplementedException();
	}


	@Override
	public String[] supportedOperators() {
		return null; // TODO!
	}

	@Override
	public <T> void release(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int insert(Object... objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insert(Iterable<?> objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Iterable<?> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int get(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int get(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int update(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int update(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> void nextPage(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void previousPage(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> PersistenceManagerAsync async() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> void paginate(Query<T> query) {
		// TODO Auto-generated method stub
		
	}


	
}
