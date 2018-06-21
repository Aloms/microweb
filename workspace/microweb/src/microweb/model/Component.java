package microweb.model;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public interface Component {

	public Properties getParameters();
	public String getName();
	public Logger getLogger();
	public List<Handler> getHandlers();
	public String getHome();
	public void invokeEvent(String name);
}
