package com.orderfoodnow.pos.shared;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

//Can have separate the database into four: staff, menu, customer, and sales.
//pros: easier to do individual backup and reduce impact on data corruption.
//cons: need separate DB connections, harder on coding logic, and need to think about impact on auto commit locking.
public class DataSource {
	private static final Logger logger = Logger.getLogger(DataSource.class.getName());

	public static boolean isDatabaseExist(String databaseName) {
		logger.finest("Entered");

		boolean exist = false;
		if (databaseName == null) {
			return exist;
		}

		Connection connection;
		try {
			connection = getConnection(databaseName);
		} catch (SQLException e) {
			logger.fine("Exception on getConnection for databaseName=" + databaseName + " Excetion=" + e.getMessage());
			return exist;
		}

		ResultSet resultSet = null;
		try {
			resultSet = connection.getMetaData().getCatalogs();
		} catch (SQLException e) {
			logger.fine("Exception on getCatalogs for databaseName=" + databaseName + " Excetion=" + e.getMessage());
			// Don't return yet so resultSet.close() is called
		}

		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					String existedDatabaseName = resultSet.getString(1); //database name at position 1
					if (existedDatabaseName.equals(databaseName)) {
						exist = true;
						break;
					}
				}
			} catch (SQLException e) {
				logger.fine("Exception on resultSet.next() for databaseName=" + databaseName + " Excetion=" + e.getMessage());
				// Don't return yet so resultSet.close() is called
			}
		}
		try {
			resultSet.close();
		} catch (SQLException e) {
			return exist;
		}

		return exist;
	}

	// Connection to database name: DB_NAME defined above
	public static Connection getConnection() throws SQLException {
		logger.finest("Entered");
		return getConnection(Configuration.getDatasourceDatabase());
	}

	public static Connection getConnection(String dbName) throws SQLException {
		logger.finest("Entered");
		return  DriverManager.getConnection(
				Configuration.getDatasourceUrl() + dbName,
				Configuration.getDatasourceUser(),
				Configuration.getDatasourcePasword()
				);
	}

	// This connection does not depend on the default DB exist.
	// That's during initial DB setup where no database has been created yet.
	// Use this connection to create the database, then call the getConnection()
	// above that's connected directly to the just created database.
	public static Connection getConnectionWithoutDb() throws SQLException {
		logger.finest("Entered");
		return  DriverManager.getConnection(
				Configuration.getDatasourceUrl(),
				Configuration.getDatasourceUser(),
				Configuration.getDatasourcePasword()
				);
	}

	// Execute an SQL statement. The caller responsible for closing the result set.
	public static ResultSet executeQuery(Connection connection, String sql) throws SQLException {
		logger.finest("Entered");
		logger.finer(sql);

		// ResultSet.TYPE_FORWARD_ONLY is faster than ResultSet.TYPE_SCROLL_INSENSITIVE
		try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_UPDATABLE);) {
			return statement.executeQuery(sql);
		}
	}

	// Execute an SQL statement The caller responsible for close the result set.
	public static int executeUpdate(Connection connection, String sql) throws SQLException {
		logger.finest("Entered");
		logger.finer(sql);

		// ResultSet.TYPE_FORWARD_ONLY is faster than ResultSet.TYPE_SCROLL_INSENSITIVE
		try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_UPDATABLE);) {
			return statement.executeUpdate(sql);
		}
	}

	// Execute an SQL statement with no default database. The caller responsible for
	// close the result set.
	public static ResultSet executeSqlWithoutDb(Connection connection, String sql) throws SQLException {
		logger.finest("Entered");
		logger.finer(sql);

		try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_UPDATABLE);) {
			return statement.executeQuery(sql);
		}
	}
}
