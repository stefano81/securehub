package it.uninsubria.dicom.cryptosocial.shared;

import it.uninsubria.dicom.cryptosocial.client.ClientDatabase;
import it.uninsubria.dicom.cryptosocial.server.ServerDatabase;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PublicKeyParameters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

public class PostgresDatabase implements ServerDatabase, ClientDatabase {
	private static PostgresDatabase database;
	
	private ConnectionPool pool;
	
	// user registration
	private final String updateKeysQuery = "UPDATE users SET public_key = ?, private_key = ? WHERE uid = ?";
	private final String getTokenQuery = "SELECT access_token FROM users WHERE uid = ?";
	private final String checkUserQuery = "SELECT uid FROM users where uid = ?";
	private final String insertFrienshipQuery = "INSERT INTO friendships (user1, user2) VALUES (?, ?)";

	// retrieve resource
	private final String listKeysQuery = "SELECT key FROM keys WHERE owner = ?";
	private final String getResourceQuery = "SELECT resource, privatekey FROM resources WHERE id = ?";

	// search resource
	private final String searchResourceQuery = "SELECT id, name FROM resources WHERE name LIKE ?";

	// publish resource
	private final String insertResourceQuery = "INSERT INTO resources (resource, privatekey, name, owner) VALUES (?, ?, ?, ?)";
	private final String ownerKeysQuery = "SELECT private_key, public_key FROM users WHERE uid = ?";
	private final String ownerFriendQuery = "SELECT user2 FROM friendships WHERE user1 = ?";
	 
	// KeyGeneration
	private final String getUserKeyQuery = "SELECT private_key FROM users WHERE uid = ?";
	private final String insertKey = "INSERT INTO keys (owner, key) VALUES (?, ?)";

	private PostgresDatabase() {		
		pool = new DatabasePoolImplPostgres(CommonProperties.getInstance());
	}

	
	// SERVER
	
	public synchronized static ServerDatabase getServerInstance() {
		if (null == database)
			database = new PostgresDatabase();
		
		return database;
	}	

	// CLIENT
	
	public synchronized static ClientDatabase getClientInstance() {
		if (null == database)
			database = new PostgresDatabase();
		
		return database;
	}
	
	@Override
	public void addUser(User user) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateKeys(String uid, AsymmetricCipherKeyPair keys) {
		try {
			Connection connection = pool.getConnection();
		
			PreparedStatement updateKeysStatement = connection.prepareStatement(updateKeysQuery);
			
			updateKeysStatement.setBytes(1,	convertKeysToBytes(keys.getPublic()));
			updateKeysStatement.setBytes(2,	convertKeysToBytes(keys.getPrivate()));
			updateKeysStatement.setString(3, uid);
	
			updateKeysStatement.executeUpdate();
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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


	@Override
	public String getAccessToken(String uid) {
		String accessToken = null;
		
		try {
			Connection connection = pool.getConnection();
				
			PreparedStatement getTokenStatement = connection.prepareStatement(getTokenQuery);
			
			getTokenStatement.setString(1, uid);
			
			ResultSet rs = getTokenStatement.executeQuery();
			
			if (rs.next()) {
				accessToken = rs.getString("access_token");
			}
			
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return accessToken;
	}


	@Override
	public boolean existsUser(String uid) {
		try {
			Connection connection = pool.getConnection();
			
			PreparedStatement checkUserStatement = connection.prepareStatement(checkUserQuery);
			
			checkUserStatement.setString(1, uid);
			
			ResultSet rs = checkUserStatement.executeQuery();
			
			if (rs.next()) {
				return true;
			}
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void insertFriendship(String user1, String user2) {
		try {
			Connection connection = pool.getConnection();
			
			PreparedStatement insertFrienshipStatement = connection.prepareStatement(insertFrienshipQuery);
			
			insertFrienshipStatement.setString(1, user1);
			insertFrienshipStatement.setString(2, user2);
		
			if (1 == insertFrienshipStatement.executeUpdate()) {
				
			} else {
				
			}
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public Resource getResource(ResourceID id) {
		EncryptedResource resource = null;
		
		try {
			Connection connection = pool.getConnection();
			
			PreparedStatement getResourceStatement = connection.prepareStatement(getResourceQuery);
			
			getResourceStatement.setInt(1, id.getID());
			
			ResultSet rs = getResourceStatement.executeQuery();
			
			if (rs.next()) {
				resource = new EncryptedResource(rs.getBytes("resource"), rs.getBytes("privatekey"));
			}
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resource;
	}


	@Override
	public Iterator<CipherParameters> enumerateUserKeys(String uid) {
		List<CipherParameters> keys = new LinkedList<CipherParameters>();
		
		try {
			Connection connection = pool.getConnection();
			
			PreparedStatement listKeysStatement = connection.prepareStatement(listKeysQuery);
			
			listKeysStatement.setString(1, uid);

			ResultSet listKeysRS = listKeysStatement.executeQuery();
				
			
			while (listKeysRS.next()) {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(listKeysRS.getBytes("key")));
				keys.add((CipherParameters) ois.readObject());
			}
			
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return keys.iterator();
	}


	@Override
	public Iterator<ResourceID> searchResources(String name) {
		List<ResourceID> resources = new LinkedList<ResourceID>();
		
		try {
			Connection connection = pool.getConnection();

			PreparedStatement statement = connection.prepareStatement(searchResourceQuery);
			
			statement.setString(1, "%" + name + "%");
	
			ResultSet rs = statement.executeQuery();
	
			while (rs.next()) {
				resources.add(new ResourceID(rs.getString("name"), rs.getInt("id")));
			}
		} catch (ConnectionPoolException e) {
			// TODO
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resources.iterator();
	}


	@Override
	public CipherParameters getPublicKey(String uid) {
		CipherParameters publicKey = null;
		
		try {
			Connection connection = pool.getConnection();
			
			PreparedStatement ownerKeysStatement = connection.prepareStatement(ownerKeysQuery);
			ownerKeysStatement.setString(1, uid);
			ResultSet ownerKeysRS = ownerKeysStatement.executeQuery();
			
			if (ownerKeysRS.next()) {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ownerKeysRS.getBytes("public_key")));

				publicKey = (HVEIP08PublicKeyParameters) ois.readObject();
			}
		} catch (ConnectionPoolException e) {
			// TODO
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return publicKey;
	}


	@Override
	public ResourceID insertResource(String uid, String name, Resource resource) {
		ResourceID rid = null;
		
		try {
			Connection connection = pool.getConnection();
			
			PreparedStatement insertResourceStatement = connection.prepareStatement(insertResourceQuery);

			insertResourceStatement.setBytes(1, resource.getResource());
			insertResourceStatement.setBytes(2, resource.getKey());
			insertResourceStatement.setString(3, name);
			insertResourceStatement.setString(4, uid);
			
			if (1 == insertResourceStatement.executeUpdate()) {
				ResultSet resourceID = insertResourceStatement.getGeneratedKeys();
				
				if (resourceID.next()) {
					rid = new ResourceID(name, resourceID.getInt("id"));
				}
			}
		} catch (ConnectionPoolException e) {
			// TODO
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rid;
	}


	@Override
	public Iterator<String> getUserFriends(String uid) {
		List<String> friendIDs = new LinkedList<String>();
		
		
		try {
			Connection connection = pool.getConnection();
			PreparedStatement ownerFriendsStatement = connection.prepareStatement(ownerFriendQuery);
	
			ownerFriendsStatement.setString(1, uid);
	
			ResultSet ownerFriendsRS = ownerFriendsStatement.executeQuery();
	
			while (ownerFriendsRS.next()) {
				friendIDs.add(ownerFriendsRS.getString("user2"));
			}
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return friendIDs.iterator();
	}
	

	@Override
	public boolean insertKey(String receiver, CipherParameters searchKey) {
		try {
			Connection connection = pool.getConnection();
			PreparedStatement ps = connection.prepareStatement(insertKey);
		    
			ps.setString(1, receiver);
		    ps.setBytes(2, convertKeysToBytes(searchKey));
		    
			return 1 == ps.executeUpdate();
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public CipherParameters getUserPrivateKey(String emitter) {
		CipherParameters privateKey = null;
		
		try {
			Connection connection = pool.getConnection();

			PreparedStatement getUserKeyStatement = connection.prepareCall(getUserKeyQuery);
			getUserKeyStatement.setString(1, emitter);
			
			ResultSet getUserKeyRS = getUserKeyStatement.executeQuery();
			
			if (getUserKeyRS.next()) {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(getUserKeyRS.getBytes("private_key")));
				privateKey = (CipherParameters) ois.readObject();
			}
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return privateKey;
	}
}
