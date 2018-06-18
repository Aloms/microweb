package microweb.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import microweb.core.AbstractDomain;
import microweb.model.PageHandler;
import microweb.model.Site;

public abstract class HostedDomain extends AbstractDomain {

	private Map<String, PageHandler> pageHandlerRegistry;
	private boolean canonical;
	
	protected HostedDomain(String name, Site site, boolean canonical) {
		super(name, site);
		this.canonical = canonical;
		
		this.pageHandlerRegistry = new ConcurrentHashMap<String, PageHandler>();
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		String uri = request.getRequestURI();
		
		if (site.getStatus() == Site.STATUS_ONLINE) {
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
		} else {
			if (logger.isLoggable(Level.INFO)) {
				logger.finest("Domain: " + this.getName() + " tried to serve uri: " + uri + ", using site: " + this.getSite().getName() + ", but it's not online.  It's status is: " + this.getSite().getStatus().getLabel());
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	protected void forwardTo(PageHandler pageHandler, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		//TODO: run pageHandler validator to check request params are valid
		pageHandler.handle(request, response);
	}

	public boolean isCanonical() {
		return canonical;
	}

}
