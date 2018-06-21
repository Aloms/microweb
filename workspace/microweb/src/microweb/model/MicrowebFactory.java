package microweb.model;

import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

public interface MicrowebFactory {
	public void loadSites(ServletContext context, String sitesConfigPath);
	
	public void loadComponent(URL configUrl);

	public void loadCore(URL microwebConfigPath, URL microwebSchemaPath);
	
	public void init();
}
