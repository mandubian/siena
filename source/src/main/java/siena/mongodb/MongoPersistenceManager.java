package siena.mongodb;

import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Query;
import siena.SienaException;
import siena.Util;
import siena.core.async.PersistenceManagerAsync;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoPersistenceManager extends AbstractPersistenceManager{
	public static final String DB = "MONGODB";
	
	private Mongo 				mongo;
	private com.mongodb.DB 		db;
	private String				dbname;

    public final static PmOptionMongoWriteConcern WRITECONCERN_NONE = 
    	new PmOptionMongoWriteConcern(WriteConcern.NONE);
    public final static PmOptionMongoWriteConcern WRITECONCERN_NORMAL = 
    	new PmOptionMongoWriteConcern(WriteConcern.NORMAL);
    public final static PmOptionMongoWriteConcern WRITECONCERN_SAFE = 
    	new PmOptionMongoWriteConcern(WriteConcern.SAFE);
    public final static PmOptionMongoWriteConcern WRITECONCERN_REPLICAS_SAFE = 
    	new PmOptionMongoWriteConcern(WriteConcern.REPLICAS_SAFE);
    public final static PmOptionMongoWriteConcern WRITECONCERN_FSYNC_SAFE = 
    	new PmOptionMongoWriteConcern(WriteConcern.FSYNC_SAFE);
	
	public DBCollection getCollection(ClassInfo info) {
		return db.getCollection(info.tableName);
	}

	public DBCollection getCollection(Class<?> clazz) {
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		return db.getCollection(info.tableName);
	}

	public DBCollection getCollection(String tableName) {
		return db.getCollection(tableName);
	}
	
	public boolean hasWriteConcern() {
		PmOptionMongoWriteConcern opt = (PmOptionMongoWriteConcern)option(WRITECONCERN_NORMAL.type);
		if(opt != null) {
			return true;
		}
		return false;
	}
	
	public WriteConcern getWriteConcern() {
		PmOptionMongoWriteConcern opt = (PmOptionMongoWriteConcern)option(WRITECONCERN_NORMAL.type);
		if(opt != null) {
			return opt.concern;
		}
		return null;
	}
	
	public void init(Properties p) {
		try {
			dbname = p.getProperty("mongo.host", "localhost");
			dbname = p.getProperty("mongo.dbname", "default");
			
			mongo = new Mongo();
			db = mongo.getDB(dbname);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new SienaException(e);
		} catch (MongoException e) {
			e.printStackTrace();
			throw new SienaException(e);
		}
	}
	
	public void insert(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		DBCollection coll = getCollection(info);
		BasicDBObject doc = MongoMappingUtils.sienaToMongoForInsert(clazz, info, obj);
		try {
			if(hasWriteConcern()){
				WriteResult res = coll.insert(doc, this.getWriteConcern());
				if(res.getError() != null){
					throw new SienaException("Mongo returned error while inserting object: "+res.getError());
				}
			}
			else coll.insert(doc);
		}catch(MongoException ex){
			throw new SienaException(ex);
		}
		MongoMappingUtils.fillSienaId(doc, clazz, info, obj);		
	}

	public void get(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(obj.getClass());
		
		DBCollection coll = getCollection(info);
		BasicDBObject doc = MongoMappingUtils.sienaToMongoForQuery(clazz, info, obj);
		try {
			DBObject foundDoc = coll.findOne(doc);
			if(foundDoc == null){
				throw new SienaException("object not found");
			}
			MongoMappingUtils.mongoToSiena(foundDoc, clazz, info, obj);
		}catch(MongoException ex){
			throw new SienaException(ex);
		}
	}

	public void delete(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(obj.getClass());
		
		DBCollection coll = getCollection(info);
		BasicDBObject doc = MongoMappingUtils.sienaToMongoForUpdate(clazz, info, obj);
		try {
			if(hasWriteConcern()){
				WriteResult res = coll.remove(doc, this.getWriteConcern());
				if(res.getError() != null){
					throw new SienaException("Mongo returned error while inserting object: "+res.getError());
				}
			}
			else coll.remove(doc);
		}catch(MongoException ex){
			throw new SienaException(ex);
		}
	}

	@Override
	public <T> int delete(Query<T> query) {
		Class<?> clazz = query.getQueriedClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		DBCollection coll = getCollection(info);
		
		DBObject filterDoc = MongoMappingUtils.buildMongoFilter(query);
		DBObject sortDoc = MongoMappingUtils.buildMongoSort(query);
		// findAndRemove + sort
		DBObject ret = coll.findAndModify(filterDoc, null, sortDoc, true, null, false, false);
		if(ret == null) return 0;
		return 1;
	}
	
	public void update(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(obj.getClass());
		
		DBCollection coll = getCollection(info);
		BasicDBObject doc = MongoMappingUtils.sienaToMongoForUpdate(clazz, info, obj);
		try {
			if(hasWriteConcern()){
				WriteResult res = coll.save(doc, this.getWriteConcern());
				if(res.getError() != null){
					throw new SienaException("Mongo returned error while inserting object: "+res.getError());
				}
			}
			else coll.save(doc);
		}catch(MongoException ex){
			throw new SienaException(ex);
		}
	}

	public void save(Object obj) {
		Class<?> clazz = obj.getClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		Field idField = info.getIdField();
		
		//Entity entity;
		Object idVal = Util.readField(obj, idField);
		// id with null value means insert
		if(idVal == null){
			insert(obj);
		}
		// id with not null value means update
		else{
			update(obj);
		}
	}

	public <T> T getByKey(Class<T> clazz, Object key) {
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		DBCollection coll = getCollection(info);
		DBObject doc = MongoMappingUtils.buildMongoQueryFromKey(key);
		
		DBObject foundDoc = coll.findOne(doc);
		if(foundDoc == null){
			return null;
		}
		
		T obj = Util.createObjectInstance(clazz);
		MongoMappingUtils.mongoToSiena(foundDoc, clazz, info, obj);
		
		return obj;
	}
	
	public <T> int count(Query<T> query) {
		Class<?> clazz = query.getQueriedClass();
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		DBCollection coll = getCollection(info);
		
		DBObject filterDoc = MongoMappingUtils.buildMongoFilter(query);
		// filter + count
		long ret = coll.count(filterDoc);
		return (int)ret;
	}

	public int insert(Object... objects) {
		return insert(Arrays.asList(objects));
	}

	public int insert(Iterable<?> objects) {
		Map<String, List<DBObject>> docMap = new HashMap<String, List<DBObject>>();
		Map<String, List<Object>> objMap = new HashMap<String, List<Object>>();

		for(Object obj: objects){
			Class<?> clazz = obj.getClass();
			ClassInfo info = ClassInfo.getClassInfo(clazz);
			
			BasicDBObject doc = MongoMappingUtils.sienaToMongoForInsert(clazz, info, obj);
			
			List<DBObject> docs = docMap.get(info.tableName);
			if(docs == null){
				docs = new ArrayList<DBObject>();
				docMap.put(info.tableName, docs);
			}
			docs.add(doc);

			List<Object> objs = objMap.get(info.tableName);
			if(objs == null){
				objs = new ArrayList<Object>();
				objMap.put(info.tableName, objs);
			}
			objs.add(obj);
		}
			
		int nb = 0;
		try {			
			for(String collName:docMap.keySet()){
				DBCollection coll = getCollection(collName);
			
				List<DBObject> docs = docMap.get(collName);
				List<Object> objs = objMap.get(collName);
				for(int i=0; i<docs.size(); i++){
					DBObject doc = docs.get(i);
					Object obj = objs.get(i);
					
					Class<?> clazz = obj.getClass();
					ClassInfo info = ClassInfo.getClassInfo(clazz);
					
					if(hasWriteConcern()){
						WriteResult res = coll.insert(doc, this.getWriteConcern());
						if(res.getError() != null){
							throw new SienaException("Mongo returned error while inserting object: "+res.getError());
						}
					}
					else coll.insert(doc);
					
					nb++;
					
					MongoMappingUtils.fillSienaId(doc, clazz, info, obj);
				}
			}
		}
		catch(MongoException ex){
			throw new SienaException(ex);
		}
		
		return nb;
	}
	
	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		return getByKeys(clazz, Arrays.asList(keys));
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		ClassInfo info = ClassInfo.getClassInfo(clazz);
		
		DBCollection coll = getCollection(info);
		DBObject queryDoc = MongoMappingUtils.buildMongoQueryFromKeys(keys);
		coll.findAndModify(queryDoc, null);
		
		for(Object key:keys){
			DBObject doc = MongoMappingUtils.buildMongoQueryFromKey(key);
			
			DBObject foundDoc = coll.f.findOne(doc);
			if(foundDoc == null){
				return null;
			}
			
			T obj = Util.createObjectInstance(clazz);
			MongoMappingUtils.mongoToSiena(foundDoc, clazz, info, obj);
		}
		return obj;
	}

	
	
	@Override
	public int save(Object... objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int save(Iterable<?> objects) {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public int delete(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(Iterable<?> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int get(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int get(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
	}	

	
	@Override
	public <T> int update(Object... models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int update(Iterable<T> models) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void beginTransaction(int isolationLevel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beginTransaction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commitTransaction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rollbackTransaction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeConnection() {
		// TODO Auto-generated method stub
		
	}	

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public <T> List<T> fetch(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void paginate(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void nextPage(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void previousPage(Query<T> query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> PersistenceManagerAsync async() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] supportedOperators() {
		// TODO Auto-generated method stub
		return null;
	}

}
