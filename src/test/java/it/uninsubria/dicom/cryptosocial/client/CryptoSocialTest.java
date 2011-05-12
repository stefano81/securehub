package it.uninsubria.dicom.cryptosocial.client;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import it.uninsubria.dicom.cryptosocial.server.ResourceRepository;
import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.generators.HVEIP08KeyPairGenerator;

import javax.crypto.KeyGenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class CryptoSocialTest {
	private final Mockery context = new JUnit4Mockery() {{
		this.setImposteriser(ClassImposteriser.INSTANCE);
	}};
	
	private static String UID = "pippo";
	private static Integer RID = 123456789;
	private static String AT = "106571229431832|133bad2d4f8a28a0f169402d.1-1578452822|7n7g67rSoAWRP20bEXg4mEZrV2Q";
	private static String QUERY = "foo.bar";
	
	private CryptoSocial cs;

	private ResourceRepository	repository;
	private HVEIP08KeyPairGenerator	keyPairGenerator;
	private ClientProperties	properties;
	private ClientDatabase	database;
	private KeyGenerator	symmetricKeyGenerator;
	private KeyGeneration	keyGeneration;
	private AsymmetricCipherKeyPair keyPair;
	
	@Before
	public void setUp() throws Exception {
		repository = context.mock(ResourceRepository.class);
		keyPairGenerator = context.mock(HVEIP08KeyPairGenerator.class);
		properties = context.mock(ClientProperties.class);
		database = context.mock(ClientDatabase.class);
		symmetricKeyGenerator = context.mock(KeyGenerator.class);
		keyGeneration = context.mock(KeyGeneration.class);
		
		cs = new CryptoSocial(properties, database, symmetricKeyGenerator, keyGeneration, keyPairGenerator, repository);
	}

	@After
	public void tearDown() throws Exception {
		cs = null;
	}

	@Test
	public void testRegisterUser() {
		keyPair = context.mock(AsymmetricCipherKeyPair.class);
		
		context.checking(new Expectations() {{
			oneOf(keyPairGenerator).generateKeyPair(); will(returnValue(keyPair));
			oneOf(database).updateKeys(with(equal(UID)), with(equal(keyPair)));
			oneOf(database).getAccessToken(with(equal(UID))); will(returnValue(AT));
			allowing(database).existsUser(with(any(String.class))); will(returnValue(true));
			allowing(database).insertFriendship(with(any(String.class)), with(equal(UID)));
			allowing(database).insertFriendship(with(equal(UID)), with(any(String.class)));
			allowing(keyGeneration).propagate(with(any(String.class)), with(equal(UID)));
		}});
		
		cs.registerUser(UID);
	}

	@Test
	public void testRetrieveResource() {
		byte[] retrievedResource = cs.retrieveResource(RID, UID);
		
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSearchResources() {
		String expected = "[\n\n]\n".trim();
		
		context.checking(new Expectations() {{
			oneOf(repository).searchResources(with(equal(QUERY)));
		}});
		
		String result = cs.searchResources(QUERY);
		
		assertThat(result.trim(), is(equalTo(expected)));
	}

	@Test
	public void testPublishResource() {
		fail("Not yet implemented"); // TODO
	}

}
