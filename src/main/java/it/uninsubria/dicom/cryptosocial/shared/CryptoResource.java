package it.uninsubria.dicom.cryptosocial.shared;

public class CryptoResource implements Resource {
    protected int depth;
    protected byte[] resource;
    protected byte[] key;


    public CryptoResource(int depth, byte[] resource, byte[] key) {
        this.depth = depth;
        this.resource = resource;
        this.key = key;
    }

    public int getDepth() {
        return depth;
    }

    public byte[] getResource() {
        return resource;
    }

    public byte[] getKey() {
        return key;
    }
}
