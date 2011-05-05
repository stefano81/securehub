package it.uninsubria.dicom.cryptosocial;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

@SuppressWarnings("serial")
public class CryptoSocial extends HttpServlet {
	private final Properties	properties;
	private final String		dbURL;
	
	private final HVEIP08Parameters	parameters;
	private final HVEIP08KeyPairGenerator	keyPairGenerator;
	
	private final Logger logger = Logger.getLogger(CryptoSocial.class.toString());

	public CryptoSocial() throws IOException, ClassNotFoundException {
		logger.log(Level.INFO,"init");

		properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream("it/uninsubria/dicom/cryptosocial/config.properties"));

		Class.forName(properties.getProperty("driver"));
		
		logger.info(properties.getProperty("username"));
		logger.info(properties.getProperty("password"));

		dbURL = properties.getProperty("URL") + "/"
				+ properties.getProperty("DBName");
		
		URL url = this.getClass().getClassLoader().getResource(properties.getProperty("parametersPath"));
		//InputStream is = 
		
		if (null == url) {
			logger.log(Level.INFO, "not existing");
			
			// generate parameters
			CurveParams curveParams = new CurveParams();
			curveParams.load(this.getClass().getClassLoader().getResourceAsStream(properties.getProperty("curvePath")));
		
			HVEIP08ParametersGenerator generator = new HVEIP08ParametersGenerator();
			generator.init(curveParams, Integer.parseInt(properties.getProperty("length")));
		
			parameters = generator.generateParameters();
			
			/* File parameterFile = new File( + properties.getProperty("parametersPath"));
			
			logger.info(parameterFile.getAbsoluteFile());
			
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(parameterFile));
			oos.writeObject(parameters);*/
			
		} else {
			logger.log(Level.INFO, "existing");
			
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(url.getFile()));
			
			parameters = (HVEIP08Parameters) ois.readObject();
		}
		
		keyPairGenerator = new HVEIP08KeyPairGenerator();
		keyPairGenerator.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), parameters));
	}

	@GET
	@Path("user/{uid}")
	public void registerUser(@PathParam("uid") String uid) {
		final String query ="INSERT INTO users (uid, public_key, private_key) VALUES (?, ?, ?)";
		
		logger.log(Level.INFO, "user/"+uid);
		
		// generate user keys
		AsymmetricCipherKeyPair keys = keyPairGenerator.generateKeyPair();
		Connection connection = null;
		
		try {
			connection = connectToDB();
			
			PreparedStatement ps = connection.prepareStatement(query);
			
			ps.setString(1, uid);
			ps.setBytes(2, convertKeysToBytes(keys.getPublic()));
			ps.setBytes(3, convertKeysToBytes(keys.getPrivate()));
			
			logger.log(Level.INFO, ps.toString());
			
			ps.executeUpdate();
			
			
			
			FacebookClient fbClient = new DefaultFacebookClient(properties.getProperty("token"));
			
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace(); // TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private byte[] convertKeysToBytes(CipherParameters cipherParameters) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		oos.writeObject(cipherParameters);
		oos.close();

		return baos.toByteArray();
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
	@Consumes("multipart/form-data")
	public void publishResource(@PathParam("uid") String uid, Map<String, String> data) {
		// TODO
	}

	private Connection connectToDB() {
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(dbURL, properties.getProperty("username"), properties.getProperty("password"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return connection;
	}

	@Override
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(arg0, arg1);
	}

	@Override
	protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(arg0, arg1);
	}

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		super.init(arg0);
	}
}
