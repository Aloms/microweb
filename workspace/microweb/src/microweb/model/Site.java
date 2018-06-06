package microweb.model;

import java.util.List;
import java.util.Properties;

import microweb.model.PageHandler;


public interface Site {

	public static final int STATUS_INVALID = 1;
	public static final int STATUS_OFFLINE = 2;
	public static final int STATUS_ONLINE = 3;
	public static final int STATUS_MAINTENANCE = 4;
	
	public String getName();

	public int getStatus();
	public void setStatus(int status);

	
	public PageHandler getPageHandler(String uri);
	
}
