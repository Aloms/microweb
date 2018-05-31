package microweb.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

import microweb.core.DomainFactory;
import microweb.core.Util;
import microweb.model.Domain;

public class XMLDomainFactory extends DomainFactory {

	protected static Logger logger = Logger.getLogger("microweb.config", "messages");
	
	public static void createAndRegister(Element domainElement) throws XPathExpressionException {
		Domain domain = null;
		XPath xPath = XPathFactory.newInstance().newXPath();

		String elName = domainElement.getNodeName();
		String s_name = xPath.evaluate("@name", domainElement);
		String s_site = xPath.evaluate("@site", domainElement);
		
		if (elName.equals("redirect")) {
			String s_scheme = xPath.evaluate("@scheme", domainElement);
			String s_port = xPath.evaluate("@port", domainElement);
			String s_uri = xPath.evaluate("@uri", domainElement);
			String s_queryString = xPath.evaluate("@queryString", domainElement);
			
			
			if (s_scheme.equals("preserve")) {
				//preserve the scheme
				domain = new RedirectedDomain(s_name.trim(), s_site, s_scheme, s_port, s_uri, s_queryString);
			}
		} else if (elName.equals("alias")){
			domain = new AliasDomain(s_name.trim(), s_site);
		} else if (elName.equals("canonical")){
			domain = new CanonicalDomain(s_name.trim(), s_site);
		} else {
			logger.log(Level.WARNING, "core.config.sitenotinitialised", new Object[] {elName});
		}
		
		Util.getDomainRegistry().put(domain.getName(), domain);
	}
	
}
