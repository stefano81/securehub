package it.uninsubria.dicom.cryptosocial.client;

import it.uninsubria.dicom.cryptosocial.shared.CommonProperties;
import it.uninsubria.dicom.cryptosocial.shared.EncryptedResource;
import it.uninsubria.dicom.cryptosocial.shared.PostgresDatabase;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
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
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.User;

@Path("cryptosocial/")
public class CryptoSocial {
	private ClientProperties		properties;

	private HVEIP08KeyPairGenerator	keyPairGenerator;

	private KeyGenerator			symmetricKeyGenerator;

	private ClientDatabase			database;
	private KeyGeneration			keyGeneration;

	private final Logger			logger	= Logger.getLogger(CryptoSocial.class.toString());

	protected CryptoSocial(ClientDatabase database, KeyGenerator symmetricKeyGenerator, KeyGeneration keyGeneration, HVEIP08KeyPairGenerator keyPairGenerator) {
		init(database, symmetricKeyGenerator, keyPairGenerator, keyGeneration);
	}

	public CryptoSocial() throws NoSuchAlgorithmException, FileNotFoundException, IOException, ClassNotFoundException {
		logger.info("init");

		properties = CommonProperties.getInstance();

		ClientDatabase database = PostgresDatabase.getClientInstance();
		KeyGeneration keyGeneration = new KeyGenerationImpl(database);

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
		logger.info("user/" + uid);

		// generate user keys
		AsymmetricCipherKeyPair keys = keyPairGenerator.generateKeyPair();
		
		database.updateKeys(uid, keys);
		
		String accessToken = database.getAccessToken(uid);
		
		FacebookClient fbClient = new DefaultFacebookClient(accessToken);

		List<User> userData = fbClient.fetchConnection("me/friends", User.class).getData();

		for (User friend : userData) {
			if (database.existsUser(friend.getId())) {
				// insert relationship (u1, u2)
				database.insertFriendship(uid, friend.getId());
				
				// insert relationship (u2, u1)
				database.insertFriendship(friend.getId(), uid);

				// insert u2, u1 for key propagation
				keyGeneration.propagate(friend.getId(), uid);
			}
		}
	}

	@GET
	@Path("resource/get/{rid}")
	@Produces("application/octet-stream")
	public byte[] retrieveResource(@PathParam("rid") Integer resourceId, @QueryParam("uid") String uid) {

		EncryptedResource encryptedResource = database.getResource(resourceId);
		
		if (null != encryptedResource) {
			// keys
			Iterator<CipherParameters> listKeys = database.enumerateUserKeys(uid);
			
			boolean decrypted = false;

			CipherParameters searchKey = null;

			while (listKeys.hasNext()) {
				// extract key
				searchKey = listKeys.next();

				// try key
				if (decrypted = testSearchKey(
						encryptedResource.getKey(), searchKey)) {
					break;
				}
			}

			if (decrypted) {
				SecretKey symmetricKey = decryptSymmetricKey(encryptedResource.getKey(), searchKey);
				
				Cipher cipher = null;
				try {
					cipher = Cipher.getInstance(properties.getSymmetricAlgorithm());
					
					cipher.init(Cipher.DECRYPT_MODE, symmetricKey);
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

				ByteArrayOutputStream resource = new ByteArrayOutputStream();
				CipherOutputStream cOut = new CipherOutputStream(resource, cipher);

				try {
					cOut.write(encryptedResource.getResource());
				
					cOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return resource.toByteArray();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private SecretKey decryptSymmetricKey(byte[] bytes, CipherParameters privateKey) {
		byte[] plainText = null;

		try {
			AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(new HHVEIP08Engine(), new ZeroBytePadding());
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

		StringBuffer resourcesJSON = new StringBuffer("[\n");

		Iterator<ResourceID> resources = database.searchResources(name);
	
		if (resources.hasNext()) {
			ResourceID resource = resources.next();
			
			resourcesJSON.append("\t{\n");
			resourcesJSON.append("\t\"id\": \"");
			resourcesJSON.append(resource.getID());
			resourcesJSON.append("\",\n\t\"name\": \"");
			resourcesJSON.append(resource.getName());
			resourcesJSON.append("\"\n\t}");
		}
		
		while (resources.hasNext()) {
			ResourceID resource = resources.next();
			
			resourcesJSON.append(",\n\t{\n");
			resourcesJSON.append("\t\"id\": \"");
			resourcesJSON.append(resource.getID());
			resourcesJSON.append("\",\n\t\"name\": \"");
			resourcesJSON.append(resource.getName());
			resourcesJSON.append("\"\n\t}");
		}
		
		resourcesJSON.append("\n]\n");

		return resourcesJSON.toString();
	}

	@POST
	@Path("resource/upload")
	public void publishResource(@Context HttpServletRequest req) throws Exception {

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

			CipherParameters publicKey = database.getPublicKey(uid);

			if (null != publicKey) {
				SecretKey symmetricKey = symmetricKeyGenerator.generateKey();
				Cipher cipher = Cipher.getInstance(properties.getSymmetricAlgorithm());
				cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);

				ByteArrayOutputStream encryptedResource = new ByteArrayOutputStream();
				CipherOutputStream cOut = new CipherOutputStream(encryptedResource, cipher);

				cOut.write(resource.toByteArray());
				cOut.close();

				byte[] encryptedSymmetricKeyBytes = encryptSymmetricKey(convertKeysToBytes(symmetricKey), publicKey, policy);

				ResourceID rid = database.insertResource(uid, name, encryptedResource.toByteArray(), encryptedSymmetricKeyBytes);  
				
				if (null != rid) {
					Iterator<String> friends = database.getUserFriends(uid);
					
					while (friends.hasNext()) {
						keyGeneration.generate(uid,	friends.next(), policy);
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
			AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(new HHVEIP08Engine(), new ZeroBytePadding());

			engine.init(
					true,
					new HVEIP08EncryptionParameters((HVEIP08PublicKeyParameters) publicKey, policy));
			ciphertext = engine.processBlock(symmetricKeyBytes, 0,
					symmetricKeyBytes.length);

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