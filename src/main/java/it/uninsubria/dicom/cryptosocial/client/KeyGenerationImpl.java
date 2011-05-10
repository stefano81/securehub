package it.uninsubria.dicom.cryptosocial.client;

import it.uninsubria.dicom.cryptosocial.server.ConnectionPool;
import it.uninsubria.dicom.cryptosocial.server.ConnectionPoolException;
import it.uninsubria.dicom.cryptosocial.server.DatabasePoolImplPostgres;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HHVEIP08SearchKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08PrivateKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08SearchKeyGenerationParameters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bouncycastle.crypto.CipherParameters;

public class KeyGenerationImpl extends Thread implements KeyGeneration {
	private static KeyGenerationImpl INSTANCE;
	private static final Logger logger = Logger.getLogger(KeyGenerationImpl.class.toString()); 
	private final ConnectionPool connectionPool;
	
	public static long SLEEPTIME = 5000l;
	
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
			this(emitter, receiver, null);
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
	
	private KeyGenerationImpl() {
		toGenerate = Collections.synchronizedList(new LinkedList<WorkItem>());
		connectionPool = DatabasePoolImplPostgres.getInstance();
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
		final String getUserKeyQuery = "SELECT private_key FROM users WHERE uid = ?";
		final String insertKey = "INSERT INTO keys (owner, key) VALUES (?, ?)";
		
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
			
			try {
				Connection connection = connectionPool.getConnection();
			
				PreparedStatement getUserKeyStatement = connection.prepareCall(getUserKeyQuery);
				getUserKeyStatement.setString(1, wi.getEmitter());
				
				ResultSet getUserKeyRS = getUserKeyStatement.executeQuery();
				
				if (getUserKeyRS.next()) {
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(getUserKeyRS.getBytes("private_key")));
					CipherParameters privateKey = (CipherParameters) ois.readObject();
					
					// get resources of emitter
					if (wi.isPolicySet()) {
						logger.info("Initial generation: (" + wi.getEmitter() + ", " + wi.getReceiver() + ", " + Arrays.toString(wi.getPolicy()) + ")");
						
						HHVEIP08SearchKeyGenerator generator = new HHVEIP08SearchKeyGenerator();
				        generator.init(new HVEIP08SearchKeyGenerationParameters((HVEIP08PrivateKeyParameters) privateKey, wi.getPolicy()));

				        CipherParameters searchKey = generator.generateKey();
				        
				        PreparedStatement ps = connection.prepareStatement(insertKey);
				        ps.setString(1, wi.getReceiver());
				        ps.setBytes(2, CryptoSocial.convertKeysToBytes(searchKey));
				        
				        if (1 == ps.executeUpdate()) {
				        	
				        } else {
				        	logger.warning("Error generating key for ");
				        }
					} else {
						logger.info("Delegation: (" + wi.getEmitter() + ", " + wi.getReceiver() + ")");

						// TODO
					}
				}
			} catch (ConnectionPoolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
