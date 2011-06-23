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
import java.sql.SQLException;

import siena.SienaException;
import siena.logging.SienaLogger;
import siena.logging.SienaLoggerFactory;

public abstract class AbstractConnectionManager implements ConnectionManager {

	/**
	 * Logger for AbstractConnectionManager classes and subclasses.
	 */
	protected static SienaLogger logger = SienaLoggerFactory.getLogger(AbstractConnectionManager.class);
	
	public void beginTransaction(int isolationLevel) {
		try {
			Connection c = getConnection();
			c.setAutoCommit(false);
			c.setTransactionIsolation(isolationLevel);
		} catch (SQLException e) {
			
			logger.severe(e, e);
			throw new SienaException(e);
		}
	}
	
	public void beginTransaction() {
		try {
			Connection c = getConnection();
			c.setAutoCommit(false);
		} catch (SQLException e) {
			
			logger.severe(e, e);
			throw new SienaException(e);
		}
	}

	public void commitTransaction() {
		try {
			Connection c = getConnection();
			c.commit();
		} catch (SQLException e) {
			
			logger.severe(e, e);
			throw new SienaException(e);
		}
	}

	public void rollbackTransaction() {
		try {
			Connection c = getConnection();
			c.rollback();
		} catch (SQLException e) {
			
			logger.severe(e, e);
			throw new SienaException(e);
		}
	}

}
