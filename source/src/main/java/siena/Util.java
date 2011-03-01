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
package siena;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

import siena.embed.Embedded;
import siena.embed.JsonSerializer;

/**
 * Util class for general proposals.
 * @author gimenete 1.0
 * @author jsanca 1.0.1
 *
 */
public class Util {

	public static String join(Collection<String> s, String delimiter) {
		if (s.isEmpty()) return "";
		Iterator<String> iter = s.iterator();
		StringBuilder buffer = new StringBuilder(iter.next());
		while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
		return buffer.toString();
	}
	
	public static String sha1(String message) {
		try {
			byte[] buffer = message.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(buffer);
			byte[] digest = md.digest();
			char[] hash = new char[40];

			for(int i=0, n=0; i<digest.length; i++) {
				byte aux = digest[i];
				int b = aux & 0xff;
				String hex = Integer.toHexString(b);
				if (hex.length() == 1) {
					hash[n++] = '0';
					hash[n++] = hex.charAt(0);
				} else {
					hash[n++] = hex.charAt(0);
					hash[n++] = hex.charAt(1);
				}
			}
			return new String(hash);
		} catch(Exception e) {
			// should never happen. UTF-8 and SHA1 are constants
			throw new RuntimeException(e);
		}
	}
	
	public static Object fromString(Class<?> type, String value) {
		if(value == null) return null;
		if(type.isPrimitive()) {
			if(type == Boolean.TYPE) return Boolean.parseBoolean(value);
			if(type == Byte.TYPE)    return Byte.parseByte(value);
			if(type == Short.TYPE)   return Short.parseShort(value);
			if(type == Integer.TYPE) return Integer.parseInt(value);
			if(type == Long.TYPE)    return Long.parseLong(value);
			if(type == Float.TYPE)   return Float.parseFloat(value);
			if(type == Double.TYPE)  return Double.parseDouble(value);
		}
		if(type == String.class)  return value;
		if(type == Boolean.class) return Boolean.valueOf(value);
		if(type == Byte.class)    return Byte.valueOf(value);
		if(type == Short.class)   return Short.valueOf(value);
		if(type == Integer.class) return Integer.valueOf(value);
		if(type == Long.class)    return Long.valueOf(value);
		if(type == Float.class)   return Float.valueOf(value);
		if(type == Double.class)  return Double.valueOf(value);
		if(type == Date.class)    return timestamp(value);
		if(type == Json.class)    return Json.loads(value);
		if(Enum.class.isAssignableFrom(type)) return Enum.valueOf((Class<Enum>) type, (String)value);
		throw new IllegalArgumentException("Unsupported type: "+type.getName());
	}
	
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") {
		private static final long serialVersionUID = 1L;
		{
			setTimeZone(TimeZone.getTimeZone("UTC"));
		}
	};
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd") {
		private static final long serialVersionUID = 1L;
		{
			setTimeZone(TimeZone.getTimeZone("UTC"));
		}
	};
	
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss") {
		private static final long serialVersionUID = 1L;
		{
			setTimeZone(TimeZone.getTimeZone("UTC"));
		}
	};
	
	public static Date timestamp(String s) {
		try {
			return TIMESTAMP_FORMAT.parse(s);
		} catch (ParseException e) {
			throw new SienaException(e);
		}
	}
	
	public static String timestamp(Date d) {
		return TIMESTAMP_FORMAT.format(d);
	}
	
	public static Date time(String s) {
		try {
			return TIME_FORMAT.parse(s);
		} catch (ParseException e) {
			throw new SienaException(e);
		}
	}
	
	public static String time(Date d) {
		return TIME_FORMAT.format(d);
	}
	
	public static Date date(String s) {
		try {
			return DATE_FORMAT.parse(s);
		} catch (ParseException e) {
			throw new SienaException(e);
		}
	}
	
	public static String date(Date d) {
		return DATE_FORMAT.format(d);
	}
	
	public static String toString(Field field, Object value) {
		if(value instanceof Date) {
			if(field.getAnnotation(DateTime.class) != null)
				return timestamp((Date) value);
			else if(field.getAnnotation(Time.class) != null)
				return time((Date) value);
			else if(field.getAnnotation(SimpleDate.class) != null)
				return date((Date) value);
			else
				return timestamp((Date) value);
		}
		return value.toString();
	}
	
	public static Object fromObject(Field field, Object value) {
		if(value == null) {
			if(field.getType().isPrimitive()) return 0;
			return null;
		}
		
		Class<?> type = field.getType();
		if(Number.class.isAssignableFrom(value.getClass())) {
			Number number = (Number) value;
			if(byte.class.isAssignableFrom(type))    return number.byteValue();
			else if(short.class.isAssignableFrom(type))   return number.shortValue();
			else if(int.class.isAssignableFrom(type) ) return number.intValue();
			else if(long.class.isAssignableFrom(type))    return number.longValue();
			else if(float.class.isAssignableFrom(type))   return number.floatValue();
			else if(double.class.isAssignableFrom(type))  return number.doubleValue();
		} 
		else if(String.class.isAssignableFrom(value.getClass()) && Json.class.isAssignableFrom(type)) {
			return Json.loads((String) value);
		} 
		else if(field.getAnnotation(Embedded.class) != null && String.class.isAssignableFrom(value.getClass())) {
			Json data = Json.loads((String) value);
			return JsonSerializer.deserialize(field, data);
		}
		else if(String.class.isAssignableFrom(value.getClass())&& type.isEnum()) {
			return Enum.valueOf((Class<Enum>) type, (String)value);
		}
		return value;
	}
	
	public static void set(Object object, Field f, Object value)
				throws IllegalArgumentException, IllegalAccessException {
		if(!f.isAccessible())
			f.setAccessible(true);
		f.set(object, value);
	}
	
	public static void setFromObject(Object object, Field f, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		set(object, f, fromObject(f, value));
	}
	
	public static void setFromString(Object object, Field f, String value)
			throws IllegalArgumentException, IllegalAccessException {
		set(object, f, fromString(f.getType(), value));
	}

}
