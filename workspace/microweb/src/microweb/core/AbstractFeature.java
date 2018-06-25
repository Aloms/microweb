package microweb.core;

import microweb.model.Feature;

public class AbstractFeature implements Feature{

	private String name;

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
		
	}
	
	
}
