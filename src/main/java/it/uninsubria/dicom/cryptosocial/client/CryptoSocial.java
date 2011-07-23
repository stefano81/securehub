package it.uninsubria.dicom.cryptosocial.client;

import it.uninsubria.dicom.cryptosocial.server.ResourceRepository;
import it.uninsubria.dicom.cryptosocial.server.ResourceStorerFB;
import it.uninsubria.dicom.cryptosocial.shared.CommonProperties;
import it.uninsubria.dicom.cryptosocial.shared.CryptoInterface;
import it.uninsubria.dicom.cryptosocial.shared.MySQLDatabase;
import it.uninsubria.dicom.cryptosocial.shared.PostgresDatabase;
import it.uninsubria.dicom.cryptosocial.shared.Resource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;
import it.uninsubria.dicom.cryptosocial.shared.dummy.DummyCipherParameters;
import it.uninsubria.dicom.cryptosocial.shared.dummy.DummyCryptoInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@Path("cryptosocial/")
public class CryptoSocial {
	private ClientProperties		properties;

	private CryptoInterface			cryptoInterface;

	private ClientDatabase			database;
	private KeyGeneration			keyGeneration;

	private ResourceRepository	repository;
	
	private final Logger			logger	= Logger.getLogger(CryptoSocial.class); 

	public CryptoSocial() throws NoSuchAlgorithmException, FileNotFoundException, IOException, ClassNotFoundException {
		this(CommonProperties.getInstance(),
				//PostgresDatabase.getClientInstance(),
				MySQLDatabase.getClientInstance(),
				 //CryptoInterfaceFB.getInstance(),
				//new DBRACryptoInterface(),
				new DummyCryptoInterface(),
				 KeyGenerationImpl.getInstance(PostgresDatabase.getClientInstance(), new DummyCryptoInterface()),
				 ResourceStorerFB.getInstance());
	}
	
	protected CryptoSocial(ClientProperties properties, ClientDatabase database, CryptoInterface criptoInterface, KeyGeneration keyGeneration, ResourceRepository repository) {
		this.properties = properties;
		this.database = database;
		this.cryptoInterface = criptoInterface;
		this.keyGeneration = keyGeneration;
		this.repository = repository;
	}

	@GET
	@Path("user/{uid}")
	public void registerUser(@PathParam("uid") String uid) {
		logger.debug("user/" + uid);

		// generate user keys
		//DBRAKeyPairParameters keys = DBRASetup.setup(properties.getLength(), properties.getCurveParamsLocation(), properties.getLength(), properties.getLength()); //cryptoInterface.generateKeyPair();
		CipherParameters keys = new DummyCipherParameters(uid); 
		
		logger.debug("Key pair generated");
		
		database.updateKeys(uid, keys);
		
		logger.debug("Key pair updated");
	
		database.addUser(uid);
		
		logger.debug("Added users");
		
		List<String> friends = database.getFriendsList(uid);
		
		logger.debug("Retrieved friends list: size == " + friends.size());

		for (String friend : friends) {
			logger.debug("Processing: " + friend);
			
			if (database.isUserRegistered(friend)) {
				// insert relationship (u1, u2)
				database.insertFriendship(uid, friend);
				
				// insert relationship (u2, u1)
				database.insertFriendship(friend, uid);

				// insert u2, u1 for key propagation
				keyGeneration.propagate(friend, uid);
			} else {
				logger.debug("The user " + uid + " is not registered in the application");
			}
		}
	}

	@GET
	@Path("resource/{rid}")
	@Produces("application/octet-stream")
	public byte[] retrieveResource(@PathParam("rid") Integer resourceId, @QueryParam("uid") String uid) {

		Resource resource = (Resource) repository.getResource(new ResourceID(resourceId));
		
		if (null != resource) {
			// keys
			Iterator<CipherParameters> listKeys = database.enumerateUserKeys(uid);
			
			boolean decrypted = false;
			
			CipherParameters key = null;
			
			
			while (listKeys.hasNext()) {
				key = listKeys.next();
				
				/*if (cryptoInterface.testKey(resource, key)) {
					decrypted = true;
					break;
				}*/
				
			}
			
			if (decrypted && null != key)
				return cryptoInterface.decrypt(key, resource);
			
		}

		return null;
	}

	@GET
	@Path("resource")
	@Produces("application/json")
	public String searchResources(@QueryParam("query") String name) {

		StringBuffer resourcesJSON = new StringBuffer("[\n");
		logger.debug("Query: "  + name);

		Iterator<ResourceID> resources = repository.searchResources(name);
	
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
	@Path("resource")
	@Consumes("multipart/form-data")
	public void publishResource(@MultipartForm FileUploadForm form) {
		
		
	}
	/*public void publishResource(@Context HttpServletRequest req) throws Exception {

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		@SuppressWarnings("unchecked")
		List<FileItem> items = (List<FileItem>) upload.parseRequest(req);

		String name = null;
		ByteArrayOutputStream resource = new ByteArrayOutputStream();

		String uid = null;
		int[] policy = {1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1,
				1,1,1,1,1,1,1,1,1,1}; // TMCH && for debug

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
			logger.debug("Retrieving public key");
			
			CipherParameters publicKey = database.getPublicKey(uid);

			if (null != publicKey) {
				logger.debug("Public key not null");
				
				Resource res = cryptoInterface.encrypt(publicKey, policy, resource.toByteArray());
				
				logger.debug("Inserting");
				ResourceID rid = repository.storeResource(uid, name, res);  
				
				logger.debug("Inserted resource id: " + rid.getID());
				
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
	}*/
	
	private int[] parsePolicy(String string) {
		int[] policy = new int[properties.getLength()];

		int i = 0;

		String[] rules = string.split(",");

		for (; i < policy.length && i < rules.length; i++) {
			try {
				policy[i] = Integer.parseInt(rules[i]);
			} catch(NumberFormatException e) {
				policy[i] = 0;
			}
		}

		if (i < policy.length) {
			for (; i < policy.length; i++)
				policy[i] = 0;
		}

		return policy;
	}
}