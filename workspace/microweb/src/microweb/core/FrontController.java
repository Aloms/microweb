package microweb.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import microweb.impl.XMLMicrowebFactory;
import microweb.model.Component;
import microweb.model.Domain;
import microweb.model.MicrowebFactory;
import microweb.model.Site;

/**
 * Servlet implementation class FrontController
 */
@WebServlet("/")
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String MICROWEB_SERVICE_URI = "/Service";
     
	private Logger logger;
	private Logger httpLogger;
	
	private boolean failedInit = false;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() {
        super();
        this.logger = Logger.getLogger("microweb.core", "messages");
        this.httpLogger = Logger.getLogger("microweb.http", "messages");
        
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
		
		if (logger.isLoggable(Level.FINER)) {
			String url = 	scheme + "://" +   // "http" + "://
					serverName +       // "myhost"
					":" + serverPort + // ":" + "8080"
					requestUri +       // "/people"
					(queryString != null ? "?" + queryString : ""); // "?" + "lastname=Fox&age=30"
	
			StringBuffer b = new StringBuffer();
			b.append("\n").append("contextPath:" + contextPath);
			b.append("\n").append("requestUrl:" + requestUrl);
			//b.append("\n").append("microwebContext:" + microwebContext);
			//b.append("\n").append("controller:" + controller);
			b.append("\n").append("scheme:" + scheme);
			b.append("\n").append("serverName:" + serverName);
			b.append("\n").append("serverPort:" + String.valueOf(serverPort));
			b.append("\n").append("requestUri:" + requestUri);
			b.append("\n").append("queryString:" + queryString);
			b.append("\n").append("url: " + url);
			
			logger.finer(b.toString());
		}
		
		
		if (failedInit) {
			
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("Unable to handle request as the microweb platform is in a failed to initialise state.");
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			
		} else {
			if (requestUri.equals(MICROWEB_SERVICE_URI)) {
				//TODO: run service validator to check service parameters
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("request is being handled by microweb service handler");
				}
				String name = request.getParameter("name");
				
				if (name != null && !name.trim().equals("")) {
					/*
					Service service = Util.getServiceRegistry().get(name);
					
					if (service != null) {
						if (logger.isLoggable(Level.FINEST)) {
							logger.finest("found service handler");
						}
					} else {
						if (logger.isLoggable(Level.FINEST)) {
							logger.finest("no service handler called [" + name + "] found");
						}
						response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
					}
					*/
				} else {
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest("service name must not be null or empty");
					}
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}
				
				
			} else {
				//TODO: run uri validator to check uri is valid
				Domain domain = Util.<Domain> getRegistry(Util.REGISTRY_DOMAINS).get(serverName);
				if (domain != null) {
					
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest("found domain: " + domain.getName());
					}
					domain.handle(request, response);
					
				} else {
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest("no hosted site for domain name: " + serverName);
					}
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}
			
		}

		if (httpLogger.isLoggable(Level.INFO)) {
			httpLogger.log(Level.INFO, "site.http.responseCode", new Object[] {response.getStatus(), request.getRequestURL()});
		}
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

		
		
		URL microwebProperties = null;
		URL systemProperties = null;
		try {
			microwebProperties = getServletContext().getResource(this.getServletContext().getInitParameter("microweb-properties"));
			systemProperties = getServletContext().getResource(this.getServletContext().getInitParameter("system-properties"));
			
			
			
		} catch (MalformedURLException e) {
			handleInitFailed(e, "microweb.application.initialisationFailed", new Object[] {});
			return;
		}

		Util.init(microwebProperties, systemProperties, getServletContext());
		//loadCore(this.getServletContext());
		
		String factoryConfigKey = "MicrowebFactory";
		String componentName = "Core";
		String configuredFactory = Util.getSystemProperty(factoryConfigKey);
		
		if (configuredFactory == null || configuredFactory.trim().equals("")) {
			
			handleInitFailed("microweb.application.config.initFailed", new Object[] {componentName, factoryConfigKey, systemProperties.toExternalForm()});
			return;
		}
		
		configuredFactory = configuredFactory.trim();
		
		Class<?> factoryClass;
		try {
			factoryClass = Class.forName(configuredFactory);
		} catch (ClassNotFoundException e) {
			handleInitFailed(e, "microweb.application.config.initFailed.factoryClass", new Object[] {factoryConfigKey, configuredFactory});
			return;
		}
		
		
		MicrowebFactory factory;
		try {
			factory = (MicrowebFactory) factoryClass.newInstance();
		} catch (InstantiationException e) {
			handleInitFailed(e, "microweb.application.config.initFailed.factoryClass", new Object[] {factoryConfigKey, configuredFactory});
			return;
		} catch (IllegalAccessException e) {
			handleInitFailed(e, "microweb.application.config.initFailed.factoryClass", new Object[] {factoryConfigKey, configuredFactory});
			return;
		}
		
		
		
		
		
		
		initialise(factory);
		
		String sitesConfigPath = Util.MICROWEB_HOME + Util.getApplicationProperty("sites-config");
		factory.loadSites(this.getServletContext(), sitesConfigPath);
		
		/*
		
		
		String methodName = "loadSites";
		Method loadSitesMethod;
		try {
			loadSitesMethod = factoryClass.getMethod(methodName, new Class[] {ServletContext.class, String.class});
			loadSitesMethod.invoke(factory, new Object[] {getServletContext(), sitesConfigPath});
		} catch (Exception e) {
			handleInitFailed(e, "microweb.application.config.initFailed.factoryMethod", new Object[] {factoryConfigKey, configuredFactory, methodName});
			return;
		}
		*/
		
		
	}



	

	private void initialise(MicrowebFactory factory) {
		
		
		
		String microwebConfigPath = Util.MICROWEB_HOME + Util.getSystemProperty("microwebConfig");
		String microwebSchemaPath = Util.MICROWEB_HOME + Util.getSystemProperty("microwebSchema");
		
		URL microwebConfigUrl = null;
		URL microwebSchemaUrl = null;
		
		if (microwebConfigPath == null || microwebConfigPath.trim().equals("")) {
			handleInitFailed("microweb.application.config.initFailed.microwebConfigPathInvalid", new Object[] {Util.getSystemProperty("microwebConfig"), microwebConfigPath});
			return;
		} else {
			try {
				microwebConfigUrl = this.getServletContext().getResource(microwebConfigPath);
			} catch (MalformedURLException e) {
				handleInitFailed(e, "microweb.application.config.initFailed.microwebConfigPathInvalid", new Object[] {Util.getSystemProperty("microwebConfig"), microwebConfigPath});
				return;
			}
		}
		
		if (microwebSchemaPath == null || microwebSchemaPath.trim().equals("")) {
			handleInitFailed("microweb.application.config.initFailed.microwebConfigSchemaInvalid", new Object[] {Util.getSystemProperty("microwebSchema"), microwebSchemaPath});
			return;
		} else {
			try {
				microwebSchemaUrl = this.getServletContext().getResource(microwebSchemaPath);
			} catch (MalformedURLException e) {
				handleInitFailed(e, "microweb.application.config.initFailed.microwebConfigSchemaInvalid", new Object[] {Util.getSystemProperty("microwebConfig"), microwebSchemaPath});
				return;
			}
		}
		

		if (logger.isLoggable(Level.FINEST)) {
			
			logger.finest("microwebConfigUrl: " + microwebConfigUrl.toExternalForm());
			logger.finest("microwebSchemaUrl: " + microwebSchemaUrl.toExternalForm());
			
		}
		
		factory.init();
		factory.loadCore(microwebConfigUrl, microwebSchemaUrl);
		
	}

	private void handleInitFailed(String messageId, Object[] values) {
		handleInitFailed(null, messageId, values);
	}
	
	private void handleInitFailed(Throwable e, String messageId, Object[] values) {
		logger.log(Level.SEVERE, messageId, values);
		if (e != null) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
	}

	
	


}
