package microweb.impl;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SectionHelper {

	private static final XPath xPath = XPathFactory.newInstance().newXPath();
	
	public static String getId(Node section) {
		return getAttribute(section, "id");
	}
	
	public static String getLabel(Node section) {
		return getAttribute(section, "label");
	}
	
	public static String toString(Node section) {
		return "(" + getId(section) + ":" + getLabel(section) + ")";
	}
	
	public static boolean isRoot(Node section) {
		verifySection(section);
		
		Node parent = section.getParentNode();
		
		if (parent != null && parent.getNodeName().equals("navigations")) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isSiteNav(Node section) {
		String s_siteNav = getAttribute(section, "site-nav");
		
		if (s_siteNav != null && Boolean.parseBoolean(s_siteNav)) {
			return true;
		}
		
		return false;
	}
	
	private static void verifySection(Node section) {
		if(!isSection(section)) {
			throw new RuntimeException(section.toString() + " is not a section");
		}
	}
	
	public static String getAttribute(Node section, String attributeName) {
		verifySection(section);
		
		try {
			return xPath.evaluate("@" + attributeName, section);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	public static boolean isSection(Node section) {
		if (section.getNodeName().equals("section")) {
			return true;
		}
		
		return false;
	}
}
