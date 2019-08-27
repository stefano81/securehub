package it.uninsubria.dicom.cryptosocial.shared;

import org.bouncycastle.crypto.CipherParameters;

public interface CryptoInterface {

    public CipherParameters keyGeneration(CipherParameters keyPair, int[] policy);

    public CipherParameters keyDelegation(CipherParameters publicKey, CipherParameters secretKey, int depth);

    public Resource encrypt(CipherParameters publicKey, int[] policy, byte[] resource);

    public byte[] decrypt(CipherParameters secretKey, Resource resource);

}
