package it.uninsubria.dicom.cryptosocial.shared;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import it.unisa.dia.gas.crypto.jpbc.fe.hve.ip08.params.HVEIP08Parameters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PostgresDatabaseTest {

	@Before
	public void setUp() throws Exception {}

	@After
	public void tearDown() throws Exception {}
	
	/*
	@Test
	public void testHHVESerialization() throws Exception {		
		AsymmetricCipherKeyPair keyPair = CryptoInterfaceFB.getInstance().generateKeyPair();
		
		HVEIP08PrivateKeyParameters private1 = (HVEIP08PrivateKeyParameters) keyPair.getPrivate();
		HVEIP08PublicKeyParameters public1 = (HVEIP08PublicKeyParameters) keyPair.getPublic();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		oos.writeObject(private1);
		oos.close();
		
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		
		HVEIP08PrivateKeyParameters private2 = (HVEIP08PrivateKeyParameters) ois.readObject();
		
		// From HVEIP08KeyParameters
		assertThat(private1.isPrivate(), is(equalTo(private2.isPrivate())));
		assertThat(private1.isPreProcessed(), is(equalTo(private2.isPreProcessed())));
		
		assertTrue(checkParameters(private1.getParameters(), private2.getParameters()));

		// From HVEIP08PrivateKeyParameters
		assertThat(private1.getY(), is(equalTo(private2.getY())));
		assertThat(private1.isPreProcessed(), is(equalTo(private2.isPreProcessed())));

		for (int i = 0; i < private1.getParameters().getN(); i++) {
			for (int j = 0; j < private1.getParameters().getAttributeNumAt(i); j++) {
				assertThat(private1.getTAt(i, j), is(equalTo(private2.getTAt(i, j))));
				assertThat(private1.getVAt(i, j), is(equalTo(private2.getVAt(i, j))));
				
				if (private1.isPreProcessed()) {
					assertThat(private1.getPreTAt(i, j), is(equalTo(private2.getPreTAt(i, j))));
					assertThat(private1.getPreVAt(i, j), is(equalTo(private2.getPreTAt(i, j))));
				}
			}
		}
		    
		baos = new ByteArrayOutputStream();
		oos = new ObjectOutputStream(baos);
		
		oos.writeObject(public1);
		oos.close();
		
		ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		
		HVEIP08PublicKeyParameters public2 = (HVEIP08PublicKeyParameters) ois.readObject();

		assertThat(public1.isPrivate(), is(equalTo(public2.isPrivate())));
		assertThat(public1.isPreProcessed(), is(equalTo(public2.isPreProcessed())));
		
		assertTrue(checkParameters(public1.getParameters(), public2.getParameters()));

		for (int i = 0; i < public1.getParameters().getN(); i++) {
			for (int j = 0; j < public1.getParameters().getAttributeNumAt(i); j++) {
				assertThat(public1.getTAt(i, j), is(equalTo(public2.getTAt(i, j))));
				assertThat(public1.getVAt(i, j), is(equalTo(public2.getVAt(i, j))));
				
				if (public1.isPreProcessed()) {
					assertThat(public1.getElementPowTAt(i, j), is(equalTo(public2.getElementPowTAt(i, j))));
					assertThat(public1.getElementPowVAt(i, j), is(equalTo(public2.getElementPowVAt(i, j))));
				}
			}
		}
	}
	*/
	
	private boolean checkParameters(HVEIP08Parameters p1, HVEIP08Parameters p2) {
	    assertThat(p1.getCurveParameters(), is(equalTo(p2.getCurveParameters())));
	    assertThat(p1.getG(), is(equalTo(p2.getG())));
	    assertThat(p1.getElementPowG(), is(equalTo(p2.getElementPowG())));
	    assertThat(p1.getN(), is(equalTo(p2.getN())));
	    assertThat(p1.getAttributeLengths(), is(equalTo(p2.getAttributeLengths())));
	    assertThat(p1.getAttributesLengthInBytes(), is(equalTo(p2.getAttributesLengthInBytes())));

	    assertThat(p1.isPreProcessed(), is(equalTo(p2.isPreProcessed())));
		
	    return true;
	}

	@Test
	public void testAddUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateKeys() {
		fail("Not yet implemented");
	}

	@Test
	public void testConvertKeysToBytes() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAccessToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testExistsUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testInsertFriendship() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetResource() {
		fail("Not yet implemented");
	}

	@Test
	public void testEnumerateUserKeys() {
		fail("Not yet implemented");
	}

	@Test
	public void testSearchResources() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPublicKey() {
		fail("Not yet implemented");
	}

	@Test
	public void testInsertResource() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserFriends() {
		fail("Not yet implemented");
	}

	@Test
	public void testInsertKey() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserPrivateKey() {
		fail("Not yet implemented");
	}
}
