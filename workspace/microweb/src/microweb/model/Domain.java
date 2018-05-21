package microweb.model;

public interface Domain {

	public static final int TYPE_CANONICAL = 1;
	public static final int TYPE_ALIAS = 2;
	public static final int TYPE_REDIRECT = 3;
	
	public String getName();
	public int getType();
	public int getHttpRedirectCode();
	public Site getSite();
}
