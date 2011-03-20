package siena.core.async;


import java.util.concurrent.Future;
import com.google.appengine.api.utils.FutureWrapper;

/**
 * Directly inspired by SimpleFutureWrapper class from objectify project
 * @link http://code.google.com/p/objectify-appengine/source/browse/trunk/src/com/googlecode/objectify/util/SimpleFutureWrapper.java
 * 
 * @author mandubian <pascal.voitot@mandubian.org>
 */
abstract public class SienaFutureWrapper<K, V> extends FutureWrapper<K, V>
{
        public SienaFutureWrapper(Future<K> base)
        {
                super(base);
        }

        @Override
        protected Throwable convertException(Throwable cause)
        {
                return cause;
        }
}
