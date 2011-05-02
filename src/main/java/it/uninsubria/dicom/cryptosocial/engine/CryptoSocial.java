package it.uninsubria.dicom.cryptosocial.engine;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public interface CryptoSocial {

	SocialParam init(int l) throws NoSuchAlgorithmException, NoSuchProviderException;

	SocialParam init(SocialParam socialParam) throws NoSuchAlgorithmException, NoSuchProviderException;

	EncryptedResource encrypt(SocialParam sp, int[] x, byte[] resource) throws InvalidKeyException, IOException;

	byte[] decrypt(EncryptedResource resource, SocialParam sp) throws Exception;

	SocialParam delegate(SocialParam masterKeys, int[] y);

	SocialParam nextStep(SocialParam keys);

}
