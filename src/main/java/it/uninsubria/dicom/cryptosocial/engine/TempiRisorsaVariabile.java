package it.uninsubria.dicom.cryptosocial.engine;

import java.io.FileOutputStream;
import java.util.Random;

import org.bouncycastle.crypto.CipherParameters;

public class TempiRisorsaVariabile {


	public static void main(String[] args) throws Exception {
		int max= 1024;
		int i;
		int k;
		byte[] data;
		@SuppressWarnings("unused")
		byte[] data2;
		String s;
		long tmp= 0;
		long tmp2= 0;
		long tmp3= 0;
		long tmp4= 0;
		Random gen;
		FileOutputStream fos;
		CryptoSocial cs;
		SocialParam sp;
		EncResources er;
		int[] vettore= new int[25];
		CipherParameters searchKey;
		
		
		//Inizializzazione
		gen= new Random();
		fos= new FileOutputStream("TempiRisorsaVar.xls");
		cs= new CryptoSocial();
		sp= cs.init(25);
		
		for(i= 0; i < 25; i++)
			vettore[i]= 1;
		
		searchKey= cs.link(sp.getPrivateKey(), vettore);
		
		s= new String ("Dimensione\tvettore:\t25\n\nKb\tCifratura\tDecifratura\n");
		fos.write(s.getBytes());
	
		for (i= 1; i <= max; i++) {
			s= new String (i + "\t");
			fos.write(s.getBytes());
			
			data= new byte[1024*i];
			gen.nextBytes(data);
			
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
