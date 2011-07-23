package it.uninsubria.dicom.cryptosocial.shared;

import it.unisa.dia.gas.crypto.engines.MultiBlockAsymmetricBlockCipher;
import it.unisa.dia.gas.crypto.engines.kem.KEMCipher;
import it.unisa.dia.gas.crypto.engines.kem.KEMCipherDecryptionParameters;
//import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HVEIP08Engine;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.engines.HVEIP08KEMEngine;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08SecretKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.*;
//import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.engines.AHIBEDIP10Engine;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.engines.AHIBEDIP10KEMEngine;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.generators.AHIBEDIP10SecretKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.ibe.dip10.params.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class DBRACryptoInterface implements CryptoInterface {

    // Symmetric Engine
    protected KeyGenerator scKeyGen;


    public DBRACryptoInterface() {
        initSymmetricKeyGenerator();
    }


    public CipherParameters keyGeneration(CipherParameters masterSecretKey, int[] policy) {
        /*DBRAKeyPairParameters keyPair = (DBRAKeyPairParameters) masterSecretKey;

        // generate hve key
        HVEIP08SecretKeyGenerator hveKeyGenerator = new HVEIP08SecretKeyGenerator();
        hveKeyGenerator.init(new HVEIP08SecretKeyGenerationParameters(
                (HVEIP08MasterSecretKeyParameters) keyPair.getMsk().getHveSk(),
                Arrays.copyOf(policy, policy.length - 1)
        ));
        CipherParameters hveSk = hveKeyGenerator.generateKey();

        // generate hibe key
        AHIBEDIP10PublicKeyParameters pk = (AHIBEDIP10PublicKeyParameters) keyPair.getPk().getHibePk();

        // Init identities
        Pairing pairing = PairingFactory.getPairing(pk.getCurveParameters());

        Element one = pairing.getZr().newOneElement();
        Element ids[] = new Element[policy[policy.length - 1]];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = one;
        }

        AHIBEDIP10SecretKeyGenerator hibeGenerator = new AHIBEDIP10SecretKeyGenerator();
        hibeGenerator.init(new AHIBEDIP10SecretKeyGenerationParameters(
                (AHIBEDIP10MasterSecretKeyParameters) keyPair.getMsk().getHibeSk(),
                (AHIBEDIP10PublicKeyParameters) keyPair.getPk().getHibePk(),
                ids
        ));

        CipherParameters hibeSk = hibeGenerator.generateKey();

        // Return secret key
        return new DBRASecretKeyParameters(hveSk, hibeSk);*/
    	return null; // FIXME
    }

    public CipherParameters keyDelegation(CipherParameters publicKey, CipherParameters secretKey, int depth) {
        /*AHIBEDIP10PublicKeyParameters pk = (AHIBEDIP10PublicKeyParameters) ((DBRAPublicKeyParameters) publicKey).getHibePk();
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

        return new DBRASecretKeyParameters(sk.getHveSk(), hibeSk);*/
    	return null; // FIXME
    }


    public Resource encrypt(CipherParameters publicKey, int[] policy, byte[] resource) {
    	/*
        try {
            // Encrypt resource using ephemeral symmetric key
            Key key = scKeyGen.generateKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ct = cipher.doFinal(resource);

            // Combine ct and the encryption of the key...

//            System.out.println("ENCRYPT KEY " + Arrays.toString(key.getEncoded()));

            // ENC HVE-HIBE
            DBRAPublicKeyParameters pk = (DBRAPublicKeyParameters) publicKey;
            return new CryptoResource(
                    policy[policy.length - 1], // depth
                    ct, // resource
                    hibeEncrypt(
                            pk.getHibePk(),
                            hveEncrypt(pk.getHvePk(), key.getEncoded(), Arrays.copyOf(policy, policy.length - 1)),
                            policy[policy.length - 1]
                    ) // key
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        */
    	
    	return null; // FIXME
    }

    public byte[] decrypt(CipherParameters secretKey, Resource resource) {
    	/*
        try {
            // Recover symmetric key
            DBRASecretKeyParameters sk = (DBRASecretKeyParameters) secretKey;
            byte[] keyMaterial = hveDecrypt(
                    sk.getHveSk(),
                    hibeDecrypt(sk.getHibeSk(), resource.getKey())
            );

//            System.out.println("DECRYPT KEY " + Arrays.toString(keyMaterial));

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
        */
    	return null;  //FIXME
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



    public byte[] hveEncrypt(CipherParameters publicKey, byte[] data, int[] attributes) {
//        System.out.println("HVE.ENC.IN : " + Arrays.toString(data));
        try {
            KEMCipher engine = new KEMCipher(Cipher.getInstance("AES/ECB/PKCS7Padding", "BC"), new HVEIP08KEMEngine());
            byte[] encapsulation = engine.init(true,
                    new HVEIP08EncryptionParameters(
                            (HVEIP08PublicKeyParameters) publicKey,
                            attributes
                    )
            );
            byte[] ct = engine.doFinal(data);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(out);
            dataOutput.writeInt(encapsulation.length);
            dataOutput.write(encapsulation);
            dataOutput.write(ct);

            byte[] result = out.toByteArray();
//            System.out.println("HVE.ENC.OUT : " + Arrays.toString(result));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] hveDecrypt(CipherParameters secretKey, byte[] ct) {
//        System.out.println("HVE.DEC.IN : " + Arrays.toString(ct));
    	/*
        try {
            // Extract encapsulation
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(ct));
            int encapsLength = in.readInt();
            byte[] encapsulation = new byte[encapsLength];
            in.readFully(encapsulation);
            byte[] ciphertext = new byte[ct.length - 4 - encapsLength];
            in.readFully(ciphertext);

            KEMCipher kemCipher = new KEMCipher(Cipher.getInstance("AES/ECB/PKCS7Padding", "BC"), new HVEIP08KEMEngine());
            kemCipher.init(false, new KEMCipherDecryptionParameters(secretKey, encapsulation));

            // Decrypt
            return kemCipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
    	
    	return null; // FIXME
    }


    public byte[] hibeEncrypt(CipherParameters publicKey, byte[] data, int depth) {
//        System.out.println("HIBE.ENC.IN : " + Arrays.toString(data));
        // Encrypt
        try {
            AHIBEDIP10PublicKeyParameters pk = (AHIBEDIP10PublicKeyParameters) publicKey;

            // Init identities
            Pairing pairing = PairingFactory.getPairing(pk.getCurveParameters());

            Element one = pairing.getZr().newOneElement();
            Element ids[] = new Element[depth];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = one;
            }

            KEMCipher engine = new KEMCipher(Cipher.getInstance("AES/ECB/PKCS7Padding", "BC"), new AHIBEDIP10KEMEngine());
            byte[] encapsulation = engine.init(true, new AHIBEDIP10EncryptionParameters(pk, ids));
            byte[] ct = engine.doFinal(data);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(out);
            dataOutput.writeInt(encapsulation.length);
            dataOutput.write(encapsulation);
            dataOutput.write(ct);

            byte[] result = out.toByteArray();
//            System.out.println("HIBE.ENC.OUT : " + Arrays.toString(result));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] hibeDecrypt(CipherParameters secretKey, byte[] ct) {
    	/*
//        System.out.println("HIBE.DEC.IN : " + Arrays.toString(ct));
        try {
            // Extract encapsulation
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(ct));
            int encapsLength = in.readInt();
            byte[] encapsulation = new byte[encapsLength];
            in.readFully(encapsulation);
            byte[] ciphertext = new byte[ct.length - 4 - encapsLength];
            in.readFully(ciphertext);

            KEMCipher kemCipher = new KEMCipher(Cipher.getInstance("AES/ECB/PKCS7Padding", "BC"), new AHIBEDIP10KEMEngine());
            kemCipher.init(false, new KEMCipherDecryptionParameters(secretKey, encapsulation));

            // Decrypt
            return kemCipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        */
    	
    	return null; // FIXME
    }

}
