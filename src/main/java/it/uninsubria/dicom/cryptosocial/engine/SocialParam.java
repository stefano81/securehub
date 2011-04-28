package it.uninsubria.dicom.cryptosocial.engine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Key;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

@SuppressWarnings("serial")
public class SocialParam implements Serializable {
	private Key AESKey;
	private AsymmetricCipherKeyPair keyPair;
	
	
	public SocialParam() {		
	}
	
	public SocialParam(Key AESkey, AsymmetricCipherKeyPair keyPair) {
		this.AESKey= AESkey;	
		this.keyPair= keyPair;
	}
	
	public Key getAesKey() {
		return this.AESKey;
	}
	
	public AsymmetricCipherKeyPair getKeyPair() {
		return this.keyPair;
	}
	
	public CipherParameters getPrivateKey() {
		return this.keyPair.getPrivate();
	}
	
	public CipherParameters getPublicKey() {
		return this.keyPair.getPublic();
	}
	
	public void saveConf(String path) throws Exception {
		ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(path));
		out.writeObject(AESKey);
		out.close();	
	}
	
	public void loadConf(String path) throws Exception {
		ObjectInputStream in= new ObjectInputStream(new FileInputStream(path));
		AESKey= (Key) in.readObject();
		in.close();
	}

}
