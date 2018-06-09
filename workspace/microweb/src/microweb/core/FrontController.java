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
import java.util.Collection;
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
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
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

import microweb.impl.XMLDomainFactory;
import microweb.impl.XMLSiteFactory;
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
	private Logger httpLogger;
	
	private boolean failedInit = false;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() {
        super();
        this.logger = Logger.getLogger("microweb.core", "messages");
        this.httpLogger = Logger.getLogger("microweb.http", "messages");
        
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
		
		if (logger.isLoggable(Level.FINER)) {
			String url = 	scheme + "://" +   // "http" + "://
					serverName +       // "myhost"
					":" + serverPort + // ":" + "8080"
					requestUri +       // "/people"
					(queryString != null ? "?" + queryString : ""); // "?" + "lastname=Fox&age=30"
	
			StringBuffer b = new StringBuffer();
			b.append("\n").append("contextPath:" + contextPath);
			b.append("\n").append("requestUrl:" + requestUrl);
			//b.append("\n").append("microwebContext:" + microwebContext);
			//b.append("\n").append("controller:" + controller);
			b.append("\n").append("scheme:" + scheme);
			b.append("\n").append("serverName:" + serverName);
			b.append("\n").append("serverPort:" + String.valueOf(serverPort));
			b.append("\n").append("requestUri:" + requestUri);
			b.append("\n").append("queryString:" + queryString);
			b.append("\n").append("url: " + url);
			
			logger.finer(b.toString());
		}
		
		
		if (failedInit) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} else {
			Domain domain = Util.getDomainRegistry().get(serverName);
			if (domain != null) {
				logger.finest("found domain: " + domain.getName());
				domain.handle(request, response);
			} else {
				logger.finest("no hosted site for domain name: " + serverName);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}

		if (httpLogger.isLoggable(Level.INFO)) {
			httpLogger.log(Level.INFO, "site.http.responseCode", new Object[] {response.getStatus(), request.getRequestURL()});
		}
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

			
		try {
			URL microwebProperties = getServletContext().getResource(this.getServletContext().getInitParameter("microweb-properties"));
			URL systemProperties = getServletContext().getResource(this.getServletContext().getInitParameter("system-properties"));
			
			Util.init(microwebProperties, systemProperties, getServletContext());
			
			
			
		} catch (MalformedURLException e1) {
			this.failedInit = true;
		}

			

		if (!this.failedInit) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			String sitesConfigPath = microwebHome + Util.getApplicationProperty("sites-config");
			String sitesConfigXsd = microwebHome + Util.getSystemProperty("sitesConfigXsd");
			
			String rootElement = "/sites-config";
			String sitesExpr = rootElement + "/sites/site";
			//String sitesConfigXsd = microwebHome + "/config/sites-config.xsd";
			
			
			try {
				
				URL sitesResourcePath = getServletContext().getResource(sitesConfigPath);
				logger.fine("loading sites configuration from: " + sitesResourcePath.toExternalForm());
				
				
				Util.validateXML(sitesResourcePath, getServletContext().getResource(sitesConfigXsd));
				
				DocumentBuilder builder = factory.newDocumentBuilder();
				
				Document siteDom = builder.parse(sitesResourcePath.openStream());
				
				loadSites(microwebHome, sitesExpr, siteDom);
				
				loadDomains(rootElement, siteDom);
				
				checkCanonicals();
				
			    logger.info("Complete");
				 
				
			} catch (ParserConfigurationException e) {
				logger.log(Level.SEVERE, "Unable to W3C DOM Parser", e);
			} catch (SAXException e) {
				logger.log(Level.SEVERE, "microweb.application.config.sites-config.invalid", new Object[] {sitesConfigPath});
			} catch (XPathExpressionException e) {
				logger.log(Level.SEVERE, "Unable to compile Xpath Expression: " + sitesExpr, e);
			} catch (MalformedURLException e) {
				logger.log(Level.SEVERE, "Invalid location for sites configuration: " + sitesConfigPath, e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to read XML file: " + sitesConfigPath, e);
			} 

			logger.info("Initialisation completed");
		} else {
			logger.log(Level.SEVERE, "microweb.application.initialisationFailed", new Object[] {});
		}

	}



	private void checkCanonicals() {
		Collection<Site> sites =  Util.getSiteRegistry().values();
		
		for (Site site : sites) {
			Domain domain = Util.getSiteCanonicalDomainsRegistry().get(site.getName());
			
			if (domain == null) {
				logger.log(Level.WARNING, "microweb.application.config.sites-config.noCanonicalDomainForSite", new Object[] {site.getName()});
				site.setStatus(Site.STATUS_INVALID);
			} else {
				logger.config(domain.getName() + " is the canonical name for the " + site.getName() + " site.");
				site.setStatus(Site.STATUS_ONLINE);
			}
		}
	}

	private void loadDomains(String rootElement, Document siteDom) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xPath.compile(rootElement + "/domains/*");
		
		NodeList domainElements = (NodeList) expr.evaluate(siteDom, XPathConstants.NODESET);

		
		for (int i = 0; i < domainElements.getLength(); i++) {
		    Element domainElement = (Element) domainElements.item(i);
		    
		    XMLDomainFactory.createAndRegister(domainElement);
		}
	}

	private void loadSites(String microwebHome, String sitesExpr, Document siteDom)	throws XPathExpressionException, MalformedURLException {
		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xPath.compile(sitesExpr);
		
		
		NodeList sites = (NodeList) expr.evaluate(siteDom.getDocumentElement(), XPathConstants.NODESET);
		
		logger.info("Printing nodes");
		for (int i = 0; i < sites.getLength(); i++) {
			Element siteElement = (Element) sites.item(i);
			String location = xPath.evaluate("@location", siteElement);
			String config = xPath.evaluate("@config", siteElement);

			logger.info("location:" + location + ", config:" + config);

			String siteConfigPath = microwebHome + "/" + Util.getApplicationProperty("sites-home") + "/" + location + "/" + config;
			
			URL siteUrl = getServletContext().getResource(siteConfigPath);		    	
			URL xsd = getServletContext().getResource(microwebHome + Util.getSystemProperty("siteXsd"));
			try {
				Util.validateXML(siteUrl, xsd);
				
				Site site = XMLSiteFactory.loadSite(siteUrl);
				
				Util.getSiteRegistry().put(site.getName(), site);
				logger.log(Level.CONFIG, "microweb.application.config.site.loaded", new Object[] {site.getName()});


			} catch (Exception e) {
				logger.log(Level.SEVERE, "microweb.application.config.site.invalid", new Object[] {siteUrl.toExternalForm()});
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
			
		}
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
