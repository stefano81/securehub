package it.uninsubria.dicom.cryptosocial.server;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.util.Iterator;

import it.uninsubria.dicom.cryptosocial.shared.ResourceID;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class ResourceStorerFBTest {
	private static final Mockery context = new JUnit4Mockery(); 
	
	private static final String QUERY = "TESTING QUERY";
	
	ServerDatabase database;
	
	private ResourceStorerFB rsfb;

	@Before
	public void setUp() throws Exception {
		database = context.mock(ServerDatabase.class);
		
		rsfb = new ResourceStorerFB(database);
	}

	@After
	public void tearDown() throws Exception {
		rsfb = null;
	}

	@Test
	public void testStoreResource() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSearchResources() {
		context.checking(new Expectations() {{
			oneOf(database).searchResources(with(equalTo(QUERY)));
		}});
		
		Iterator<ResourceID> iterator = rsfb.searchResources(QUERY);
	}

	@Test
	public void testGetResource() {
		fail("Not yet implemented"); // TODO
	}
}
