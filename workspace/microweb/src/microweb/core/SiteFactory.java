package microweb.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import microweb.model.Site;

public class SiteFactory {

	private static Logger logger = Logger.getLogger(SiteFactory.class.getPackage().getName());
	
	
	public static Site createFromXML(String xmlFilename) {
		
		//check if file exists
		if (!new File(xmlFilename).exists()) {
			throw new Exception("no configuration file could be found at: " + xmlFilename);
		}
		
		
		logger.info("Initialising site from configuration file is [" + xmlFilename + "]");
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(xmlFilename);
		
		Map<String, Node> uriRegistry = new HashMap<String, Node>();
		
		logger.finest("populating uri registry for site [" + site.getName() + "]");
		
		Element e = doc.getDocumentElement();
		
		logger.finest("document node: " + e.toString());
		
		org.w3c.dom.Node strutureNode = e.getElementsByTagName("structure").item(0);
		logger.finest("strutureNode: " + strutureNode.toString());
		
		NodeList rootNodes = strutureNode.getChildNodes();
		logger.finest("rootNodes: " + rootNodes.getLength());
		
		
		populateURIMappings(uriRegistry, rootNodes);
		
		logger.finest("populating uri registry for site [" + site.getName() + "]");
		
		SiteImpl site = new SiteImpl();
		
		
		
		SiteFactory site = new SiteFactory(siteKey);
		
		sites.put(site.getName(), site);
		
		
		
		
		
		//FIXME: initialise url registry for this site
		
		
		try {
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		return site;
	}

	
	protected class SiteImpl implements Site {
		private String name;
		private Map<String, Node> urlRegistry;
		private Map<String, Node> nodes;
		
		protected SiteImpl() {
			urlRegistry = new ConcurrentHashMap<String, Node>();
			nodes = new ConcurrentHashMap<String, Node>();
		}
		
		
		@Override
		public String getName() {
			return this.name;
		}
		
	}
}
