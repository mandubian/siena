/**
 * 
 */
package siena.gae;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import siena.ClassInfo;
import siena.Json;
import siena.SienaException;
import siena.Util;
import siena.core.DecimalPrecision;
import siena.embed.Embedded;
import siena.embed.JavaSerializer;
import siena.embed.JsonSerializer;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class GaeNativeSerializer {

	public static void embed(Entity entity, String embeddingColumnName, Object embeddedObj){
		Class<?> clazz = embeddedObj.getClass();
		if(clazz.isArray() || Collection.class.isAssignableFrom(clazz)){
			throw new SienaException("can't serializer Array/Collection in native mode");
		}
		
		for (Field f : ClassInfo.getClassInfo(clazz).allFields) {
			// doesn't try to analyze fields, just try to store it
			Class<?> fieldClass = f.getType();
			String propName = embeddingColumnName + "." + ClassInfo.getSingleColumnName(f);
			Object propValue = Util.readField(embeddedObj, f);			
			
			if (propValue != null) {
				if (fieldClass == Json.class) {
					propValue = propValue.toString();
				} else if (propValue instanceof String) {
					String s = (String) propValue;
					if (s.length() > 500)
						propValue = new Text(s);
				} else if (propValue instanceof byte[]) {
					byte[] arr = (byte[]) propValue;
					// GAE Blob doesn't accept more than 1MB
					if (arr.length < 1000000)
						propValue = new Blob(arr);
					else
						propValue = new Blob(Arrays.copyOf(arr, 1000000));
				}
				else if (ClassInfo.isEmbedded(f)) {
					Embedded embed = f.getAnnotation(Embedded.class);
					switch(embed.mode()){
					case SERIALIZE_JSON:
						propValue = JsonSerializer.serialize(propValue).toString();
						String s = (String) propValue;
						if (s.length() > 500)
							propValue = new Text(s);
						break;
					case SERIALIZE_JAVA:
						// this embedding mode doesn't manage @EmbedIgnores
						try {
							byte[] b = JavaSerializer.serialize(propValue);
							// if length is less than 1Mb, can store in a blob else???
							if(b.length <= 1000000){
								propValue = new Blob(b);
							}else{
								throw new SienaException("object can be java serialized because it's too large >1mb");
							}								
						}
						catch(IOException ex) {
							throw new SienaException(ex);
						}
						break;
					case NATIVE:
						GaeNativeSerializer.embed(entity, embeddingColumnName + "." + ClassInfo.getSingleColumnName(f), propValue);
						// has set several new properties in entity so go to next field
						continue;
					}
					
				}
				else if (fieldClass == BigDecimal.class){
					DecimalPrecision ann = f.getAnnotation(DecimalPrecision.class);
					if(ann == null) {
						propValue = ((BigDecimal)propValue).toPlainString();
					}else {
						switch(ann.storageType()){
						case DOUBLE:
							propValue = ((BigDecimal)propValue).doubleValue();
							break;
						case STRING:
						case NATIVE:
							propValue = ((BigDecimal)propValue).toPlainString();
							break;
						}
					}
				}
				// enum is after embedded because an enum can be embedded
				// don't know if anyone will use it but it will work :)
				else if (Enum.class.isAssignableFrom(fieldClass)) {
					propValue = propValue.toString();
				} 
				else if(ClassInfo.isModel(fieldClass)){
					// if it is a model, anyway how it is annotated, it is native embedded
					GaeNativeSerializer.embed(entity, embeddingColumnName + "." + ClassInfo.getSingleColumnName(f), propValue);
					continue;
				}
			}
			
			Unindexed ui = f.getAnnotation(Unindexed.class);
			if (ui == null) {
				entity.setProperty(propName, propValue);
			} else {
				entity.setUnindexedProperty(propName, propValue);
			}
		}
	}
	
	public static <T> T unembed(Class<T> clazz, String embeddingFieldName, Entity entity){
		if(clazz.isArray() || Collection.class.isAssignableFrom(clazz)){
			throw new SienaException("can't serializer Array/Collection in native mode");
		}

		T obj = Util.createObjectInstance(clazz);
		try {
			for (Field f : ClassInfo.getClassInfo(clazz).allFields) {
				// doesn't try to analyze fields, just try to store it
				String propName = embeddingFieldName + "." + ClassInfo.getSingleColumnName(f);			
				Object propValue = entity.getProperty(propName);
				
				if(ClassInfo.isEmbedded(f) && f.getAnnotation(Embedded.class).mode() == Embedded.Mode.NATIVE){
					Object value = GaeNativeSerializer.unembed(
							f.getType(), embeddingFieldName + "." + ClassInfo.getSingleColumnName(f), entity);
					Util.setField(obj, f, value);
				}
				else {
					GaeMappingUtils.setFromObject(obj, f, propValue);
				}
			}
			
			return obj;
		}catch(Exception e){
			throw new SienaException(e);
		}
	}
}
