package microweb.impl;

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

import microweb.core.AbstractDomain;
import microweb.model.Domain;
import microweb.model.Site;

public class HostedDomain extends AbstractDomain {
	
	protected boolean canonical;
	protected Site site;
	
	public boolean isCanonical() {
		return canonical;
	}

	public Site getSite() {
		return this.site;
	}
	
	private HostedDomain(String name, boolean canonical, Site site) throws XPathExpressionException {
		
		super(name);
		this.canonical = canonical;
		this.site = site;
		
		logger.log(Level.CONFIG, "site.domain.created", new Object[] {this.name, this.canonical});
	}
	
	public static HostedDomain createFromElement(Element domainElement, Site site) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();

		
		String s_name = xPath.evaluate("@name", domainElement);
		String s_isCanonical = xPath.evaluate("@isCanonical", domainElement);
		
		if (s_name == null || s_name.trim().equals("")) {
			
			logger.log(Level.WARNING, "site.domain.name.missingorempty", new Object[] {s_name});
			return null;
			
		} 
		
		return new HostedDomain(s_name.trim(), Boolean.parseBoolean(s_isCanonical), site);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) {
		logger.log(Level.FINEST, "site.domain.hosted.handle", new Object[] {site.getName()});
	}
}
