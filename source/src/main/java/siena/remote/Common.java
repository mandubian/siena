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

import java.lang.reflect.Field;
import java.util.List;

import org.dom4j.Element;

import siena.ClassInfo;
import siena.Model;
import siena.SienaException;
import siena.Util;

public class Common {
	
	public static void fillRequestElement(Object obj, Element element, boolean ids) {
		Class<?> clazz = obj.getClass();
		element.addAttribute("class", clazz.getName());
		
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if(field.getType() == Class.class) continue;
			if(ids && !ClassInfo.isId(field)) continue;
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(obj);
			} catch (Exception e) {
				throw new SienaException(e);
			}
			Class<?> type = field.getType();
			if(ClassInfo.isModel(type)) {
				Element f = element.addElement("object");
				f.addAttribute("name", field.getName());
				if(value != null) {
					fillRequestElement((Model) value, f, true);
				}
			} else {
				Element f = element.addElement("field");
				f.addAttribute("name", field.getName());
				if(value != null) {
					f.setText(Util.toString(field, value));
				}
			}
		}
	}
	
	public static Model parseEntity(Element element, ClassLoader classLoader) {
		String clazzName = element.attributeValue("class");
		Model obj = null;
		try {
			Class<?> clazz = classForName(clazzName, classLoader);
			obj = (Model) clazz.newInstance();
		} catch(Exception e) {
			throw new SienaException("Error while trying to create an instance of "+clazzName+". "+e.getMessage());
		}
		parseEntity(obj, element, classLoader);
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	public static void parseEntity(Object obj, Element element, ClassLoader classLoader) {
		Class<?> clazz = obj.getClass();
		Field field = null;
		Object value = null;
		try {
			List<Element> list = element.elements();
			for (Element el : list) {
				String name = el.attributeValue("name");
				field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				
				if(el.hasContent()) {
					if("object".equals(el.getName())) {
						value = parseEntity(el, classLoader);
					} else {
						value = Util.fromString(field.getType(), el.getText());
					}
				} else {
					value = null;
				}
				field.set(obj, value);
			}
		} catch(Exception e) {
			String message = "Error while setting field values (class: "+clazz.getName();
			if(field != null) {
				message += ", field: "+field.getName();
			} else {
				message += ", field: null";
			}
			if(value != null) {
				message += ", value: "+value+" ["+value.getClass().getName()+"]";
			} else {
				message += ", value: null";
			}
			message += ")";
			throw new SienaException(message, e);
		}
	}
	
	public static Class<?> classForName(String clazzName, ClassLoader classLoader)
			throws ClassNotFoundException {
		if(classLoader == null)
			return Class.forName(clazzName);
		return Class.forName(clazzName, true, classLoader);
	}

}
