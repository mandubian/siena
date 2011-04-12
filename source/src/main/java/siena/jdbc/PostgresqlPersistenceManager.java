package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import siena.ClassInfo;
import siena.Generator;
import siena.Id;
import siena.QueryFilterSearch;
import siena.SienaException;
import siena.Util;
import siena.core.options.QueryOption;

public class PostgresqlPersistenceManager extends JdbcPersistenceManager {
	private static final String DB = "POSTGRES";

	@Override
    protected void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value != null && value instanceof Date) {
            Date date = (Date) value;
            ps.setTimestamp(index, new Timestamp(date.getTime()));
        } else {
            ps.setObject(index, value);
        }
    }

	@Override
	protected void insertWithAutoIncrementKey(JdbcClassInfo classInfo, Object obj) throws SQLException, IllegalAccessException {
		List<String> keyNames = new ArrayList<String>();
		for (Field field : classInfo.generatedKeys) {
			keyNames.add(field.getName());
		}

		ResultSet gk = null;
		PreparedStatement ps = null;
		try {
			ps = getConnection().prepareStatement(
					classInfo.insertSQL + " RETURNING " + Util.join(keyNames, ","));
			addParameters(obj, classInfo.insertFields, ps, 1);
			gk = ps.executeQuery();
			if (!gk.next())
				throw new SienaException("No such generated keys");

			int i = 1;
			for (Field field : classInfo.generatedKeys) {
				//field.setAccessible(true);
				Util.setFromObject(obj, field, gk.getObject(i));
				// field.set(obj, gk.getObject(i));
				i++;
			}
		} finally {
			JdbcDBUtils.closeResultSet(gk);
			JdbcDBUtils.closeStatement(ps);
		}
	}
	
	/**
	 * required to be overriden for Postgres
	 * 
	 * @param classInfo
	 * @param objMap
	 * @throws SQLException
	 * @throws IllegalAccessException
	 */
	@Override
	protected int insertBatchWithAutoIncrementKey(JdbcClassInfo classInfo, Map<JdbcClassInfo, List<Object>> objMap) throws SQLException, IllegalAccessException {
		List<String> keyNames = new ArrayList<String>();
		for (Field field : classInfo.generatedKeys) {
			keyNames.add(field.getName());
		}
		
		// can't use batch in Postgres with generated keys... known bug
		// http://postgresql.1045698.n5.nabble.com/PreparedStatement-batch-statement-impossible-td3406927.html
		PreparedStatement ps = null;
		ResultSet gk = null;
		int res = 0;
		try {
			ps = getConnection().prepareStatement(
					classInfo.insertSQL + " RETURNING " + Util.join(keyNames, ","));
			
			for(Object obj: objMap.get(classInfo)){
				for (Field field : classInfo.keys) {
					Id id = field.getAnnotation(Id.class);
					if (id.value() == Generator.UUID) {
						field.set(obj, UUID.randomUUID().toString());
					}
				}
				// TODO: implement primary key generation: SEQUENCE
				addParameters(obj, classInfo.insertFields, ps, 1);
				gk = ps.executeQuery();
				if (!gk.next())
					throw new SienaException("No such generated keys");
	
				int i = 1;
				for (Field field : classInfo.generatedKeys) {
					//field.setAccessible(true);
					Util.setFromObject(obj, field, gk.getObject(i));
					// field.set(obj, gk.getObject(i));
					i++;
				}
				
				JdbcDBUtils.closeResultSet(gk);
				res++;
			}
			
		} finally {
			JdbcDBUtils.closeStatement(ps);
		}
		// doesn't work with Postgres because it doesn't manage generated keys
		// int[] res = ps.executeBatch();
		
		return res;
	}
	
	@Override
	public <T> void appendSqlSearch(QueryFilterSearch qf, Class<?> clazz, JdbcClassInfo info, StringBuilder sql, List<Object> parameters) {
		List<String> cols = new ArrayList<String>();
		try {
			for (String field : qf.fields) {
				Field f = clazz.getDeclaredField(field);
				String[] columns = ClassInfo.getColumnNames(f, info.tableName);
				for (String col : columns) {
					cols.add("coalesce("+col+", '')");
				}
			}
			QueryOption opt = qf.option;
			if(opt != null){
				// only manages QueryOptionJdbcSearch
				if(QueryOptionPostgresqlSearch.class.isAssignableFrom(opt.getClass())){
					String lang = ((QueryOptionPostgresqlSearch)opt).language;
					if(lang != null && !"".equals(lang) ){
						sql.append("to_tsvector('"+lang+"', "+Util.join(cols, " || ' ' || ")+") @@ to_tsquery(?)");
					}
					else {
						sql.append("to_tsvector('english', "+Util.join(cols, " || ' ' || ")+") @@ to_tsquery(?)");
					}
				}else{
				}
			}else {
				sql.append("to_tsvector('english', "+Util.join(cols, " || ' ' || ")+") @@ to_tsquery(?)");
			}
			parameters.add(qf.match);
		}catch(Exception e){
			throw new SienaException(e);
		}
	}
}
