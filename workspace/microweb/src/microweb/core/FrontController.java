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
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() {
        super();
        this.logger = Logger.getLogger("microweb.core", "messages");
        
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
		
		StringBuffer b = new StringBuffer();
		b.append("\n").append("contextPath:" + contextPath);
		b.append("\n").append("requestUrl:" + requestUrl);
		b.append("\n").append("microwebContext:" + microwebContext);
		b.append("\n").append("controller:" + controller);
		b.append("\n").append("scheme:" + scheme);
		b.append("\n").append("serverName:" + serverName);
		b.append("\n").append("serverPort:" + String.valueOf(serverPort));
		b.append("\n").append("requestUri:" + requestUri);
		b.append("\n").append("queryString:" + queryString);
		b.append("\n").append("url: " + url);
		
		logger.finest(b.toString());
		
		Domain domain = Util.getDomainRegistry(this.getServletContext()).get(serverName);
		if (domain != null) {
			/*
			if(domain.getType() == Domain.TYPE_REDIRECT) {
				response.sendRedirect(requestUri);
			}
			*/
			
			logger.finest("found domain: " + domain.getName());
			domain.handle(request, response);
		} else {
			logger.finest("no hosted site for domain name: " + serverName);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
		
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		String sitesConfigPath = microwebHome + Util.getConfig().getProperty("sites-config");

		String rootElement = "/sites-config";
		String sitesExpr = rootElement+ "/sites/site";
		
		
		try {
			
			URL sitesResourcePath = getServletContext().getResource(sitesConfigPath);
			logger.fine("loading sites configuration from: " + sitesResourcePath.toExternalForm());
			
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document siteDom = builder.parse(sitesResourcePath.openStream());
			
			XPath xPath = XPathFactory.newInstance().newXPath();

			XPathExpression expr = xPath.compile(sitesExpr);
			
			
			NodeList sites = (NodeList) expr.evaluate(siteDom.getDocumentElement(), XPathConstants.NODESET);
			
		    logger.info("Printing nodes");
		    for (int i = 0; i < sites.getLength(); i++) {
		    	Element siteElement = (Element) sites.item(i);
		    	String location = xPath.evaluate("@location", siteElement);
		    	String config = xPath.evaluate("@config", siteElement);

		    	logger.info("location:" + location + ", config:" + config);

		    	String siteConfigPath = microwebHome + "/" + Util.getConfig().getProperty("sites-home") + "/" + location + "/" + config;
		    	URL siteUrl = getServletContext().getResource(siteConfigPath);		    	
		    	
				try {
					Site site = XMLSiteFactory.loadSite(siteUrl);
					
					Util.getSiteRegistry(this.getServletContext()).put(site.getName(), site);
					logger.log(Level.CONFIG, "core.config.loadedsite", new Object[] {site.getName()});
					
					List<Domain> domains = site.getDomains();
					
					for (Domain domain : domains) {
						Util.getDomainRegistry(this.getServletContext()).put(domain.getName(), domain);
						logger.log(Level.CONFIG, "core.config.loadeddomain", new Object[] {domain.getName(), site.getName() });
					}
					

				} catch (Exception e) {
					logger.log(Level.SEVERE, "core.config.sitenotinitialised", new Object[] {siteUrl.toExternalForm()});
				}
		    	
		    }

		    logger.info("Complete");
			
		} catch (ParserConfigurationException e) {
			logger.log(Level.SEVERE, "Unable to W3C DOM Parser", e);
		} catch (SAXException e) {
			logger.log(Level.SEVERE, "Unable to parse XML file: " + sitesConfigPath, e);
		} catch (XPathExpressionException e) {
			logger.log(Level.SEVERE, "Unable to compile Xpath Expression: " + sitesExpr, e);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "Invalid location for sites configuration: " + sitesConfigPath, e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to read XML file: " + sitesConfigPath, e);
		} 
		
		
		
		
		logger.info("Initialisation completed");
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
