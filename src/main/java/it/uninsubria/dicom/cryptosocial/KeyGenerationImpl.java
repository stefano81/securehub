package it.uninsubria.dicom.cryptosocial;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class KeyGenerationImpl extends Thread implements KeyGeneration {
	private static KeyGenerationImpl INSTANCE;
	public static long SLEEPTIME = 5000l;
	
	private class WorkItem {
		private String	emitter;
		private String	receiver;
		private String	resource;

		public WorkItem(String emitter, String receiver, String resource) {
			this.emitter = emitter;
			this.receiver = receiver;
			this.resource = resource;
		}
		
		public WorkItem(String emitter, String receiver) {
			this(emitter, receiver, null);
		}

		public String getEmitter() {
			return emitter;
		}

		public String getReceiver() {
			return receiver;
		}

		public String getResource() {
			return resource;
		}
		
		public boolean isSetResource() {
			return null == resource;
		}
	}
	
	public List<WorkItem> toGenerate;
	
	private KeyGenerationImpl() {
		toGenerate = Collections.synchronizedList(new LinkedList<WorkItem>());
	}

	@Override
	public synchronized void generate(String emitter, String receiver, String resource) {
		WorkItem wi = new WorkItem(emitter, receiver, resource);
		
		toGenerate.add(wi);
	}

	@Override
	public synchronized void propagate(String emitter, String receiver) {
		WorkItem wi = new WorkItem(emitter, receiver);
		
		toGenerate.add(wi);
	}
	
	@Override
	public void run() {
		while(true) {
			while(toGenerate.isEmpty()) {
				try {
					sleep(SLEEPTIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			WorkItem wi = toGenerate.remove(0);
			
			// get resources of emitter
			if (wi.isSetResource()) {
				
			}
			// get keys of emitter
			// generate keys for receiver
		}
	}

	public static KeyGeneration getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new KeyGenerationImpl();
			INSTANCE.start();
		}
		
		return INSTANCE;
	}
}
