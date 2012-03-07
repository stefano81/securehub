package it.uninsubria.dicom.cryptosocial.shared;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResourceIDTest {
	private static final String RESOURCENAME = "RESOURCENAME";
	private static final Long ID = new Long(111111111);
	
	ResourceID rid;

	@Before
	public void setUp() throws Exception {
		rid = new ResourceID(RESOURCENAME, ID);
	}

	@After
	public void tearDown() throws Exception {
		rid = null;
	}

	@Test
	public void testGetName() {
		assertThat(RESOURCENAME, is(equalTo(rid.getName())));
	}

	@Test
	public void testGetID() {
		assertThat(ID, is(equalTo(rid.getID())));
	}

}
