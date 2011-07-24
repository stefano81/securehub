package it.uninsubria.dicom.cryptosocial.client;

import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

class FileUploadForm {
	@FormParam("filedata")
    @PartType("application/octet-stream")
    private byte[] filedata;
    
    @FormParam("filename")
    @PartType("text/plain")
	private String filename;
    
    @FormParam("uid")
    @PartType("text/plain")
    private String uid;

    @FormParam("policy")
    @PartType("text/plain")
	private String policy;

    public FileUploadForm() {}
    
    public String getFileName() {
    	return this.filename;
    }
    
    
    public void setFileName(final String filename) {
    	this.filename = filename;
    }

    public byte[] getFileData() {
        return this.filedata;
    }

    
    public void setFileData(final byte[] filedata) {
        this.filedata = filedata;
    }

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPolicy() {
		return policy;
	}
	
	public void setPolicy(String policy) {
		this.policy = policy;
	}
}