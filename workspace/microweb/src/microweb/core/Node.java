package microweb.core;

public abstract class Node {
	
	public static final int SECTION = 1;
	public static final int REDIRECT = 2;
	
	protected int type;
	
	private String name;
	private String uri;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}
