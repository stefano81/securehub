package it.uninsubria.dicom.cryptosocial.engine.impl.aeshhve;

import it.uninsubria.dicom.cryptosocial.engine.SocialParam;

import java.io.Serializable;
import java.security.Key;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

public class SocialParamAES_HHVE implements Serializable, SocialParam {
    protected AsymmetricCipherKeyPair asymmetricKeys;
    protected int attributeLen;

    public SocialParamAES_HHVE(AsymmetricCipherKeyPair asymmetric, int attributeLen) {
        this.asymmetricKeys = asymmetric;
        this.attributeLen = attributeLen;
    }

    public int getAttributeLen() {
        return attributeLen;
    }

    @Override
    public Key getSymmetricKey() {
        throw new UnsupportedOperationException("Symmetric Keys not stored in search keys");
    }

    @Override
    public AsymmetricCipherKeyPair getAsymmetricKeyPair() {
        return asymmetricKeys;
    }
}
