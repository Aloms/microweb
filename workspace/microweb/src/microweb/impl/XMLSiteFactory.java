package microweb.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import microweb.model.Domain;
import microweb.model.Site;

public class XMLSiteFactory extends SiteFactory {

	private static final String ROOT_NODE = "root";
	
	private static final String NODE_ATTR_ACTION = "action";
	
	private static final String TEMPLATES_FOLDER = "templates";
	
	private static Logger logger = Logger.getLogger(XMLSiteFactory.class.getPackage().getName());
	
	
	public static Site loadSite(URL siteInstallation) throws Exception {
		
		
		
		logger.info("Initialising site from configuration file is [" + siteInstallation.toExternalForm() + "]");
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document siteDom = builder.parse(siteInstallation.openStream());
		
		Site site = SiteImpl.createFromElement(siteDom.getDocumentElement());
		
		return site;

	}

	/*
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
	*/
}
