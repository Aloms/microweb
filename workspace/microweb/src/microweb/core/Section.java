package microweb.core;

public class Section extends Node {

	private String template;
	private String canonicalUri;
	
	public Section() {
		this.type = this.SECTION;
	}
	
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public String getCanonicalUri() {
		return canonicalUri;
	}
	public void setCanonicalUri(String canonicalUri) {
		this.canonicalUri = canonicalUri;
	}
	
}
