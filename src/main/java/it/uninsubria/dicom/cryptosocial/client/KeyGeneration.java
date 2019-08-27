package it.uninsubria.dicom.cryptosocial.client;

public interface KeyGeneration {
	public void generate(String emitter, String receiver, int ... policy);
	public void propagate(String emitter, String receiver);
}
