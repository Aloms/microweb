package microweb.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import microweb.core.Util;
import microweb.model.Domain;
import microweb.model.PageHandler;
import microweb.model.Site;

public class SiteImpl implements Site{

	private static final String SITE_ROOT = "/site";
	
	protected static Logger logger = Logger.getLogger("microweb.core", "messages");
	
	protected String name;
	protected String context;
	protected int status = Site.STATUS_INVALID;
	
	protected NodeList navigations;
	protected Element siteNav;
	

	private static XPath xPath = XPathFactory.newInstance().newXPath();
	
	private SiteImpl() {
		
	}
	
	public static Site createFromElement(Element siteElement) throws XPathExpressionException {
		
		
		String s_name = xPath.evaluate(SITE_ROOT + "/@name", siteElement);
		String s_context = xPath.evaluate(SITE_ROOT + "/@context", siteElement);
		
		if (s_name == null || s_name.trim().equals("")) {
			
			logger.log(Level.WARNING, "site.name.missingorempty", new Object[] {s_name});
			return null;
			
		}


		SiteImpl site = new SiteImpl();
		site.setName(s_name);
		site.setContext(s_context);
		
		site.loadSections(siteElement);
		
		return site;
	}

	private void loadSections(Element siteElement) throws XPathExpressionException {

		
		
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
			
			String expr = uri.replaceAll("/", "/@slug=''");
			
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
			
			String s_id = null;
			String s_label = null;
			String s_slug = null;
			
		
			try {
				s_id = xPath.evaluate("@id", section);
				s_label = xPath.evaluate("@label", section);
				s_slug = xPath.evaluate("@slug", section);
				
				logger.finest("section with id: " + s_id + " will handle uri: " + uri);
				
			} catch (XPathExpressionException e) {
				logger.log(Level.SEVERE, "failed to read node attribute(s)", e);
				return (HttpServletRequest request, HttpServletResponse response) -> {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				};
			}
			
			String out_id = s_id;
			String out_label = s_label;
			String out_slug = s_slug;
			
			return (HttpServletRequest request, HttpServletResponse response) -> {
				

				response.getWriter().println("out_id: " + out_id);
				response.getWriter().println("out_label: " + out_label);
				response.getWriter().println("out_slug: " + out_slug);
				
				
			};
			
		}
		
		
		
	}
}
