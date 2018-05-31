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
	protected int status = Site.STATUS_INVALID;
	
	

	private SiteImpl() {
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
		
		return site;
	}

	public String getContext() {
		return context;
	}

	protected void setContext(String context) {
		this.context = context;
	}

	protected void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getStatus() {
		return this.status;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

}
