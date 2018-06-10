package microweb.model;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import microweb.model.PageHandler;


public interface Site {

	public static final Status STATUS_CONFIGURED = () -> {return "Configured";};
	public static final Status STATUS_INVALID = () -> {return "Invalid";};
	public static final Status STATUS_OFFLINE = () -> {return "Offline";};
	public static final Status STATUS_ONLINE = () -> {return "Online";};
	public static final Status STATUS_MAINTENANCE = () -> {return "Maintenance";};
	
	public String getName();

	public Status getStatus();
	public void setStatus(Status status);
	
	public Domain getCanonicalDomain();
	public Collection<Domain> getDomains();

	
	public PageHandler getPageHandler(String uri);
}
