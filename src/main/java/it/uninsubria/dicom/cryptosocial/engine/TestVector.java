package it.uninsubria.dicom.cryptosocial.engine;

import it.unisa.dia.gas.crypto.engines.MultiBlockAsymmetricBlockCipher;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HHVEIP08Engine;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HHVEIP08SearchKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08EncryptionParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PrivateKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PublicKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08SearchKeyGenerationParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;

import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;

public class TestVector {
    private static final int VECTORSIZE = 50;
	private static final boolean D = false;
	private static final String message = "This is a test message";

	public static void main(String[] args) {
    /*    AsymmetricCipherKeyPair keyPair = setup(genParam(1, 3, 1, 3, 2, 1));

        String message = "Hello World!!!";

        assertEquals(message,
                decrypt(
                        keyGen(keyPair.getPrivate(), 0, 7, -1, 3, -1, 1),
                        enc(keyPair.getPublic(), message, 0, 7, 0, 3, 2, 1)
                )
        );

        assertNotSame(message,
                decrypt(
                        keyGen(keyPair.getPrivate(), 0, 5, -1, 3, -1, 1),
                        enc(keyPair.getPublic(), message, 0, 7, 0, 3, 2, 1)
                )
        );

        assertEquals(message,
                decrypt(
                        keyGen(keyPair.getPrivate(), -1, -1, -1, -1, -1, -1),
                        enc(keyPair.getPublic(), message, 0, 7, 0, 3, 2, 1)
                )
        );

        assertEquals(message,
                decrypt(
                        delegate(
                                keyPair.getPublic(),
                                keyGen(keyPair.getPrivate(), -1, -1, -1, -1, -1, -1),
                                0, 7, 0, 3, -1, 1
                        ),
                        enc(keyPair.getPublic(), message, 0, 7, 0, 3, 2, 1)
                )
        );

        assertEquals(message,
                decrypt(
                        delegate(
                                keyPair.getPublic(),
                                keyGen(keyPair.getPrivate(), 0, 7, -1, 3, -1, 1),
                                0, 7, 0, 3, -1, 1
                        ),
                        enc(keyPair.getPublic(), message, 0, 7, 0, 3, 2, 1)
                )
        );*/
    	// load class
    	CurveParams curveParams = new CurveParams();
    	curveParams.load(TestVector.class.getResourceAsStream("/it/unisa/dia/gas/plaf/jpbc/crypto/a_181_603.properties"));
    	// init parameter generation
    	HVEIP08ParametersGenerator generator = new HVEIP08ParametersGenerator();
    	generator.init(curveParams, VECTORSIZE);
    	// init parameter
    	HVEIP08Parameters parameters = generator.generateParameters();
    	
    	AsymmetricCipherKeyPair keyPair = setup(parameters);
    	
    	// create test vectors
    	int[][] vector = new int[VECTORSIZE][];
    	
    	
    	for (int i = 0; i < VECTORSIZE; i++) {
    		vector[i] = new int[VECTORSIZE];
    		for (int j = 0; j < VECTORSIZE; j++) {
    			vector[i][j] = j < i ? 0 : 1;
    		}

    		if (D) {
    			System.out.print("vector["+i+"] := [");
	    		for (int j = 0; j < VECTORSIZE; j++) {
	    			System.out.print(vector[i][j]);
	    		}
	    		System.out.println("];");
	    	}
    	}
    	
    	// test
    	for (int i = 0; i < VECTORSIZE; i++) {
    		CipherParameters key = keyGen(keyPair.getPrivate(), vector[i]);
    		
    		for (int j = 0; j < 10; j++) {
    			long startTime = System.currentTimeMillis();
    			byte[] cipherText = enc(keyPair.getPublic(), message, vector[i]);
    			
    			long encTime = System.currentTimeMillis();
    			
    			String m = decrypt(key, cipherText);
    			
    			long stopTime = System.currentTimeMillis();
    			
    			System.out.println("Enc["+ i + ", " + j + "]: " + (encTime - startTime));
    			System.out.println("Dec["+ i + ", " + j + "]: " + (stopTime - encTime));
    			
    			if (D)
    				System.out.println(m.equals(message));
    		}
    	}
    }

    /*private CipherParameters delegate(CipherParameters publicKey, CipherParameters searchKey, int... attributesPattern) {
        HHVEIP08SearchKeyGenerator generator = new HHVEIP08SearchKeyGenerator();
        generator.init(new HHVEIP08DelegateSecretKeyGenerationParameters(
                (HVEIP08PublicKeyParameters) publicKey,
                (HHVEIP08SearchKeyParameters) searchKey,
                attributesPattern)
        );

        return generator.generateKey();
    }*/


    protected HVEIP08Parameters genParam(int... attributeLengths) {
        CurveParams curveParams = new CurveParams();
        curveParams.load(this.getClass().getClassLoader().getResourceAsStream("it/unisa/dia/gas/plaf/jpbc/crypto/a_181_603.properties"));

        HVEIP08ParametersGenerator generator = new HVEIP08ParametersGenerator();
        generator.init(curveParams, attributeLengths);

        return generator.generateParameters();
    }

    protected static AsymmetricCipherKeyPair setup(HVEIP08Parameters hveParameters) {
        HVEIP08KeyPairGenerator generator = new HVEIP08KeyPairGenerator();
        generator.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), hveParameters));

        return generator.generateKeyPair();
    }

    protected static byte[] enc(CipherParameters publicKey, String message, int... attributes) {
        byte[] bytes = message.getBytes();

        byte[] ciphertext = new byte[0];
        try {
            AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(
                    new HHVEIP08Engine(),
                    new ZeroBytePadding()
            );
            engine.init(true, new HVEIP08EncryptionParameters((HVEIP08PublicKeyParameters) publicKey, attributes));
            ciphertext = engine.processBlock(bytes, 0, bytes.length);

            //assertNotNull(ciphertext);
            //assertNotSame(0, ciphertext.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
            //fail(e.getMessage());
        }
        return ciphertext;
    }

    protected static CipherParameters keyGen(CipherParameters privateKey, int... pattern) {
        HHVEIP08SearchKeyGenerator generator = new HHVEIP08SearchKeyGenerator();
        generator.init(new HVEIP08SearchKeyGenerationParameters((HVEIP08PrivateKeyParameters) privateKey, pattern));

        return generator.generateKey();
    }

    protected static String decrypt(CipherParameters searchKey, byte[] ct) {
        byte[] plainText = new byte[0];
        try {
            AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(
                    new HHVEIP08Engine(),
                    new ZeroBytePadding()
            );
            // Decrypt
            engine.init(false, searchKey);
            plainText = engine.processBlock(ct, 0, ct.length);

            //assertNotNull(plainText);
            //assertNotSame(0, plainText.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
            //fail(e.getMessage());
        }

        return new String(plainText).trim();
    }
}