package it.uninsubria.dicom.cryptosocial.shared;

import java.util.Properties;

import it.uninsubria.dicom.cryptosocial.client.ClientDatabase;
import it.uninsubria.dicom.cryptosocial.server.ServerDatabase;

public class PostgresDatabase implements ServerDatabase, ClientDatabase {
	private static PostgresDatabase database;
	
	private CommonProperties properties;
	
	/*protected PostgresDatabase() {
		Class.forName();
	}*/

	private PostgresDatabase() {
		
	}

	public synchronized static ServerDatabase getServerInstance() {
		if (null == database)
			database = new PostgresDatabase();
		
		return database;
	}	
	
	public synchronized static ClientDatabase getClientInstance() {
		if (null == database)
			database = new PostgresDatabase();
		
		return database;
	}
}
