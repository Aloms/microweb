package microweb.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import microweb.core.AbstractDomain;
import microweb.model.Site;

public class AliasDomain extends HostedDomain {

	protected AliasDomain(String name, Site site) throws XPathExpressionException {
		super(name, site, false);
	}
}
