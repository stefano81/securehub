package it.uninsubria.dicom.cryptosocial.engine;

import it.uninsubria.dicom.cryptosocial.CryptoSocial;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptoProvider {
	private static CryptoProvider INSTANCE;
	
	private final Properties	properties;
	
	private final HVEIP08Parameters	parameters;
	private final HVEIP08KeyPairGenerator	keyPairGenerator;
	
	private final Logger logger = Logger.getLogger(CryptoProvider.class.toString());
	
	private CryptoProvider() throws IOException, ClassNotFoundException {
		properties = new Properties();
		
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("it/uninsubria/dicom/cryptosocial/config.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		URL url = this.getClass().getClassLoader().getResource(properties.getProperty("parametersPath"));
		//InputStream is = 
		
		if (null == url) {
			logger.log(Level.INFO, "not existing");
			
			// generate parameters
			CurveParams curveParams = new CurveParams();
			curveParams.load(this.getClass().getClassLoader().getResourceAsStream(properties.getProperty("curvePath")));
		
			HVEIP08ParametersGenerator generator = new HVEIP08ParametersGenerator();
			generator.init(curveParams, Integer.parseInt(properties.getProperty("length")));
		
			parameters = generator.generateParameters();
			/*
			File parameterFile = new File(this.getClass().getResource("/") + properties.getProperty("parametersPath"));
			
			logger.info(parameterFile.getAbsolutePath());
			
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(parameterFile));
			oos.writeObject(parameters);*/
			
		} else {
			logger.log(Level.INFO, "existing");
			
			ObjectInputStream ois;
			
			ois = new ObjectInputStream(new FileInputStream(url.getFile()));
		
			parameters = (HVEIP08Parameters) ois.readObject();
		}
		
		keyPairGenerator = new HVEIP08KeyPairGenerator();
		keyPairGenerator.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), parameters));
	}
	
	
	public static synchronized CryptoProvider getInstance() {
		if (null == INSTANCE) {
			try {
				INSTANCE = new CryptoProvider();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return INSTANCE;
	}
}
