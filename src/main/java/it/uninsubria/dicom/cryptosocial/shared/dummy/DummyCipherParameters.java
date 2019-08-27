package it.uninsubria.dicom.cryptosocial.shared.dummy;

import java.io.Serializable;

import org.bouncycastle.crypto.CipherParameters;

public class DummyCipherParameters implements CipherParameters, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String DEFAULTUID = "John Doe";
	private String userId;
	private long timestamp;

	
	public DummyCipherParameters(String userId) {
		this.userId = userId;
		timestamp = System.currentTimeMillis();
	}


	public String getUserId() {
		return userId;
	}


	public long getTimestamp() {
		return timestamp;
	}
	
	
}
