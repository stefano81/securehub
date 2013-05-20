package it.uninsubria.dicom.cryptosocial.server;

import java.util.Iterator;

import it.uninsubria.dicom.cryptosocial.shared.Resource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;

public interface ServerDatabase {
	public Resource getResource(ResourceID id);
	public Iterator<ResourceID> searchResources(String name);
	public ResourceID insertResource(String uid, String name, Resource resource);
}
