package it.uninsubria.dicom.cryptosocial.shared;

import org.bouncycastle.crypto.CipherParameters;

public class DBRASecretKeyParameters implements CipherParameters {
    protected CipherParameters hveSk;
    protected CipherParameters hibeSk;

    public DBRASecretKeyParameters(CipherParameters hveSk, CipherParameters hibeSk) {
        this.hveSk = hveSk;
        this.hibeSk = hibeSk;
    }


    public CipherParameters getHveSk() {
        return hveSk;
    }

    public CipherParameters getHibeSk() {
        return hibeSk;
    }
}
