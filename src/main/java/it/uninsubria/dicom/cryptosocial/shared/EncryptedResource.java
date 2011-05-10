package it.uninsubria.dicom.cryptosocial.shared;

public class EncryptedResource implements Resource {
	private byte[] resource;
	private byte[] key;
	
	public EncryptedResource(byte[] resource, byte[] key) {
		this.resource = resource;
		this.key = key;
	}

	@Override
	public byte[] getResource() {
		return resource;
	}

	@Override
	public byte[] getKey() {
		return key;
	}

}
