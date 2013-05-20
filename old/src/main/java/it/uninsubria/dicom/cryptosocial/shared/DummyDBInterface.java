package it.uninsubria.dicom.cryptosocial.shared;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.crypto.CipherParameters;

import it.uninsubria.dicom.cryptosocial.client.ClientDatabase;
import it.uninsubria.dicom.cryptosocial.client.UserNotFoundException;
import it.uninsubria.dicom.cryptosocial.server.ServerDatabase;

public class DummyDBInterface implements ClientDatabase, ServerDatabase {
	
	private static final Resource res = new EncryptedResource(null, null);
	private static final ResourceID rid = new ResourceID(1233456789L);

	@Override
	public Resource getResource(ResourceID id) {
		return res;
	}

	@Override
	public Iterator<ResourceID> searchResources(String name) {
		return new LinkedList<ResourceID>(){
			private static final long serialVersionUID = 6281519333048827853L;
		{
			add(rid);
		}}.iterator();
	}

	@Override
	public ResourceID insertResource(String uid, String name, Resource resource) {
		return rid;
	}

	@Override
	public void updateKeys(String uid, CipherParameters keys) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isUserRegistered(String id) {
		return false;
	}

	@Override
	public void insertFriendship(String uid, String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<CipherParameters> enumerateUserKeys(String uid) throws UserNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CipherParameters getPublicKey(String uid) throws UserNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<String> getUserFriends(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int insertKey(String receiver, String emitter,
			CipherParameters searchKey) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CipherParameters getUserPrivateKey(String emitter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getFriendsList(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addUser(String uid) {
		// TODO Auto-generated method stub
		
	}

	public static DummyDBInterface getClientInstance() {
		return new DummyDBInterface();
	}

}
