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
package siena.sdb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Query;
import siena.QueryFilter;
import siena.QueryFilterSimple;
import siena.QueryOrder;
import siena.SienaException;
import siena.Util;
import siena.sdb.ws.Item;
import siena.sdb.ws.SelectResponse;
import siena.sdb.ws.SimpleDB;

public class SdbPersistenceManager extends AbstractPersistenceManager {
	
	private static final String[] supportedOperators = { "<", ">", ">=", "<=", "=" };
	private static long ioffset = Math.abs(0l+Integer.MIN_VALUE);

	private SimpleDB ws;
	private String prefix;
	private List<String> domains;

	public void init(Properties p) {
		String awsAccessKeyId = p.getProperty("awsAccessKeyId");
		String awsSecretAccessKey = p.getProperty("awsSecretAccessKey");
		if(awsAccessKeyId == null || awsSecretAccessKey == null)
			throw new SienaException("Both awsAccessKeyId and awsSecretAccessKey properties must be set");
		prefix = p.getProperty("prefix");
		if(prefix == null) prefix = "";
		ws = new SimpleDB(awsAccessKeyId, awsSecretAccessKey);
	}

	public void delete(Object obj) {
		ws.deleteAttributes(getDomainName(obj.getClass()), toItem(obj));
	}

	public void get(Object obj) {
		Item item = ws.getAttributes(getDomainName(obj.getClass()), getIdValue(obj)).item;
		fillModel(item, obj);
	}

	public void insert(Object obj) {
		ws.putAttributes(getDomainName(obj.getClass()), toItem(obj));
	}

	public void update(Object obj) {
		ws.putAttributes(getDomainName(obj.getClass()), toItem(obj));
	}

	public String getIdValue(Object obj) {
		try {
			return (String) ClassInfo.getIdField(obj.getClass()).get(obj);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}

	protected String getDomainName(Class<?> clazz) {
		String domain = prefix+ClassInfo.getClassInfo(clazz).tableName;
		if(domains == null) {
			domains = ws.listDomains(null, null).domains; // TODO pagination
		}
		if(!domains.contains(domain)) {
			ws.createDomain(domain);
		}
		return domain;
	}

	private String getAttributeName(Field field) {
		return ClassInfo.getColumnNames(field)[0];
	}

	private Object readField(Object object, Field field) {
		field.setAccessible(true);
		try {
			return field.get(object);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	private Item toItem(Object obj) {
		Item item = new Item();
		Class<?> clazz = obj.getClass();
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			try {
				String value = toString(field, field.get(obj));
				if(value != null)
					item.add(getAttributeName(field), value);
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		Field id = ClassInfo.getIdField(clazz);
		String name = (String) readField(obj, id);
		if(name == null) { // TODO: only if auto-generated
			try {
				name = UUID.randomUUID().toString();
				id.set(obj, name);
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		item.name = name;
		return item;
	}
	
	private static String toString(Field field, Object object) {
		if(object == null) return null;
		Class<?> type = field.getType();
		if(type == Integer.class || type == int.class) {
			return toString((Integer) object);
		}
		if(ClassInfo.isModel(type)) {
			try {
				return ClassInfo.getIdField(type).get(object).toString();
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return Util.toString(field, object);
	}

	private void fillModel(Item item, Object obj) {
		Class<?> clazz = obj.getClass();
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			List<String> values = item.attributes.get(getAttributeName(field));
			if(values == null || values.isEmpty())
				continue;
			try {
				String value = values.get(0);
				if(field.getType() == Integer.class || field.getType() == int.class) {
					field.set(obj, fromString(value));
				} else {
					Class<?> type = field.getType();
					if(ClassInfo.isModel(type)) {
						Object rel = type.newInstance();
						Field id = ClassInfo.getIdField(type);
						id.set(rel, value);
						field.set(obj, rel);
					} else {
						Util.setFromString(obj, field, value);
					}
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		
		Field id = ClassInfo.getIdField(clazz);
		try {
			id.set(obj, item.name);
		} catch (Exception e) {
			throw new SienaException(e);
		}
	}
	
	private static String toString(int i) {
		return String.format("%010d", i+ioffset);
	}
	
	private static int fromString(String s) {
		long l = Long.parseLong(s);
		return (int) (l-ioffset);
	}
	
	/* transactions */

	public void beginTransaction(int isolationLevel) {
	}

	public void closeConnection() {
	}

	public void commitTransaction() {
	}

	public void rollbackTransaction() {
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> query(Query<T> query, String suffix, String nextToken) {
		Class<?> clazz = query.getQueriedClass();
		String domain = getDomainName(clazz);
		String q = buildQuery(query, "select * from "+domain)+suffix;
		SelectResponse response = ws.select(q, nextToken);
		query.setNextOffset(response.nextToken);
		List<Item> items = response.items;
		List<T> result = new ArrayList<T>(items.size());
		for (Item item : items) {
			try {
				T object = (T) clazz.newInstance();
				fillModel(item, object);
				result.add(object);
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return result;
	}
	
	private <T> String buildQuery(Query<T> query, String prefix) {
		StringBuilder q = new StringBuilder(prefix);
		
		List<QueryFilter> filters = query.getFilters();
		if(!filters.isEmpty()) {
			q.append(" where ");
			
			boolean first = true;
			
			for (QueryFilter filter : filters) {
				if(QueryFilterSimple.class.isAssignableFrom(filter.getClass())){
					QueryFilterSimple qf = (QueryFilterSimple)filter;
					Field f      = qf.field;
					Object value = qf.value;
					String op    = qf.operator;
					
					if(!first) {
						q.append(" and ");
					}
					first = false;
					
					String column = null;
					if(ClassInfo.isId(f)) {
						column = "itemName()";
					} else {
						column = ClassInfo.getColumnNames(f)[0];
					}
					if(value == null && op.equals("=")) {
						q.append(column+" is null");
					} else {
						String s = SdbPersistenceManager.toString(f, value);
						q.append(column+op+SimpleDB.quote(s));
					}
				}
			}
			
		}
		
		List<QueryOrder> orders = query.getOrders();
		if(!orders.isEmpty()) {
			QueryOrder last = orders.get(orders.size()-1);
			Field field = last.field;
			
			if(ClassInfo.isId(field)) {
				q.append("order by itemName()");
			} else {
				q.append("order by ");
				q.append(ClassInfo.getColumnNames(field)[0]);
			}
			if(!last.ascending)
				q.append(" desc");
		}
		
		return q.toString();
	}

	@Override
	public <T> List<T> fetch(Query<T> query) {
		return query(query, "", null);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		return query(query, " limit "+limit, null);
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		return query(query, " limit "+limit, offset.toString());
	}

	@Override
	public <T> int count(Query<T> query) {
		Class<?> clazz = query.getQueriedClass();
		String domain = getDomainName(clazz);
		String q = buildQuery(query, "select count(*) from "+domain);
		SelectResponse response = ws.select(q, null);
		query.setNextOffset(response.nextToken);
		return Integer.parseInt(response.items.get(0).attributes.get("Count").get(0));
	}

	@Override
	public <T> int delete(Query<T> query) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void release(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] supportedOperators() {
		return supportedOperators;
	}

	@Override
	public void insert(Object... objects) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(Iterable<?> objects) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Object... models) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Iterable<?> models) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void deleteByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		
	}

}
