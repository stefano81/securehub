package it.uninsubria.dicom.cryptosocial.server;

import it.uninsubria.dicom.cryptosocial.shared.PostgresDatabase;
import it.uninsubria.dicom.cryptosocial.shared.Resource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;

import java.util.Iterator;

public class ResourceStorerFB implements ResourceRepository {
	private final ServerDatabase database;
	
	private static ResourceRepository repository;
	
	protected ResourceStorerFB(ServerDatabase database) {
		this.database = database;
	}
	
	public ResourceStorerFB() {
		this(PostgresDatabase.getServerInstance());
	}

	@Override
	public ResourceID storeResource(String uid, String name,Resource resource) {
		return database.insertResource(uid, name, resource);
	}

	@Override
	public Iterator<ResourceID> searchResources(String name) {		
		return database.searchResources(name);
	}

	@Override
	public Resource getResource(ResourceID id) {
		return database.getResource(id);
	}
	
	public static ResourceRepository getInstance() {
		if (null == repository) {
			repository = new ResourceStorerFB(); 
		}
		
		return repository;
	}
}
