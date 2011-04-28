package it.uninsubria.dicom.cryptosocial.engine;

import java.io.FileOutputStream;
import java.util.Random;

import org.bouncycastle.crypto.CipherParameters;

public class TempiVettoreVariabile {


	public static void main(String[] args) throws Exception {
		int max= 50;
		int i;
		int k;
		@SuppressWarnings("unused")
		byte[] data, data2;
		String s;
		long tmp= 0;
		long tmp2= 0;
		long tmp3= 0;
		long tmp4= 0;
		Random gen;
		FileOutputStream fos;
		CryptoSocial cs;
		SocialParam sp;
		int[] vettore;
		int j;
		CipherParameters searchKey;
		EncResources er= new EncResources();
		
		
		//Inizializzazione
		gen= new Random();
		fos= new FileOutputStream("TempiVettoreVar.xls");
		cs= new CryptoSocial();
		data= new byte[1024*512];
		gen.nextBytes(data);
				
		s= new String ("Dimensione\trisorsa:\t512Kb\n\nLunghezza\tCifratura\tDecifratura\n");
		fos.write(s.getBytes());
	
		for (i= 10; i <= max; i+=5) {
			sp= cs.init(i);
			s= new String (i + "\t");
			fos.write(s.getBytes());
			
			// Inizializzazione vettore di dimensione i
			vettore= new int[i];
			for (j= 0; j < i; j++)
				vettore[j]= 1;
			
			searchKey= cs.link(sp.getPrivateKey(), vettore);

			for (k= 0; k < 10; k++) {
				tmp= System.currentTimeMillis();				
				/* crypt  */	
				er= cs.enc(sp, vettore, data);				
				tmp= (System.currentTimeMillis() - tmp);
				tmp2+= tmp;
				
				tmp3= System.currentTimeMillis();
				/* decrypt  */
				data2= cs.dec(er, searchKey);								
				tmp3= (System.currentTimeMillis() - tmp3);
				tmp4+= tmp3;		
			}
			
			s= new String(String.valueOf(tmp2/ 10) + "\t");
			fos.write(s.getBytes());
			s= new String(String.valueOf(tmp4/ 10) + "\n");
			fos.write(s.getBytes());
		}

		fos.close();


	}

}
