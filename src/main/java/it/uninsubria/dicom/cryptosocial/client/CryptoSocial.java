package it.uninsubria.dicom.cryptosocial.client;

import it.uninsubria.dicom.cryptosocial.server.DatabasePoolImplPostgres;
import it.uninsubria.dicom.cryptosocial.shared.ClientDatabase;
import it.uninsubria.dicom.cryptosocial.shared.CommonProperties;
import it.uninsubria.dicom.cryptosocial.shared.ConnectionPoolException;
import it.unisa.dia.gas.crypto.engines.MultiBlockAsymmetricBlockCipher;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HHVEIP08AttributesEngine;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HHVEIP08Engine;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08EncryptionParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PublicKeyParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;

@Path("cryptosocial/")
public class CryptoSocial {
	private CommonProperties				properties;
	
	private HVEIP08KeyPairGenerator	keyPairGenerator;
	
	private KeyGenerator				symmetricKeyGenerator;
	
	private ClientDatabase			database;
	private KeyGeneration				keyGeneration;

	private final Logger				logger	= Logger.getLogger(CryptoSocial.class.toString());

	protected CryptoSocial(ClientDatabase database, KeyGenerator symmetricKeyGenerator, KeyGeneration keyGeneration, HVEIP08KeyPairGenerator keyPairGenerator) {
		init(database, symmetricKeyGenerator, keyPairGenerator, keyGeneration);
	}
	
	public CryptoSocial() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
		logger.info("init");
		
		properties = CommonProperties.getInstance();

		ClientDatabase database = DatabasePoolImplPostgres.getInstance();
		KeyGeneration keyGeneration = KeyGenerationImpl.getInstance();

		
		
		KeyGenerator symmetricKeyGenerator = KeyGenerator.getInstance(properties.getSymmetricAlgorithm());
		symmetricKeyGenerator.init(properties.getKeySize());
		
		URL url = properties.getParametersPath();
		
		HVEIP08Parameters parameters = null;
		
		if (null == url) {
			logger.info("not existing");

			// generate parameters
			CurveParams curveParams = new CurveParams();
			curveParams.load(properties.getCurveParams());

			HVEIP08ParametersGenerator generator = new HVEIP08ParametersGenerator();
			generator.init(curveParams, properties.getLength());

			parameters = generator.generateParameters();

			File parameterFile = new File(new File(this.getClass().getClassLoader().getResource(
					"/").getFile() + "../").getAbsolutePath() + properties.getParametersPath().getFile());

			logger.info(parameterFile.getAbsolutePath());

			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(parameterFile));
			oos.writeObject(parameters);

		} else {
			logger.info("existing");

			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(url.getFile()));

			parameters = (HVEIP08Parameters) ois.readObject();
		}

		HVEIP08KeyPairGenerator keyPairGenerator = new HVEIP08KeyPairGenerator();
		keyPairGenerator.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), parameters));
		
		init(database, symmetricKeyGenerator, keyPairGenerator, keyGeneration);
	}

	private void init(ClientDatabase database, KeyGenerator symmetricKeyGenerator, HVEIP08KeyPairGenerator keyPairGenerator, KeyGeneration keyGeneration) {
		this.database = database;
		this.symmetricKeyGenerator = symmetricKeyGenerator;
		this.keyGeneration = keyGeneration;
		this.keyPairGenerator = keyPairGenerator;
	}

	@GET
	@Path("user/{uid}")
	public void registerUser(@PathParam("uid") String uid) {
		final String updateKeysQuery = "UPDATE users SET public_key = ?, private_key = ? WHERE uid = ?";
		final String getTokenQuery = "SELECT access_token FROM users WHERE uid = ?";
		final String checkUserQuery = "SELECT uid FROM users where uid = ?";
		final String insertFrienshipQeuery = "INSERT INTO friendships (user1, user2) VALUES (?, ?)";

		logger.info("user/" + uid);

		// generate user keys
		AsymmetricCipherKeyPair keys = keyPairGenerator.generateKeyPair();
		Connection connection = null;

		try {
			connection = database.getConnection();

			PreparedStatement updateKeysStatement = connection.prepareStatement(updateKeysQuery);

			updateKeysStatement.setBytes(1,
					convertKeysToBytes(keys.getPublic()));
			updateKeysStatement.setBytes(2,
					convertKeysToBytes(keys.getPrivate()));
			updateKeysStatement.setString(3, uid);

			logger.info(updateKeysStatement.toString());

			updateKeysStatement.executeUpdate();

			PreparedStatement getTokenStatement = connection.prepareStatement(getTokenQuery);

			getTokenStatement.setString(1, uid);

			ResultSet getTokenRS = getTokenStatement.executeQuery();

			if (getTokenRS.next()) {
				FacebookClient fbClient = new DefaultFacebookClient(getTokenRS.getString("access_token"));

				List<User> userData = fbClient.fetchConnection("me/friends",
						User.class).getData();

				PreparedStatement checkUserStatement = connection.prepareStatement(checkUserQuery);
				PreparedStatement insertFriendshipStatement = connection.prepareStatement(insertFrienshipQeuery);

				for (User friend : userData) {
					checkUserStatement.setString(1, friend.getId());

					if (checkUserStatement.executeQuery().next()) {
						// insert relationship (u1, u2)
						insertFriendshipStatement.setString(1, uid);
						insertFriendshipStatement.setString(2, friend.getId());

						insertFriendshipStatement.executeUpdate();

						// insert relationship (u2, u1)
						insertFriendshipStatement.setString(1, friend.getId());
						insertFriendshipStatement.setString(2, uid);

						insertFriendshipStatement.executeUpdate();
						
						// insert u2, u1 for key propagation
						keyGeneration.propagate(friend.getId(), uid);
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace(); // TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static byte[] convertKeysToBytes(CipherParameters cipherParameters) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(cipherParameters);
		oos.close();

		return baos.toByteArray();
	}

	@GET
	@Path("resource/get/{rid}")
	@Produces("application/octet-stream")
	public byte[] retrieveResource(@PathParam("rid") Integer resourceId, @QueryParam("uid") String userId) {
		final String listKeysQuery = "SELECT key FROM keys WHERE owner = ?";
		final String getResourceQuery = "SELECT resource, privatekey FROM resources WHERE id = ?";

		try {
			Connection connection = database.getConnection();
			PreparedStatement getResourceStatement = connection.prepareStatement(getResourceQuery);
			
			getResourceStatement.setInt(1, resourceId);
			ResultSet getResourceRS = getResourceStatement.executeQuery();

			if (getResourceRS.next()) {
				// keys
				PreparedStatement listKeysStatement = connection.prepareStatement(listKeysQuery);
				listKeysStatement.setString(1, userId);
				
				ResultSet listKeysRS = listKeysStatement.executeQuery();

				boolean decrypted = false;
				
				CipherParameters searchKey = null;
				
				while (listKeysRS.next()) {
					// extract key
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(listKeysRS.getBytes("key")));
					searchKey = (CipherParameters) ois.readObject();
					
					// try key
					if (decrypted = testSearchKey(getResourceRS.getBytes("privatekey"), searchKey)) {
						break;
					}
				}
				
				if (decrypted) {
					//ObjectInputStream symmetricKeyOis = new ObjectInputStream(getResourceRS.getBinaryStream("privatekey"));
					//SecretKey symmetricKey = (SecretKey) symmetricKeyOis.readObject();
					
					SecretKey symmetricKey = decryptSymmetricKey(getResourceRS.getBytes("privatekey"), searchKey);
					Cipher cipher = Cipher.getInstance(properties.getSymmetricAlgorithm());
					cipher.init(Cipher.DECRYPT_MODE, symmetricKey);
					
					ByteArrayOutputStream resource = new ByteArrayOutputStream();
					CipherOutputStream cOut = new CipherOutputStream(resource, cipher);
					
					cOut.write(getResourceRS.getBytes("resource"));
					cOut.close();
					
					return resource.toByteArray();
				}
			}
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null; // TMCH
	}

	private SecretKey decryptSymmetricKey(byte[] bytes, CipherParameters privateKey) {
		byte[] plainText = null;
		
		try {
			AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(
					new HHVEIP08Engine(),
					new ZeroBytePadding());
			engine.init(false, privateKey);
			
			plainText = engine.processBlock(bytes, 0, bytes.length);
		
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(plainText));	
	
			return (SecretKey) ois.readObject();
		} catch (InvalidCipherTextException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null; // TMCH
	}

	private boolean testSearchKey(byte[] ct, CipherParameters searchKey) {
		HHVEIP08AttributesEngine engine = new HHVEIP08AttributesEngine();
        engine.init(false, searchKey);

        return engine.processBlock(ct, 0, ct.length)[0] == 0;
	}
	

	@GET
	@Path("resource/search")
	@Produces("application/json")
	public String searchResources(@QueryParam("query") String name) {
		final String query = "SELECT id,name FROM resources WHERE name LIKE '%" + name + "%'";

		Connection connection = null;

		StringBuffer resources = new StringBuffer("[\n");
		
		try {
			connection = database.getConnection();

			Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) {
				resources.append("\t{\n");
				resources.append("\t\"id\": \"");
				resources.append(rs.getInt("id"));
				resources.append("\",\n\t\"name\": \"");
				resources.append(rs.getString("name"));
				resources.append("\"\n\t}");
			}
			while (rs.next()) {
				resources.append(",\n\t{\n");
				resources.append("\t\"id\": \"");
				resources.append(rs.getInt("id"));
				resources.append("\",\n\t\"name\": \"");
				resources.append(rs.getString("name"));
				resources.append("\"\n\t}");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resources.append("\n]\n");
		
		return resources.toString();
	}

	@POST
	@Path("resource/upload")
	public void publishResource(@Context HttpServletRequest req) throws Exception {
		final String insertResourceQuery = "INSERT INTO resources (resource, privatekey, name, owner) VALUES (?, ?, ?, ?)";
		final String ownerKeysQuery = "SELECT private_key, public_key FROM users WHERE uid = ?";
		final String ownerFriendQuery = "SELECT user2 FROM friendships WHERE user1 = ?"; 
		
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		@SuppressWarnings("unchecked")
		List<FileItem> items = (List<FileItem>) upload.parseRequest(req);
		
		String name = null;
		ByteArrayOutputStream resource = new ByteArrayOutputStream();

		String uid = null;
		int[] policy = null; // TMCH
		
		for (FileItem item : items) {
			if (item.isFormField()) {
				if (item.getFieldName().equals("uid")) {
					// get user id
					uid = item.getString();
				} else if (item.getFieldName().equals("policy")) {
					policy = parsePolicy(item.getString());
				}
			} else {
				int c;
				InputStream is = item.getInputStream();
				
				while (-1 != (c = is.read())) {
					resource.write((byte) c);
				}
				
				name = item.getName();
			}
		}
		
		if (uid != null) {
			Connection connection = database.getConnection();
			
			PreparedStatement ownerKeysStatement = connection.prepareStatement(ownerKeysQuery);
			ResultSet ownerKeysRS = ownerKeysStatement.executeQuery();
			
			if (ownerKeysRS.next()) {
				SecretKey symmetricKey = symmetricKeyGenerator.generateKey();
				Cipher cipher = Cipher.getInstance(properties.getSymmetricAlgorithm());
				cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
				
				ByteArrayOutputStream encryptedResource = new ByteArrayOutputStream();
				CipherOutputStream cOut = new CipherOutputStream(encryptedResource, cipher);
				
				cOut.write(resource.toByteArray());
				cOut.close();
				
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ownerKeysRS.getBytes("public_key")));
				
				CipherParameters publicKey = (CipherParameters) ois.readObject();
				
				byte[] encryptedSymmetricKeyBytes = encryptSymmetricKey(convertKeysToBytes(symmetricKey), publicKey, policy);
				
				PreparedStatement insertResourceStatement = connection.prepareStatement(insertResourceQuery);
				
				insertResourceStatement.setBytes(1, encryptedResource.toByteArray());
				insertResourceStatement.setBytes(2, encryptedSymmetricKeyBytes);
				insertResourceStatement.setString(3, name);
				insertResourceStatement.setString(4, uid);
				
				if (1 == insertResourceStatement.executeUpdate()) {
					ResultSet resourceID = insertResourceStatement.getGeneratedKeys();
					PreparedStatement ownerFriendsStatement = connection.prepareStatement(ownerFriendQuery);
					
					ownerFriendsStatement.setString(1, uid);
					
					ResultSet ownerFriendsRS = ownerFriendsStatement.executeQuery();
					
					if (resourceID.next()) {
						while (ownerFriendsRS.next()) {
							keyGeneration.generate(uid, ownerFriendsRS.getString("user2"), policy);
						}
					}
				}
			} else {
				throw new WebApplicationException();
			}
		} else {
			throw new WebApplicationException();
		}
	}

	private byte[] encryptSymmetricKey(byte[] symmetricKeyBytes, CipherParameters publicKey, int[] policy) {
        byte[] ciphertext = null;
        
        try {
            AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(
                    new HHVEIP08Engine(),
                    new ZeroBytePadding()
            );
            
            engine.init(true, new HVEIP08EncryptionParameters((HVEIP08PublicKeyParameters) publicKey, policy));
            ciphertext = engine.processBlock(symmetricKeyBytes, 0, symmetricKeyBytes.length);

        } catch (InvalidCipherTextException e) {
        	// TMCH
            e.printStackTrace();
        }
        
        return ciphertext;
	}

	private int[] parsePolicy(String string) {
		int[] policy = new int[properties.getLength()];
		
		int i = 0;
		
		String[] rules = string.split("\\s");
		
		for (; i < policy.length && i < rules.length; i++) {
			policy[i] = Integer.parseInt(rules[i]);
		}
		
		if (i < policy.length) {
			for (; i < policy.length; i++)
				policy[i] = 0;
		}
		
		return policy;
	}

	private byte[] convertKeysToBytes(SecretKey symmetricKey) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(symmetricKey);
		oos.close();

		return baos.toByteArray();
	}
}