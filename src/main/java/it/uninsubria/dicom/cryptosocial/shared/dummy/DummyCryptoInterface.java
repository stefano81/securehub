package it.uninsubria.dicom.cryptosocial.shared.dummy;

import org.bouncycastle.crypto.CipherParameters;

import it.uninsubria.dicom.cryptosocial.shared.CryptoInterface;
import it.uninsubria.dicom.cryptosocial.shared.EncryptedResource;
import it.uninsubria.dicom.cryptosocial.shared.Resource;

public class DummyCryptoInterface implements CryptoInterface {

	@Override
	public CipherParameters keyGeneration(CipherParameters keyPair, int[] policy) {
		if (keyPair instanceof DummyCipherParameters)
			return new DummyCipherParameters(((DummyCipherParameters) keyPair).getUserId());
		else
			return new DummyCipherParameters(DummyCipherParameters.DEFAULTUID);
	}

	@Override
	public CipherParameters keyDelegation(CipherParameters publicKey, CipherParameters secretKey, int depth) {
		if (publicKey instanceof DummyCipherParameters)
			if (secretKey instanceof DummyCipherParameters)
				return new DummyCipherParameters(((DummyCipherParameters) publicKey).getUserId() + ", " + ((DummyCipherParameters) secretKey).getUserId());
			else
				return new DummyCipherParameters(((DummyCipherParameters) publicKey).getUserId() + ", " + DummyCipherParameters.DEFAULTUID);
		else
			if (secretKey instanceof DummyCipherParameters)
				return new DummyCipherParameters(DummyCipherParameters.DEFAULTUID + ", " + ((DummyCipherParameters) secretKey).getUserId());	
		
		return new DummyCipherParameters(DummyCipherParameters.DEFAULTUID + ", " + DummyCipherParameters.DEFAULTUID);
	}

	@Override
	public Resource encrypt(CipherParameters publicKey, int[] policy, byte[] resource) {
		return new EncryptedResource(resource, null);
	}

	@Override
	public byte[] decrypt(CipherParameters secretKey, Resource resource) {
		throw new UnsupportedOperationException("Not available yet");
	}

}
