package it.uninsubria.dicom.cryptosocial.shared;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

public interface CryptoInterface {
	public abstract AsymmetricCipherKeyPair generateKeyPair();

	public abstract Resource encrypt(byte[] resource, int[] policy,
			CipherParameters publicKey);

	public abstract boolean testKey(Resource resource, CipherParameters key);

	public abstract byte[] decryptResource(Resource resource,
			CipherParameters key);

	public abstract CipherParameters generateSearchKey(
			CipherParameters privateKey, int... policy);

	public abstract CipherParameters delegate(CipherParameters oldSearch,
			int depth);

	public CipherParameters keyGeneration(CipherParameters masterSecretKey,
			int[] policy);

	public CipherParameters keyDelegation(CipherParameters publicKey,
			CipherParameters secretKey, int depth);

	public Resource encrypt(CipherParameters publicKey, int[] policy,
			byte[] resource);

	public byte[] decrypt(CipherParameters secretKey, Resource resource);

}
