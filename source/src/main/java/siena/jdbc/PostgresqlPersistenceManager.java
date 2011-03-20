package siena.jdbc;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import siena.SienaException;
import siena.Util;

public class PostgresqlPersistenceManager extends JdbcPersistenceManager {

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
			JdbcDBUtils.addParameters(obj, classInfo.insertFields, ps, 1);
			gk = ps.executeQuery();
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
}
