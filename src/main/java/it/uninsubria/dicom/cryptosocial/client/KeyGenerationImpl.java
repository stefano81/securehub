package it.uninsubria.dicom.cryptosocial.client;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HHVEIP08AttributesOnlySearchKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PrivateKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08SearchKeyGenerationParameters;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;

public class KeyGenerationImpl extends Thread implements KeyGeneration {
	private static final Logger logger = Logger.getLogger(KeyGenerationImpl.class);  
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
			return null != policy;
		}
	}
	
	public List<WorkItem> toGenerate;
	private HHVEIP08AttributesOnlySearchKeyGenerator	generator;
	
	protected KeyGenerationImpl(ClientDatabase database) {
		toGenerate = new LinkedList<WorkItem>();
		this.database = database;
		
		this.generator = new HHVEIP08AttributesOnlySearchKeyGenerator();
	}
	
	@Override
	public synchronized void generate(String emitter, String receiver, int ... policy) {
		logger.debug("Generate from:" + emitter + " to " + receiver + " on " + Arrays.toString(policy));
		WorkItem wi = new WorkItem(emitter, receiver, policy);
		
		toGenerate.add(wi);
		
		this.notify();
	}

	@Override
	public synchronized void propagate(String emitter, String receiver) {
		logger.debug("Propagate from:" + emitter + " to " + receiver);
		
		WorkItem wi = new WorkItem(emitter, receiver);
		
		toGenerate.add(wi);
		
		this.notify();
	}
	
	@Override
	public void run() {
		while(true) {
			synchronized(toGenerate) {
				try {
					while(toGenerate.isEmpty()) {
						logger.debug("Empty: wait");
						this.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				logger.debug("Woken up");
			}
			
			WorkItem wi = toGenerate.remove(0);
			
			CipherParameters privateKey = database.getUserPrivateKey(wi.getEmitter());
			
			if (null != privateKey) {				
				// get resources of emitter
				if (wi.isPolicySet()) {
					logger.debug("Initial generation: (" + wi.getEmitter() + ", " + wi.getReceiver() + ", " + Arrays.toString(wi.getPolicy()) + ")");
					
			        generator.init(new HVEIP08SearchKeyGenerationParameters((HVEIP08PrivateKeyParameters) privateKey, wi.getPolicy()));

			        CipherParameters searchKey = generator.generateKey();
			      
			        int key = -1;
			        
			        if (-1 != (key = database.insertKey(wi.getReceiver(), wi.getEmitter(), searchKey))) {
			        	logger.debug("Key inserted, what to do now?");
			        	
			        	Iterator<String> friends = database.getUserFriends(wi.getReceiver());
			        	
			        	while  (friends.hasNext()) {
			        		this.propagate(wi.getReceiver(), friends.next(), key);
			        	}
			        } else {
						logger.error("Error generating key for (" + wi.getEmitter() + ", " + wi.getReceiver() + ")");
			        }
				} else {
					logger.debug("Delegation: (" + wi.getEmitter() + ", " + wi.getReceiver() + ")");

					// TODO
				}
			} else {
				logger.error("Private Key not found");
			}
		}
	}

	protected void propagate(String receiver, String next, int key) {
		logger.debug("Propagate key: (" + receiver + ", " + next + ", " + key + ")");
		
		// TODO
	}

	public static KeyGeneration getInstance(ClientDatabase clientDatabase) {
		if (null == instance) {
			instance = new KeyGenerationImpl(clientDatabase);
			((KeyGenerationImpl)instance).start();
		}
		
		return instance;
	}
}
