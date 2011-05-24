package it.uninsubria.dicom.cryptosocial.shared;

import java.util.Iterator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

public interface CryptoInterface {
	public abstract Resource encrypt(byte[] resource, int[] policy, CipherParameters publicKey);
	public abstract AsymmetricCipherKeyPair generateKeyPair();
	public abstract byte[] decryptResource(Resource resource, Iterator<CipherParameters> listKeys);
}
