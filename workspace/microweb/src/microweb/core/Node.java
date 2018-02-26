package microweb.core;

public abstract class Node {
	
	public static final int SECTION = 1;
	public static final int REDIRECT = 2;
	
	protected int type;
	
	private String name;
	private String uri;
	
	protected Node(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public String getName() {
		return name;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}
