package it.uninsubria.dicom.cryptosocial.client;


import java.util.Iterator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import it.uninsubria.dicom.cryptosocial.shared.EncryptedResource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;
import it.uninsubria.dicom.cryptosocial.shared.User;

public interface ClientDatabase {
	public void addUser(User user);
	public void updateKeys(String uid, AsymmetricCipherKeyPair keys);
	public String getAccessToken(String uid);
	public boolean existsUser(String id);
	public void insertFriendship(String uid, String id);
	public EncryptedResource getResource(int resourceId);
	public Iterator<CipherParameters> enumerateUserKeys(String uid);
	public Iterator<ResourceID> searchResources(String name);
	public CipherParameters getPublicKey(String uid);
	public ResourceID insertResource(String uid, String name, byte[] byteArray, byte[] encryptedSymmetricKeyBytes);
	public Iterator<String> getUserFriends(String uid);
	public boolean insertKey(String receiver, CipherParameters searchKey);
	public CipherParameters getUserPrivateKey(String emitter);
}
