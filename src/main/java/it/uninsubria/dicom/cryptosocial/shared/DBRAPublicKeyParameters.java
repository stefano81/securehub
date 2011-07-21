package it.uninsubria.dicom.cryptosocial.shared;

import org.bouncycastle.crypto.CipherParameters;

public class DBRAPublicKeyParameters implements CipherParameters {
    protected CipherParameters hvePk;
    protected CipherParameters hibePk;


    public DBRAPublicKeyParameters(CipherParameters hvePk, CipherParameters hibePk) {
        this.hvePk = hvePk;
        this.hibePk = hibePk;
    }


    public CipherParameters getHvePk() {
        return hvePk;
    }

    public CipherParameters getHibePk() {
        return hibePk;
    }
}
