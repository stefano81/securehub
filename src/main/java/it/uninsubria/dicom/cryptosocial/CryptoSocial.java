package it.uninsubria.dicom.cryptosocial;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("cryptosocial/")
public class CryptoSocial {
	private final Properties properties;
	private final Properties connectionProperties;
	private final String dbURL;

	public CryptoSocial() throws IOException, ClassNotFoundException {

		properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream(
						"it/uninsubria/dicom/cryptosocial/config.properties"));

		Class.forName(properties.getProperty("driver"));
		
		connectionProperties = new Properties();
		connectionProperties.put("user", properties.get("username"));
		connectionProperties.put("password", properties.get("password"));
		connectionProperties.put("ssl", true);
		
		dbURL = properties.getProperty("URL") + "/" + properties.getProperty("DBName");
	}

	@GET
	@Path("search/{resourceName}")
	@Produces("application/json")
	public String searchResources(@PathParam("resourceName") String name) {
		final String query = "SELECT * FROM resources WHERE name LIKE '%" + name + "%'" ;
		
		Connection connection = connectToDB();
		
		StringBuffer resources = new StringBuffer("[\n");
		
		try {
			Statement statement = connection.createStatement();
			
			ResultSet rs = statement.executeQuery(query);
			
			while (rs.next()) {
				resources.append("\t{\n");
				resources.append("\t\"id\": \"");
				resources.append(rs.getInt("id"));
				resources.append("\"\n\t\"name\": \"");
				resources.append(rs.getInt("name"));
				resources.append("\"\n\t}\n");
			}
			
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		resources.append("]\n");
		
		return resources.toString();
	}

	@POST
	@Path("resource/{uid}")
	public void publishResource(@PathParam("uid") String uid,
			@PathParam("policy") String policy) {

	}
	
	private Connection connectToDB() {
		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection(dbURL, connectionProperties);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return connection;
	}
}
