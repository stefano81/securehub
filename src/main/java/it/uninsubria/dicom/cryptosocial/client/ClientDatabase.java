package it.uninsubria.dicom.cryptosocial.client;


import java.util.Iterator;
import java.util.List;

import org.bouncycastle.crypto.CipherParameters;

public interface ClientDatabase {
	public void updateKeys(String uid, CipherParameters keys);
	public boolean isUserRegistered(String id);
	public void insertFriendship(String uid, String id);
	public Iterator<CipherParameters> enumerateUserKeys(String uid);
	public CipherParameters getPublicKey(String uid);
	public Iterator<String> getUserFriends(String uid);
	public int insertKey(String receiver, String emitter, CipherParameters searchKey);
	public CipherParameters getUserPrivateKey(String emitter);
	public List<String> getFriendsList(String uid);
	public void addUser(String uid);
}
