package microweb.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Site {

	private String name;
	private Map<String, Node> urlRegistry;
	
	public Site() {
		this.urlRegistry = new ConcurrentHashMap<String, Node>();
	}
	public Site(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public Map<String, Node> getUrlRegistry() {
		return urlRegistry;
	}

	
	
}
