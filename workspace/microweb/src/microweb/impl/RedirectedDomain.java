package microweb.impl;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import microweb.core.AbstractDomain;
import microweb.core.Util;
import microweb.model.Domain;
import microweb.model.Site;

public class RedirectedDomain extends AbstractDomain {

	private static final String PRESERVE = "preserve";
	private int port;
	private String scheme;
	private String uri;
	private String queryString;
	
	private boolean preserveScheme;
	private boolean preservePort;
	private boolean preserveUri;
	private boolean preserveQueryString;
	
	
	
	protected RedirectedDomain(String name, Site site, String scheme, String port, String uri, String queryString) throws XPathExpressionException {
		super(name, site);
		
		if (PRESERVE.equals(scheme)) {
			this.preserveScheme = true;
		} else {
			this.scheme = scheme;
		}
		
		if (PRESERVE.equals(port)) {
			this.preservePort = true;
		} else {
			this.port = Integer.parseInt(port);
		}
		
		if (PRESERVE.equals(uri)) {
			this.preserveUri = true;
		} else {
			this.uri = uri;
		}
		
		if (PRESERVE.equals(queryString)) {
			this.preserveQueryString = true;
		} else {
			this.queryString = queryString;
		}
	}
	
	
	public int getPort() {
		return port;
	}
	
	public String getScheme() {
		return scheme;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String getQueryString() {
		return queryString;
	}
	
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String url = (this.preserveScheme ? request.getScheme() : this.getScheme()) + "://"
				+ this.getSite().getCanonicalDomain().getName()
				+ (this.preservePort ? ":" + String.valueOf(request.getServerPort()) : (this.port == 80 ? "" : ":" + String.valueOf(this.port)))
				+ (this.preserveUri ? ("/".equals(request.getRequestURI()) ? "" : request.getRequestURI()) : this.getUri())
				+ (this.preserveQueryString ? (request.getQueryString() != null ? request.getQueryString() : "") : this.getQueryString());
		
		if (logger.isLoggable(Level.FINER)) {
			logger.finest(this.getClass().getCanonicalName() + " is redirecting to: " + url);
		}
		response.sendRedirect(url);
	}
	
	
}
