package microweb.model;

import java.util.List;
import java.util.Properties;

public interface Site {

	public String getName();
	
	public String getContext();

	public List<Domain> getDomains();
	
	/*
	public Properties getPageRegistry();
	
	public Section getSectionByURI(String uri);
	 */
}
