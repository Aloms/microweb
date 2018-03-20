package microweb.model;

import java.util.Properties;

public interface Site {

	public String getName();
	
	public Properties getPageRegistry();
	
	public Section getSectionByURI(String uri);
}
