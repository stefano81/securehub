package it.uninsubria.dicom.cryptosocial.shared;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

public interface CryptoInterface {
	public abstract AsymmetricCipherKeyPair generateKeyPair();
	
	public abstract Resource encrypt(byte[] resource, int[] policy, CipherParameters publicKey);
	public abstract boolean testKey(Resource resource, CipherParameters key);
	public abstract byte[] decryptResource(Resource resource, CipherParameters key);
	
	public abstract CipherParameters generateSearchKey(CipherParameters privateKey, int ... policy);
	public abstract CipherParameters delegate(CipherParameters oldSearch, int depth);
}
