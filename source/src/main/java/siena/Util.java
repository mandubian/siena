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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.UUID;

import siena.embed.Embedded;
import siena.embed.JavaSerializer;
import siena.embed.JsonSerializer;
import siena.jdbc.JdbcPersistenceManager.JdbcClassInfo;

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
		if(type == BigDecimal.class) return new BigDecimal(value);
		if(Enum.class.isAssignableFrom(type)) return Enum.valueOf((Class<Enum>) type, (String)value);
		throw new IllegalArgumentException("Unsupported type: "+type.getName());
	}
	
	public static Object fromString(Class<?> type, String value, boolean retValueIfNotSupported) {
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
		if(type == BigDecimal.class) return new BigDecimal(value);
		if(Enum.class.isAssignableFrom(type)) return Enum.valueOf((Class<Enum>) type, (String)value);
		if(!retValueIfNotSupported){
			throw new IllegalArgumentException("Unsupported type: "+type.getName());
		}
		return value;
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
		Class<?> type = field.getType();
		if(value == null) {
			if(type.isPrimitive()){
				if(byte.class==type)    return (byte)0;
				else if(Character.TYPE==type) return (char)0;
				else if(Short.TYPE==type)   return (short)0;
				else if(Integer.TYPE==type) return (int)0;
				else if(Long.TYPE==type)    return (long)0;
				else if(Float.TYPE==type)   return (float)0;
				else if(Double.TYPE==type)  return (double)0;
				else if(Boolean.TYPE==type) return false;
			}
			return null;
		}
		
		if(Number.class.isAssignableFrom(value.getClass())) {
			Number number = (Number) value;
			if(byte.class==type || Byte.class==type)    return number.byteValue();
			else if(Short.TYPE==type || Short.class==type)   return number.shortValue();
			else if(Integer.TYPE==type || Integer.class==type) return number.intValue();
			else if(Long.TYPE==type || Long.class==type)    return number.longValue();
			else if(Float.TYPE==type || Float.class==type)   return number.floatValue();
			else if(Double.TYPE==type || Double.class==type)  return number.doubleValue();
			else if(Boolean.TYPE==type || Boolean.class==type) return number.byteValue() != 0;
			else if(BigDecimal.class==type) return (BigDecimal)value;
		} 
		
		if(String.class.isAssignableFrom(value.getClass())) {
			if(Json.class.isAssignableFrom(type)) {
				return Json.loads((String) value);
			}
			if(UUID.class == type) {
				return UUID.fromString((String)value);
			}
		} 
		
		Embedded embed = field.getAnnotation(Embedded.class);
		if(embed != null) {
			switch(embed.mode()){
			case SERIALIZE_JSON:
				if(String.class.isAssignableFrom(value.getClass())) {
					Json data = Json.loads((String) value);
					return JsonSerializer.deserialize(field, data);
				}
				break;
			case SERIALIZE_JAVA:
				try {
					return JavaSerializer.deserialize((byte[])value);
				} catch (IOException e) {
					throw new SienaException(e);
				} catch (ClassNotFoundException e) {
					throw new SienaException(e);
				}
			case NATIVE:
				break;
			}
			
		}
		
		if(String.class.isAssignableFrom(value.getClass())&& type.isEnum()) {
			return Enum.valueOf((Class<Enum>) type, (String)value);
		}
		
		if(String.class.isAssignableFrom(value.getClass())&& type != String.class) {
			return fromString(field.getType(), (String)value, true);
		}
		return value;
	}
	
	public static void setField(Object object, Field f, Object value) {
		try { 
				f.set(object, value); 
		} catch (Exception e) { 
			try { 
				f.setAccessible(true); 
			  f.set(object, value);  
		} catch (Exception e2) { 
			 	throw new SienaException(e2); 
			} 
		} 
	} 
	
	public static void setFromObject(Object object, Field f, Object value) {
		setField(object, f, fromObject(f, value));
	}
	
	public static void setFromString(Object object, Field f, String value) {
		setField(object, f, fromString(f.getType(), value));
	}

	public static Object readField(Object object, Field field) {
		boolean wasAccess = true;
		if(!field.isAccessible()){
			field.setAccessible(true);
			wasAccess = false;
		}
		try {
			return field.get(object);
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			if(!wasAccess){
				field.setAccessible(false);
			}
		}
	}
	
	public static Field getField(Class<?> clazz, String fieldName) {
		Class<?> cl = clazz;
		
        while (cl!=null) {
        	try {
        		return cl.getDeclaredField(fieldName);
        	}
        	catch(NoSuchFieldException e){
        		cl = cl.getSuperclass();
        	}
        	catch (Exception e) {
    			throw new SienaException(e);
    		}        	 
        }
		
        return null;
	}
	
	public static Object translateDate(Field f, Date value) {
		long t = value.getTime();

		SimpleDate simpleDate = f.getAnnotation(SimpleDate.class);
		if(simpleDate != null) {
			return new java.sql.Date(t);
		}

		DateTime dateTime = f.getAnnotation(DateTime.class);
		if(dateTime != null) {
			return new java.sql.Timestamp(t); 
		}

		Time time = f.getAnnotation(Time.class);
		if(time != null) {
			return new java.sql.Time(t); 
		}

		return new java.sql.Timestamp(t);
	}

	
	/**
	 * Creates an instance of a model from its class.
	 * It tries to find a default constructor and if not found, it uses class.newInstance()
	 * 
	 * @param clazz the class of the model
	 * @return the instance of the model
	 */
	public static <T> T createObjectInstance(Class<T> clazz){
		try {
			Constructor<T> c = clazz.getDeclaredConstructor();
			c.setAccessible(true);
			return c.newInstance();
		}catch(NoSuchMethodException ex){
			try {
				return clazz.newInstance();
			}catch (Exception e) {
				throw new SienaException(e);
			}
		}catch(Exception e){
			throw new SienaException(e);
		}		
	}
	
	public static void copyObject(Object objFrom, Object objTo) {
		Class<?> clazz = objFrom.getClass();
		for (Field field : JdbcClassInfo.getClassInfo(clazz).allFields) {
			Util.setField(objTo, field, Util.readField(objFrom, field));
		}
	}
	
	public static Class<?> getGenericClass(Field f, int n) {
		Type genericFieldType = f.getGenericType();
		if(genericFieldType instanceof ParameterizedType){
		    ParameterizedType aType = (ParameterizedType) genericFieldType;
		    Type[] fieldArgTypes = aType.getActualTypeArguments();
		    return (Class<?>) fieldArgTypes[n];
		}
		return null;
	}


}
