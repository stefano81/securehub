package it.uninsubria.dicom.cryptosocial.shared;

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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

import javax.crypto.KeyGenerator;

public class CriptoInterfaceFB implements CriptoInterface {
	private static CriptoInterface instance;
	private static Logger logger = Logger.getLogger(CriptoInterfaceFB.class.getName());
	
	private KeyGenerator	symmetricKeyGenerator;
	private HVEIP08KeyPairGenerator	keyPairGenerator;
	
	private CriptoInterfaceFB() {
		CommonProperties properties = CommonProperties.getInstance();
		
		try {
			symmetricKeyGenerator = KeyGenerator.getInstance(properties.getSymmetricAlgorithm());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		symmetricKeyGenerator.init(properties.getKeySize());

		URL url = properties.getParametersPath();

		HVEIP08Parameters parameters = null;

		if (null == url) {
			logger.info("not existing");

			// generate parameters
			CurveParams curveParams = new CurveParams();
			curveParams.load(properties.getCurveParams());

			HVEIP08ParametersGenerator generator = new HVEIP08ParametersGenerator();
			generator.init(curveParams, properties.getLength());

			parameters = generator.generateParameters();

			File parameterFile = new File(new File(this.getClass().getClassLoader().getResource("/").getFile() + "/../").getAbsolutePath() + properties.getParametersPathString());

			logger.info(parameterFile.getAbsolutePath());

			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(new FileOutputStream(parameterFile));
				oos.writeObject(parameters);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			logger.info("existing");

			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(new FileInputStream(url.getFile()));

				parameters = (HVEIP08Parameters) ois.readObject();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		keyPairGenerator = new HVEIP08KeyPairGenerator();
		keyPairGenerator.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), parameters));
	}

	public static CriptoInterface getInstance() {
		if (null == instance)
			instance = new CriptoInterfaceFB();
		
		return instance;
	}

	@Override
	public KeyGenerator getSymmetricKeyGenerator() {
		return this.symmetricKeyGenerator;
	}

	@Override
	public HVEIP08KeyPairGenerator getKeyPairGenerator() {
		return this.keyPairGenerator;
	}

}
