package it.uninsubria.dicom.cryptosocial.shared;

public interface DatabaseProperties {

	String getDriver();
	String getConnectionURL();
	String getPassword();
	String getUsername();
}
