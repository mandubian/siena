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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import siena.SienaException;

public class ThreadedConnectionManager extends AbstractConnectionManager {

	private String url;
	private String user;
	private String pass;
	private String jndi;
	
	private DataSource dataSource;

	private ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();
	
	public void init(Properties p) {
		String driver = p.getProperty("driver");
		this.url    = p.getProperty("url");
		this.user   = p.getProperty("user");
		this.pass   = p.getProperty("password");
		this.jndi   = p.getProperty("jndi");
		
		if(jndi == null) {
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				throw new SienaException("Error while loading JDBC driver", e);
			}
		} else {
			try {
				InitialContext ctx = new InitialContext();
				dataSource = (DataSource) ctx.lookup(jndi);
			} catch (Exception e) {
				throw new SienaException("Error while looking up for JNDI resource: "+jndi, e);
			}
		}
	}
	
	public Connection getConnection() {
		if(dataSource != null) {
			try {
				return dataSource.getConnection();
			} catch (SQLException e) {
				throw new SienaException(e);
			}
		} else {
			Connection c = currentConnection.get();
			if(c == null) {
				try {
					c = DriverManager.getConnection(url, user, pass);
				} catch (SQLException e) {
					throw new SienaException(e);
				}
				currentConnection.set(c);
			}
			return c;
		}
	}

	public void closeConnection() {
		try {
			Connection c = currentConnection.get();
			if(c != null) {
				currentConnection.remove();
				c.close();
			}
		} catch (SQLException e) {
			throw new SienaException(e);
		}
	}

	public void setDataSource (DataSource dataSource) {
	
		this.dataSource = dataSource;
	}
}
