package microweb.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import microweb.core.AbstractDomain;

public class AliasDomain extends AbstractDomain {

	protected AliasDomain(String name, String site) throws XPathExpressionException {
		super(name, site);
	}
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.finest(this.getClass().getCanonicalName() + " is passing on to site handler for site: " + this.getSite());
	}
}
