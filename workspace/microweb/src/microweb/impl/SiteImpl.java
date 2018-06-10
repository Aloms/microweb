package microweb.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.xml.sax.SAXException;

import microweb.core.Util;
import microweb.model.Domain;
import microweb.model.PageHandler;
import microweb.model.Site;
import microweb.model.Status;

public class SiteImpl implements Site{

	//private static final String SITE_ROOT = "/site";
	
	private Logger commonLogger = Logger.getLogger("microweb.core.sites", "messages");
	private Logger logger;
	
	private String name;
	private String siteHome;
	private String configName;
	
	//protected String context;
	private Status status;
	
	private NodeList navigations;
	private Element siteNav;
	
	private Element siteElement;
	
	private Domain canonicalDomain;
	private List<Domain> domains;
	
	private Properties properties;
	

	private static XPath xPath = XPathFactory.newInstance().newXPath();
	
	private SiteImpl(ServletContext context, String location, String config) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		
		this.siteHome = Util.MICROWEB_HOME + Util.getApplicationProperty("sites-home") + "/" + location;
		this.configName = siteHome + "/" + config;
		
		
		
		URL siteUrl = context.getResource(configName);		    	
		URL xsd = context.getResource(Util.MICROWEB_HOME + Util.getSystemProperty("siteXsd"));

		if(commonLogger.isLoggable(Level.FINE)) {
			commonLogger.fine("Initialising new site at location: " + this.siteHome + ", and using config file: " + this.configName);
			
			commonLogger.fine("Full path to config: " + siteUrl.toExternalForm());
		}
		
		
		Util.validateXML(siteUrl, xsd);

		commonLogger.info("Initialising site from configuration file is [" + siteUrl.toExternalForm() + "]");
		
		Element siteElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(siteUrl.openStream()).getDocumentElement();
				
		String s_name = xPath.evaluate("@name", siteElement);
		
		//assign variables
		this.name = s_name;
		this.siteElement = siteElement;
		this.domains = new ArrayList<Domain>();
		this.properties = new Properties();
		
		//setup logger for this specific site
		this.logger = Logger.getLogger("microweb.core.sites." + this.getName(), "messages");
		
		//load site
		this.loadProperties(siteElement);
		this.loadDomains(siteElement);
		this.loadSections(siteElement);
		
		//set status
		this.status = Site.STATUS_CONFIGURED;
		
		
	}
	
	

	public static Site createFromConfig(ServletContext context, String location, String config) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {

		return new SiteImpl(context, location, config);
	}
	
	protected void loadProperties(Element siteElement) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xPath.compile("properties/*");
		
		NodeList propertyElements = (NodeList) expr.evaluate(siteElement, XPathConstants.NODESET);

		StringBuffer printProps = null;
		if (logger.isLoggable(Level.CONFIG)) {
			printProps = new StringBuffer();
	    }
		
		for (int i = 0; i < propertyElements.getLength(); i++) {
		    Element propertyElement = (Element) propertyElements.item(i);
		    String s_name = xPath.evaluate("@name", propertyElement);
		    String s_value = xPath.evaluate("@value", propertyElement);
		    
		    this.properties.put(s_name, s_value);
		    
		    if (logger.isLoggable(Level.CONFIG)) {
		    	printProps.append("\nSite [" + this.getName() + "] property name: '" + s_name + "', value: '" + s_value + "'");
		    }
		}
		
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config(printProps.toString());
		}
		
	}

	protected void loadDomains(Element siteElement) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xPath.compile("domains/*");
		
		NodeList domainElements = (NodeList) expr.evaluate(siteElement, XPathConstants.NODESET);

		
		for (int i = 0; i < domainElements.getLength(); i++) {
		    Element domainElement = (Element) domainElements.item(i);

			//String elName = domainElement.getNodeName();
			String s_name = xPath.evaluate("@name", domainElement);
			String s_type = xPath.evaluate("@type", domainElement);
			
			Domain domain = null;
			if (s_type.equals("redirect")) {
				String s_scheme = xPath.evaluate("@scheme", domainElement);
				String s_port = xPath.evaluate("@port", domainElement);
				String s_uri = xPath.evaluate("@uri", domainElement);
				String s_queryString = xPath.evaluate("@queryString", domainElement);
				
				
				if (s_scheme.equals("preserve")) {
					//preserve the scheme
					domain = new RedirectedDomain(s_name.trim(), this, s_scheme, s_port, s_uri, s_queryString);
				}
			} else if (s_type.equals("canonical")){
				domain = new CanonicalDomain(s_name.trim(), this);
				
				if (this.getCanonicalDomain() != null) {
					if(logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "microweb.application.config.sites-config.multipleCanonicalDomains", new Object[] {this.getName(), this.getCanonicalDomain(), domain.getName()});
					}
				} else {
					this.canonicalDomain = domain;
				}
				
			} else if (s_type.equals("alias")){
				domain = new AliasDomain(s_name.trim(), this);
				
			} else {
				throw new RuntimeException("Unknown domain type: " + s_type);
			}
			
			if (domain != null) {
				
				if(logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "microweb.application.config.domain.loaded", new Object[] {domain.getName(), (domain instanceof CanonicalDomain ? "canonical" : (domain instanceof RedirectedDomain ? "Redirect" : "Alias")), this.getName()});
				}
				
				this.domains.add(domain);
				
				
			}
			
		}
	}
	
	protected void loadSections(Element siteElement) throws XPathExpressionException {
		
		XPathExpression expr = xPath.compile("navigations/*");
		
		NodeList navigationElements = (NodeList) expr.evaluate(siteElement, XPathConstants.NODESET);

		this.navigations = navigationElements;
		
		for (int i = 0; i < navigationElements.getLength(); i++) {
			
			if (logger.isLoggable(Level.FINEST)) {
	    		logger.finest("Processing navigation tree for site: " + this.getName());
	    	}
			
			
		    Element section = (Element) navigationElements.item(i);
		    
		    String siteNav = xPath.evaluate("@site-nav", section);
		    
		    if (Boolean.parseBoolean(siteNav)) {
		    	
		    	if (logger.isLoggable(Level.FINE)) {
		    		logger.fine("Found site navigation tree for site: " + this.getName());
		    	}
		    	this.siteNav = section;
		    	break;
		    }
		}
		
		if (this.siteNav == null) {
			logger.log(Level.WARNING, "microweb.application.config.sites-config.noSiteNav", new Object[] {this.getName()});
		}
	}
	
	/*
	public String getContext() {
		return context;
	}

	protected void setContext(String context) {
		this.context = context;
	}
	*/

	protected void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Status getStatus() {
		return this.status;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public PageHandler getPageHandler(String uri) {
		if (this.siteNav == null) {

			return (HttpServletRequest request, HttpServletResponse response) -> {
				response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			};
		} 
		
		Node section = null;
		
		
		if ("/".equals(uri)) {
			section = this.siteNav;
		} else {
			
			String expr = uri.replaceAll("\\/([^\\/]+)", "/section[@slug='$1']");
			
			if (expr.endsWith("/")) {
				expr = expr.substring(0, expr.length()-1);
			}
			
			expr = expr.substring(1);
			
			logger.fine("expr: " + expr);
			
			try {
				section = (Node) xPath.evaluate(expr, this.siteNav, XPathConstants.NODE);
			} catch (XPathExpressionException e) {
				logger.log(Level.SEVERE, "failed to find node using xpath expression: " + expr, e);
				return (HttpServletRequest request, HttpServletResponse response) -> {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				};
			}
		}
		
		
		if (section == null) {
			return (HttpServletRequest request, HttpServletResponse response) -> {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			};
		} else {
			
			
		
			try {
				String s_id = xPath.evaluate("@id", section);
				String s_label = xPath.evaluate("@label", section);
				String s_slug = xPath.evaluate("@slug", section);
				String s_page = xPath.evaluate("@page", section);
				
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("section with id: " + s_id + " will handle uri: " + uri);
				}
				
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("section " + s_id + " - s_page: " + s_page + ", defaultPage: " + this.properties.getProperty("defaultPage"));
				}
				String page = s_page != null && !s_page.equals("") ? s_page : this.properties.getProperty("defaultPage");
				
				
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("jsp page for this section: " + s_id + ", is:" + page);
				}
				
				if (page != null && !page.equals("")) {
					
					String pagePath = this.siteHome + "/" + page;
					return (HttpServletRequest request, HttpServletResponse response) -> {
						

						//response.getWriter().println("s_id: " + s_id);
						//response.getWriter().println("s_label: " + s_label);
						//response.getWriter().println("s_slug: " + s_slug);
						//response.getWriter().println("s_page: " + s_page);
						
						//String resourcePath = request.getServletContext().getRealPath(pagePath);
						
						//response.getWriter().println("pagePath: " + pagePath);
						//response.getWriter().println("resourcePath: " + resourcePath);
						request.getRequestDispatcher(pagePath).forward(request, response);
						
					};
					
				} else {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "No Page to render output for section: " + s_id + "  (" + s_label + ").  either set the 'page' attribute for this section or set a the 'defaultPage' site property");
					}
					return (HttpServletRequest request, HttpServletResponse response) -> {
						response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
					};
				}
				
				
				
			} catch (XPathExpressionException e) {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.log(Level.SEVERE, "failed to read node attribute(s)", e);
				}
				return (HttpServletRequest request, HttpServletResponse response) -> {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				};
			}
		}
	}

	@Override
	public Domain getCanonicalDomain() {
		return this.canonicalDomain;
	}

	@Override
	public List getDomains() {
		return this.domains;
	}
}
