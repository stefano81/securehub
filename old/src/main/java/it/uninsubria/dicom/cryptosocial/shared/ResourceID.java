package it.uninsubria.dicom.cryptosocial.shared;

public class ResourceID {
	private String	name;
	private Long	id;

	public ResourceID(String name, Long l) {
		this.name = name;
		this.id = l;
	}
	
	public ResourceID(Long id) {
		this(null, id);
	}

	public boolean equals(ResourceID resID) {
		return this.equals(resID);
	}
	
	public String getName() {
		return this.name;
	}
	
	public Long getID() {
		return this.id;
	}
}
