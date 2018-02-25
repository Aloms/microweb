package microweb.core;

public class Redirect extends Node {
	
	private String redirect;

	public Redirect() {
		this.type = this.REDIRECT;
	}
	
	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
	
	
}
