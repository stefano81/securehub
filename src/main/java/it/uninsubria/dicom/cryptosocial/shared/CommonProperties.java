package it.uninsubria.dicom.cryptosocial.shared;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Security;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class CommonProperties {
	private static CommonProperties instance;
	
	private Properties properties;
	
	private CommonProperties() {
		Security.addProvider(new BouncyCastleProvider());

		properties = new Properties();
		try {
			properties.load(this.getClass().getResourceAsStream("it/uninsubria/dicom/cryptosocial/config.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static CommonProperties getInstance() {
		if (null == instance)
			instance = new CommonProperties();
		
		return instance;
	}

	public String getSymmetricAlgorithm() {
		return properties.getProperty("symmetricAlgorithm");
	}

	public int getKeySize() {
		return Integer.parseInt(properties.getProperty("keySize"));
	}

	public URL getParametersPath() {
		return this.getClass().getClassLoader().getResource(properties.getProperty("parametersPath"));
	}

	public InputStream getCurveParams() {
		return this.getClass().getClassLoader().getResourceAsStream(properties.getProperty("curvePath"));
	}

	public int getLength() {
		return Integer.parseInt(properties.getProperty("length"));
	}
}
