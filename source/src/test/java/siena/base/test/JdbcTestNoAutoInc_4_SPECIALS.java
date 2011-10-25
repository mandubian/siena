package siena.base.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.CreationParameters;

import siena.PersistenceManager;
import siena.base.test.model.EnumTest;
import siena.base.test.model.PersonStringID;
import siena.jdbc.JdbcPersistenceManager;
import siena.jdbc.ddl.DdlGenerator;

public class JdbcTestNoAutoInc_4_SPECIALS extends BaseTestNoAutoInc_4_SPECIALS {
	private static JdbcPersistenceManager pm;
	
	@Override
	public PersistenceManager createPersistenceManager(List<Class<?>> classes) throws Exception {
		if(pm == null){
			Properties p = new Properties();
			
			String driver   = "com.mysql.jdbc.Driver";
			String username = "siena";
			String password = "siena";
			String url      = "jdbc:mysql://localhost/siena";
			
			p.setProperty("driver",   driver);
			p.setProperty("user",     username);
			p.setProperty("password", password);
			p.setProperty("url",      url);
	
			Class.forName(driver);
			BasicDataSource dataSource = new BasicDataSource();
			dataSource = new BasicDataSource();
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
			dataSource.setMaxWait(2000); // 2 seconds max for wait a connection.
			
			DdlGenerator generator = new DdlGenerator();
			for (Class<?> clazz : classes) {
				generator.addTable(clazz);
			}
	
			// get the Database model
			Database database = generator.getDatabase();
	
			Platform platform = PlatformFactory.createNewPlatformInstance("mysql");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection(url, username, password);
			
			System.out.println(platform.getAlterTablesSql(connection, database));
			
			// this will perform the database changes
			CreationParameters cp = new CreationParameters();
			// to search, it requires MyISAM
			cp.addParameter(database.findTable("discoveries_search"), "ENGINE", "MyISAM");
			cp.addParameter(database.findTable("discoveries_search2"), "ENGINE", "MyISAM");

			platform.alterTables(connection, database, cp, true);
				
			connection.close();
			
			pm = new JdbcPersistenceManager();
			pm.init(p);
		}
		
		return pm;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public boolean supportsAutoincrement() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsMultipleKeys() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsDeleteException() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean supportsSearchStart() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean supportsSearchEnd() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean supportsTransaction() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean supportsListStore() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void testEnum() {
		// TODO Auto-generated method stub
		super.testEnum();
	}
   
    
}
