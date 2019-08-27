package it.uninsubria.dicom.cryptosocial.shared;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import java.io.*;

public class DBRAKeyPairParameters implements CipherParameters, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected DBRAPublicKeyParameters pk;
    protected DBRAMasterSecretKeyParameters sk;


    public DBRAKeyPairParameters(AsymmetricCipherKeyPair hveKeyPair, AsymmetricCipherKeyPair hibeKeyPair) {
        this.pk = new DBRAPublicKeyParameters(hveKeyPair.getPublic(), hibeKeyPair.getPublic());
        this.sk = new DBRAMasterSecretKeyParameters(hveKeyPair.getPrivate(), hibeKeyPair.getPrivate());
    }


    public DBRAPublicKeyParameters getPk() {
        return pk;
    }

    public DBRAMasterSecretKeyParameters getMsk() {
        return sk;
    }

}
