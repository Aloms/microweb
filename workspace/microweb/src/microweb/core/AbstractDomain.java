package microweb.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

import microweb.model.Domain;
import microweb.model.Site;

public abstract class AbstractDomain implements Domain {
	
	protected static Logger logger = Logger.getLogger("microweb.core", "messages");

	protected String name;
	protected Site site;
	
	
	protected AbstractDomain(String name, Site site) {
		this.name = name;
		this.site = site;
	}
	
	public String getName() {
		return name;
	}

	public Site getSite() {
		return this.site;
	}
	
}
