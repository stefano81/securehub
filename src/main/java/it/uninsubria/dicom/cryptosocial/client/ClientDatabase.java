package it.uninsubria.dicom.cryptosocial.client;


import it.uninsubria.dicom.cryptosocial.shared.User;

import java.util.Iterator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

public interface ClientDatabase {
	public void addUser(User user);
	public void updateKeys(String uid, AsymmetricCipherKeyPair keys);
	public String getAccessToken(String uid);
	public boolean existsUser(String id);
	public void insertFriendship(String uid, String id);
	public Iterator<CipherParameters> enumerateUserKeys(String uid);
	public CipherParameters getPublicKey(String uid);
	public Iterator<String> getUserFriends(String uid);
	public boolean insertKey(String receiver, CipherParameters searchKey);
	public CipherParameters getUserPrivateKey(String emitter);
}
