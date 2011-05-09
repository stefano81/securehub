package it.uninsubria.dicom.cryptosocial.engine.impl.aeshhve;

import it.uninsubria.dicom.cryptosocial.engine.CryptoSocial;
import it.uninsubria.dicom.cryptosocial.engine.EncryptedResource;
import it.uninsubria.dicom.cryptosocial.engine.SocialParam;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HHVEIP08AttributesEngine;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HHVEIP08SearchKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HHVEIP08DelegateSecretKeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HHVEIP08SearchKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEAttributes;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08KeyGenerationParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PublicKeyParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @author Stefano
 */
public class CryptoSocialAES_HHVE implements CryptoSocial {
    private static final String SYMMETRIC = "AES";
    private static final String PROVIDER = "BC";
    private static final int RBIT = 181;
    private static final int QBIT = 603;

    private CurveParams curveParams;
    private HHVEIP08AttributesEngine attributesEngine;
	private Cipher	symmetricCipher;
    
    //private Cipher symmetricCipher;

    public CryptoSocialAES_HHVE() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public SocialParam init(int l) throws NoSuchAlgorithmException, NoSuchProviderException {
        // generate symmetric key
        KeyGenerator AESKeyGenerator = KeyGenerator.getInstance(SYMMETRIC, PROVIDER);
        Key key = AESKeyGenerator.generateKey();

        // generate asymmetric key pair
        // generate curve
        TypeACurveGenerator curveGenerator = new TypeACurveGenerator(RBIT, QBIT);
        curveParams = (CurveParams) curveGenerator.generate();

        // generate HHVE parameters
        HVEIP08ParametersGenerator parametersGenerator = new HVEIP08ParametersGenerator();
        parametersGenerator.init(curveParams, l);
        HVEIP08Parameters hhveip08Parameters = parametersGenerator.generateParameters();

        // generate keys
        HVEIP08KeyPairGenerator keyPairGenerator = new HVEIP08KeyPairGenerator();
        keyPairGenerator.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), hhveip08Parameters));

        AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
        
        /*try {
            symmetricCipher = Cipher.getInstance(SYMMETRIC, PROVIDER);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();  // TODO Automatically created catch statement body
        }*/

        attributesEngine = new HHVEIP08AttributesEngine();

        return new MasterSocialParamAES_HHVE(key, asymmetricCipherKeyPair, l);
    }

    @Override
    public SocialParam init(SocialParam socialParam) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!(socialParam instanceof MasterSocialParamAES_HHVE))
            throw new IllegalArgumentException("Incorrect SocialParam argument");

        return init(((MasterSocialParamAES_HHVE) socialParam).getAttributeLen());
    }

    @Override
    public EncryptedResource encrypt(SocialParam sp, int[] x, byte[] resource) throws InvalidKeyException, IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        // Encrypt the encryptedResource with AES
        symmetricCipher.init(Cipher.ENCRYPT_MODE, sp.getSymmetricKey());

        CipherOutputStream cOut = new CipherOutputStream(bOut, symmetricCipher);
        cOut.write(resource);
        cOut.close();

        byte[] encryptedResource = bOut.toByteArray();

        // Encrypt the secret key with HHVE
        // encrypt the attribute
        byte[] attrs = HVEAttributes.attributesToByteArray(
                ((HVEIP08PublicKeyParameters) sp.getAsymmetricKeyPair().getPublic()).getParameters(),
                x
        );
        attributesEngine.init(true, sp.getAsymmetricKeyPair().getPublic());

        // encrypt the key
        bOut.reset();
        ObjectOutputStream oos = new ObjectOutputStream(bOut);

        oos.writeObject(sp.getSymmetricKey());
        oos.close();

        byte[] key = bOut.toByteArray();

        System.out.println("key.length: " + key.length);
        System.out.println("attributesEngine.getInputBlockSize(): " + attributesEngine.getInputBlockSize());

        int blocks = key.length / attributesEngine.getInputBlockSize();

        byte[][] cipherKey = new byte[blocks + 1][];
        cipherKey[0] = attributesEngine.processBlock(attrs, 0, attrs.length);

        for (int i = 0; i < blocks + 1; i++) {
            //System.out.println(i+": (key, "+i*blo", ");
            cipherKey[i + 1] = attributesEngine.processBlock(key, i * attributesEngine.getInputBlockSize(), i != blocks ? attributesEngine.getInputBlockSize() : key.length % attributesEngine.getInputBlockSize());
            System.out.println("ok");
        }


        return new EncryptedResource(encryptedResource, null);
    }

    @Override
    public byte[] decrypt(EncryptedResource resource, SocialParam sp) throws Exception {
        CipherInputStream cIn;
        ByteArrayInputStream bIn = new ByteArrayInputStream(resource.getR1());

        symmetricCipher.init(Cipher.DECRYPT_MODE, sp.getSymmetricKey());

        cIn = new CipherInputStream(bIn, symmetricCipher);

        ByteArrayOutputStream decrypt = new ByteArrayOutputStream();

        boolean stop = false;
        while (!stop) {
            int b = cIn.read();
            if (-1 == b)
                stop = true;
            else
                decrypt.write(b);
        }

        return decrypt.toByteArray();
    }

    @Override
    public SocialParam delegate(SocialParam masterKeys, int[] y) {
        HVEIP08PublicKeyParameters publicKey = (HVEIP08PublicKeyParameters) masterKeys.getAsymmetricKeyPair().getPublic();
        HHVEIP08SearchKeyParameters searchKey = (HHVEIP08SearchKeyParameters) masterKeys.getAsymmetricKeyPair().getPrivate();

        HHVEIP08SearchKeyGenerator generator = new HHVEIP08SearchKeyGenerator();

        generator.init(new HHVEIP08DelegateSecretKeyGenerationParameters(
                publicKey,
                searchKey,
                y)
        );

        CipherParameters keys = generator.generateKey();


        return null;  // TODO it.uninsubria.dicom.osnsimulator.node.cryptosocial.impl.aeshhve.CryptoSocialAES_HHVE.delegate from CryptoSocialAES_HHVE
    }

    @Override
    public SocialParam nextStep(SocialParam keys) {
        return null;  // TODO it.uninsubria.dicom.osnsimulator.node.cryptosocial.impl.aeshhve.CryptoSocialAES_HHVE.nextStep from CryptoSocialAES_HHVE
    }


    public void saveConf(String path) throws Exception {
//		throw new Exception("Not yet implemented");

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
        out.writeObject(curveParams);
        //out.writeObject(AESKeyGenerator);
        out.close();
    }

    public void loadConf(String path) {
        throw new UnsupportedOperationException("Not yet implemented");

/*		ObjectInputStream symmetricCipher = new ObjectInputStream(new FileInputStream(path));
		symmetricKey = (Key) symmetricCipher.readObject();
		System.out.println("Configurazione caricata.");
		symmetricCipher.close();*/
    }

}
