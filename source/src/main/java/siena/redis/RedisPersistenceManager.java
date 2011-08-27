/*
 * Copyright 2011 Pascal Voitot <pascal.voitot@mandubian.org>
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
 */
package siena.redis;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Response;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.exceptions.JedisException;
import siena.BaseQueryData;
import siena.ClassInfo;
import siena.PersistenceManager;
import siena.Query;
import siena.Util;
import siena.core.Many4PM;
import siena.core.One4PM;
import siena.core.async.PersistenceManagerAsync;
import siena.core.batch.Batch;

/**
 * @author Roch Delsalle <rdelsalle@gmail.com>
 * @author Pascal Voitot <pascal.voitot@mandubian.org>
 *
 */
public class RedisPersistenceManager implements PersistenceManager{
	private static final String DB = "REDIS";

	private static JedisPool pool;
	private Properties props;
	
	private ThreadLocal<Jedis> currentJedis = new ThreadLocal<Jedis>();
	
        @Override
	public void init(Properties p) {
		props = p;
		
		// by default, host is "localhost"
		String host = "localhost";
		
		if(p!=null){
			host    = p.getProperty("host", "localhost");
		}
		
		// TODO manage some config params?
		JedisPoolConfig cfg = new JedisPoolConfig();
		
		pool = new JedisPool(cfg, host);
	}
	
	public Jedis jedis() {
		Jedis jedis = currentJedis.get(); 
		if(jedis == null){
			jedis = pool.getResource();
			currentJedis.set(jedis);
		}
		return jedis;
	}
	
	public void returnJedis() {
		Jedis jedis = currentJedis.get(); 
		if(jedis != null){
			pool.returnResource(jedis);
		}
	}

	
	/*
	 * CRUD FUNCTIONS
	 */
	
        @Override
        public void insert(Object obj) {
            try {
                Class<?> clazz = obj.getClass();
                ClassInfo info = ClassInfo.getClassInfo(clazz);

                final Map<String, String> map = new HashMap<String, String>();

                for (Field field : info.updateFields) {
                    String fieldName = ClassInfo.getColumnNames(field)[0];
                    Object value = Util.readField(obj, field);

                    map.put(fieldName, value.toString());
                }
                final String key = info.tableName + ":" + Util.readField(obj, info.getIdField());
                System.out.println("saving " + key + ": " + map);
                jedis().multi(new TransactionBlock() {
                    @Override
                    public void execute() throws JedisException {
                        del(key);
                        hmset(key, map);
                    }
                });
            } finally {
                returnJedis();
            }
        }
	
        @Override
	public void get(final Object obj) {
           List<Object> res = null;
           try {
                Class<?> clazz = obj.getClass();
                ClassInfo info = ClassInfo.getClassInfo(clazz);

                final String[] fields = new String[info.updateFields.size()];

                for (int i = 0; i < info.updateFields.size(); i++) {
                    fields[i] = ClassInfo.getColumnNames(info.updateFields.get(i))[0];
                }
                final String key = info.tableName + ":" + Util.readField(obj, info.getIdField());
                System.out.println("getting " + key);
                res = jedis().multi(new TransactionBlock() {
                     @Override
                     public void execute() throws JedisException {
                         hmget(key, fields).get();
                     }
                 });
            } finally {
                RedisMappingUtils.fillModel(obj, res);
                returnJedis();
            }
	}

	@Override
	public void delete(Object obj) {
            try {
                Class<?> clazz = obj.getClass();
                ClassInfo info = ClassInfo.getClassInfo(clazz);

                final String key = info.tableName + ":" + Util.readField(obj, info.getIdField());
                System.out.println("deleting " + key);
                jedis().multi(new TransactionBlock() {

                    @Override
                    public void execute() throws JedisException {
                        del(key);
                    }
                });
            } finally {
                returnJedis();
            }
	}

	@Override
	public void update(Object obj) {
            insert(obj);
	}

	@Override
	public void save(Object obj) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void beginTransaction(int isolationLevel) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * TRANSACTIONAL FUNCTIONS
	 */
	
	@Override
	public void beginTransaction() {
		
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
		returnJedis();
	}

	
	
	@Override
	public <T> Query<T> createQuery(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Query<T> createQuery(BaseQueryData<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Batch<T> createBatch(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
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
	public int insert(Object... objects) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insert(Iterable<?> objects) {
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
	public <T> T getByKey(Class<T> clazz, Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		// TODO Auto-generated method stub
		return null;
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
	public <T> T get(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int delete(Query<T> query) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int count(Query<T> query) {
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
	public <T> Iterable<T> iterPerPage(Query<T> query, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void release(Query<T> query) {
		// TODO Auto-generated method stub
		
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

	@Override
	public <T> int count(Query<T> query, int limit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> int count(Query<T> query, int limit, Object offset) {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public <T> Many4PM<T> createMany(Class<T> clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> One4PM<T> createOne(Class<T> clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
