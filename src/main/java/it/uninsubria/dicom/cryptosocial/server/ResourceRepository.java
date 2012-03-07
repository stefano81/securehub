package it.uninsubria.dicom.cryptosocial.server;

import it.uninsubria.dicom.cryptosocial.shared.Resource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;

import java.util.Iterator;

public interface ResourceRepository {
	public abstract ResourceID storeResource(String uid, String name, Resource res);
	public abstract Iterator<ResourceID> searchResources(String name);
	public abstract Resource getResource(ResourceID id) throws ResourceNotFoundException;
}
