package it.uninsubria.dicom.cryptosocial.engine.impl.aeshhve;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import java.io.*;
import java.security.Key;

public class MasterSocialParamAES_HHVE extends SocialParamAES_HHVE implements Serializable {
    private Key symmetricKey;

    public MasterSocialParamAES_HHVE(Key symmetric, AsymmetricCipherKeyPair asymmetric, int attributeLen) {
        super(asymmetric, attributeLen);
        this.symmetricKey = symmetric;
    }

    public int getAttributeLen() {
        return attributeLen;
    }

    @Override
    public Key getSymmetricKey() {
        return symmetricKey;
    }

    @Override
    public AsymmetricCipherKeyPair getAsymmetricKeyPair() {
        return asymmetricKeys;
    }
}
