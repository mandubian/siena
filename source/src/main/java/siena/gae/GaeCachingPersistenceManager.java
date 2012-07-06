/*
 * Copyright 2009 Alberto Gimeno <gimenete at gmail.com>
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
 *   
 *   @author mandubian <pascal.voitot@mandubian.org>
 */
package siena.gae;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.ClassInfo.FieldMapKeys;
import siena.Query;
import siena.QueryAggregated;
import siena.SienaException;
import siena.Util;
import siena.core.Many;
import siena.core.Many4PM;
import siena.core.One;
import siena.core.One4PM;
import siena.core.Relation;
import siena.core.RelationMode;
import siena.core.async.PersistenceManagerAsync;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

import com.googlecode.objectify.cache.CachingDatastoreServiceFactory;

public class GaeCachingPersistenceManager extends GaePersistenceManager {

    @Override
	public void init(Properties p) {
		ds = CachingDatastoreServiceFactory.getDatastoreService();
		props = p;
	}

    @Override
	public <T> PersistenceManagerAsync async() {
		if(asyncPm==null){
			asyncPm = new GaeCachingPersistenceManagerAsync();
			asyncPm.init(props);
		}
		return asyncPm;		
	}
}
