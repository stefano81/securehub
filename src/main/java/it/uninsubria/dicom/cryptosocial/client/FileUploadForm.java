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
}