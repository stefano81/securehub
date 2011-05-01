package it.uninsubria.dicom.cryptosocial.engine;

import java.security.Key;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

public interface SocialParam {

	Key getSymmetricKey();

	AsymmetricCipherKeyPair getAsymmetricKeyPair();

}
