package it.uninsubria.dicom.cryptosocial.shared;

import it.unisa.dia.gas.crypto.engines.MultiBlockAsymmetricBlockCipher;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HVEIP08Engine;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08SecretKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.*;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.engines.AHIBEDIP10Engine;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.generators.AHIBEDIP10KeyPairGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.generators.AHIBEDIP10SecretKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.params.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;

public class DBRACryptoInterface implements CryptoInterface {

    // Symmetric Engine
    protected KeyGenerator scKeyGen;

    // HVE
    protected HVEIP08Parameters hveParameters;


    public DBRACryptoInterface(int n, String curve) {
        initSymmetricKeyGenerator();

        initHVE(n, curve);
    }

    public DBRACryptoInterface(HVEIP08Parameters hveParameters) {
        initSymmetricKeyGenerator();

        this.hveParameters = hveParameters;
    }


    public CipherParameters keyGeneration(CipherParameters masterSecretKey, int[] policy) {
        DBRAKeyPairParameters keyPair = (DBRAKeyPairParameters) masterSecretKey;

        // generate hve key
        HVEIP08SecretKeyGenerator hveKeyGenerator = new HVEIP08SecretKeyGenerator();
        hveKeyGenerator.init(new HVEIP08SecretKeyGenerationParameters(
                (HVEIP08MasterSecretKeyParameters) keyPair.getHveKeyPair().getPrivate(), policy
        ));
        CipherParameters hveSk = hveKeyGenerator.generateKey();

        // generate hibe key
        AHIBEDIP10PublicKeyParameters pk = (AHIBEDIP10PublicKeyParameters) keyPair.getHibeKeyPair().getPublic();

        // Init identities
        Pairing pairing = PairingFactory.getPairing(pk.getCurveParameters());

        Element one = pairing.getZr().newOneElement();
        Element ids[] = new Element[hveParameters.getAttributesLengthInBytes()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = one;
        }

        AHIBEDIP10SecretKeyGenerator hibeGenerator = new AHIBEDIP10SecretKeyGenerator();
        hibeGenerator.init(new AHIBEDIP10SecretKeyGenerationParameters(
                (AHIBEDIP10MasterSecretKeyParameters) keyPair.getHibeKeyPair().getPrivate(),
                (AHIBEDIP10PublicKeyParameters) keyPair.getHibeKeyPair().getPublic(),
                ids
        ));

        CipherParameters hibeSk = hibeGenerator.generateKey();

        // Return secret key
        return new DBRASecretKeyParameters(hveSk, hibeSk);
    }

    public CipherParameters keyDelegation(CipherParameters publicKey, CipherParameters secretKey, int depth) {
        AHIBEDIP10PublicKeyParameters pk = (AHIBEDIP10PublicKeyParameters) ((DBRAPublicKeyParameters) publicKey).getHibePk();
        Pairing pairing = PairingFactory.getPairing(pk.getCurveParameters());

        DBRASecretKeyParameters sk = (DBRASecretKeyParameters) secretKey;

        AHIBEDIP10SecretKeyGenerator generator = new AHIBEDIP10SecretKeyGenerator();

        AHIBEDIP10SecretKeyParameters hibeSk = (AHIBEDIP10SecretKeyParameters) sk.getHibeSk();
        for (int i = 0, size = depth - hibeSk.getDepth(); i < size; i++) {
            generator.init(new AHIBEDIP10DelegateGenerationParameters(
                    pk, hibeSk, pairing.getZr().newOneElement()
            ));
            hibeSk = (AHIBEDIP10SecretKeyParameters) generator.generateKey();
        }

        return new DBRASecretKeyParameters(sk.getHveSk(), hibeSk);
    }


    public Resource encrypt(CipherParameters publicKey, int[] policy, byte[] resource) {
        try {
            // Encrypt data using ephemeral symmetric key
            Key key = scKeyGen.generateKey();

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipher.update(resource, 0, resource.length);
            byte[] ct = cipher.doFinal();

            // Combine ct and the encryption of the key...
            DBRAPublicKeyParameters pk = (DBRAPublicKeyParameters) publicKey;
            return new CryptoResource(
                    policy[hveParameters.getAttributesLengthInBytes()],
                    ct,
                    hibeEncrypt(
                            pk.getHibePk(),
                            hveEncrypt(pk.getHvePk(), key.getEncoded(), policy),
                            policy[hveParameters.getAttributesLengthInBytes()]
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decrypt(CipherParameters secretKey, Resource resource) {
        try {
            // Recover symmetric key
            DBRASecretKeyParameters sk = (DBRASecretKeyParameters) secretKey;
            byte[] keyMaterial = hveDecrypt(
                    sk.getHveSk(),
                    hibeDecrypt(sk.getHibeSk(), resource.getResource())
            );

            // Decrypt
            SecretKeySpec key = new SecretKeySpec(keyMaterial, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] res = resource.getResource();
            cipher.update(res, 0, res.length);

            return cipher.doFinal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public HVEIP08Parameters getHveParameters() {
        return hveParameters;
    }


    protected void initSymmetricKeyGenerator() {
        try {
            // Init Symmetric Key Generator
            scKeyGen = KeyGenerator.getInstance("AES", "BC");
            scKeyGen.init(new SecureRandom());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void initHVE(int n, String curve) {
        HVEIP08ParametersGenerator paramGen = new HVEIP08ParametersGenerator();
        paramGen.init(n, PairingFactory.getInstance().loadCurveParameters(curve));
        hveParameters = paramGen.generateParameters();
    }


    protected byte[] hveEncrypt(CipherParameters publicKey, byte[] data, int[] attributes) {
        byte[] ct;
        try {
            AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(
                    new HVEIP08Engine(),
                    new ZeroBytePadding()
            );
            engine.init(true, new HVEIP08EncryptionParameters((HVEIP08PublicKeyParameters) publicKey, attributes));
            ct = engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
        return ct;
    }

    protected byte[] hveDecrypt(CipherParameters secretKey, byte[] ct) {
        byte[] data;
        try {
            AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(
                    new HVEIP08Engine(),
                    new ZeroBytePadding()
            );
            engine.init(false, secretKey);
            data = engine.processBlock(ct, 0, ct.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }

        return data;
    }

    protected byte[] hibeEncrypt(CipherParameters publicKey, byte[] data, int depth) {
        // Encrypt
        byte[] ct;
        try {
            AHIBEDIP10PublicKeyParameters pk = (AHIBEDIP10PublicKeyParameters) publicKey;

            // Init identities
            Pairing pairing = PairingFactory.getPairing(pk.getCurveParameters());

            Element one = pairing.getZr().newOneElement();
            Element ids[] = new Element[depth];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = one;
            }

            // Enc
            AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(
                    new AHIBEDIP10Engine(),
                    new ZeroBytePadding()
            );
            engine.init(true, new AHIBEDIP10EncryptionParameters(pk, ids));
            ct = engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }

        return ct;
    }

    protected byte[] hibeDecrypt(CipherParameters secretKey, byte[] ct) {
        byte[] data;
        try {
            AsymmetricBlockCipher engine = new MultiBlockAsymmetricBlockCipher(
                    new AHIBEDIP10Engine(),
                    new ZeroBytePadding()
            );
            engine.init(false, secretKey);
            data = engine.processBlock(ct, 0, ct.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }

        return data;
    }


}
