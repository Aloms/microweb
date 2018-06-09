package microweb.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import microweb.core.AbstractDomain;
import microweb.core.Util;
import microweb.model.Domain;
import microweb.model.PageHandler;

public class CanonicalDomain extends AbstractDomain {

	private Map<String, PageHandler> pageHandlerRegistry;
	
	protected CanonicalDomain(String name, String site) throws XPathExpressionException {
		super(name, site);
		
		this.pageHandlerRegistry = new ConcurrentHashMap<String, PageHandler>();
		
		if (this.getSite() != null) {
			
			Domain existingDomain = Util.getSiteCanonicalDomainsRegistry().putIfAbsent(name, this);
			
			if (existingDomain != null) {
				if(logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "microweb.application.config.sites-config.multipleCanonicalDomains", new Object[] {this.getSite().getName(), existingDomain.getName(), this.getName()});
				}
			} else {
				if(logger.isLoggable(Level.FINE)) {
					logger.fine("Registered canonical domain: " + this.getName() + " for site: " +  this.getSite().getName());
				}
			}

		} else {
			throw new RuntimeException("Could not find associated site for this domain: " + this.getName());
		}
		
	}
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String uri = request.getRequestURI();
		
		PageHandler pageHandler = pageHandlerRegistry.get(uri);
		
		if (pageHandler == null) {
			//Map<String, PageHandler> pageRegistry = this.getSite().getPageRegistry();
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("Did not find existing Page Handler for domain: " + this.getName() + " and uri: " + uri + ".  Requesting a new Page Handler from site: " + this.getSite().getName());
			}
			
			pageHandler = site.getPageHandler(uri);
			
			if (pageHandler == null) {
				
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("Site: " + this.getSite().getName() + " did not return a Page handler for the domain: " + this.getName() + " and uri: " + uri + ", sending Page Not Found.");
				}
				
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				
				PageHandler existingPageHandler = pageHandlerRegistry.putIfAbsent(uri, pageHandler);
				
				if (existingPageHandler != null) {
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest("Attempted to add a new PageHandler for the uri: " + uri + " to the domain: " + this.getName() + " but found that one already exists.  discarding this one after use.");
					}
				}
				
				this.forwardTo(pageHandler, request, response);
			}
		} else {
			
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("Found existing Page Handler in the Page Handler Registry for the " + this.getName() + " domain");
			}
			
			this.forwardTo(pageHandler, request, response);
		}
		
		
		
		
	}
	
	protected void forwardTo(PageHandler pageHandler, HttpServletRequest request, HttpServletResponse response) throws IOException {
		pageHandler.handle(request, response);
	}
}
