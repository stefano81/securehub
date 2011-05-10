package it.uninsubria.dicom.cryptosocial.client;

import java.io.InputStream;
import java.net.URL;

public interface ClientProperties {

	int getLength();

	InputStream getCurveParams();

	URL getParametersPath();

	int getKeySize();

	String getSymmetricAlgorithm();

}
