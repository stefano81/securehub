package it.uninsubria.dicom.cryptosocial.server;

import it.uninsubria.dicom.cryptosocial.shared.ClientDatabase;
import it.uninsubria.dicom.cryptosocial.shared.ConnectionPoolException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Logger;

public class DatabasePoolImplPostgres implements ClientDatabase {
	private static DatabasePoolImplPostgres databasePool;
	private static Logger	logger = Logger.getLogger(DatabasePoolImplPostgres.class.toString());
	
	private Queue<Connection> freeConnection;
	private String dbURL;
	private Properties	properties;

	
	
	
	private DatabasePoolImplPostgres() throws IOException, ClassNotFoundException {
		freeConnection = new LinkedList<Connection>();
		
		properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream("it/uninsubria/dicom/cryptosocial/config.properties"));

		Class.forName(properties.getProperty("driver"));
		
		logger.info(properties.getProperty("username"));
		logger.info(properties.getProperty("password"));

		dbURL = properties.getProperty("URL") + "/"
				+ properties.getProperty("DBName");
	}
	
	public static synchronized ClientDatabase getInstance() {
		if (null == databasePool)
			try {
				databasePool = new DatabasePoolImplPostgres();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		
		return databasePool;	
	}

	@Override
	public synchronized Connection getConnection() throws ConnectionPoolException {
		Connection conn = null;
		
		if (0 < freeConnection.size()) {
			conn = freeConnection.peek();
			
			try {
				if (conn.isClosed()) {
					conn = getConnection();
				}
			} catch (SQLException e) {
				conn = getConnection();
			}
			
		} else {
			conn = newConnection();
				
			freeConnection.add(conn);
		}
		
		return conn;
	}

	private Connection newConnection() throws ConnectionPoolException {
		try {
			return DriverManager.getConnection(dbURL, properties.getProperty("username"), properties.getProperty("password"));
		} catch (SQLException e) {
			throw new ConnectionPoolException("Unable to create connection");
		}
	}
}
