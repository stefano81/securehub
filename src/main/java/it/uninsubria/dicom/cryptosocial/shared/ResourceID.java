package it.uninsubria.dicom.cryptosocial.shared;

public class ResourceID {
	private String	name;
	private Integer	id;

	public ResourceID(String name, Integer id) {
		this.name = name;
		this.id = id;
	}
	
	public boolean equals(ResourceID resID) {
		return this.equals(resID);
	}
	
	public String getName() {
		return this.name;
	}
	
	public Integer getID() {
		return this.id;
	}
}
