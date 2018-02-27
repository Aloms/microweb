package microweb.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import microweb.model.Node;
import microweb.model.Site;

public class XMLSiteFactory extends SiteFactory {

	private static Logger logger = Logger.getLogger(XMLSiteFactory.class.getPackage().getName());
	
	
	public static Site createFromXML(String xmlFilename) throws Exception {
		
		//check if file exists
		if (!new File(xmlFilename).exists()) {
			throw new Exception("no configuration file could be found at: " + xmlFilename);
		}
		
		
		logger.info("Initialising site from configuration file is [" + xmlFilename + "]");
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document siteDom = builder.parse(xmlFilename);
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xPath.compile("//site/@name");
		String siteName = (String) expr.evaluate(siteDom, XPathConstants.STRING);
		
		logger.fine("Loading site [" + siteName + "] from xml configuration [" + xmlFilename + "]");
		
		Site site = new Site() {

			private String name = siteName;
			private Document document = siteDom;
			
			
			@Override
			public String getName() {
				return name;
			}
			
		};
		
		return site;
	}

	
}
