package microweb.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tomcat.util.digester.Digester;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import microweb.model.Section;
import microweb.model.Site;


/**
 * Servlet Filter implementation class GlobalHandler
 */
//@WebFilter("/*")
public class GlobalHandler implements Filter {
	

	Logger logger;

    /**
     * Default constructor. 
     */
    public GlobalHandler() {
    	this.logger = Logger.getLogger(this.getClass().getPackage().getName());
    	//logger.info("java.util.logging.config.file: " + System.getProperty("java.util.logging.config.file"));
    	
    	//Util.init();
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String path = req.getRequestURI();
		this.logger.finest("path: " + path);
		
		//FIXME: implement global check to handle input parameters and validate which services can take which parameters
		
		//FIXME: move creation of sitesRegistry to application initialisation
		Map<String, String> sitesConfigurationRegistry = new ConcurrentHashMap<String, String>();
		sitesConfigurationRegistry.put("admin", "/WEB-INF/sites/admin");
		sitesConfigurationRegistry.put("prep", "/WEB-INF/sites/prep");
		//sitesConfigurationRegistry.put("lounge", "/WEB-INF/sites/lounge");
		//sitesConfigurationRegistry.put("website2", "/WEB-INF/sites/website2");
		
		//sites contains global set of loaded sites
		Map<String, Site> sitesRegistry = new ConcurrentHashMap<String, Site>();
				
		
		Iterator<String> i = sitesConfigurationRegistry.keySet().iterator();
		
		while (i.hasNext()) {
			
			String siteKey = i.next();
			
			String siteConfig = sitesConfigurationRegistry.get(siteKey);
			
			String absolutePath = request.getServletContext().getRealPath(siteConfig);
			
			Site site;
			try {
				/*
				site = XMLSiteFactory.loadSite(absolutePath);
				sitesRegistry.put(site.getName(), site);
				logger.info("added [" + site.getName() + "] to site registry.");
				*/
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not initialise site: " + siteKey + "(" + absolutePath + ")", e);
			}
			
			
		}
		
		request.getServletContext().setAttribute(Util.SITES_KEY, sitesRegistry);
		
		//Map<String, Node> urlRegistry
		
		String microwebContext = Util.getConfig().getProperty("microweb-context");
		
		if (path.startsWith(Util.getMicrowebPath())) {
			String staticPath = path.substring(microwebContext.length() + 1);

			if (staticPath.equals("") || staticPath.equals("/")) {
				this.logger.finest("path starts with microweb context [" + microwebContext + "] and static path is: [" + staticPath + "]. redirecting to microweb admin.");
				request.getRequestDispatcher("/" + Util.getConfig().getProperty("controller")).forward(request, response);
			} else {
				
				this.logger.finest("path starts with microweb context [" + microwebContext + "] and static path is: [" + staticPath + "]. loading static asset.");
				
				String siteName = staticPath.substring(1).split("/")[0];
				
				this.logger.fine("siteUri:" + siteName);
				
				Map<String, Site> sites = (Map<String, Site>) request.getServletContext().getAttribute(Util.SITES_KEY);
				
				if (sites == null) {
					logger.severe("No Sites registry found under key: [" + Util.SITES_KEY + "]");
				} else {
					if (sites.containsKey(siteName)) {
						this.logger.finer("found [" + siteName + "] in site registry");
						
						Site site = sites.get(siteName);
						
						String siteRelativePath = staticPath.substring(siteName.length() + 1);
						this.logger.fine("siteRelativePath:" + siteRelativePath);
						
						
						
						Section node = site.getSectionByURI(siteRelativePath);
						
						if (node != null) {
							logger.fine("found a section handler [" + node.getName() + "] for the uri: " + siteRelativePath);
							request.setAttribute("Action", node.getAction());
						} else {
							logger.fine("no section handler exists for the uri: " + siteRelativePath);
						}
						
						/*
						
						String action = request.getParameter("Action");
						String name = request.getParameter("Name");
						
						if (action == null || action.equals("") || action.trim().equals("")) {
							
							action = "Page";
							name = "AdminConsole";
							
							if (logger.isLoggable(Level.FINE)) {
								logger.fine("action is not given, defaulting to show portal");
							}
						}
						
						Properties pageRegistry = new Properties();

						pageRegistry.put("AdminConsole", Util.TEMPLATE_PATH + "/microweb/pages/admin-console.jsp");
						*/
						/*
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document doc = builder.parse("/WEB-INF/sites/prep/prep.xml");
						
						XPathFactory xPathfactory = XPathFactory.newInstance();
						XPath xpath = xPathfactory.newXPath();
						
						//first look for fully qualified urls
						//XPathExpression expr = xpath.compile("//Type[@type_id=\"4218\"]");
						XPathExpression expr = xpath.compile("/site/structure//node[@type_id=\"4218\"]");
						NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
						
						XPathExpression expr = xpath.compile("/site/structure/");
						*/
						
						logger.fine("filter complete, forwading to filter chain");
						chain.doFilter(request, response);
						
					} else {
						throw new ServletException("could not find [" + siteName + "] in site registry.");
					}
				}
			}
			
			
			
		} else {
			this.logger.finest("path does not start with microweb context [" + microwebContext + "] forwarding to static path: [" + path + "]");
			chain.doFilter(request, response);
		}
		
	}

	

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
