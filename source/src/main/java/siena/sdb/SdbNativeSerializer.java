/**
 * 
 */
package siena.sdb;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import siena.ClassInfo;
import siena.Json;
import siena.SienaException;
import siena.Util;
import siena.core.DecimalPrecision;
import siena.embed.Embedded;
import siena.embed.JavaSerializer;
import siena.embed.JsonSerializer;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

/**
 * @author mandubian <pascal.voitot@mandubian.org>
 *
 */
public class SdbNativeSerializer {

	private static final String SEPARATOR = ".";
	
	public static String getEmbeddedAttributeName(String embeddingColumnName, Field field) {
		return embeddingColumnName + SEPARATOR + ClassInfo.getSingleColumnName(field);
	}
	
	public static void embed(PutAttributesRequest req, String embeddingColumnName, Object embeddedObj){
		Class<?> clazz = embeddedObj.getClass();
		if(clazz.isArray() || Collection.class.isAssignableFrom(clazz)){
			throw new SienaException("can't serializer Array/Collection in native mode");
		}
		
		for (Field f : ClassInfo.getClassInfo(clazz).updateFields) {
			String propValue = SdbMappingUtils.objectFieldToString(embeddedObj, f);
			if(propValue != null){
				ReplaceableAttribute attr = 
					new ReplaceableAttribute(
							getEmbeddedAttributeName(embeddingColumnName, f), propValue, true);
				req.withAttributes(attr);
			}else {
				if (ClassInfo.isEmbeddedNative(f)){
					SdbNativeSerializer.embed(
						req, 
						getEmbeddedAttributeName(embeddingColumnName, f), 
						Util.readField(embeddedObj, f));
				}
			}
		}
	}
	
	public static void embed(ReplaceableItem item, String embeddingColumnName, Object embeddedObj){
		Class<?> clazz = embeddedObj.getClass();
		if(clazz.isArray() || Collection.class.isAssignableFrom(clazz)){
			throw new SienaException("can't serializer Array/Collection in native mode");
		}
		
		for (Field f : ClassInfo.getClassInfo(clazz).updateFields) {
			String propValue = SdbMappingUtils.objectFieldToString(embeddedObj, f);
			if(propValue != null){
				ReplaceableAttribute attr = 
					new ReplaceableAttribute(
							getEmbeddedAttributeName(embeddingColumnName, f), propValue, true);
				item.withAttributes(attr);
			}else {
				if (ClassInfo.isEmbeddedNative(f)){
					SdbNativeSerializer.embed(
						item, 
						getEmbeddedAttributeName(embeddingColumnName, f), 
						Util.readField(embeddedObj, f));
				}
			}
		}
	}
	
	public static <T> T unembed(
			Class<T> clazz, String embeddingFieldName, List<Attribute> attrs){
		if(clazz.isArray() || Collection.class.isAssignableFrom(clazz)){
			throw new SienaException("can't serializer Array/Collection in native mode");
		}

		T obj = Util.createObjectInstance(clazz);
		try {
			Attribute theAttr;
			
			for (Field f : ClassInfo.getClassInfo(clazz).updateFields) {
				if(!ClassInfo.isEmbeddedNative(f)){
					// doesn't try to analyze fields, just try to store it
					String attrName = getEmbeddedAttributeName(embeddingFieldName, f);			
					
					theAttr = null;
					// searches attribute and if found, removes it from the list to reduce number of attributes
					for(Attribute attr: attrs){
						if(attrName.equals(attr.getName())){
							theAttr = attr;
							attrs.remove(attr);
							break;
						}
					}
					if(theAttr != null){
						SdbMappingUtils.setFromString(obj, f, theAttr.getValue());
					}
				} else {
					Object value = SdbNativeSerializer.unembed(
							f.getType(), getEmbeddedAttributeName(embeddingFieldName, f), 
							attrs);
					Util.setField(obj, f, value);
				}
			}
			
			return obj;
		}catch(Exception e){
			throw new SienaException(e);
		}
	}
}
