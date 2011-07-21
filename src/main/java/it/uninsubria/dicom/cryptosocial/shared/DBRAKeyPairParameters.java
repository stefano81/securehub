package it.uninsubria.dicom.cryptosocial.shared;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

public class DBRAKeyPairParameters implements CipherParameters {
    protected AsymmetricCipherKeyPair hveKeyPair;
    protected AsymmetricCipherKeyPair hibeKeyPair;


    public DBRAKeyPairParameters(AsymmetricCipherKeyPair hveKeyPair, AsymmetricCipherKeyPair hibeKeyPair) {
        this.hveKeyPair = hveKeyPair;
        this.hibeKeyPair = hibeKeyPair;
    }


    public AsymmetricCipherKeyPair getHveKeyPair() {
        return hveKeyPair;
    }

    public AsymmetricCipherKeyPair getHibeKeyPair() {
        return hibeKeyPair;
    }
}
