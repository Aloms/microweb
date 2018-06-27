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
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import microweb.model.Component;
import microweb.model.Domain;
import microweb.model.Site;

public class Util {
	
	public static String MICROWEB_HOME = "/WEB-INF/";

	//public static final String TEMPLATE_PATH = "/WEB-INF/templates";
	private static final String SITES_KEY = "Sites";
	private static final String SERVICES_KEY = "Services";
	private static final String DOMAINS_KEY = "Domains";
	//private static final String SITE_CANONICAL_DOMAIN_KEY = "CanonicalDomains";
	
	public static final String REGISTRY_COMPONENTS = "MW_COMPONENTS";
	public static final String REGISTRY_SITES = "MW_SITES";
	public static final String REGISTRY_DOMAINS = "MW_DOMAINS";
	public static final String REGISTRY_EXTENSION_FACTORIES = "MW_EXTENSION_FACTORIES";
	
	
	
	private static Logger logger = Logger.getLogger(Util.class.getPackage().getName());
	
	private static Properties applicationProperties = new Properties();
	private static Properties systemProperties = new Properties();
	
	private static ServletContext context = null;
	
	private static Object monitor = new Object();
	
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
	public static Map<String, Domain> getDomainRegistry() {
		return (Map) createAndStore(DOMAINS_KEY, () -> {return new ConcurrentHashMap<String, Domain>();});
	}
	
	public static Map<String, Site> getSiteRegistry() {
		return (Map) createAndStore(SITES_KEY, () -> {return new ConcurrentHashMap<String, Site>();});
	}
	
	public static Map<String, Component> getComponentRegistry() {
		return (Map) createAndStore(SITES_KEY, () -> {return new ConcurrentHashMap<String, Component>();});
	}
	*/
	public static <T> Map<String, T> getRegistry(String name) {
		return (Map<String, T>) createAndStore(name, () -> {return new ConcurrentHashMap<String, T>();});
	}
	
	
	/*
	public static Map<String, Service> getServiceRegistry() {
		return (Map) createAndStore(SERVICES_KEY, () -> {return new ConcurrentHashMap<String, Service>();});
	}
	*/
	/*
	public static Map<String, Domain> getSiteCanonicalDomainsRegistry() {
		return (Map) createAndStore(SITE_CANONICAL_DOMAIN_KEY, () -> {return new ConcurrentHashMap<String, Domain>();});
	}
	*/
	
	private static <T> T createAndStore(String key, ObjectFactory<T> factory) {
		T o = (T) context.getAttribute(key);
		
		if (o == null) {
			
			synchronized(monitor) {
				o = (T) context.getAttribute(key);
				
				if (o == null) {
					o = factory.create();
					context.setAttribute(key, o);
					
					logger.info("loaded new Registry: " + key);
				}
			}
		}
		return o;
	}
	
	public static void validateXML(URL xmlPath, URL xsdPath) throws SAXException, IOException {
		
		InputStream xsdFile = xsdPath.openStream(); 
		SchemaFactory factory =  SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdFile));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlPath.openStream()));
        
	}
	
	public interface ObjectFactory<T> {
		public T create();
	}
	
	public static ServletContext getServletContext() {
		return context;
	}
	
	public static Document getDocument(URL url) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		Document dom = builder.parse(url.openStream());
		
		return dom;
	}
}
