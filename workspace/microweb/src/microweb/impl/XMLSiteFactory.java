package microweb.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import microweb.core.SiteFactory;
import microweb.core.Util;
import microweb.model.Domain;
import microweb.model.Site;

public class XMLSiteFactory extends SiteFactory {

	private static final String ROOT_NODE = "root";
	
	private static final String NODE_ATTR_ACTION = "action";
	
	private static final String TEMPLATES_FOLDER = "templates";
	
	private static Logger logger = Logger.getLogger("microweb.config");
	
	public static void loadSites(ServletContext context, String sitesConfigPath) {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		
		String sitesConfigXsd = Util.MICROWEB_HOME + Util.getSystemProperty("sitesConfigXsd");
		
		String rootElement = "/sites-config";
		String sitesExpr = rootElement + "/sites/site";
		//String sitesConfigXsd = microwebHome + "/config/sites-config.xsd";
		
		
		try {
			
			URL sitesResourcePath = context.getResource(sitesConfigPath);
			logger.fine("loading sites configuration from: " + sitesResourcePath.toExternalForm());
			
			
			Util.validateXML(sitesResourcePath, context.getResource(sitesConfigXsd));
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document siteDom = builder.parse(sitesResourcePath.openStream());
			
			loadSites(context, sitesExpr, siteDom);
			
			checkCanonicals();
			
			registerDomains();
			
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
	}
	
	private static Site loadSite(ServletContext context, String location, String config) throws Exception {

		
		return SiteImpl.createFromConfig(context, location, config);
		
	}
	
	private static void registerDomains() {
		Collection<Site> sites =  Util.getSiteRegistry().values();
		
		for (Site site : sites) {
			
			Collection<Domain> domains = site.getDomains();
			
			for (Domain domain : domains) {
				Domain existingDomain = Util.getDomainRegistry().putIfAbsent(domain.getName(), domain);
				
				if (existingDomain != null) {
					logger.log(Level.WARNING, "microweb.application.config.sites-config.duplicatedDomains", new Object[] {domain.getName()});
				}
			}
		}
	}

	private static void checkCanonicals() {
		Collection<Site> sites =  Util.getSiteRegistry().values();
		
		for (Site site : sites) {
			
			if (site.getCanonicalDomain() == null) {
				logger.log(Level.WARNING, "microweb.application.config.sites-config.noCanonicalDomainForSite", new Object[] {site.getName()});
				site.setStatus(Site.STATUS_INVALID);
			} else {
				logger.config(site.getCanonicalDomain().getName() + " is the canonical name for the " + site.getName() + " site.");
				site.setStatus(Site.STATUS_ONLINE);
			}
		}
	}



	private static void loadSites(ServletContext context, String sitesExpr, Document siteDom)	throws XPathExpressionException, MalformedURLException {
		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xPath.compile(sitesExpr);
		
		
		NodeList sites = (NodeList) expr.evaluate(siteDom.getDocumentElement(), XPathConstants.NODESET);
		
		for (int i = 0; i < sites.getLength(); i++) {
			Element siteElement = (Element) sites.item(i);
			String location = xPath.evaluate("@location", siteElement);
			String config = xPath.evaluate("@config", siteElement);

			logger.info("location:" + location + ", config:" + config);

			try {
				Site site = XMLSiteFactory.loadSite(context, location, config);
				
				Util.getSiteRegistry().put(site.getName(), site);
				logger.log(Level.CONFIG, "microweb.application.config.site.loaded", new Object[] {site.getName()});
			} catch (Exception e) {
				logger.log(Level.SEVERE, "microweb.application.config.site.failedToLoad", new Object[] {location + "/" + config});
				logger.log(Level.SEVERE, "Fail to instantiate site", e);
			}
			
		}
	}

}
