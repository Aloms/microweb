package microweb.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import microweb.core.Util;
import microweb.model.Domain;
import microweb.model.Site;

public class SiteImpl implements Site{

	private static final String SITE_ROOT = "/site";
	
	protected static Logger logger = Logger.getLogger("microweb.core", "messages");
	
	protected String name;
	protected String context;
	protected List<Domain> domains;
	
	
	

	private SiteImpl() {
		this.domains = new ArrayList<Domain>();
	}
	
	public static Site createFromElement(Element siteElement) throws XPathExpressionException {
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		String s_name = xPath.evaluate(SITE_ROOT + "/@name", siteElement);
		String s_context = xPath.evaluate(SITE_ROOT + "/@context", siteElement);
		
		if (s_name == null || s_name.trim().equals("")) {
			
			logger.log(Level.WARNING, "site.name.missingorempty", new Object[] {s_name});
			return null;
			
		}


		SiteImpl site = new SiteImpl();
		site.setName(s_name);
		site.setContext(s_context);
		
		
		
		XPathExpression expr = xPath.compile(SITE_ROOT + "/domains/domain");
		
		NodeList domainElements = (NodeList) expr.evaluate(siteElement, XPathConstants.NODESET);

		boolean hasCanonical = false;
		
		for (int i = 0; i < domainElements.getLength(); i++) {
		    Element domainElement = (Element) domainElements.item(i);
		    
		    Domain domain;
			try {
				domain = DomainImpl.createFromElement(domainElement, site);
				
				if (domain != null) {
			    	site.domains.add(domain);
			    	
			    	if(domain.isCanonical()) {
			    		hasCanonical = true;
			    	}
			    }
			} catch (XPathExpressionException e) {
				logger.log(Level.WARNING, "site.invalidconfig", new Object[] {site.getName()});
			}
		}
		
		logger.log(Level.CONFIG, "site.created", new Object[] {site.getName(), site.getContext()});
		
		if (site.getDomains().size() == 0) {
			logger.log(Level.WARNING, "site.nodomains", new Object[] {site.getName()});
		} 
		
		if (!hasCanonical) {
			logger.log(Level.WARNING, "site.nocanonicaldomains", new Object[] {site.getName()});
		} 
		
		return site;
	}

	public String getContext() {
		return context;
	}

	protected void setContext(String context) {
		this.context = context;
	}

	public List<Domain> getDomains() {
		return domains;
	}

	protected void setDomains(List<Domain> domains) {
		this.domains = domains;
	}

	protected void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
