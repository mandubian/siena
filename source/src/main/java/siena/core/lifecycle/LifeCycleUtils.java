package siena.core.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import siena.ClassInfo;
import siena.SienaException;

public class LifeCycleUtils {
	private static final Map<Class<? extends Annotation>, LifeCyclePhase> lifeCycleClasses = new ConcurrentHashMap<Class<? extends Annotation>, LifeCyclePhase>() {
		private static final long serialVersionUID = -3454152184796684592L;
		{
			put(PreFetch.class, LifeCyclePhase.PRE_FETCH);
			put(PreInsert.class, LifeCyclePhase.PRE_INSERT);
			put(PreDelete.class, LifeCyclePhase.PRE_DELETE);
			put(PreUpdate.class, LifeCyclePhase.PRE_UPDATE);
			put(PreSave.class, LifeCyclePhase.PRE_SAVE);
			put(PostFetch.class, LifeCyclePhase.POST_FETCH);
			put(PostInsert.class, LifeCyclePhase.POST_INSERT);
			put(PostDelete.class, LifeCyclePhase.POST_DELETE);
			put(PostUpdate.class, LifeCyclePhase.POST_UPDATE);
			put(PostSave.class, LifeCyclePhase.POST_SAVE);
		}
	};
	
	public static List<LifeCyclePhase> getMethodLifeCycles(Method m){
		List<LifeCyclePhase> l = new ArrayList<LifeCyclePhase>();
		
		for(Class<? extends Annotation> cl : lifeCycleClasses.keySet()){
			if(m.isAnnotationPresent(cl)){
				l.add(lifeCycleClasses.get(cl));
			}
		}
		
		return l;
	}
	
	public static void executeMethods(LifeCyclePhase lcp, ClassInfo ci, Object obj){
		List<Method> methods = ci.getLifeCycleMethod(lcp);
		if(methods == null) return;
		try {
			for(Method m: methods){
				// injects lifeCyclePhase if it is the FIRST param
				Class<?> params[] = m.getParameterTypes();
				boolean wasAccessible = true;
				if(!m.isAccessible()){
					wasAccessible = false;
					m.setAccessible(true);
				}
				if(params != null && params.length != 0){
					if(LifeCyclePhase.class.isAssignableFrom(params[0])){
						m.invoke(obj, lcp);
					}
				}
				else {
					m.invoke(obj);
				}
				if(!wasAccessible){
					m.setAccessible(false);
				}
			} 			
		}catch (IllegalArgumentException e) {
			throw new SienaException(e);
		} catch (IllegalAccessException e) {
			throw new SienaException(e);
		} catch (InvocationTargetException e) {
			throw new SienaException(e);
		}
		
	}
}
