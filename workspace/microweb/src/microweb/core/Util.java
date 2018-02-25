package microweb.core;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {

	public static final String TEMPLATE_PATH = "/WEB-INF/templates";
	public static final String SITES_KEY = "sites";
	
	
	private static Logger logger = Logger.getLogger(Util.class.getPackage().getName());
	
	private static Properties properties = new Properties();
	
	private static boolean loaded;
	
	public static void init() {
		init(false);
	}
	
	public static void init(boolean reload) {
		if(reload || !loaded) {
			
			loaded = true;
			
			properties.setProperty("microweb-context", "site");
			properties.setProperty("controller", "Controller");
		}
	}
	
	public static Properties getConfig() {
		return properties;
	}
	
	public static String getMicrowebPath() {
		return "/" + Util.getConfig().getProperty("microweb-context");
	}
	
	public static String getControllerPath() {
		return getMicrowebPath() + "/" + Util.getConfig().getProperty("controller");
	}
	
	
	
	public static boolean isController(String path) {
		
		if (path == null || path.equals("") || path.trim().equals("")) {
			return false;
		}
		
		String controllerPath =  getControllerPath();
		
		if (logger.isLoggable(Level.FINEST)) {
			logger.finer("path: " + path);
			logger.finer("controllerPath: " + controllerPath);
		}
		
		if (path.matches(controllerPath + "/?")) {
			return true;
		}
		
		return false;
		
	}
	
	public static boolean isMicrowebRoot(String path) {
		
		if (path == null || path.equals("") || path.trim().equals("")) {
			return false;
		}
		
		String microwebPath = getMicrowebPath();
		if (path.matches(microwebPath + "/?")) {
			return true;
		}
		
		return false;
		
	}
}
