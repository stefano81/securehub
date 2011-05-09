package it.uninsubria.dicom.cryptosocial;

public interface KeyGeneration {
	public void generate(String emitter, String receiver, int ... policy);
	public void propagate(String emitter, String receiver);
}
