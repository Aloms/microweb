package microweb.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.tomcat.util.digester.Digester;
import org.w3c.dom.Document;


/**
 * Servlet Filter implementation class GlobalHandler
 */
@WebFilter("/*")
public class GlobalHandler implements Filter {
	
	Logger logger;

    /**
     * Default constructor. 
     */
    public GlobalHandler() {
    	this.logger = Logger.getLogger(this.getClass().getPackage().getName());
    	
    	Util.init();
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
		Map<String, String> sitesRegistry = new ConcurrentHashMap<String, String>();
		sitesRegistry.put("prep", "/WEB-INF/sites/prep");
		sitesRegistry.put("lounge", "/WEB-INF/sites/lounge");
		sitesRegistry.put("website2", "/WEB-INF/sites/website2");
		
		//sites contains global set of loaded sites
		Map<String, Site> sites = new ConcurrentHashMap<String, Site>();
				
		
		Iterator<String> i = sitesRegistry.keySet().iterator();
		
		while (i.hasNext()) {
			String siteKey = i.next();
			
			String siteHome = sitesRegistry.get(siteKey);
			String siteConfigPath = siteHome + "/site.xml";
			
			String absolutePath = request.getServletContext().getRealPath(siteConfigPath);
			File f = new File(absolutePath);
			
			if (f.exists()) {
				logger.info("Initialising site [" + siteKey + "], configuration file is [" + absolutePath + "]");
				
				Site site = new Site(siteKey);
				
				sites.put(site.getName(), site);
				
				
				
				//FIXME: initialise url registry for this site
				
				request.getServletContext().setAttribute(Util.SITES_KEY, sites);
				
			} else {
				logger.warning("The Site Registry contains an entry for a site named [" + siteKey + "] but no site configuration file could be found at [" + absolutePath + "]");
			}
			
			
		}
		//Map<String, Node> urlRegistry
		
		String microwebContext = Util.getConfig().getProperty("microweb-context");
		
		if (path.startsWith(Util.getMicrowebPath())) {
			String staticPath = path.substring(microwebContext.length() + 1);

			if (staticPath.equals("") || staticPath.equals("/")) {
				this.logger.finest("path starts with microweb context [" + microwebContext + "] and static path is: [" + staticPath + "]. redirecting to microweb admin.");
				request.getRequestDispatcher("/" + Util.getConfig().getProperty("controller")).forward(request, response);
			} else {
				
				this.logger.finest("path starts with microweb context [" + microwebContext + "] and static path is: [" + staticPath + "]. loading static asset.");
				
				String site = staticPath.substring(1).split("/")[0];
				
				this.logger.fine("site:" + site);
				
				
				if (sitesRegistry.containsKey(site)) {
					this.logger.finer("found [" + site + "] in site registry");
					String siteRelativePath = staticPath.substring(site.length() + 1);
					this.logger.fine("siteRelativePath:" + siteRelativePath);
					
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
					
				} else {
					this.logger.finer("could not find [" + site + "] in site registry");
				}
				
				chain.doFilter(request, response);
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
