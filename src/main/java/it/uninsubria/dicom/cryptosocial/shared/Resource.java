package it.uninsubria.dicom.cryptosocial.shared;

import java.io.Serializable;

public interface Resource extends Serializable {

	public byte[] getResource();

	public byte[] getKey();
}
