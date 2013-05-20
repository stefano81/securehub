package it.uninsubria.dicom.cryptosocial.client;

import it.uninsubria.dicom.cryptosocial.shared.CryptoInterface;

import java.util.Arrays;
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
		private int		depth;
		private CipherParameters key;

		public WorkItem(String emitter, String receiver, int ... policy) {
			this.emitter = emitter;
			this.receiver = receiver;
			this.depth = 1;
			this.policy = Arrays.copyOf(policy, policy.length);
		}
		
		public WorkItem(String emitter, String receiver, CipherParameters key, int depth, int ... policy) {
			this.emitter = emitter;
			this.receiver = receiver;
			this.depth = depth;
			this.key = key;
			this.policy = Arrays.copyOf(policy, policy.length);
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
		
		public boolean isKeySet() {
			return null != key;
		}

		public CipherParameters getKey() {
			return key;
		}

		public int getDepth() {
			return depth;
		}
	}
	
	public List<WorkItem> toGenerate;
	private CryptoInterface	cryptoInterface;
	
	protected KeyGenerationImpl(ClientDatabase database, CryptoInterface cryptoInterface) {
		toGenerate = new LinkedList<WorkItem>();
		this.database = database;
		
		this.cryptoInterface = cryptoInterface;
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
		
		// TODO
		// scan for every key belonging to emitter and propagate it to the receiver
		/*WorkItem wi = new WorkItem(emitter, receiver);
		
		toGenerate.add(wi);
		
		this.notify();*/
	}
	
	@Override
	public void run() {
		return;
		/*
		while(true) {
			synchronized(toGenerate) {
				try {
					while(toGenerate.isEmpty()) {
						logger.debug("Empty: wait");
						this.wait();
					}
				} catch (InterruptedException e) {
					// Hope it will not happen
					e.printStackTrace();
				}
				
				logger.debug("Woken up");
			}
			
			WorkItem wi = toGenerate.remove(0);
			
			CipherParameters asymmetricKey = database.getUserPrivateKey(wi.getEmitter());
			
			if (null != asymmetricKey) {				
				// get resources of emitter
				if (wi.isKeySet()) {
					logger.debug("Initial generation: (" + wi.getEmitter() + ", " + wi.getReceiver() + ", " + Arrays.toString(wi.getPolicy()) + ")");

			        CipherParameters searchKey = cryptoInterface.keyGeneration(asymmetricKey, wi.getPolicy());
			        
			        database.insertKey(wi.getReceiver(), wi.getEmitter(), searchKey);
			        	
			        logger.debug("Key inserted, what to do now?");
			        	
		        	Iterator<String> friends = database.getUserFriends(wi.getReceiver());
		        	
		        	while  (friends.hasNext()) {
		        		this.propagate(wi.getReceiver(), friends.next(), searchKey);
		        	}
				} else {
					logger.debug("Delegation: (" + wi.getEmitter() + ", " + wi.getReceiver() + ")");

					// TODO
				}
			} else {
				logger.error("Private Key not found");
			}
		}*/
	}

	protected void propagate(String receiver, String next, CipherParameters	key) {
		logger.debug("Propagate key: (" + receiver + ", " + next + ", " + key + ")");
		
		// TODO
	}

	public static KeyGeneration getInstance(ClientDatabase clientDatabase, CryptoInterface cryptoInterface) {
		if (null == instance) {
			instance = new KeyGenerationImpl(clientDatabase, cryptoInterface);
			((KeyGenerationImpl)instance).start();
		}
		
		return instance;
	}
}
