package it.uninsubria.dicom.cryptosocial.shared;

import it.uninsubria.dicom.cryptosocial.client.ClientProperties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Security;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class CommonProperties implements DatabaseProperties, ClientProperties {
	private static CommonProperties instance;
	
	private Properties properties;
	
	private CommonProperties(String configurationPath) {
		Security.addProvider(new BouncyCastleProvider());

		properties = new Properties();
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream(configurationPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static CommonProperties getInstance() {
		if (null == instance) {
			String configurationPath = "it/uninsubria/dicom/cryptosocial/config.properties";
			instance = new CommonProperties(configurationPath);
		}
		
		return instance;
	}

	@Override
	public String getSymmetricAlgorithm() {
		return properties.getProperty("symmetricAlgorithm");
	}

	@Override
	public int getKeySize() {
		return Integer.parseInt(properties.getProperty("keySize"));
	}

	@Override
	public URL getParametersPath() {
		return this.getClass().getClassLoader().getResource(properties.getProperty("parametersPath"));
	}

	@Override
	public InputStream getCurveParams() {
		return this.getClass().getClassLoader().getResourceAsStream(properties.getProperty("curvePath"));
	}

	@Override
	public int getLength() {
		return Integer.parseInt(properties.getProperty("length"));
	}

	@Override
	public String getDriver() {
		return properties.getProperty("driver");
	}

	@Override
	public String getConnectionURL() {
		return properties.getProperty("URL");
	}

	@Override
	public String getPassword() {
		return properties.getProperty("password");
	}

	@Override
	public String getUsername() {
		return properties.getProperty("username");
	}

	@Override
	public String getParametersPathString() {
		return properties.getProperty("parametersPath");
	}

	@Override
	public String getCurveParamsLocation() {
		return properties.getProperty("curvePath");
	}
}
