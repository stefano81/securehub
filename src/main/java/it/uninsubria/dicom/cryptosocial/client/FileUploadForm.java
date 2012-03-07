package it.uninsubria.dicom.cryptosocial.client;

import javax.ws.rs.FormParam;

class FileUploadForm {
	@FormParam("filedata")
    //@PartType("application/octet-stream")
	public byte[] filedata;
    
    @FormParam("filename")
    //@PartType("text/plain")
    public String filename;
    
    @FormParam("uid")
    //@PartType("text/plain")
    public String uid;

    @FormParam("policy")
    //@PartType("text/plain")
    public String policy;

    public FileUploadForm() {}

	public byte[] getFiledata() {
		return filedata;
	}

	public void setFiledata(byte[] filedata) {
		this.filedata = filedata;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
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