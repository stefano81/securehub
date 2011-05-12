package it.uninsubria.dicom.cryptosocial.client;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HHVEIP08SearchKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PrivateKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08SearchKeyGenerationParameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bouncycastle.crypto.CipherParameters;

public class KeyGenerationImpl extends Thread implements KeyGeneration {
	private static final Logger logger = Logger.getLogger(KeyGenerationImpl.class.toString()); 
	private final ClientDatabase database;
	
	public static long SLEEPTIME = 5000l;
	private static KeyGeneration	instance;
	
	private class WorkItem {
		private String	emitter;
		private String	receiver;
		private int[]	policy;

		public WorkItem(String emitter, String receiver, int ... policy) {
			this.emitter = emitter;
			this.receiver = receiver;
			this.policy = Arrays.copyOf(policy, policy.length);
		}
		
		public WorkItem(String emitter, String receiver) {
			this.emitter = emitter;
			this.receiver = receiver;
		}

		public String getEmitter() {
			return emitter;
		}

		public String getReceiver() {
			return receiver;
		}

		public int[] getPolicy() {
			return policy;
		}
		
		public boolean isPolicySet() {
			return null == policy;
		}
	}
	
	public List<WorkItem> toGenerate;
	
	public KeyGenerationImpl(ClientDatabase database) {
		toGenerate = Collections.synchronizedList(new LinkedList<WorkItem>());
		this.database = database;
	}
	
	@Override
	public synchronized void generate(String emitter, String receiver, int ... policy) {
		WorkItem wi = new WorkItem(emitter, receiver, policy);
		
		toGenerate.add(wi);
		
		synchronized(this) {
			this.notify();
		}
	}

	@Override
	public synchronized void propagate(String emitter, String receiver) {
		WorkItem wi = new WorkItem(emitter, receiver);
		
		toGenerate.add(wi);
		
		synchronized(this) {
			this.notify();
		}
	}
	
	@Override
	public void run() {
		while(true) {
			while(toGenerate.isEmpty()) {
				synchronized(this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			WorkItem wi = toGenerate.remove(0);
			
			CipherParameters privateKey = database.getUserPrivateKey(wi.getEmitter());
			
			if (null != privateKey) {
				// get resources of emitter
				if (wi.isPolicySet()) {
					logger.info("Initial generation: (" + wi.getEmitter() + ", " + wi.getReceiver() + ", " + Arrays.toString(wi.getPolicy()) + ")");
					
					HHVEIP08SearchKeyGenerator generator = new HHVEIP08SearchKeyGenerator();
			        generator.init(new HVEIP08SearchKeyGenerationParameters((HVEIP08PrivateKeyParameters) privateKey, wi.getPolicy()));

			        CipherParameters searchKey = generator.generateKey();
			      
			        if (database.insertKey(wi.getReceiver(), searchKey)) {
			        	logger.info("Key inserted, what to do now?");
			        } else {
			        	logger.warning("Error generating key for ");
			        }
				} else {
					logger.info("Delegation: (" + wi.getEmitter() + ", " + wi.getReceiver() + ")");

					// TODO
				}
			}
		}
	}

	public static KeyGeneration getInstance(ClientDatabase clientDatabase) {
		if (null == instance)
			instance = new KeyGenerationImpl(clientDatabase);
		
		return instance;
	}
}
