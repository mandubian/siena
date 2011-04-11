/*
 * Copyright 2008 Alberto Gimeno <gimenete at gmail.com>
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
package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;

import siena.AbstractPersistenceManager;
import siena.ClassInfo;
import siena.Generator;
import siena.Id;
import siena.Query;
import siena.SienaException;
import siena.Util;
import siena.core.async.PersistenceManagerAsync;
import siena.core.options.QueryOptionFetchType;
import siena.core.options.QueryOptionOffset;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

public class JdbcPersistenceManager extends AbstractPersistenceManager {
	private static final String DB = "JDBC";
	
	private ConnectionManager connectionManager;

	public JdbcPersistenceManager() {
	}

	public JdbcPersistenceManager(ConnectionManager connectionManager, Class<?> listener) {
		this();
		this.connectionManager = connectionManager;
	}

	public void init(Properties p) {
		String cm = p.getProperty("transactions");
		if(cm != null) {
			try {
				connectionManager = (ConnectionManager) Class.forName(cm).newInstance();
			} catch (Exception e) {
				throw new SienaException(e);
			}
		} else {
			connectionManager = new ThreadedConnectionManager();
		}

		connectionManager.init(p);
	}

	protected Connection getConnection() throws SQLException {
		return connectionManager.getConnection();
	}

	public void delete(Object obj) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(classInfo.deleteSQL);
			JdbcDBUtils.addParameters(obj, classInfo.keys, ps, 1);
			int n = ps.executeUpdate();
			if(n == 0) {
				throw new SienaException("No updated rows");
			}
			if(n > 1) {
				throw new SienaException(n+" rows deleted");
			}
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public void get(Object obj) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConnection().prepareStatement(classInfo.selectSQL);
			JdbcDBUtils.addParameters(obj, classInfo.keys, ps, 1);
			rs = ps.executeQuery();
			if(rs.next()) {
				JdbcMappingUtils.mapObject(obj, rs, null, null);
			} else {
				throw new SienaException("No such object");
			}
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeResultSet(rs);
			JdbcDBUtils.closeStatement(ps);
		}
	}



	public void insert(Object obj) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		try {
			for (Field field : classInfo.keys) {
				Id id = field.getAnnotation(Id.class);
				if (id.value() == Generator.UUID) {
					field.set(obj, UUID.randomUUID().toString());
				}
			}
			// TODO: implement primary key generation: SEQUENCE

			if (!classInfo.generatedKeys.isEmpty()) {
				insertWithAutoIncrementKey(classInfo, obj);
			} else {
				ps = getConnection().prepareStatement(classInfo.insertSQL);
				JdbcDBUtils.addParameters(obj, classInfo.insertFields, ps, 1);
				ps.executeUpdate();
			}
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public void update(Object obj) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());

		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(classInfo.updateSQL);
			int i = 1;
			i = JdbcDBUtils.addParameters(obj, classInfo.updateFields, ps, i);
			JdbcDBUtils.addParameters(obj, classInfo.keys, ps, i);
			ps.executeUpdate();
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public void beginTransaction(int isolationLevel) {
		connectionManager.beginTransaction(isolationLevel);
	}

	public void commitTransaction() {
		connectionManager.commitTransaction();
	}

	public void rollbackTransaction() {
		connectionManager.rollbackTransaction();
	}

	public void closeConnection() {
		connectionManager.closeConnection();
	}

	private PreparedStatement createStatement(String sql,
			List<Object> parameters) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(sql);
		if(parameters != null) {
			int i = 1;
			for (Object parameter : parameters) {
				JdbcDBUtils.setParameter(statement, i++, parameter);
			}
		}
		return statement;
	}

	protected void insertWithAutoIncrementKey(JdbcClassInfo classInfo, Object obj) throws SQLException, IllegalAccessException {
		ResultSet gk = null;
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(classInfo.insertSQL,
					Statement.RETURN_GENERATED_KEYS);
			JdbcDBUtils.addParameters(obj, classInfo.insertFields, ps, 1);
			ps.executeUpdate();
			gk = ps.getGeneratedKeys();
			if (!gk.next())
				throw new SienaException("No such generated keys");
			int i = 1;
			for (Field field : classInfo.generatedKeys) {
				field.setAccessible(true);
				Util.setFromObject(obj, field, gk.getObject(i));
				// field.set(obj, gk.getObject(i));
				i++;
			}
		} finally {
			JdbcDBUtils.closeResultSet(gk);
			JdbcDBUtils.closeStatement(ps);
		}
	}



	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}


	
	private <T> List<T> doFetch(Query<T> query, int limit, int offset) {
		QueryOptionJdbcContext jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);
		if(jdbcCtx==null){
			jdbcCtx = new QueryOptionJdbcContext();
			query.customize(jdbcCtx);
		}
		
		// activates page and offset options as there are always used in SQL requests
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			if(pag.isActive()){
				if(limit!=Integer.MAX_VALUE){
					jdbcCtx.realPageSize = limit;
				}
				else {
					jdbcCtx.realPageSize = pag.pageSize;
				}
			}
			else {
				jdbcCtx.realPageSize = limit;
			}
		}else {
			jdbcCtx.realPageSize = pag.pageSize;
		}

		QueryOptionOffset offsetOpt = (QueryOptionOffset)(query.option(QueryOptionOffset.ID));
		// if local offset has been set, uses it
		if(offset!=0){
			offsetOpt.activate();
			offsetOpt.offset = offset;
		}
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(jdbcCtx.noMoreDataBefore){
			return new ArrayList<T>();
		}
				
		if(state.isStateless() || (state.isStateful() && !jdbcCtx.isActive())) {
			if(state.isStateless()){
				if(pag.isPaginating()){
					if(offsetOpt.isActive()){
						jdbcCtx.realOffset+=offsetOpt.offset;
						offsetOpt.passivate();
					}else {
						// keeps realOffset
					}
				}else {
					// if page is active, immediately passivates it not to keep is active
					if(pag.isActive()) {
						pag.passivate();
					}
					if(offsetOpt.isActive()){
						jdbcCtx.realOffset=offsetOpt.offset;
						offsetOpt.passivate();
					}else{
						jdbcCtx.realOffset = 0;
					}
				}
			} else {
				if(offsetOpt.isActive()){
					jdbcCtx.realOffset+=offsetOpt.offset;
					offsetOpt.passivate();
				}else {
					// keeps realOffset
				}
			}
			Class<T> clazz = query.getQueriedClass();
			List<Object> parameters = new ArrayList<Object>();
			StringBuilder sql = JdbcDBUtils.buildSqlSelect(query);
			JdbcDBUtils.appendSqlWhere(query, sql, parameters);
			JdbcDBUtils.appendSqlOrder(query, sql);
			JdbcDBUtils.appendSqlLimitOffset(query, sql, parameters);
			//sql.append(suffix);
			PreparedStatement statement = null;
			ResultSet rs = null;
			try {
				statement = createStatement(sql.toString(), parameters);
				if(pag.isPaginating()) {
					// this is just a hint to the DB so wonder if it should be used
					statement.setFetchSize(jdbcCtx.realPageSize);
				}
				rs = statement.executeQuery();
				List<T> result = JdbcMappingUtils.mapList(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, 
						JdbcMappingUtils.getJoinFields(query), jdbcCtx.realPageSize);
				
				if(pag.isPaginating()){
					if(result.size() == 0){
						jdbcCtx.noMoreDataAfter = true;
					}
					else {
						jdbcCtx.noMoreDataAfter = false;
					}
				}else {
					if(state.isStateful()){
						jdbcCtx.realOffset += result.size();
					}
				}
				
				if(state.isStateless()){
					JdbcDBUtils.closeResultSet(rs);
					JdbcDBUtils.closeStatement(statement);
				}else {
					Integer offsetParamIdx = parameters.size();
					Integer limitParamIdx = offsetParamIdx - 1;
					// store indexes of offset and limit for reuse
					jdbcCtx.activate();
					jdbcCtx.statement = statement;
					jdbcCtx.limitParamIdx = limitParamIdx;
					jdbcCtx.offsetParamIdx = offsetParamIdx;
				}
				
				return result;
			} 
			catch(SQLException e) {
				JdbcDBUtils.closeResultSet(rs);
				JdbcDBUtils.closeStatement(statement);
				throw new SienaException(e);
			}
		}else {
			// payload has been initialized so goes on
			Class<T> clazz = query.getQueriedClass();
			
			if(offsetOpt.isActive()){
				jdbcCtx.realOffset+=offsetOpt.offset;
				offsetOpt.passivate();
			}else {
				// keeps realOffset
			}
			
			try {
				// when paginating, should update limit and offset
				//if(pag.isActive()){
					// update limit and offset
				jdbcCtx.statement.setObject(jdbcCtx.limitParamIdx, jdbcCtx.realPageSize);
				//}
				//if(offsetOpt.isActive()){
				jdbcCtx.statement.setObject(jdbcCtx.offsetParamIdx, jdbcCtx.realOffset);
				//}
				
				ResultSet rs = jdbcCtx.statement.executeQuery();
				List<T> result = JdbcMappingUtils.mapList(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, 
					JdbcMappingUtils.getJoinFields(query), jdbcCtx.realPageSize);
				// increases offset
				
				if(pag.isPaginating()){
					if(result.size() == 0){
						jdbcCtx.noMoreDataAfter = true;
					}
					else {
						jdbcCtx.noMoreDataAfter = false;
					}
				}else {
					jdbcCtx.realOffset += result.size();
				}
				return result;
			}catch(SQLException ex){
				JdbcDBUtils.closeStatement(jdbcCtx.statement);
				throw new SienaException(ex);
			} 
		}
	}


	@Override
	public <T> List<T> fetch(Query<T> query) {
		List<T> result = doFetch(query, Integer.MAX_VALUE, 0);
		return result;
	}

	@Override
	public <T> List<T> fetch(Query<T> query, int limit) {
		//((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		List<T> result = doFetch(query, limit, 0);
		//List<T> result = fetch(query, " LIMIT "+limit);
		return result;
	}

	public <T> List<T> fetch(Query<T> query, int limit, Object offset) {
		//((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		//((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;
		List<T> result = doFetch(query, limit, (Integer)offset);
		//List<T> result = fetch(query, " LIMIT "+limit+" OFFSET "+offset);
		//query.setNextOffset(result.size());
		return result;
	}

	public <T> int count(Query<T> query) {
		ClassInfo info = ClassInfo.getClassInfo(query.getQueriedClass());
		List<Object> parameters = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
		sql.append(info.tableName);
		JdbcDBUtils.appendSqlWhere(query, sql, parameters);
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = createStatement(sql.toString(), parameters);
			rs = statement.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeResultSet(rs);
			JdbcDBUtils.closeStatement(statement);
		}
	}

	public <T> int delete(Query<T> query) {
		ClassInfo info = ClassInfo.getClassInfo(query.getQueriedClass());
		List<Object> parameters = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder("DELETE FROM ");
		sql.append(info.tableName);
		JdbcDBUtils.appendSqlWhere(query, sql, parameters);
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = createStatement(sql.toString(), parameters);
			return statement.executeUpdate();
		} catch(SQLException e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeResultSet(rs);
			JdbcDBUtils.closeStatement(statement);
		}
	}
	
	private <T> List<T> doFetchKeys(Query<T> query, int limit, int offset) {
		QueryOptionJdbcContext jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);
		if(jdbcCtx==null){
			jdbcCtx = new QueryOptionJdbcContext();
			query.customize(jdbcCtx);
		}
		
		// activates page and offset options as there are always used in SQL requests
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
		if(!pag.isPaginating()){
			if(pag.isActive()){
				if(limit!=Integer.MAX_VALUE){
					jdbcCtx.realPageSize = limit;
				}
				else {
					jdbcCtx.realPageSize = pag.pageSize;
				}
			}
			else {
				jdbcCtx.realPageSize = limit;
			}
		}else {
			jdbcCtx.realPageSize = pag.pageSize;
		}

		QueryOptionOffset offsetOpt = (QueryOptionOffset)(query.option(QueryOptionOffset.ID));
		// if local offset has been set, uses it
		if(offset!=0){
			offsetOpt.activate();
			offsetOpt.offset = offset;
		}
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		
		
		// if previousPage has detected there is no more data, simply returns an empty list
		if(jdbcCtx.noMoreDataBefore){
			return new ArrayList<T>();
		}
				
		if(state.isStateless() || (state.isStateful() && !jdbcCtx.isActive())) {
			if(state.isStateless()){
				if(pag.isPaginating()){
					if(offsetOpt.isActive()){
						jdbcCtx.realOffset+=offsetOpt.offset;
						offsetOpt.passivate();
					}else {
						// keeps realOffset
					}
				}else {
					// if page is active, immediately passivates it not to keep is active
					if(pag.isActive()) {
						pag.passivate();
					}
					if(offsetOpt.isActive()){
						jdbcCtx.realOffset=offsetOpt.offset;
						offsetOpt.passivate();
					}else{
						jdbcCtx.realOffset = 0;
					}
				}
			} else {
				if(offsetOpt.isActive()){
					jdbcCtx.realOffset+=offsetOpt.offset;
					offsetOpt.passivate();
				}else {
					// keeps realOffset
				}
			}
			
			Class<T> clazz = query.getQueriedClass();
			List<Object> parameters = new ArrayList<Object>();
			StringBuilder sql = JdbcDBUtils.buildSqlSelect(query);
			JdbcDBUtils.appendSqlWhere(query, sql, parameters);
			JdbcDBUtils.appendSqlOrder(query, sql);
			JdbcDBUtils.appendSqlLimitOffset(query, sql, parameters);
			//sql.append(suffix);
			PreparedStatement statement = null;
			ResultSet rs = null;
			try {
				statement = createStatement(sql.toString(), parameters);
				if(pag.isActive()) {
					// this is just a hint to the DB so wonder if it should be used
					statement.setFetchSize(jdbcCtx.realPageSize);
				}
				rs = statement.executeQuery();
				List<T> result = JdbcMappingUtils.mapListKeys(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, 
						JdbcMappingUtils.getJoinFields(query), jdbcCtx.realPageSize);
				
				if(pag.isPaginating()){
					if(result.size() == 0){
						jdbcCtx.noMoreDataAfter = true;
					}
					else {
						jdbcCtx.noMoreDataAfter = false;
					}
				}else {
					if(state.isStateful()){
						jdbcCtx.realOffset += result.size();
					}
				}
				
				if(state.isStateless()){
					JdbcDBUtils.closeResultSet(rs);
					JdbcDBUtils.closeStatement(statement);
				}else {
					Integer offsetParamIdx = parameters.size();
					Integer limitParamIdx = offsetParamIdx - 1;
					// store indexes of offset and limit for reuse
					jdbcCtx.activate();
					jdbcCtx.statement = statement;
					jdbcCtx.limitParamIdx = limitParamIdx;
					jdbcCtx.offsetParamIdx = offsetParamIdx;
				}
				return result;
			} catch(SQLException e) {
				JdbcDBUtils.closeResultSet(rs);
				JdbcDBUtils.closeStatement(statement);
				throw new SienaException(e);
			} 
		}else {
			// payload has been initialized so goes on
			Class<T> clazz = query.getQueriedClass();

			if(offsetOpt.isActive()){
				jdbcCtx.realOffset+=offsetOpt.offset;
				offsetOpt.passivate();
			}else {
				// keeps realOffset
			}
			
			try {
				// when paginating, should update limit and offset
				//if(pag.isActive()){
					// update limit and offset
					jdbcCtx.statement.setObject(jdbcCtx.limitParamIdx, jdbcCtx.realPageSize);
				//}
				//if(offsetOpt.isActive()){
				jdbcCtx.statement.setObject(jdbcCtx.offsetParamIdx, jdbcCtx.realOffset);
				//}
				
				ResultSet rs = jdbcCtx.statement.executeQuery();
				List<T> result = JdbcMappingUtils.mapListKeys(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, 
					JdbcMappingUtils.getJoinFields(query), jdbcCtx.realPageSize);
				
				if(pag.isPaginating()){
					if(result.size() == 0){
						jdbcCtx.noMoreDataAfter = true;
					}
					else {
						jdbcCtx.noMoreDataAfter = false;
					}
				}else {
					jdbcCtx.realOffset += result.size();
				}
				return result;
			}catch(SQLException ex){
				JdbcDBUtils.closeStatement(jdbcCtx.statement);
				throw new SienaException(ex);
			} 
		}
	}
	
	public <T> List<T> fetchKeys(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;
		List<T> result = doFetchKeys(query, Integer.MAX_VALUE, 0);
		//query.setNextOffset(result.size());
		return result;
	}

	public <T> List<T> fetchKeys(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;
		//((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		List<T> result = doFetchKeys(query, limit, 0);
		//query.setNextOffset(result.size());
		return result;
	}

	public <T> List<T> fetchKeys(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.KEYS_ONLY;
//		((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
//		((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;
		List<T> result = doFetchKeys(query, limit, (Integer)offset);
		//query.setNextOffset(result.size());
		return result;
	}
	
	
	private <T> Iterable<T> doIter(Query<T> query, int limit, int offset) {		
		QueryOptionJdbcContext jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);
		if(jdbcCtx==null){
			jdbcCtx = new QueryOptionJdbcContext();
			query.customize(jdbcCtx);
		}
				
		// activates page and offset options as there are always used in SQL requests
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
//		QueryOptionFetchType fetchType = (QueryOptionFetchType)query.option(QueryOptionFetchType.ID);
		// in iter_per_page mode, always trigger pagination
//		if(fetchType.fetchType == QueryOptionFetchType.Type.ITER_PER_PAGE){
//			pag.pageType = QueryOptionPage.PageType.PAGINATING;
//			pag.pageSize = limit;
//			jdbcCtx.realPageSize = limit;
//		}
//		else {
			if(!pag.isPaginating()){
				if(pag.isActive()){
					if(limit!=Integer.MAX_VALUE){
						jdbcCtx.realPageSize = limit;
					}
					else {
						jdbcCtx.realPageSize = pag.pageSize;
					}
				}
				else {
					jdbcCtx.realPageSize = limit;
				}
			}else {
				jdbcCtx.realPageSize = pag.pageSize;
			}
//		}

		QueryOptionOffset offsetOpt = (QueryOptionOffset)(query.option(QueryOptionOffset.ID));
		// if local offset has been set, uses it
		if(offset!=0){
			offsetOpt.activate();
			offsetOpt.offset = offset;
		}
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);

		// if previousPage has detected there is no more data, simply returns an empty list
		if(jdbcCtx.noMoreDataBefore){
			return new ArrayList<T>();
		}
		
		// forces the reusable option since iteration requires it!!!
		//query.stateful();		
		
		if(state.isStateless() || (state.isStateful() && !jdbcCtx.isActive())) {
			if(state.isStateless()){
				if(pag.isPaginating()){
					if(offsetOpt.isActive()){
						jdbcCtx.realOffset+=offsetOpt.offset;
						offsetOpt.passivate();
					}else {
						// keeps realOffset
					}
				}else {
					// if page is active, immediately passivates it not to keep is active
					if(pag.isActive()) {
						pag.passivate();
					}
					if(offsetOpt.isActive()){
						jdbcCtx.realOffset=offsetOpt.offset;
						offsetOpt.passivate();
					}else{
						jdbcCtx.realOffset = 0;
					}
				}
			} else {
				if(offsetOpt.isActive()){
					jdbcCtx.realOffset+=offsetOpt.offset;
					offsetOpt.passivate();
				}else {
					// keeps realOffset
				}
			}
			
			List<Object> parameters = new ArrayList<Object>();
			StringBuilder sql = JdbcDBUtils.buildSqlSelect(query);
			JdbcDBUtils.appendSqlWhere(query, sql, parameters);
			JdbcDBUtils.appendSqlOrder(query, sql);
			JdbcDBUtils.appendSqlLimitOffset(query, sql, parameters);
			//sql.append(suffix);
			PreparedStatement statement = null;
			ResultSet rs = null;
			try {
				statement = createStatement(sql.toString(), parameters);
				if(pag.isActive()) {
					// this is just a hint to the DB so wonder if it should be used
					statement.setFetchSize(jdbcCtx.realPageSize);
				}
				rs = statement.executeQuery();
				
				if(state.isStateless()){
					//in iteration, doesn't close the resultset to reuse it
					//JdbcDBUtils.closeResultSet(rs);
					//JdbcDBUtils.closeStatement(statement);
				}else {
					Integer offsetParamIdx = parameters.size();
					Integer limitParamIdx = offsetParamIdx - 1;
					// store indexes of offset and limit for reuse
					jdbcCtx.activate();
					jdbcCtx.statement = statement;
					jdbcCtx.limitParamIdx = limitParamIdx;
					jdbcCtx.offsetParamIdx = offsetParamIdx;
				}
				
				return new JdbcSienaIterable<T>(statement, rs, query);
			} 
			catch(SQLException e) {
				JdbcDBUtils.closeResultSet(rs);
				JdbcDBUtils.closeStatement(statement);
				throw new SienaException(e);
			} 
		}else {
			if(offsetOpt.isActive()){
				jdbcCtx.realOffset+=offsetOpt.offset;
				offsetOpt.passivate();
			}else {
				// keeps realOffset
			}
			// payload has been initialized so goes on
			try {
				// when paginating, should update limit and offset
				//if(pag.isActive()){
					// update limit and offset
				jdbcCtx.statement.setObject(jdbcCtx.limitParamIdx, jdbcCtx.realPageSize);
				//}
				//if(offsetOpt.isActive()){
				jdbcCtx.statement.setObject(jdbcCtx.offsetParamIdx, jdbcCtx.realOffset);
				//}
				
				ResultSet rs = jdbcCtx.statement.executeQuery();

				return new JdbcSienaIterable<T>(jdbcCtx.statement, rs, query);
			}catch(SQLException ex){
				JdbcDBUtils.closeStatement(jdbcCtx.statement);
				throw new SienaException(ex);
			} 
		}
	}

	public <T> Iterable<T> iter(Query<T> query) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		return doIter(query, Integer.MAX_VALUE, 0);
	}

	public <T> Iterable<T> iter(Query<T> query, int limit) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		//((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		return doIter(query, limit, 0);
	}

	public <T> Iterable<T> iter(Query<T> query, int limit, Object offset) {
		((QueryOptionFetchType)query.option(QueryOptionFetchType.ID)).fetchType=QueryOptionFetchType.Type.ITER;
		//((QueryOptionPage)query.option(QueryOptionPage.ID).activate()).pageSize=limit;
		
		// if in stateful mode, the offset should be added to the current one
		//((QueryOptionOffset)query.option(QueryOptionOffset.ID).activate()).offset=(Integer)offset;
		return doIter(query, limit, (Integer)offset);
	}
	
	
	public <T> void release(Query<T> query) {
		super.release(query);
		QueryOptionJdbcContext jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);
		
		if(jdbcCtx != null && jdbcCtx.isActive()){
			JdbcDBUtils.closeStatement(jdbcCtx.statement);
			jdbcCtx.statement = null;
			jdbcCtx.passivate();
		}
	}

	@Override
	public <T> void paginate(Query<T> query) {
		QueryOptionJdbcContext jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);
		QueryOptionState state = (QueryOptionState)query.option(QueryOptionState.ID);
		if(jdbcCtx==null){
			jdbcCtx = new QueryOptionJdbcContext();
			query.customize(jdbcCtx);
		}
		
		// resets the realoffset to 0 if stateless
		if(state.isStateless()){
			jdbcCtx.realOffset = 0;
		}
	}

	public <T> void previousPage(Query<T> query) {
		QueryOptionJdbcContext jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);
		if(jdbcCtx==null){
			jdbcCtx = new QueryOptionJdbcContext();
			query.customize(jdbcCtx);
		}
		
		// if no more data before, doesn't try to go before
		if(jdbcCtx.noMoreDataBefore){
			return;
		}
		
		// if no more data after, removes flag to be able to go before
		if(jdbcCtx.noMoreDataAfter){
			jdbcCtx.noMoreDataAfter = false;
		}
		
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
	
		if(pag.isPaginating()){
			//QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
			//if(offset.isActive()){
			jdbcCtx.realPageSize = pag.pageSize;
			if(jdbcCtx.realOffset>=pag.pageSize) {
				jdbcCtx.realOffset-=pag.pageSize;
			}
			else {
				jdbcCtx.realOffset = 0;
				jdbcCtx.noMoreDataBefore = true;
			}
			//}
		}else {
			// throws exception because it's impossible to reuse nextPage when paginating has been interrupted, the cases are too many
			throw new SienaException("Can't use nextPage after pagination has been interrupted...");
		}
	}

	public <T> void nextPage(Query<T> query) {
		QueryOptionJdbcContext jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);
		if(jdbcCtx==null){
			jdbcCtx = new QueryOptionJdbcContext();
			query.customize(jdbcCtx);
		}

		// if no more data after, doesn't try to go after
		if(jdbcCtx.noMoreDataAfter){
			return;
		}
		
		// if no more data before, removes flag to be able and stay there
		if(jdbcCtx.noMoreDataBefore){
			jdbcCtx.noMoreDataBefore = false;
			return;
		}
		
		QueryOptionPage pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
				
		if(pag.isPaginating()){
			//QueryOptionOffset offset = (QueryOptionOffset)query.option(QueryOptionOffset.ID);
			//if(offset.isActive()){
				jdbcCtx.realPageSize = pag.pageSize;
				jdbcCtx.realOffset+=pag.pageSize;
			//}
		}else {
			// throws exception because it's impossible to reuse nextPage when paginating has been interrupted, the cases are too many
			throw new SienaException("Can't use nextPage after pagination has been interrupted...");
		}
	}

	
	public int get(Object... objects) {
		Map<JdbcClassInfo, List<Object>> objMap = new HashMap<JdbcClassInfo, List<Object>>();
		PreparedStatement ps = null;
		
		for(Object obj:objects){
			JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());
			if(!objMap.containsKey(classInfo)){
				List<Object> l = new ArrayList<Object>();
				l.add(obj);
				objMap.put(classInfo, l);
			}else{
				objMap.get(classInfo).add(obj);
			}
		}
		int total = 0;
		try {
			for(JdbcClassInfo classInfo: objMap.keySet()){
				// doesn't manage multiple keys case
				if(classInfo.keys.size()>1){
					throw new SienaException("Can't batch select multiple keys objects");
				}
				
				Field f = classInfo.keys.get(0);
				
				HashMap<Object, Object> keyObj = new HashMap<Object, Object>();
				
				for(Object obj: objMap.get(classInfo)){
					Object key = Util.readField(obj, f);
					keyObj.put(key, obj);
				}
				
				Query<?> q = createQuery(classInfo.info.clazz);
				List<?> results = q.filter(f.getName()+ " IN", keyObj.keySet()).fetch();
				
				for(Object res:results){
					Object resKey = Util.readField(res, f);
					Util.copyObject(res, keyObj.get(resKey));
				}
				total+=results.size();
			}
			return total;

		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public <T> int get(Iterable<T> objects) {
		Map<JdbcClassInfo, List<Object>> objMap = new HashMap<JdbcClassInfo, List<Object>>();
		PreparedStatement ps = null;
		
		for(Object obj:objects){
			JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());
			if(!objMap.containsKey(classInfo)){
				List<Object> l = new ArrayList<Object>();
				l.add(obj);
				objMap.put(classInfo, l);
			}else{
				objMap.get(classInfo).add(obj);
			}
		}
		
		int total = 0;
		try {
			for(JdbcClassInfo classInfo: objMap.keySet()){
				// doesn't manage multiple keys case
				if(classInfo.keys.size()>1){
					throw new SienaException("Can't batch select multiple keys objects");
				}
				
				Field f = classInfo.keys.get(0);
				
				HashMap<Object, Object> keyObj = new HashMap<Object, Object>();
				
				for(Object obj: objMap.get(classInfo)){
					Object key = Util.readField(obj, f);
					keyObj.put(key, obj);
				}
				
				Query<?> q = createQuery(classInfo.info.clazz);
				List<?> results = q.filter(f.getName()+ " IN", keyObj.keySet()).fetch();
				
				for(Object res:results){
					Object resKey = Util.readField(res, f);
					Util.copyObject(res, keyObj.get(resKey));
				}
				
				total+=results.size();
			}
			
			return total;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public <T> List<T> getByKeys(Class<T> clazz, Object... keys) {
		PreparedStatement ps = null;
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(clazz);
		
		try {
			// doesn't manage multiple keys case
			if(classInfo.keys.size()>1){
				throw new SienaException("Can't batch select multiple keys objects");
			}
				
			Field f = classInfo.keys.get(0);
			List<Object> keyList = new ArrayList<Object>();
			Collections.addAll(keyList, keys);
			
			Query<T> q = createQuery(clazz);
			List<T> results = q.filter(f.getName()+ " IN", keyList).fetch();
			List<T> realResults = new ArrayList<T>();
			HashMap<Object, T> keyObj = new HashMap<Object, T>();
			
			for(Object key: keys){
				if(!keyObj.containsKey(key)){
					for(int i=0; i<results.size();i++){
						T res = results.get(i);
						Object resKey = Util.readField(res, f);
						if(key.equals(resKey)){
							keyObj.put(key, res);
							results.remove(i);
							break;
						}
					}
				}
				
				realResults.add(keyObj.get(key));
			}
			
			return realResults;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}	
	}

	public <T> List<T> getByKeys(Class<T> clazz, Iterable<?> keys) {
		PreparedStatement ps = null;
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(clazz);
		
		try {
			// doesn't manage multiple keys case
			if(classInfo.keys.size()>1){
				throw new SienaException("Can't batch select multiple keys objects");
			}
				
			Field f = classInfo.keys.get(0);
			List<Object> keyList = new ArrayList<Object>();
			for(Object key:keys){
				keyList.add(key);
			}
			
			Query<T> q = createQuery(clazz);
			List<T> results = q.filter(f.getName()+ " IN", keyList).fetch();
			List<T> realResults = new ArrayList<T>();
			HashMap<Object, T> keyObj = new HashMap<Object, T>();
			
			for(Object key: keys){
				if(!keyObj.containsKey(key)){
					for(int i=0; i<results.size();i++){
						T res = results.get(i);
						Object resKey = Util.readField(res, f);
						if(key.equals(resKey)){
							keyObj.put(key, res);
							results.remove(i);
							break;
						}
					}
				}
				
				realResults.add(keyObj.get(key));
			}
			
			return realResults;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}	
	}

	public <T> PersistenceManagerAsync async() {
		throw new NotImplementedException();
	}

	public int insert(Object... objects) {
		Map<JdbcClassInfo, List<Object>> objMap = new HashMap<JdbcClassInfo, List<Object>>();
		PreparedStatement ps = null;
		
		for(Object obj:objects){
			JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());
			if(!objMap.containsKey(classInfo)){
				List<Object> l = new ArrayList<Object>();
				l.add(obj);
				objMap.put(classInfo, l);
			}else{
				objMap.get(classInfo).add(obj);
			}
		}

		int total = 0;
		try {
			for(JdbcClassInfo classInfo: objMap.keySet()){
				if(classInfo.generatedKeys.isEmpty()){
					ps = getConnection().prepareStatement(classInfo.insertSQL);
				}else {
					ps = getConnection().prepareStatement(classInfo.insertSQL,
							Statement.RETURN_GENERATED_KEYS);
				}
				
				for(Object obj: objMap.get(classInfo)){
					for (Field field : classInfo.keys) {
						Id id = field.getAnnotation(Id.class);
						if (id.value() == Generator.UUID) {
							field.set(obj, UUID.randomUUID().toString());
						}
					}
					// TODO: implement primary key generation: SEQUENCE
					JdbcDBUtils.addParameters(obj, classInfo.insertFields, ps, 1);
					ps.addBatch();
				}
				
				// TODO what to do with results of executeBatch ??????
				int[] res = ps.executeBatch();
				
				if(!classInfo.generatedKeys.isEmpty()){
					ResultSet gk = ps.getGeneratedKeys();
					int i;
					int idx = 0;
					while(gk.next()) {
						i=1;
						for (Field field : classInfo.generatedKeys) {
							field.setAccessible(true);
							Util.setFromObject(objMap.get(classInfo).get(idx++), field, gk.getObject(i++));
						}
					}
				}	
				total+=res.length;
			}
			
			return total;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
		
	}

	public int insert(Iterable<?> objects) {
		Map<JdbcClassInfo, List<Object>> objMap = new HashMap<JdbcClassInfo, List<Object>>();
		PreparedStatement ps = null;
		
		for(Object obj:objects){
			JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());
			if(!objMap.containsKey(classInfo)){
				List<Object> l = new ArrayList<Object>();
				l.add(obj);
				objMap.put(classInfo, l);
			}else{
				objMap.get(classInfo).add(obj);
			}
		}

		int total = 0;
		try {
			for(JdbcClassInfo classInfo: objMap.keySet()){
				if(classInfo.generatedKeys.isEmpty()){
					ps = getConnection().prepareStatement(classInfo.insertSQL);
				}else {
					ps = getConnection().prepareStatement(classInfo.insertSQL,
							Statement.RETURN_GENERATED_KEYS);
				}
				
				for(Object obj: objMap.get(classInfo)){
					for (Field field : classInfo.keys) {
						Id id = field.getAnnotation(Id.class);
						if (id.value() == Generator.UUID) {
							field.set(obj, UUID.randomUUID().toString());
						}
					}
					// TODO: implement primary key generation: SEQUENCE
					JdbcDBUtils.addParameters(obj, classInfo.insertFields, ps, 1);
					ps.addBatch();
				}
				
				// TODO what to do with results of executeBatch ??????
				int[] res = ps.executeBatch();
				
				if(!classInfo.generatedKeys.isEmpty()){
					ResultSet gk = ps.getGeneratedKeys();
					int i;
					int idx = 0;
					while(gk.next()) {
						i=1;
						for (Field field : classInfo.generatedKeys) {
							field.setAccessible(true);
							Util.setFromObject(objMap.get(classInfo).get(idx++), field, gk.getObject(i++));
						}
					}
				}
				
				total+=res.length;
			}
			
			return total;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public int delete(Object... objects) {
		Map<JdbcClassInfo, List<Object>> objMap = new HashMap<JdbcClassInfo, List<Object>>();
		PreparedStatement ps = null;
		
		for(Object obj:objects){
			JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());
			if(!objMap.containsKey(classInfo)){
				List<Object> l = new ArrayList<Object>();
				l.add(obj);
				objMap.put(classInfo, l);
			}else{
				objMap.get(classInfo).add(obj);
			}
		}

		int total = 0;
		try {
			for(JdbcClassInfo classInfo: objMap.keySet()){
				
				ps = getConnection().prepareStatement(classInfo.deleteSQL);
				
				for(Object obj: objMap.get(classInfo)){
					JdbcDBUtils.addParameters(obj, classInfo.keys, ps, 1);
					ps.addBatch();
				}
				
				// TODO what to do with results of executeBatch ??????
				int[] res = ps.executeBatch();
				
				total+=res.length;

			}
			return total;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public int delete(Iterable<?> objects) {
		Map<JdbcClassInfo, List<Object>> objMap = new HashMap<JdbcClassInfo, List<Object>>();
		PreparedStatement ps = null;
		
		for(Object obj:objects){
			JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(obj.getClass());
			if(!objMap.containsKey(classInfo)){
				List<Object> l = new ArrayList<Object>();
				l.add(obj);
				objMap.put(classInfo, l);
			}else{
				objMap.get(classInfo).add(obj);
			}
		}
		
		int total = 0;

		try {
			for(JdbcClassInfo classInfo: objMap.keySet()){
				
				ps = getConnection().prepareStatement(classInfo.deleteSQL);
				
				for(Object obj: objMap.get(classInfo)){
					JdbcDBUtils.addParameters(obj, classInfo.keys, ps, 1);
					ps.addBatch();
				}
				
				// TODO what to do with results of executeBatch ??????
				int[] res = ps.executeBatch();
				
				total+=res.length;
			}
			
			return total;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public <T> int deleteByKeys(Class<T> clazz, Object... keys) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(clazz);
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(classInfo.deleteSQL);
			
			for(Object key: keys){
				JdbcDBUtils.setParameter(ps, 1, key);
				ps.addBatch();
			}
				
			// TODO what to do with results of executeBatch ??????
			int res[] = ps.executeBatch();
			return res.length;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}

	public <T> int deleteByKeys(Class<T> clazz, Iterable<?> keys) {
		JdbcClassInfo classInfo = JdbcClassInfo.getClassInfo(clazz);
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(classInfo.deleteSQL);
			
			for(Object key: keys){
				JdbcDBUtils.setParameter(ps, 1, key);
				ps.addBatch();
			}
				
			// TODO what to do with results of executeBatch ??????
			int res[] = ps.executeBatch();
			
			return res.length;
		} catch (SienaException e) {
			throw e;
		} catch (Exception e) {
			throw new SienaException(e);
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
	}


	public <T> int update(Object... models) {
		throw new NotImplementedException("update not implemented for JDBC yet");
	}

	public <T> int update(Iterable<T> models) {
		throw new NotImplementedException("update not implemented for JDBC yet");
	}

	public <T> int update(Query<T> query, Map<String, ?> fieldValues) {
		throw new NotImplementedException("update not implemented for JDBC yet");
	}


	private static final String[] supportedOperators = new String[]{ "<", ">", ">=", "<=", "!=", "=", "IN" };

	public String[] supportedOperators() {
		return supportedOperators;
	}

	public static class JdbcClassInfo {
		protected static Map<Class<?>, JdbcClassInfo> infoClasses = new ConcurrentHashMap<Class<?>, JdbcClassInfo>();

		// encapsulates a classinfo
		public ClassInfo info;
		
		public String tableName;
		public String insertSQL;
		public String updateSQL;
		public String deleteSQL;
		public String selectSQL;
		public String baseSelectSQL;
		public String keySelectSQL;
		public String baseKeySelectSQL;

		public List<Field> keys = null;
		public List<Field> insertFields = null;
		public List<Field> updateFields = null;
		public List<Field> generatedKeys = null;
		public List<Field> allFields = null;
		public List<Field> joinFields = null;
		public Map<String, String> joinFieldAliases = new HashMap<String, String>();

		public JdbcClassInfo(ClassInfo info) {
			this.info = info;
			keys = info.keys;
			insertFields = info.insertFields;
			updateFields = info.updateFields;
			generatedKeys = info.generatedKeys;
			allFields = info.allFields;
			tableName = info.tableName;
			joinFields = info.joinFields;

			List<String> keyColumns = new ArrayList<String>();
			List<String> keyWhereColumns = new ArrayList<String>();
			List<String> insertColumns = new ArrayList<String>();
			List<String> updateColumns = new ArrayList<String>();
			List<String> allColumns = new ArrayList<String>();

			calculateColumns(info.insertFields, insertColumns, null, "");
			calculateColumns(info.updateFields, updateColumns, null, "=?");
			calculateColumns(info.keys, keyColumns, null, "");
			calculateColumns(info.keys, keyWhereColumns, null, "=?");
			calculateColumns(info.allFields, allColumns, null, "");

			deleteSQL = "DELETE FROM " + tableName + JdbcDBUtils.WHERE + Util.join(keyWhereColumns, JdbcDBUtils.AND);

			String[] is = new String[insertColumns.size()];
			Arrays.fill(is, "?");
			insertSQL = "INSERT INTO "+tableName+" ("+Util.join(insertColumns, ", ")+") VALUES("+Util.join(Arrays.asList(is), ", ")+")";

			updateSQL = "UPDATE "+tableName+" SET ";
			updateSQL += Util.join(updateColumns, ", ");
			updateSQL += JdbcDBUtils.WHERE;
			updateSQL += Util.join(keyWhereColumns, JdbcDBUtils.AND);

			baseSelectSQL = "SELECT "+Util.join(allColumns, ", ")+" FROM "+tableName;
			baseKeySelectSQL = "SELECT "+Util.join(keyColumns, ", ")+" FROM "+tableName;

			selectSQL = baseSelectSQL + JdbcDBUtils.WHERE + Util.join(keyWhereColumns, JdbcDBUtils.AND);
			keySelectSQL = baseKeySelectSQL+JdbcDBUtils.WHERE+Util.join(keyWhereColumns, JdbcDBUtils.AND);
		}

		public static void calculateColumns(List<Field> fields, List<String> columns, String tableName, String suffix) {
			for (Field field : fields) {
				String[] columnNames = ClassInfo.getColumnNames(field, tableName);
				for (String columnName : columnNames) {
					columns.add(columnName+suffix);
				}
			}
		}
		
		public static JdbcClassInfo getClassInfo(Class<?> clazz) {
			JdbcClassInfo ci = infoClasses.get(clazz);

			if(ci == null) {
				ci = new JdbcClassInfo(ClassInfo.getClassInfo(clazz));
				infoClasses.put(clazz, ci);
			}
			return ci;
		}
	}



	
}
