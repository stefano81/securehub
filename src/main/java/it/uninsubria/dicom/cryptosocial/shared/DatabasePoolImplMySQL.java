package it.uninsubria.dicom.cryptosocial.shared;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class DatabasePoolImplMySQL implements ConnectionPool {

	private Queue<Connection> freeConnection;
	private DatabaseProperties properties;
	private String dbURL;

	public DatabasePoolImplMySQL(DatabaseProperties properties) {
		freeConnection = new LinkedList<Connection>();
		this.properties = properties;

		try {
			Class.forName(properties.getDriver());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dbURL = properties.getConnectionURL();
	}
	
	@Override
	public Connection getConnection() throws ConnectionPoolException {
		Connection conn = null;
		
		if (0 < freeConnection.size()) {
			conn = freeConnection.poll();
			
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
			return DriverManager.getConnection(dbURL, properties.getUsername(), properties.getPassword());
		} catch (SQLException e) {
			throw new ConnectionPoolException("Unable to create connection");
		}
	}

}
