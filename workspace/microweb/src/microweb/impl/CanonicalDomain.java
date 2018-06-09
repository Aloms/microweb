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
import microweb.model.Site;

public class CanonicalDomain extends HostedDomain {

	
	
	protected CanonicalDomain(String name, Site site) throws XPathExpressionException {
		super(name, site, true);
	}
	
	
}
