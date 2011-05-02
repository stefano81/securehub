package it.uninsubria.dicom.cryptosocial;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

@Path("cryptosocial/")
public class CryptoSocial {
	private final Properties	properties;
	private final Properties	connectionProperties;
	private final String		dbURL;
	
	private final HVEIP08Parameters	parameters;
	private final HVEIP08KeyPairGenerator	keyPairGenerator;
	

	public CryptoSocial() throws IOException, ClassNotFoundException {

		properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream(
				"it/uninsubria/dicom/cryptosocial/config.properties"));

		Class.forName(properties.getProperty("driver"));

		connectionProperties = new Properties();
		connectionProperties.put("user", properties.get("username"));
		connectionProperties.put("password", properties.get("password"));
		connectionProperties.put("ssl", true);

		dbURL = properties.getProperty("URL") + "/"
				+ properties.getProperty("DBName");
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(properties.getProperty("parametersPath"));
		
		if (null == is) {
			// generate parameters
			CurveParams curveParams = new CurveParams();
			curveParams.load(this.getClass().getClassLoader().getResourceAsStream(properties.getProperty("curvePath")));
		
			HVEIP08ParametersGenerator generator = new HVEIP08ParametersGenerator();
			generator.init(curveParams, Integer.parseInt(properties.getProperty("length")));
		
			parameters = generator.generateParameters();
			
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(properties.getProperty("parametersPath")));
			oos.writeObject(parameters);
			
		} else {
			ObjectInputStream ois = new ObjectInputStream(is);
			
			parameters = (HVEIP08Parameters) ois.readObject();
		}
		
		keyPairGenerator = new HVEIP08KeyPairGenerator();
		keyPairGenerator.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), parameters));
	}

	@POST
	@Path("user/{uid}")
	@Consumes("multipart/form-data")
	public void registerUser(@PathParam("uid") String uid, Map<String, String> userData) {
		final String query ="INSERT INTO users (uid, public, private) VALUE (?, ?, ?)";
		
		// generate user keys
		AsymmetricCipherKeyPair keys = keyPairGenerator.generateKeyPair();
		
		try {
			ByteArrayInputStream baisPrivate = convertKeysToInputStream(keys.getPrivate());
			ByteArrayInputStream baisPublic = convertKeysToInputStream(keys.getPublic());
			
			Connection connection = connectToDB();
			
			PreparedStatement ps = connection.prepareStatement(query);
			
			ps.setString(1, uid);
			ps.setBinaryStream(2, baisPublic, baisPublic.available());
			ps.setBinaryStream(3, baisPrivate, baisPrivate.available());
			
			if (ps.execute()) {
				// TODO
			} else {
				// TODO
			}
		} catch (SQLException e) {
			e.printStackTrace(); // TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ByteArrayInputStream convertKeysToInputStream(CipherParameters cipherParameters) throws IOException {
		ByteArrayOutputStream baosPrivate = new ByteArrayOutputStream();
		ObjectOutputStream oosPrivate = new ObjectOutputStream(baosPrivate);
		oosPrivate.writeObject(cipherParameters);
		oosPrivate.close();
		ByteArrayInputStream baisPrivate = new ByteArrayInputStream(baosPrivate.toByteArray());
		return baisPrivate;
	}
	
	@POST
	@Path("user/{uid}/{friend}")
	public void addFriend(@PathParam("uid") String uid, @PathParam("friend") String friend) {
		// TODO
		// check if registered
		// recover key for uid
		// generate keys for friend
		// store keys
	}
	
	@GET
	@Path("resource/{uid}/{rid}")
	public byte[] retrieveResource(@PathParam("rid") Integer resourceId, @PathParam("uid") String userId) {
		final String queryListKeys = "SELECT key FROM keys WHERE owner='" + userId + "'";
		final String queryGetResource = "SELECT resource, privatekey FROM resources WHERE id=" + resourceId;
		
		try {
			Connection connection = connectToDB();
			ByteArrayInputStream bis = null;
			
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(queryGetResource);
			
			if (rs.next()) {
				// get encrypted resource
				// get encrypted SK
				
				statement = connection.createStatement();
				rs = statement.executeQuery(queryListKeys);
				
				boolean decrypted = false;
				while (rs.next()) {
					// extract key
					// try key
					if (false)
						break;
				}

				if (decrypted) {
					// send resource back
				}
			}
			
			connection.close();
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
		}
		
		return null; // TMCH
	}
	
	@GET
	@Path("search/{resourceName}")
	@Produces("application/json")
	public String searchResources(@PathParam("resourceName") String name) {
		final String query = "SELECT * FROM resources WHERE name LIKE '%"
				+ name + "%'";

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
		// TODO
	}

	private Connection connectToDB() {
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(dbURL,
					connectionProperties);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return connection;
	}
}
