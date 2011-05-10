package it.uninsubria.dicom.cryptosocial.shared;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

import java.util.Random;

import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EncryptedResourceTest {
	private EncryptedResource resource;
	private byte[] res1;
	private byte[] key1;
	private Random random;
	
	private static int DIM = 100;
	
	@Before
	public void setUp() {
		random = new Random();
		res1 = new byte[DIM];
		key1 = new byte[DIM];
		
		random.nextBytes(res1);
		random.nextBytes(key1);
		
		resource = new EncryptedResource(res1, key1);
	}
	
	@After
	public void tearDown() {
		resource = null;
		res1 = null;
		key1 = null;
		
		random = null;
	}

	@Test
	public void testGetResource() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetKey() {
		fail("Not yet implemented");
	}

}
