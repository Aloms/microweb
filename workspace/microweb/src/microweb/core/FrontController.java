package microweb.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import microweb.model.Domain;
import microweb.model.Site;

/**
 * Servlet implementation class FrontController
 */
@WebServlet("/")
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String MICROWEB_PROPERTIES_FILE = "microweb.properties"; 
     
	private Logger logger;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() {
        super();
        this.logger = Logger.getLogger(this.getClass().getPackage().getName());
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String contextPath = request.getContextPath();
		String requestUrl = request.getRequestURL().toString();
		String requestUri = request.getRequestURI();
		
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String queryString = request.getQueryString();
		
		
		
		String microwebContext = Util.getConfig().getProperty("microweb-context");
		String controller = Util.getConfig().getProperty("controller");
		
		String url = 	scheme + "://" +   // "http" + "://
						serverName +       // "myhost"
						":" + serverPort + // ":" + "8080"
						requestUri +       // "/people"
						(queryString != null ? "?" + queryString : ""); // "?" + "lastname=Fox&age=30"
		
		logger.finest("contextPath:" + contextPath);
		logger.finest("requestUrl:" + requestUrl);
		
		logger.finest("microwebContext:" + microwebContext);
		logger.finest("controller:" + controller);
		logger.finest("scheme:" + scheme);
		logger.finest("serverName:" + serverName);
		logger.finest("serverPort:" + String.valueOf(serverPort));
		logger.finest("requestUri:" + requestUri);
		logger.finest("queryString:" + queryString);
		
		logger.finest("url: " + url);
		
		
		Map<String, Domain> domainRegistry = (Map<String, Domain>) getServletContext().getAttribute(Util.DOMAINS_KEY);
		if (domainRegistry == null) {
			logger.severe("servlet context does not have an attribute mapped to the [" + Util.DOMAINS_KEY + "] key.  This should be a " + Properties.class.getCanonicalName() + " object containing domain names mapped to " + Site.class.getCanonicalName() + " instances.");
		} else {
			if (domainRegistry.containsKey(serverName)) {
				logger.finest("found hosted site for domain name: " + serverName);
			} else {
				logger.finest("no hosted site for domain name: " + serverName);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
		
		
		
		
		
		
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	@Override
	public void init() throws ServletException {
		logger.info("Initialising " + this.getClass().getName() +  " Servlet");
		
		String microwebHome = "/WEB-INF/";
		String propPathStr = microwebHome + MICROWEB_PROPERTIES_FILE;
		
		
		//Properties props = new Properties();
		
		try {
			URL propertiesPath = getServletContext().getResource(propPathStr);
			//URL url = new URL("url");
			Util.init(propertiesPath);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to initialise Microweb Core from properties file: " + propPathStr, e);
		}
		
		
		//Map<String, URL> sitesConfigurationRegistry = new ConcurrentHashMap<String, URL>();
		//sites contains global set of loaded sites
		Map<String, Site> sitesRegistry = new ConcurrentHashMap<String, Site>();
		getServletContext().setAttribute(Util.SITES_KEY, sitesRegistry);
		
		Map<String, Domain> domainRegistry = new ConcurrentHashMap<String, Domain>();
		getServletContext().setAttribute(Util.DOMAINS_KEY, domainRegistry);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		String sitesConfigPath = microwebHome + Util.getConfig().getProperty("sites-config");

		String sitesXpr = "/sites/site";
		
		
		try {
			
			URL sitesResourcePath = getServletContext().getResource(sitesConfigPath);
			logger.fine("loading sites configuration from: " + sitesResourcePath.toExternalForm());
			
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document siteDom = builder.parse(sitesResourcePath.openStream());
			
			XPath xPath = XPathFactory.newInstance().newXPath();

			XPathExpression expr = xPath.compile(sitesXpr);
			
			
			NodeList sites = (NodeList) expr.evaluate(siteDom.getDocumentElement(), XPathConstants.NODESET);
			
		    logger.info("Printing nodes");
		    for (int i = 0; i < sites.getLength(); i++) {
		    	Element siteElement = (Element) sites.item(i);
		    	String name = xPath.evaluate("@name", siteElement);
		    	String location = xPath.evaluate("@location", siteElement);
		    	String config = xPath.evaluate("@config", siteElement);

		    	logger.info("name:" + name + ", location:" + location + ", config:" + config);

		    	String siteConfigPath = microwebHome + "/" + Util.getConfig().getProperty("sites-home") + "/" + location + "/" + config;
		    	URL siteUrl = getServletContext().getResource(siteConfigPath);
		    	//sitesConfigurationRegistry.put(name, siteUrl);
		    	
		    	
				try {
					Site site = XMLSiteFactory.loadSite(siteUrl);
					sitesRegistry.put(site.getName(), site);
					//TODO: get all domains name put them in the Util.DOMAINS_KEY
					logger.info("added [" + site.getName() + "] to site registry.");
					
					List<Domain> domains = this.getDomains(siteElement, site);
					
					for (Domain d : domains) {
						domainRegistry.put(d.getName(), d);
					}
					
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Could not initialise site: " + name + "(" + siteUrl.toExternalForm() + ")", e);
				}
				
				//initialise domains and map to site
				
				
				
		    	
		    }
		    logger.info("Complete");
			
		} catch (ParserConfigurationException e) {
			logger.log(Level.SEVERE, "Unable to W3C DOM Parser", e);
		} catch (SAXException e) {
			logger.log(Level.SEVERE, "Unable to parse XML file: " + sitesConfigPath, e);
		} catch (XPathExpressionException e) {
			logger.log(Level.SEVERE, "Unable to compile Xpath Expression: " + sitesXpr, e);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "Invalid location for sites configuration: " + sitesConfigPath, e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to read XML file: " + sitesConfigPath, e);
		} 
		
		
		
		
		logger.info("Initialisation completed");
	}
	
	private List<Domain> getDomains(Element siteElement, Site site) {
		List<Domain> domains = new ArrayList<Domain>();
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		try {
			NodeList domainNodes = (NodeList) xPath.evaluate("domains/domain", siteElement, XPathConstants.NODESET);
			
			for (int i = 0; i < domainNodes.getLength(); i++) {
				 Element domainElement = (Element) domainNodes.item(i);
				 
				 
				 Domain domain = this.createDomain(xPath, domainElement, site);
				 domains.add(domain);
				 
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return domains;
	}

	private Domain createDomain(XPath xPath, Element domainElement, Site site) throws XPathExpressionException {
		 String name = xPath.evaluate("@name", domainElement);
		 String type = xPath.evaluate("@type", domainElement);
		 String status = xPath.evaluate("@status", domainElement);
		 
		 
		 int dType = Domain.TYPE_CANONICAL;
		 int dRedirectCode = 0;
		 
		 if (type != null) {
			 if (type.trim().equals("canonical")) {
				 dType = Domain.TYPE_CANONICAL;
			 } else if (type.trim().equals("alias")) {
				 dType = Domain.TYPE_ALIAS;
			 } else if (type.trim().equals("redirect")) {
				 dType = Domain.TYPE_REDIRECT;
				 
				 dRedirectCode = Integer.parseInt(status);
			 }
		 }
		 
		 int domainType = dType;
		 int domainRedirectCode = dRedirectCode;
		 
		 Domain domain = new Domain() {

			 private String myName = name;
			 private int myType = domainType;
			 private int myHttpCode = domainRedirectCode;
			 private Site mySite = site;
			 
			 @Override
			 public String getName() {
				 return myName;
			 }
			
			 @Override
			 public int getType() {
				 return myType;
			 }
			
			 @Override
			 public int getHttpRedirectCode() {
				 return myHttpCode;
			 }

			@Override
			public Site getSite() {
				return mySite;
			}
			 
		 };
		 
		 return domain;
	}

	public static void main(String[] args) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		
		Logger logger = Logger.getLogger(FrontController.class.getPackage().getName());
		
		/*
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		String sitesConfigPath = "D:/git/microweb/bin/apache-tomcat-8.5.27-windows-x64/apache-tomcat-8.5.27/wtpwebapps/microweb/WEB-INF/sites/sites.xml";

		String sitesXpr = "/sites/site";
		
		DocumentBuilder builder = factory.newDocumentBuilder();

		
		Document siteDom = builder.parse(new FileInputStream(sitesConfigPath));
		
		//logger.info(siteDom.getDocumentElement().getTextContent());
		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xPath.compile(sitesXpr);
		
		
		NodeList sites = (NodeList) expr.evaluate(siteDom.getDocumentElement(), XPathConstants.NODESET);
		
		logger.info("Printing nodes");
		for (int i = 0; i < sites.getLength(); i++) {
			Node siteNode = sites.item(i);
			logger.info(siteNode.getNodeName() + ":" + siteNode.getNodeName());
			//logger.info(siteNode.getTextContent());
		}
		logger.info("Complete");
		*/
		
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xPath = factory.newXPath();

	   
	    
	    NodeList sites = (NodeList) xPath.evaluate("/sites/site", new InputSource(new FileReader(
	        "D:/git/microweb/bin/apache-tomcat-8.5.27-windows-x64/apache-tomcat-8.5.27/wtpwebapps/microweb/WEB-INF/sites/sites.xml")), XPathConstants.NODESET);
	    
	    logger.info("Printing nodes");
	    for (int i = 0; i < sites.getLength(); i++) {
	      Element site = (Element) sites.item(i);
	      String name = xPath.evaluate("@name", site);
	      String location = xPath.evaluate("@location", site);
	      String config = xPath.evaluate("@config", site);
	      
	      logger.info("name:" + name + ", location:" + location + ", config:" + config);

	    }
	    logger.info("Complete");
	}
	
	

}
