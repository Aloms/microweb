package microweb.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

import microweb.model.Domain;
import microweb.model.Site;

public class DomainImpl implements Domain {
	
	protected static Logger logger = Logger.getLogger("microweb.core", "messages");
	
	protected String name;
	protected boolean canonical;
	protected Site site;
	
	private DomainImpl(String name, boolean canonical, Site site) throws XPathExpressionException {
		
		this.name = name;
		this.canonical = canonical;
		this.site = site;
		
		logger.log(Level.CONFIG, "site.domain.created", new Object[] {this.name, this.canonical});
	}
	
	public static Domain createFromElement(Element domainElement, Site site) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();

		
		String s_name = xPath.evaluate("@name", domainElement);
		String s_isCanonical = xPath.evaluate("@isCanonical", domainElement);
		
		if (s_name == null || s_name.trim().equals("")) {
			
			logger.log(Level.WARNING, "site.domain.name.missingorempty", new Object[] {s_name});
			return null;
			
		} 
		
		return new DomainImpl(s_name.trim(), Boolean.parseBoolean(s_isCanonical), site);
	}
	
	public String getName() {
		return name;
	}

	
	public boolean isCanonical() {
		return canonical;
	}

	@Override
	public Site getSite() {
		return this.site;
	}
}
