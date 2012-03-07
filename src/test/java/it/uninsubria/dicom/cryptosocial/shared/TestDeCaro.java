package it.uninsubria.dicom.cryptosocial.shared;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.Security;
import java.util.Arrays;

import org.junit.*;

import static org.junit.Assert.*;


/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class TestDeCaro {

	@Test
    public void testFull() {
        try {
            Security.addProvider(new BouncyCastleProvider());

            // Setup
            DBRAKeyPairParameters keyPair = (DBRAKeyPairParameters) load(store(setup()));

            CryptoInterface cryptoInterface = new DBRACryptoInterface();

            // Encrypt
            byte[] message = "Hello World!!!".getBytes();
            Resource resource = (Resource) load(store(cryptoInterface.encrypt(keyPair.getPk(), new int[]{1, 0, 1, 0, 1, 3}, message)));

            // KeyGen
            CipherParameters interSecretKey = (CipherParameters) load(store(cryptoInterface.keyGeneration(keyPair, new int[]{1, 0, 1, 0, 1, 2})));
            CipherParameters secretKey = (CipherParameters) load(store(cryptoInterface.keyDelegation(keyPair.getPk(), interSecretKey, 3)));

            // Decrypt
            byte[] messagePrime = cryptoInterface.decrypt(secretKey, resource);
            assertEquals(true, Arrays.equals(message, messagePrime));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    protected DBRAKeyPairParameters setup() {
        return DBRASetup.setup(
                5, "it/uninsubria/dicom/cryptosocial/shared/a_160_512.properties",
                5, 32);
    }

    protected byte[] store(Object o) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            ObjectOutputStream out = new ObjectOutputStream(buffer);
            out.writeObject(o);
            out.flush();
            out.close();

            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Object load(byte[] bytes) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return in.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
