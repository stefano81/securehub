package it.uninsubria.dicom.cryptosocial.client;

import it.uninsubria.dicom.cryptosocial.server.ResourceNotFoundException;
import it.uninsubria.dicom.cryptosocial.server.ResourceRepository;
import it.uninsubria.dicom.cryptosocial.server.ResourceStorerFB;
import it.uninsubria.dicom.cryptosocial.shared.CommonProperties;
import it.uninsubria.dicom.cryptosocial.shared.CryptoInterface;
import it.uninsubria.dicom.cryptosocial.shared.DBRAKeyPairParameters;
import it.uninsubria.dicom.cryptosocial.shared.DBRASetup;
import it.uninsubria.dicom.cryptosocial.shared.DummyDBInterface;
import it.uninsubria.dicom.cryptosocial.shared.MySQLDatabase;
import it.uninsubria.dicom.cryptosocial.shared.PostgresDatabase;
import it.uninsubria.dicom.cryptosocial.shared.Resource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;
import it.uninsubria.dicom.cryptosocial.shared.dummy.DummyCipherParameters;
import it.uninsubria.dicom.cryptosocial.shared.dummy.DummyCryptoInterface;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;

import com.sun.jersey.multipart.FormDataParam;

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
				//MySQLDatabase.getClientInstance(),
				DummyDBInterface.getClientInstance(),
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
	@Produces(MediaType.TEXT_PLAIN)
	public String registerUser(@PathParam("uid") String uid) {
		logger.debug("user/" + uid);

		// generate user keys
		DBRAKeyPairParameters keys = DBRASetup.setup(properties.getLength(), properties.getCurveParamsLocation(), properties.getLength(), properties.getLength());
		//CipherParameters keys = new DummyCipherParameters(uid); 
		
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
		
		return "OK!";
	}

	@GET
	@Path("resource/{rid}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] retrieveResource(@PathParam("rid") Long resourceId, @QueryParam("uid") String uid) {

		Resource resource = null;
		try {
			resource = (Resource) repository.getResource(new ResourceID(resourceId));
		} catch (ResourceNotFoundException e) {
			logger.info("Resource " + resourceId + " not found");
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		if (null != resource) {
			// keys
			Iterator<CipherParameters> listKeys;
			try {
				listKeys = database.enumerateUserKeys(uid);
			} catch (UserNotFoundException e1) {
				logger.info("User " + uid + " not found");
				throw new WebApplicationException(Status.BAD_REQUEST);
			}
			
			int i = 0;
			while (listKeys.hasNext()) {
				CipherParameters key = listKeys.next();
				
				try {
					return cryptoInterface.decrypt(key, resource);
				} catch (RuntimeException e) {
					logger.info("Key " + i++ + " failed");
				}
			}
			
		}

		throw new WebApplicationException(Status.FORBIDDEN);
	}

	@GET
	@Path("resource")
	@Produces(MediaType.APPLICATION_JSON)
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
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void publishResource(@FormDataParam("uid") String uid, @FormDataParam("policy") String policy, @FormDataParam("filename") String filename, @FormDataParam("filedata") InputStream filedata) {		
		
		if (null != uid) {
			logger.info("Retrieving public key for user " + uid);
			
			CipherParameters publicKey;
			try {
				publicKey = database.getPublicKey(uid);
			} catch (UserNotFoundException e) {
				logger.info("User not found: " + uid);
				throw new WebApplicationException(Status.BAD_REQUEST);
			}

			if (null != publicKey) {
				logger.debug("Public key not null");
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				try {
					int readBytes = -1;
					byte[] buffer = new byte[1<<10];
							
					while (-1 != (readBytes = filedata.read(buffer))) {
						baos.write(buffer, 0, readBytes);
					}
				} catch (IOException e) {
					logger.error("Error reading the uploaded file");
					throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
				}
				
				Resource res = cryptoInterface.encrypt(publicKey, parsePolicy(policy), baos.toByteArray());
				
				logger.debug("Inserting");
				ResourceID rid = repository.storeResource(uid, filename, res);  
				
				logger.debug("Inserted resource id: " + rid.getID());
				
				if (null != rid) {
					Iterator<String> friends = database.getUserFriends(uid);
					
					while (friends.hasNext()) {
						keyGeneration.generate(uid, friends.next(), parsePolicy(policy));
					}
				}
			} else {
				throw new WebApplicationException(Status.BAD_REQUEST);
			}
		} else {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
	}
	
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