package siena.sdb;

import java.lang.reflect.Field;
import java.util.UUID;

import siena.ClassInfo;
import siena.SienaException;
import siena.Util;
import siena.sdb.ws.Item;

import com.amazonaws.services.simpledb.model.PutAttributesRequest;

public class SdbMappingUtils {
	public static String getDomainName(Class<?> clazz, String prefix) {
		String domain = prefix+ClassInfo.getClassInfo(clazz).tableName;
		return domain;
	}

	private static PutAttributesRequest createPutRequest(String domain, Class<?> clazz, ClassInfo info, Object obj) {
		PutAttributesRequest req = new PutAttributesRequest().withDomainName(domain);
		// sets ID
		Field id = ClassInfo.getIdField(clazz);
		String idVal = (String) Util.readField(obj, id);
		if(idVal == null) { // TODO: only if auto-generated
			idVal = UUID.randomUUID().toString();
			Util.setField(obj, id, idVal);
		}
		req.withItemName(idVal);
		
		for (Field field : ClassInfo.getClassInfo(clazz).updateFields) {
			try {
				String value = toString(field, field.get(obj));
				if(value != null){
					Attribute attr = new Attribute("Color", "Blue");
					item.add(getAttributeName(field), value);
				}
			} catch (Exception e) {
				throw new SienaException(e);
			}
		}
		
		item.name = name;
		return item;
	}
	
	private static String stringify(Object obj, Field field) {
		Object val = Util.readField(obj, field);
		if(val == null) return null;
		
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
}
