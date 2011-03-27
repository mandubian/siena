package siena.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import siena.ClassInfo;
import siena.Query;
import siena.SienaException;
import siena.core.options.QueryOptionPage;
import siena.core.options.QueryOptionState;

/**
 * @author mandubian
 * 
 *         A Siena Iterable<Model> encapsulating a Jdbc ResultSet
 *         its Iterator<Model>...
 */
public class JdbcSienaIterable<T> implements Iterable<T> {
    /**
     * The wrapped <code>Statement</code>.
     */
    private final Statement st;
	
	/**
     * The wrapped <code>ResultSet</code>.
     */
    private final ResultSet rs;
    
    /**
     * The wrapped <code>Query</code>.
     */
    private Query<T> query;
        
	JdbcSienaIterable(Statement st, ResultSet rs, Query<T> query) {
		this.st = st;
		this.rs = rs;
		this.query = query;
	}

	public Iterator<T> iterator() {
		return new SienaJdbcIterator<T>(query);
	}

	// only constructs the iterator with Class<V> in order to transmit the generic type T
	public class SienaJdbcIterator<V> implements Iterator<V> {
		private Query<V> query;
		private int idx = 0;
		private QueryOptionPage pag;
		private QueryOptionJdbcContext jdbcCtx;
		private QueryOptionState state;
		private boolean hasNext = true;
		SienaJdbcIterator(Query<V> query) {
			this.query = query;
			this.pag = (QueryOptionPage)query.option(QueryOptionPage.ID);
			this.jdbcCtx = (QueryOptionJdbcContext)query.option(QueryOptionJdbcContext.ID);
			this.state = (QueryOptionState)query.option(QueryOptionState.ID);
			// if paginating and 0 results then no more data else resets noMoreDataAfter
			try {
				if(pag.isPaginating()){
					if(rs.isLast()){
						jdbcCtx.noMoreDataAfter = true;
					}else {
						jdbcCtx.noMoreDataAfter = false;
					}
				}
			} catch (SQLException ex) {
	            throw new SienaException(ex);
	        }
		}

		@Override
		public boolean hasNext() {
			try {
				hasNext = false;
	            if(rs.next()){
	            	if(pag.isPaginating()) {
	            		if(idx<pag.pageSize){
	            			hasNext = true;
	            		}
	            	}
	            	else {
	            		hasNext = true;
	            	}
	            }
	            return hasNext;
	        } catch (SQLException ex) {
	            throw new SienaException(ex);
	        }
		}

		@Override
		public V next() {
			try {
				if(hasNext || rs.next()){
					Class<V> clazz = query.getQueriedClass();
					
					if(pag.isPaginating() && idx<(Integer)pag.pageSize){
						idx++;
						return JdbcMappingUtils.mapObject(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, JdbcMappingUtils.getJoinFields(query));
					}else {
						if(state.isStateful()){
							jdbcCtx.realOffset++;
						}
						
						return JdbcMappingUtils.mapObject(clazz, rs, ClassInfo.getClassInfo(clazz).tableName, JdbcMappingUtils.getJoinFields(query));
					}
				}
				else {
					throw new NoSuchElementException();
				}
			} catch (SQLException e) {
				throw new SienaException(e);
	        }
		}

		@Override
		public void remove() {
			// doesn't delete row because it REALLY deletes row from DB!!!
			// need to think about it
			/*try {
				
	            rs.deleteRow();
	        } catch (SQLException e) {
	        	throw new SienaException(e);
	        }*/
		}

		@Override
		protected void finalize() throws Throwable {
			JdbcDBUtils.closeResultSet(rs);
			JdbcDBUtils.closeStatement(st);
			super.finalize();
		}

	}

	@Override
	protected void finalize() throws Throwable {
		JdbcDBUtils.closeResultSet(rs);
		JdbcDBUtils.closeStatement(st);
		super.finalize();
	}

}