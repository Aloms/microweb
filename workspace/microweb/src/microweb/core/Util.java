package microweb.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import microweb.model.Domain;
import microweb.model.Site;

public class Util {

	public static final String TEMPLATE_PATH = "/WEB-INF/templates";
	public static final String SITES_KEY = "sites";
	public static final String DOMAINS_KEY = "domains";
	public static final String SITE_CANONICAL_DOMAIN_KEY = "canonicalDomains";
	
	
	
	private static Logger logger = Logger.getLogger(Util.class.getPackage().getName());
	
	private static Properties applicationProperties = new Properties();
	private static Properties systemProperties = new Properties();
	
	private static ServletContext context = null;
	
	public static void init(URL microwebPropertiesPath, URL systemPropertiesPath, ServletContext servletContext) {

		context = servletContext;
		
		applicationProperties = readProperties(microwebPropertiesPath);
		systemProperties = readProperties(systemPropertiesPath);
		
		
	}
	
	private static Properties readProperties(URL url) {

		try (InputStream in = url.openStream()){
			Reader reader = new InputStreamReader(in, "UTF-8"); // for example
			Properties props = new Properties();
			props.load(reader);
			
			logger.info("Initialised properties using: " + url.toExternalForm());
			
			return props;
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "microweb.application.config.initialisationFailed", new Object[] {url.toExternalForm()});
		}
		
		return null;
	}
	
	public static String getApplicationProperty(String key) {
		return applicationProperties.getProperty(key);
	}
	
	public static String getSystemProperty(String key) {
		return systemProperties.getProperty(key);
	}
	/*
	public static String getMicrowebPath() {
		return "/" + Util.getApplicationConfig().getProperty("microweb-context");
	}
	
	public static String getControllerPath() {
		return getMicrowebPath() + "/" + Util.getApplicationConfig().getProperty("controller");
	}
	*/
	
	/*
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
	*/
	
	/*
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
	*/

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
	
	public static void validateXML(URL xmlPath, URL xsdPath) throws SAXException, IOException {
		
		if (xmlPath == null) {
			throw new RuntimeException("No url given for xmlPath, failed to validate xml.");
		}
		
		if (xsdPath == null) {
			throw new RuntimeException("No url given for xsdpath, failed to validate xml: " + xmlPath.toExternalForm());
		}
		
		InputStream xsdFile = xsdPath.openStream(); 
		SchemaFactory factory =  SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdFile));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlPath.openStream()));
	}
}
