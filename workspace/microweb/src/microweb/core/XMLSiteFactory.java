package microweb.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

import microweb.model.Action;
import microweb.model.Section;
import microweb.model.Site;

public class XMLSiteFactory extends SiteFactory {

	private static final String ROOT_NODE = "root";
	private static final String SITE_ROOT = "/site";
	private static final String SITE_STRUCTURE_PATH = SITE_ROOT + "/structure";
	private static final String SITE_PROPERTIES_PATH = SITE_ROOT + "/properties";
	private static final String NODE_ATTR_ACTION = "action";
	
	private static final String TEMPLATES_FOLDER = "templates";
	
	private static Logger logger = Logger.getLogger(XMLSiteFactory.class.getPackage().getName());
	
	
	public static Site loadSite(String siteInstallation) throws Exception {
		
		String xmlFilename = siteInstallation + File.separator + "config.xml";
		
		//check if file exists
		if (!new File(xmlFilename).exists()) {
			throw new Exception("no configuration file could be found at: " + xmlFilename);
		}
		
		
		logger.info("Initialising site from configuration file is [" + xmlFilename + "]");
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document siteDom = builder.parse(xmlFilename);
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xPath.compile(SITE_ROOT + "/@name");
		String siteName = (String) expr.evaluate(siteDom, XPathConstants.STRING);
		
		logger.fine("Loading site [" + siteName + "] from xml configuration [" + xmlFilename + "]");
		
		Site site = new Site() {

			private String name = siteName;
			private Document document = siteDom;
			private Logger siteLogger = Logger.getLogger("site." + siteName);
			
			
			@Override
			public String getName() {
				return name;
			}


			@Override
			public Properties getPageRegistry() {
				// TODO Auto-generated method stub
				return null;
			}


			@Override
			public Section getSectionByURI(String uri) {
				XPathFactory xPathfactory = XPathFactory.newInstance();
				XPath xpath = xPathfactory.newXPath();
				
				String computedPath = uri;
				
				logger.finest("finding node for computedPath: " + computedPath);
				
				computedPath = "/" + ROOT_NODE + computedPath;
				
				logger.finest("prefixed computedPath with " + ROOT_NODE + ": " + computedPath);
				
				if (computedPath.endsWith("/")) {
					computedPath = computedPath.substring(0, computedPath.length()-1);
					
					logger.finest("Stripped trailing slash from computedPath: " + computedPath);
				}
				
				String xpathStr = SITE_STRUCTURE_PATH + computedPath;
				
				logger.finest("Getting [" + this.name + "] site node: " + xpathStr);
				try {
					XPathExpression expr = xpath.compile(xpathStr);
					
					org.w3c.dom.Node node = (org.w3c.dom.Node) expr.evaluate(siteDom, XPathConstants.NODE);
					
					String sectionName = node.getNodeName();
					logger.log(Level.FINE, "found node: " + sectionName);
					
					String nodeAction =  (String) xpath.evaluate(xpathStr + "/@action", node, XPathConstants.STRING);
					Action action = null;
					
					if (nodeAction == null || nodeAction.trim().equals("")) {
						//SITE_ROOT + "/@name"
						//SITE_PROPERTIES_PATH + "/property/[@name='defaultTemplate']/@value"
						String defaultTemplate = (String) xpath.evaluate(SITE_PROPERTIES_PATH + "/property[@name='defaultTemplate']/@value", siteDom, XPathConstants.STRING);

						logger.fine("defaultTemplate: " + defaultTemplate);
						
						if (defaultTemplate != null && !defaultTemplate.trim().equals("")) {
							logger.fine("creating a new page action for the site section: " + sectionName);
							action = new PageAction(siteInstallation, TEMPLATES_FOLDER, defaultTemplate);
							
						} else {
							logger.warning(node.getNodeName() + " does not have a defined action and no defaultTemplate attribute has bee set as a site property.");
							return null;
						}
						
					}
					
					Section section = new SectionImpl(sectionName, action);
					
					
					return section;
					
				} catch (XPathExpressionException e) {
					logger.log(Level.WARNING, "Could not compute xpath expression from uri: " + uri, e);
				}
				return null;
			}
		};
		
		return site;
	}

	public static void main (String[] args) throws Exception {
		Site admin = loadSite("D:\\git\\microweb\\workspace\\microweb\\WebContent\\WEB-INF\\sites\\admin");
		
		Section section  = admin.getSectionByURI("/");
		
		logger.info(section.getName());
		
	}
	
	static class SectionImpl implements Section {

		private String name;
		private Action action;
		
		SectionImpl(String name, Action action) {
			this.name = name;
			this.action = action;
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public Action getAction() {
			return action;
		}
		
	}
	
	static class PageAction implements Action {

		private String template;
		
		PageAction(String siteInstallation, String templatesFolder, String templateName) {
			this.template = siteInstallation + File.separator + TEMPLATES_FOLDER + File.separator + templateName;
		}
		
		@Override
		public void doAction(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
			
			logger.fine("template: " + this.template);
			RequestDispatcher dispatcher = servletContext.getRequestDispatcher(this.template);
			dispatcher.forward(request, response);
		}
		
	}
}
