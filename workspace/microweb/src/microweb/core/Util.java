package microweb.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import microweb.model.Domain;
import microweb.model.Site;

public class Util {

	public static final String TEMPLATE_PATH = "/WEB-INF/templates";
	public static final String SITES_KEY = "sites";
	public static final String DOMAINS_KEY = "domains";
	public static final String SITE_CANONICAL_DOMAIN_KEY = "canonicalDomains";
	
	
	
	private static Logger logger = Logger.getLogger(Util.class.getPackage().getName());
	
	private static Properties properties = new Properties();
	private static ServletContext context = null;
	
	public static void init(URL propertiesPath, ServletContext servletContext) {

		try (InputStream in = propertiesPath.openStream()){
			Reader reader = new InputStreamReader(in, "UTF-8"); // for example
			properties.load(reader);
			
			context = servletContext;
			
			logger.info("Initialised microweb properties using: " + propertiesPath.toExternalForm());
			logger.info(properties.toString());
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to read core microweb properties from file: " + propertiesPath.toExternalForm(), e);
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

	public static Map<String, Domain> getDomainRegistry() {
		Map<String, Domain> domains = (Map<String, Domain>) context.getAttribute(DOMAINS_KEY);
		
		if (domains == null) {
			domains = new ConcurrentHashMap<String, Domain>();
			context.setAttribute(DOMAINS_KEY, domains);
		}
		return domains;
	}
	
	public static Map<String, Site> getSiteRegistry() {
		Map<String, Site> sites = (Map<String, Site>) context.getAttribute(SITES_KEY);
		
		if (sites == null) {
			sites = new ConcurrentHashMap<String, Site>();
			context.setAttribute(SITES_KEY, sites);
		}
		return sites;
	}
	
	public static Map<String, Domain> getSiteCanonicalDomainsRegistry() {
		Map<String, Domain> siteCanonicalDomains = (Map<String, Domain>) context.getAttribute(SITE_CANONICAL_DOMAIN_KEY);
		
		if (siteCanonicalDomains == null) {
			siteCanonicalDomains = new ConcurrentHashMap<String, Domain>();
			context.setAttribute(SITE_CANONICAL_DOMAIN_KEY, siteCanonicalDomains);
		}
		return siteCanonicalDomains;
	}
}
