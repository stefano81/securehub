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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class CryptoSocial {

	public SocialParam init(int Arraylength) {	
		byte[] key;
		SecureRandom gen;
		Key AESKey;
		CurveParams curveParams;
		HVEIP08ParametersGenerator generator;
		HVEIP08Parameters param;
		HVEIP08KeyPairGenerator generatorKP;
		AsymmetricCipherKeyPair keyPair;
		Security.addProvider(new BouncyCastleProvider());
				
		// Inizializzazione parametri per chiave AES
		key= new byte[32];
		gen= new SecureRandom();
		gen.nextBytes(key);
		
		// Inizializzazione parametri per chiavi HHVE
		// curveGenerator= new TypeACurveGenerator(181, 603);
		// curveParams= (CurveParams) curveGenerator.generate();
		
		curveParams = new CurveParams();
        curveParams.load("a_181_603.properties");
		generator= new HVEIP08ParametersGenerator();
        generator.init(curveParams, Arraylength);
        param= generator.generateParameters();       
        generatorKP= new HVEIP08KeyPairGenerator();
        generatorKP.init(new HVEIP08KeyGenerationParameters(new SecureRandom(), param));
     
        //Generazione chiavi
		//AESKey= new SecretKeySpec(key, "AES");
        
        SecretKeyFactory.getInstance("AES", "BC").generateSecret(arg0)
        
        //AESKey = Cipher.getInstance("AES", "BC")
        
		keyPair= generatorKP.generateKeyPair();
        		
		return new SocialParam(AESKey, keyPair);
	}
		

	public EncResources enc(SocialParam sp, int[] vett, byte[] resource) throws Exception {
		AsymmetricBlockCipher engine;
		byte[] cryptRes;
		byte[] AESEnc;
		byte[] AESAlg;
		byte[] AESKeyParams;
		byte[] AESKeyCrypt;
		int i;
			
		// Cifratura risorsa con chiave AES
		cryptRes= this.aesCrypt(sp.getAesKey(), resource);
						
		// Conversione chiave AES in byte[]
		AESEnc= sp.getAesKey().getEncoded();
		AESAlg= sp.getAesKey().getAlgorithm().getBytes();		
		AESKeyParams= new byte[AESEnc.length + AESAlg.length];

		for (i= 0; i < 32; i++)
			AESKeyParams[i]= AESEnc[i];
		for (i= 32; i < AESKeyParams.length; i++)
			AESKeyParams[i]= AESAlg[i-32];
		
		// Cifratura chiave AES con HHVE		
		engine= new MultiBlockAsymmetricBlockCipher(new HHVEIP08Engine(), new ZeroBytePadding());
		engine.init(true, new HVEIP08EncryptionParameters((HVEIP08PublicKeyParameters) sp.getPublicKey(), vett));
        AESKeyCrypt= engine.processBlock(AESKeyParams, 0, AESKeyParams.length);
				
		return new EncResources(cryptRes, AESKeyCrypt);			
	}
	
	public byte[] dec(EncResources resource, CipherParameters searchKey) throws Exception {
		AsymmetricBlockCipher engine;
		Key AESKey;
		byte[] decryptRES;
		byte[] AESDec;
		byte[] AESKeyParams;
		String AESAlg;
        
        // Decifratura e ricostruzione chiave AES
        engine= new MultiBlockAsymmetricBlockCipher(new HHVEIP08Engine(), new ZeroBytePadding());
        engine.init(false, searchKey);
        AESKeyParams= engine.processBlock(resource.getAESCrypt(), 0, resource.getAESCrypt().length);
        
        AESDec= Arrays.copyOfRange(AESKeyParams, 0, 32);
        AESAlg= new String(Arrays.copyOfRange(AESKeyParams, 32, AESKeyParams.length));      
        AESKey= new SecretKeySpec(AESDec, AESAlg);
               
        decryptRES= this.aesDecrypt(AESKey, resource.getResCrypt());
        
		return decryptRES;
	}
	
	private byte[] aesCrypt(Key key, byte[] resource) throws Exception {
		Cipher out;
		CipherOutputStream cOut;
		ByteArrayOutputStream bOut;
		byte[] cryptRes;
		
		out= Cipher.getInstance("AES/ECB/ZeroBytePadding", "BC");
		out.init(Cipher.ENCRYPT_MODE, key);			
		bOut= new ByteArrayOutputStream();
		cOut= new CipherOutputStream(bOut, out);		
		cOut.write(resource);
		cOut.close();
		cryptRes= bOut.toByteArray();		
		bOut.close();

		return cryptRes;
	}
	
	private byte[] aesDecrypt(Key key, byte[]resource) throws Exception {
		Cipher in;
		CipherInputStream cIn;
		ByteArrayOutputStream bOut;		
		ByteArrayInputStream bIn;
		int r;
		byte[] decryptRes;
		
		bOut = new ByteArrayOutputStream();
        bIn= new ByteArrayInputStream(resource);
		
		in= Cipher.getInstance("AES/ECB/ZeroBytePadding", "BC");
		in.init(Cipher.DECRYPT_MODE, key);
		cIn= new CipherInputStream(bIn, in);

		while ((r= cIn.read()) != -1) 
               bOut.write(r);
        
        decryptRes= bOut.toByteArray();
			
		bOut.close();
		cIn.close();
		bIn.close();

		return decryptRes;
	}
	
	public CipherParameters link(CipherParameters privateKey, int... vettore) {
        HHVEIP08SearchKeyGenerator generator= new HHVEIP08SearchKeyGenerator();
        generator.init(new HVEIP08SearchKeyGenerationParameters((HVEIP08PrivateKeyParameters) privateKey, vettore));

        return generator.generateKey();
	}

}
