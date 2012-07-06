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

public class GaeCachingPersistenceManagerAsync extends GaePersistenceManagerAsync {

    @Override
	public void init(Properties p) {
		ds = CachingDatastoreServiceFactory.getAsyncDatastoreService();
		props = p;
	}
	
    @Override
	public void init(Properties p, PersistenceManager syncPm) {
		this.syncPm = syncPm;
		ds = CachingDatastoreServiceFactory.getAsyncDatastoreService();
		props = p;
	}
	
    @Override
	public PersistenceManager sync() {
		if(syncPm==null){
			syncPm = new GaeCachingPersistenceManager();
			syncPm.init(props);
		}
		return syncPm;
	}
}
