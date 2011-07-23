package it.uninsubria.dicom.cryptosocial.client;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import it.uninsubria.dicom.cryptosocial.server.ResourceRepository;
import it.uninsubria.dicom.cryptosocial.shared.CryptoInterface;
import it.uninsubria.dicom.cryptosocial.shared.EncryptedResource;
import it.uninsubria.dicom.cryptosocial.shared.Resource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
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
	private ClientProperties	properties;
	private ClientDatabase	database;
	private CryptoInterface	cryptoInterface;
	private KeyGeneration	keyGeneration;
	private AsymmetricCipherKeyPair keyPair;
	private EncryptedResource resource = context.mock(EncryptedResource.class);
	
	@Before
	public void setUp() throws Exception {
		repository = context.mock(ResourceRepository.class);
		properties = context.mock(ClientProperties.class);
		database = context.mock(ClientDatabase.class);
		cryptoInterface = context.mock(CryptoInterface.class);
		keyGeneration = context.mock(KeyGeneration.class);
		
		cs = new CryptoSocial(properties, database, cryptoInterface, keyGeneration, repository);
	}

	@After
	public void tearDown() throws Exception {
		cs = null;
	}

	@Test
	public void testRegisterUser() {
		keyPair = context.mock(AsymmetricCipherKeyPair.class);
		@SuppressWarnings("serial")
		final List<String> friendList = new ArrayList<String>() {{
			add("paperino");
			add("pluto");
		}};
		
		context.checking(new Expectations() {{
			//oneOf(database).updateKeys(with(equalTo(UID)), with(equal(keyPair)));
			atLeast(1).of(database).isUserRegistered(with(any(String.class))); will(returnValue(true));
			atLeast(1).of(database).insertFriendship(with(any(String.class)), with(equal(UID)));
			allowing(database).insertFriendship(with(equal(UID)), with(any(String.class)));
			oneOf(database).addUser(with(equal(UID)));
			oneOf(database).getFriendsList(with(equal(AT))); will(returnValue(friendList));
			atLeast(1).of(keyGeneration).propagate(with(any(String.class)), with(equal(UID)));
		}});
		
		cs.registerUser(UID);
	}

	@Test
	public void testRetrieveResourceNotFoundKey() {
		
		
		context.checking(new Expectations() {{
			oneOf(repository).getResource(with(any(ResourceID.class))); will(returnValue((Resource)resource));
			oneOf(database).enumerateUserKeys(with(equal(UID))); will(returnIterator(new ArrayList<CipherParameters>()));
		}});
		
		byte[] retrievedResource = cs.retrieveResource(RID, UID);
		
		assertThat(null, is(equalTo(retrievedResource)));
	}
	
	@Test
	public void testRetrieveResourceNotFoundResource() {
		context.checking(new Expectations() {{
			oneOf(repository).getResource(with(any(ResourceID.class))); will(returnValue(null));
			allowing(database).enumerateUserKeys(with(equal(UID)));
		}});
		
		byte[] retrievedResource = cs.retrieveResource(RID, UID);
		
		assertThat(null, is(equalTo(retrievedResource)));
	}
	
	@Test
	public void testRetrieveResourceFoundKey() {
		fail("Not implemented yet");

		context.checking(new Expectations() {{
			oneOf(repository).getResource(with(any(ResourceID.class))); will(returnValue((Resource)resource));
		//	oneOf(database).enumerateUserKeys(with(equal(UID))); will(return)
		}});
		
		byte[] retrievedResource = cs.retrieveResource(RID, UID);
		
		assertThat(null, is(equalTo(retrievedResource)));
	}

	@Test
	public void testSearchResources() {
		String expected = "[\n\t{\n\t\"id\": \"1\",\n\t\"name\": \"pippo\"\n\t},\n\t{\n\t\"id\": \"2\",\n\t\"name\": \"pluto\"\n\t}\n]";
		
		@SuppressWarnings("serial")
		final List<ResourceID> res = new ArrayList<ResourceID>() {{
			add(new ResourceID("pippo", 1));
			add(new ResourceID("pluto", 2));
		}};
		
		context.checking(new Expectations() {{
			oneOf(repository).searchResources(with(equal(QUERY))); will(returnIterator(res));
		}});
		
		String result = cs.searchResources(QUERY);
		
		assertThat(result.trim(), is(equalTo(expected)));
	}

	@Test
	public void testPublishResource() {
		fail("Not yet implemented"); // TODO
	}

}
