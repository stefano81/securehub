package it.uninsubria.dicom.cryptosocial.engine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class EncResources implements Serializable {
	private static final long serialVersionUID = 7989624380900246758L;
	private byte[] resCrypt;
	private byte[] AESCrypt;
	
	public EncResources() {		
	}
	
	public EncResources(byte[] res, byte[] res2) {
		this.resCrypt= res;
		this.AESCrypt= res2;
	}
	
	public byte[] getResCrypt() {
		return this.resCrypt;
	}

	public byte[] getAESCrypt() {
		return this.AESCrypt;
	}
	
	public void saveResources(String path) throws Exception {
		ObjectOutputStream oos= new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(resCrypt);
		oos.writeObject(AESCrypt);
		oos.close();
	}
	
	public void loadResources(String path) throws Exception {
		ObjectInputStream ois= new ObjectInputStream(new FileInputStream(path));
		resCrypt= (byte[]) ois.readObject();
		AESCrypt= (byte[]) ois.readObject();
		ois.close();
	}

}
