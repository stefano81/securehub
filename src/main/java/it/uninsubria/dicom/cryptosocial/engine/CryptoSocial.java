package it.uninsubria.dicom.cryptosocial.engine;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public interface CryptoSocial {

	SocialParam init(int l) throws NoSuchAlgorithmException,
			NoSuchProviderException;

}
