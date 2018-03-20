package microweb.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import microweb.model.Site;

/**
 * Servlet implementation class FrontController
 */
@WebServlet("/")
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String MICROWEB_PROPERTIES_FILE = "microweb.properties"; 
     
	private Logger logger;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() {
        super();
        this.logger = Logger.getLogger(this.getClass().getPackage().getName());
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String contextPath = request.getContextPath();
		String requestUrl = request.getRequestURL().toString();
		String requestUri = request.getRequestURI();
		
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String queryString = request.getQueryString();
		
		
		
		String microwebContext = Util.getConfig().getProperty("microweb-context");
		String controller = Util.getConfig().getProperty("controller");
		
		String url = 	scheme + "://" +   // "http" + "://
						serverName +       // "myhost"
						":" + serverPort + // ":" + "8080"
						requestUri +       // "/people"
						(queryString != null ? "?" + queryString : ""); // "?" + "lastname=Fox&age=30"
		
		logger.finest("contextPath:" + contextPath);
		logger.finest("requestUrl:" + requestUrl);
		
		logger.finest("microwebContext:" + microwebContext);
		logger.finest("controller:" + controller);
		logger.finest("scheme:" + scheme);
		logger.finest("serverName:" + serverName);
		logger.finest("serverPort:" + String.valueOf(serverPort));
		logger.finest("requestUri:" + requestUri);
		logger.finest("queryString:" + queryString);
		
		logger.finest("url: " + url);
		
		
		Properties hostedDomains = new Properties();
		
		if (hostedDomains.containsKey(serverName)) {
			logger.finest("found hosted site for domain name: " + serverName);
		} else {
			logger.finest("no hosted site for domain name: " + serverName);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		
		
		
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	@Override
	public void init() throws ServletException {
		logger.info("Initialising " + this.getClass().getName() +  " Servlet");
		
		String propPathStr = "/WEB-INF/" + MICROWEB_PROPERTIES_FILE;
		
		
		Properties props = new Properties();
		
		try {
			URL propertiesPath = getServletContext().getResource(propPathStr);
			//URL url = new URL("url");
			InputStream in = propertiesPath.openStream();
			Reader reader = new InputStreamReader(in, "UTF-8"); // for example
			
			props.load(reader);
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Unable to initialise Microweb Core from properties file: " + propPathStr, e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to initialise Microweb Core from properties file: " + propPathStr, e);
		}
		
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
			
			String absolutePath = getServletContext().getRealPath(siteConfig);
			
			Site site;
			try {
				site = XMLSiteFactory.loadSite(absolutePath);
				sitesRegistry.put(site.getName(), site);
				logger.info("added [" + site.getName() + "] to site registry.");
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not initialise site: " + siteKey + "(" + absolutePath + ")", e);
			}
			
			
		}
		
		getServletContext().setAttribute(Util.SITES_KEY, sitesRegistry);
		logger.info("Initialisation completed");
	}
	
	

}
