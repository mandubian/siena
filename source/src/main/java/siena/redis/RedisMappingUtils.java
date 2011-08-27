package siena.redis;

import java.lang.reflect.Field;
import java.util.List;
import siena.ClassInfo;
import siena.SienaException;
import siena.Util;

public class RedisMappingUtils {

    public static void fillModel(Object obj, List<Object> res) {
        Class<?> clazz = obj.getClass();
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        
        for (int i = 0; i < info.updateFields.size(); i++) {
            Field field = ClassInfo.getClassInfo(clazz).updateFields.get(i);
            String property = ClassInfo.getColumnNames(info.updateFields.get(i))[0];
            try {
                Class<?> fieldClass = field.getType();
                
                Object value = res.get(i);
                Util.setFromObject(obj, field, value);
            } catch (Exception e) {
                throw new SienaException(e);
            }
        }
    }
}
