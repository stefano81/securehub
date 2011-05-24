package it.uninsubria.dicom.cryptosocial.client;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class KeyGenerationImplTest {
	private final Mockery context = new JUnit4Mockery() {{
		setImposteriser(ClassImposteriser.INSTANCE);
	}};
	
	private ClientDatabase db;
	private String emitter = "EMITTER";
	private String receiver = "RECEIVER";
	private int[] policy = {1, 1, 1, 1, 1};
	
	private KeyGenerationImpl kg;

	@Before
	public void setUp() throws Exception {
		db = context.mock(ClientDatabase.class);
		
		kg = new KeyGenerationImpl(db);
	}

	@After
	public void tearDown() throws Exception {
		kg = null;
	}

	@Test
	public void testGenerate() throws Exception {
		kg.generate(emitter, receiver, policy);
		
		assertThat(1, is(equalTo(kg.toGenerate.size())));
	}

	@Test
	public void testPropagate() throws Exception {
		kg.propagate(emitter, receiver);
		
		assertThat(1, is(equalTo(kg.toGenerate.size())));
	}
}
