package microweb.core;

import microweb.model.Extension;

public class AbstractExtension implements Extension{

	private String name;

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
		
	}
	
	
}
