package siena.sdb;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import siena.ClassInfo;
import siena.SienaException;
import siena.Util;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

public class SdbMappingUtils {
	private static long ioffset = Math.abs(0L+Integer.MIN_VALUE);

	public static String getDomainName(Class<?> clazz, String prefix) {
		String domain = prefix+ClassInfo.getClassInfo(clazz).tableName;
		return domain;
	}
	
	public static String getAttributeName(Field field) {
		return ClassInfo.getColumnNames(field)[0];
	}

	public static String getItemName(Class<?> clazz, Object obj){
		Field idField = ClassInfo.getIdField(clazz);
		String idVal = (String) Util.readField(obj, idField);
		if(idVal == null) { // TODO: only if auto-generated
			idVal = UUID.randomUUID().toString();
			Util.setField(obj, idField, idVal);
		}
		return idVal;
	}
	
	public static PutAttributesRequest createPutRequest(String domain, Class<?> clazz, ClassInfo info, Object obj) {
		PutAttributesRequest req = new PutAttributesRequest().withDomainName(domain);
		req.withItemName(getItemName(clazz, obj));
		
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			try {
				String value = toString(obj, field);
				if(value != null){
					ReplaceableAttribute attr = new ReplaceableAttribute(getAttributeName(field), value, true);
					req.withAttributes(attr);
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return req;
	}
	
	public static ReplaceableItem createItem(Object obj) {
		Class<?> clazz = obj.getClass();
		
		ReplaceableItem item = new ReplaceableItem(getItemName(clazz, obj));
		
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			try {
				String value = toString(obj, field);
				if(value != null){
					ReplaceableAttribute attr = new ReplaceableAttribute(getAttributeName(field), value, true);
					item.withAttributes(attr);
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}

		return item;
	}

	
	public static GetAttributesRequest createGetRequest(String domain, Class<?> clazz, Object obj) {
		GetAttributesRequest req = 
			new GetAttributesRequest().withDomainName(domain).withItemName(getItemName(clazz, obj));
		
		return req;
	}
	
	public static DeleteAttributesRequest createDeleteRequest(String domain, Class<?> clazz, Object obj) {
		DeleteAttributesRequest req = 
			new DeleteAttributesRequest().withDomainName(domain).withItemName(getItemName(clazz, obj));
		
		return req;
	}
	
	public static String toString(Object obj, Field field) {
		Object val = Util.readField(obj, field);
		if(val == null) return null;
		
		Class<?> type = field.getType();
		if(type == Integer.class || type == int.class) {
			return toString((Integer)val);
		}
		if(ClassInfo.isModel(type)) {
			try {
				return toString(val, ClassInfo.getIdField(type)); 
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		return Util.toString(field, val);
	}
	
	public static String toString(int i) {
		return String.format("%010d", i+ioffset);
	}
	
	public static int fromString(String s) {
		long l = Long.parseLong(s);
		return (int) (l-ioffset);
	}

	public static void fillModel(String itemName, GetAttributesResult res, Class<?> clazz, Object obj) {
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		Field idField = ClassInfo.getIdField(clazz);
		try {
			Util.setField(obj, idField, itemName);
		} catch (Exception e) {
			throw new SienaException(e);
		}
		
		List<Attribute> attrs = res.getAttributes();
		Attribute theAttr;
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			theAttr = null;
			String attrName = getAttributeName(field);
			// searches attribute and if found, removes it from the list to reduce number of attributes
			for(Attribute attr: attrs){
				if(attrName.equals(attr.getName())){
					theAttr = attr;
					attrs.remove(attr);
					break;
				}
			}
			if(theAttr != null){
				String val = theAttr.getValue();
				
				if(field.getType() == Integer.class || field.getType() == int.class) {
					Util.setField(obj, field, fromString(val));
				} else {
					Class<?> type = field.getType();
					if(ClassInfo.isModel(type)) {
						Object rel = Util.createObjectInstance(type);
						Field relIdField = ClassInfo.getIdField(type);
						Util.setField(rel, relIdField, val);
						
						Util.setField(obj, field, rel);
					} else {
						Util.setFromString(obj, field, val);
					}
				}
			}
		}
		

	}
}
