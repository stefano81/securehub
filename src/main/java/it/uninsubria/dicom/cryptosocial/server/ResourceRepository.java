package it.uninsubria.dicom.cryptosocial.server;

import java.util.List;

import it.uninsubria.dicom.cryptosocial.shared.Resource;
import it.uninsubria.dicom.cryptosocial.shared.ResourceID;

public interface ResourceRepository {
	public abstract void storeResource(Resource res);
	public abstract List<ResourceID> searchResource(String name);
	public abstract Resource getResource(ResourceID id);
}
