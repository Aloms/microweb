package microweb.model;

public interface Domain {
	public String getName();
	public boolean isCanonical();
	public Site getSite();
}
