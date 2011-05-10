package it.uninsubria.dicom.cryptosocial.server;

import it.uninsubria.dicom.cryptosocial.shared.PostgresDatabase;
import it.uninsubria.dicom.cryptosocial.shared.Resource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;

import java.util.List;

public class ResourceStorerFB implements ResourceRepository {
	private final ServerDatabase serverDatabase;
	
	private static ResourceRepository repository;
	
	protected ResourceStorerFB(ServerDatabase serverDatabase) {
		this.serverDatabase = serverDatabase;
	}
	
	public ResourceStorerFB() {
		this(PostgresDatabase.getServerInstance());
	}

	@Override
	public void storeResource(Resource res) {
		
	}

	@Override
	public List<ResourceID> searchResource(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getResource(ResourceID id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ResourceRepository getInstance() {
		if (null == repository) {
			repository = new ResourceStorerFB(); 
		}
		
		return repository;
	}
}
