package microweb.impl;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import microweb.core.AbstractDomain;
import microweb.core.Util;
import microweb.model.Domain;

public class CanonicalDomain extends AbstractDomain {

	protected CanonicalDomain(String name, String site) throws XPathExpressionException {
		super(name, site);
		
		if (this.getSite() != null) {
			Domain domain = Util.getSiteCanonicalDomainsRegistry().get(this.getSite().getName());
			
			if (domain != null) {
				//site already has a canonical domain
				logger.log(Level.WARNING, "microweb.application.config.sites-config.multipleCanonicalDomains", new Object[] {this.getSite().getName(), domain.getName(), this.getName()});
			} else {
				Util.getSiteCanonicalDomainsRegistry().put(this.getSite().getName(), this);
			}
		}
		
	}
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.finest(this.getClass().getCanonicalName() + " is passing on to site handler for site: " + this.getSite());
	}
}
