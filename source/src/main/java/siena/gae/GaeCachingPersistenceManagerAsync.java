/*
 * @author mandubian <pascal.voitot@mandubian.org>
 */
package siena.gae;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.commons.lang.NotImplementedException;

import siena.ClassInfo;
import siena.PersistenceManager;
import siena.SienaException;
import siena.Util;
import siena.core.async.AbstractPersistenceManagerAsync;
import siena.core.async.QueryAsync;
import siena.core.async.SienaFuture;
import siena.core.async.SienaFutureContainer;
import siena.core.async.SienaFutureMock;
import siena.core.async.SienaFutureWrapper;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

import com.googlecode.objectify.cache.CachingDatastoreServiceFactory;
import com.googlecode.objectify.cache.EntityMemcache;

public class GaeCachingPersistenceManagerAsync extends GaePersistenceManagerAsync {
    
    protected EntityMemcache entityMemcache;
    
    public GaeCachingPersistenceManagerAsync() {
        super();
    }
    
    public GaeCachingPersistenceManagerAsync(EntityMemcache em) {
        super();
        entityMemcache = em;
    }

    @Override
	public void init(Properties p) {
		ds = entityMemcache == null ? CachingDatastoreServiceFactory.getAsyncDatastoreService() : CachingDatastoreServiceFactory.getAsyncDatastoreService(entityMemcache);
		props = p;
	}
	
    @Override
	public void init(Properties p, PersistenceManager syncPm) {
		this.syncPm = syncPm;
		ds = entityMemcache == null ? CachingDatastoreServiceFactory.getAsyncDatastoreService() : CachingDatastoreServiceFactory.getAsyncDatastoreService(entityMemcache);
		props = p;
	}
	
    @Override
	public PersistenceManager sync() {
		if(syncPm==null){
			syncPm = new GaeCachingPersistenceManager(entityMemcache);
			syncPm.init(props);
		}
		return syncPm;
	}
}
