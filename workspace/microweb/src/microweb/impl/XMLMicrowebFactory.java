package microweb.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import microweb.core.AbstractHandler;
import microweb.core.AbstractMicrowebFactory;
import microweb.core.Util;
import microweb.model.Component;
import microweb.model.Domain;
import microweb.model.Handler;
import microweb.model.Site;

public class XMLMicrowebFactory extends AbstractMicrowebFactory {

	
	private static final String ROOT_NODE = "root";
	
	private static final String NODE_ATTR_ACTION = "action";
	
	private static final String TEMPLATES_FOLDER = "templates";
	
	private static final Logger logger = Logger.getLogger("microweb.config");
	
	@Override
	public void init() {
	}
	
	@Override
	public void loadComponent(URL configUrl) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		
	}
	
	public void loadSites(ServletContext context, String sitesConfigPath) {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		
		String sitesConfigXsd = Util.MICROWEB_HOME + Util.getSystemProperty("sitesConfigXsd");
		
		String rootElement = "/sites-config";
		String sitesExpr = rootElement + "/sites/site";
		//String sitesConfigXsd = microwebHome + "/config/sites-config.xsd";
		
		
		try {
			
			URL sitesResourcePath = context.getResource(sitesConfigPath);
			logger.fine("loading sites configuration from: " + sitesResourcePath.toExternalForm());
			
			
			Util.validateXML(sitesResourcePath, context.getResource(sitesConfigXsd));
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document siteDom = builder.parse(sitesResourcePath.openStream());
			
			loadSites(context, sitesExpr, siteDom);
			
			checkCanonicals();
			
			registerDomains();
			
		    logger.info("Complete");
			 
			
		} catch (ParserConfigurationException e) {
			logger.log(Level.SEVERE, "Unable to W3C DOM Parser", e);
		} catch (SAXException e) {
			logger.log(Level.SEVERE, "microweb.application.config.sites-config.invalid", new Object[] {sitesConfigPath});
		} catch (XPathExpressionException e) {
			logger.log(Level.SEVERE, "Unable to compile Xpath Expression: " + sitesExpr, e);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "Invalid location for sites configuration: " + sitesConfigPath, e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to read XML file: " + sitesConfigPath, e);
		} 

		logger.info("Initialisation completed");
	}
	
	private static Site loadSite(ServletContext context, String location, String config) throws Exception {

		
		return SiteImpl.createFromConfig(context, location, config);
		
	}
	
	private void registerDomains() {
		Collection<Site> sites =  Util.<Site> getRegistry(Util.REGISTRY_SITES).values();
		
		for (Site site : sites) {
			
			Collection<Domain> domains = site.getDomains();
			
			for (Domain domain : domains) {
				Domain existingDomain = Util.<Domain> getRegistry(Util.REGISTRY_DOMAINS).putIfAbsent(domain.getName(), domain);
				
				if (existingDomain != null) {
					logger.log(Level.WARNING, "microweb.application.config.sites-config.duplicatedDomains", new Object[] {domain.getName()});
				}
			}
		}
	}

	private void checkCanonicals() {
		Collection<Site> sites =  Util.<Site> getRegistry(Util.REGISTRY_SITES).values();
		
		for (Site site : sites) {
			
			if (site.getCanonicalDomain() == null) {
				logger.log(Level.WARNING, "microweb.application.config.sites-config.noCanonicalDomainForSite", new Object[] {site.getName()});
				site.setStatus(Site.STATUS_INVALID);
			} else {
				logger.config(site.getCanonicalDomain().getName() + " is the canonical name for the " + site.getName() + " site.");
				site.setStatus(Site.STATUS_ONLINE);
			}
		}
	}



	private void loadSites(ServletContext context, String sitesExpr, Document siteDom)	throws XPathExpressionException, MalformedURLException {
		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xPath.compile(sitesExpr);
		
		
		NodeList sites = (NodeList) expr.evaluate(siteDom.getDocumentElement(), XPathConstants.NODESET);
		
		for (int i = 0; i < sites.getLength(); i++) {
			Element siteElement = (Element) sites.item(i);
			String location = xPath.evaluate("@location", siteElement);
			String config = xPath.evaluate("@config", siteElement);

			logger.info("location:" + location + ", config:" + config);

			try {
				Site site = XMLMicrowebFactory.loadSite(context, location, config);
				
				Util.getRegistry(Util.REGISTRY_SITES).put(site.getName(), site);
				logger.log(Level.CONFIG, "microweb.application.config.site.loaded", new Object[] {site.getName()});
			} catch (Exception e) {
				logger.log(Level.SEVERE, "microweb.application.config.site.failedToLoad", new Object[] {location + "/" + config});
				logger.log(Level.SEVERE, "Fail to instantiate site", e);
			}
			
		}
	}

	@Override
	public void loadCore(URL microwebConfigUrl, URL microwebSchemaUrl) {
		if (logger.isLoggable(Level.FINEST)) {
			
			logger.finest("microwebConfigUrl: " + microwebConfigUrl.toExternalForm());
			logger.finest("microwebSchemaUrl: " + microwebSchemaUrl.toExternalForm());
			
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		
		
		try {
			
			
			
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Initialising microweb platform from : " + microwebConfigUrl.toExternalForm());
			}
			
			Util.validateXML(microwebConfigUrl, microwebSchemaUrl);
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document dom = builder.parse(microwebConfigUrl.openStream());
			
			loadMicrowebConfig(dom);
			
			/*
			loadSites(context, sitesExpr, siteDom);
			
			checkCanonicals();
			
			registerDomains();
			
			*/
			
			if (logger.isLoggable(Level.FINE)) {
				logger.info("Finished initialising platform");
			}
			
		} catch (ParserConfigurationException e) {
			handleInitFailed(e, "microweb.application.config.schemaValidationFailed", new Object[] {microwebConfigUrl.toExternalForm(), microwebSchemaUrl.toExternalForm()});
		} catch (SAXException e) {
			handleInitFailed(e, "microweb.application.config.invalidCoreConfigFiles", new Object[] {microwebConfigUrl.toExternalForm(), microwebSchemaUrl.toExternalForm()});
		} catch (MalformedURLException e) {
			handleInitFailed(e, "microweb.application.config.invalidUrlToCoreConfigFiles", new Object[] {microwebConfigUrl.toExternalForm(), microwebSchemaUrl.toExternalForm()});
		} catch (IOException e) {
			handleInitFailed(e, "microweb.application.config.unableToReadCoreConfigFiles", new Object[] {microwebConfigUrl.toExternalForm(), microwebSchemaUrl.toExternalForm()});
		} catch (XPathExpressionException e) {
			handleInitFailed(e, "microweb.application.config.xPathInvalid", new Object[] {microwebConfigUrl.toExternalForm()});
		} 

		logger.info("Initialisation completed");
		
	}

	private void loadMicrowebConfig(Document dom) throws XPathExpressionException {
		
		String componentSchemaPath = Util.MICROWEB_HOME + Util.getSystemProperty("componentSchema");
		logger.fine("using component schema at: " + componentSchemaPath);
		
		URL componentSchemaUrl = null;
		
		
		
		if (componentSchemaPath == null || componentSchemaPath.trim().equals("")) {
			logger.log(Level.SEVERE, "microweb.components.config.nullSchemaPath", new Object[] {"componentSchema"});
			return;
		} else {
			try {
				componentSchemaUrl = Util.getServletContext().getResource(componentSchemaPath);
			} catch (MalformedURLException e) {
				logger.log(Level.SEVERE, "microweb.components.config.nullSchemaPath", new Object[] {"componentSchema"});
				return;
			}
			if (componentSchemaUrl == null) {
				logger.log(Level.SEVERE, "microweb.components.config.nullSchemaPath", new Object[] {"componentSchema"});
				return;
			}
		}
		
		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression expr = xPath.compile("/microweb-config/components/component");
		
		NodeList nodes = (NodeList) expr.evaluate(dom.getDocumentElement(), XPathConstants.NODESET);
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			String s_active = xPath.evaluate("@active", element);
			String s_config = xPath.evaluate("@config", element);
			
			if (!Boolean.parseBoolean(s_active)) {
				if (logger.isLoggable(Level.FINE)) {
					logger.info("found inactive component configuration at: " + s_config + ", skipping...");
				}
				continue;
			}


			String componentConfigPath = Util.MICROWEB_HOME + s_config;
			
			
			try {

				URL componentConfigUrl = Util.getServletContext().getResource(componentConfigPath);
				
				
				if (componentConfigUrl != null) {
					Util.validateXML(componentConfigUrl, componentSchemaUrl);
					
					Component component = this.createComponent(componentConfigUrl);
					Util.getRegistry(Util.REGISTRY_COMPONENTS).put(component.getName(), component);
					
				} else {
					handleInitFailed("microweb.components.config.invalidConfigLocation", new Object[] {componentConfigPath, dom.getDocumentURI(), componentSchemaPath});
				}
				
				
			} catch (MalformedURLException e) {
				handleInitFailed(e, "microweb.components.config.invalidConfigLocation", new Object[] {componentConfigPath, dom.getDocumentURI(), componentSchemaPath});
				continue;
			} catch (SAXException e) {
				handleInitFailed(e, "microweb.components.config.schemaValidationFailed", new Object[] {componentConfigPath, componentSchemaPath});
				continue;
			} catch (IOException e) {
				handleInitFailed(e, "microweb.components.config.unableToReadConfigFiles", new Object[] {componentConfigPath, componentSchemaPath});
				continue;
			} catch (ParserConfigurationException e) {
				handleInitFailed(e, "microweb.components.config.xPathInvalid", new Object[] {componentConfigPath});
				continue;
			} catch (ClassNotFoundException e) {
				handleInitFailed(e, "microweb.components.config.classNotFound", new Object[] {componentConfigPath});
				continue;
			} catch (InstantiationException e) {
				handleInitFailed(e, "microweb.components.config.classNotInstantiated", new Object[] {componentConfigPath});
				continue;
			} catch (IllegalAccessException e) {
				handleInitFailed(e, "microweb.components.config.classNotAccessable", new Object[] {componentConfigPath});
				continue;
			} catch (NoSuchMethodException e) {
				handleInitFailed(e, "microweb.components.config.classNotInstantiated", new Object[] {componentConfigPath});
				continue;
			} catch (SecurityException e) {
				handleInitFailed(e, "microweb.components.config.instantiationSecurityException", new Object[] {componentConfigPath});
				continue;
			} catch (IllegalArgumentException e) {
				handleInitFailed(e, "microweb.components.config.classNotInstantiated", new Object[] {componentConfigPath});
				continue;
			} catch (InvocationTargetException e) {
				handleInitFailed(e, "microweb.components.config.classNotInstantiated", new Object[] {componentConfigPath});
				continue;
			}
				
			
			
			

			
			
			/*
			String config = xPath.evaluate("@config", siteElement);

			logger.info("location:" + location + ", config:" + config);

			try {
				Site site = XMLSiteFactory.loadSite(context, location, config);
				
				Util.getSiteRegistry().put(site.getName(), site);
				logger.log(Level.CONFIG, "microweb.application.config.site.loaded", new Object[] {site.getName()});
			} catch (Exception e) {
				logger.log(Level.SEVERE, "microweb.application.config.site.failedToLoad", new Object[] {location + "/" + config});
				logger.log(Level.SEVERE, "Fail to instantiate site", e);
			}
			*/
			
		}
		
	}
	
	private Component createComponent(URL componentConfigUrl) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		
		
		
		
		
		/*
		XPathExpression expr = xPath.compile("/component/components/component");
		
		expr.evaluate(dom.getDocumentElement(), XPathConstants.NODE);
		String s_active = xPath.evaluate("@active", dom);
		
		NodeList nodes = (NodeList) expr.evaluate(dom.getDocumentElement(), XPathConstants.NODESET);
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			String s_active = xPath.evaluate("@active", element);
			String s_config = xPath.evaluate("@config", element);
		*/
		Component component = new ComponentImpl(componentConfigUrl);
		
		return component;
		
	}

	private void handleInitFailed(String messageId, Object[] values) {
		handleInitFailed(null, messageId, values);
	}
	
	private void handleInitFailed(Throwable e, String messageId, Object[] values) {
		logger.log(Level.WARNING, messageId, values);
		if (e != null) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		
	}
	

	private class ComponentImpl implements Component{
		private static final String EVENT_REGISTRY_PREFIX = "MW_COMPONENT_EVENT_";
		private Properties parameters;
		private String name;
		private Logger myLogger;
		private List<Handler> handlers;
		private String componentHome;
		private MyClassLoader classLoader;
		//private List<String> classNames;
		
		private ComponentImpl(URL componentConfigUrl) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
			
			this.componentHome = componentConfigUrl.toExternalForm().substring(0, componentConfigUrl.toExternalForm().lastIndexOf("/"));
			this.classLoader = new MyClassLoader(this);
			
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Initialising component at: " + this.componentHome);
			}
			
			Document dom = Util.getDocument(componentConfigUrl);
			
			XPath xPath = XPathFactory.newInstance().newXPath();

			
			this.name = (String) xPath.evaluate("@name", dom.getDocumentElement(), XPathConstants.STRING);
			
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("component name: " + this.getName());
			}
			
			
			this.parameters = this.extractProperties(dom.getDocumentElement(), xPath);
			this.myLogger = this.extractLogger(dom.getDocumentElement(), xPath);
			this.handlers = this.extractHandlers(dom.getDocumentElement(), xPath, this.classLoader);
			
			
			
		}

		private class MyClassLoader extends ClassLoader {
			
			private Collection<String> handledClasses;
			private Component component;
			
			private MyClassLoader(Component component) {
		        super(component.getClass().getClassLoader());
		        this.handledClasses = new HashSet<String>();
		        this.component = component;
		    }
			
			public void handleClass(String classname) {
				this.handledClasses.add(classname);
				
				if(this.component.getLogger().isLoggable(Level.FINEST)) {
	        		this.component.getLogger().finest("Custom classloader for the " + this.component.getName() + " component will handle loading requests for: " + classname);
	        	}
			}
			
			public Class loadClass(String name) throws ClassNotFoundException {
				
		        if (!this.handledClasses.contains(name)) {
		        	if(this.component.getLogger().isLoggable(Level.FINEST)) {
		        		this.component.getLogger().finest(name + " is not one of the classes this class loader is handling for component: " + this.component.getName() + ".  Passing on the class load request to parent class loader: " + this.getParent().getClass().getCanonicalName());
		        	}
		        	return super.loadClass(name);
		        }
		        
		        if(this.component.getLogger().isLoggable(Level.FINEST)) {
	        		this.component.getLogger().finest(name + " is one of the classes this class loader is handling for component: " + this.component.getName());
	        	}
		        
		        String urlString = this.component.getHome() + "/" + "classes" + "/" + name.replace(".", "/") + ".class";
	            
	            if(this.component.getLogger().isLoggable(Level.FINEST)) {
	        		this.component.getLogger().finest("attempting to load class at: " + urlString);
	        	}
	            
	  
	            URL url = null;
				try {
					url = new URL(urlString);
					
					if(this.component.getLogger().isLoggable(Level.FINEST)) {
		        		this.component.getLogger().finest("valid url to class: " + urlString);
		        	}
					
				} catch (MalformedURLException e) {
					if(this.component.getLogger().isLoggable(Level.WARNING)) {
		        		this.component.getLogger().warning("invalid url to class: " + urlString);
		        	}
				}
	            
	            if (url != null) {
	            	try (InputStream input = url.openConnection().getInputStream(); ByteArrayOutputStream buffer = new ByteArrayOutputStream()){
			            //String url = "file:C:/data/projects/tutorials/web/WEB-INF/" + "classes/reflection/MyObject.class";

			            
			            int data = input.read();

			            while(data != -1){
			                buffer.write(data);
			                data = input.read();
			            }

			            input.close();

			            byte[] classData = buffer.toByteArray();

			            return defineClass(name, classData, 0, classData.length);

			        } catch (Exception e) {
			        	logger.log(Level.SEVERE, e.getMessage(), e);
			        } 
	            }
	            

		        return null;
		    }
		}
		
		private List<Handler> extractHandlers(Element documentElement, XPath xPath, MyClassLoader classLoader) throws XPathExpressionException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
			List<Handler> handlers = new ArrayList<Handler>();
			
			NodeList nodes = (NodeList) xPath.evaluate("handlers/handler", documentElement, XPathConstants.NODESET);
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				String s_event = xPath.evaluate("@event", element);
				String s_class = xPath.evaluate("@class", element);
				
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("initialising new handler: " + s_class + " for event: " + s_event);
				}
				
				classLoader.handleClass(s_class);
				Class handlerClass = classLoader.loadClass(s_class);
				
				if (handlerClass != null) {
					//Constructor c = handlerClass.getConstructor( new Class[] {this.getClass()});
					//Handler handler = (Handler) c.newInstance(this);
							
					AbstractHandler handler = (AbstractHandler) handlerClass.newInstance();
					handler.setComponent(this);
					
					handlers.add(handler);
					
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest("initialising new handler: " + s_class + " for event: " + s_event);
					}
					
					Map<String, Collection<Handler>> eventRegistry = Util.getRegistry(this.EVENT_REGISTRY_PREFIX + this.getName());
					
					Collection<Handler> registeredHandlers = null;
					if (!eventRegistry.containsKey(s_event)) {
						
						if (logger.isLoggable(Level.FINER)) {
							logger.finer("component name: " + this.getName() + " doesnt have any existing handlers registered to the " + s_event + " event");
						}
						registeredHandlers = new HashSet<Handler>();
						
					}
					
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("adding new handler: " + handler.getClass().getName() + " for the " + s_event + " event in the " + this.getName() + " component");
					}
					registeredHandlers.add(handler);
					eventRegistry.put(s_event, registeredHandlers);
				} else  {
					if (logger.isLoggable(Level.WARNING)) {
						logger.warning("failed to load class for handler: " + s_class);
					}
				}
				
				
				/*
				ClassLoader cl = new URLClassLoader(urls);

			    // Load in the class
			    Class cls = cl.loadClass("MyReloadableClassImpl");
			    
				Handler h = Class.f
				*/
			}
			return handlers;
		}

		private Logger extractLogger(Element documentElement, XPath xPath) throws XPathExpressionException {
			Element element = (Element) xPath.evaluate("logger", documentElement, XPathConstants.NODE);
			
			String s_name = xPath.evaluate("@name", element);
			String s_level = xPath.evaluate("@level", element);
			
			Logger newLogger = Logger.getLogger(s_name);
			newLogger.setLevel(Level.parse(s_level));
			
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Component: " + this.getName() + " will be logging to the " + newLogger.getName() + " logger");
			}
			
			if (newLogger.isLoggable(Level.CONFIG)) {
				newLogger.log(Level.CONFIG, "Component: " + this.getName() + " logger initialised at: " + newLogger.getName());
			}
			
			return newLogger;
		}

		private Properties extractProperties(Element documentElement, XPath xPath) throws XPathExpressionException {
			Properties properties = new Properties();
			
			NodeList nodes = (NodeList) xPath.evaluate("parameters/parameter", documentElement, XPathConstants.NODESET);
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				String s_name = xPath.evaluate("@name", element);
				String s_value = xPath.evaluate("@value", element);
				
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("reading parameter name: " + s_name + ", value: " + s_value);
				}
				properties.put(s_name, s_value);
				
			}
			return properties;
		}

		@Override
		public Properties getParameters() {
			return this.parameters;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Logger getLogger() {
			return this.myLogger;
		}

		@Override
		public List<Handler> getHandlers() {
			return this.handlers;
		}

		@Override
		public String getHome() {
			return componentHome;
		}

		@Override
		public void invokeEvent(String name) {
			Map<String, Collection<Handler>> eventRegistry = Util.getRegistry(this.EVENT_REGISTRY_PREFIX + this.getName());
			
			if (!eventRegistry.containsKey(name)) {
				if (this.myLogger.isLoggable(Level.FINEST)) {
					this.myLogger.finest("Component: " + this.getName() + " does not have any handlers registered to the " + name + " event.  nothing to do.");
				}
				return;
			}
			
			Collection<Handler> handlers = eventRegistry.get(name);
			
			if (this.myLogger.isLoggable(Level.FINEST)) {
				this.myLogger.finest("Component: " + this.getName() + " has " + handlers.size() + " handlers registered to the " + name + " event.");
			}
			
			for (Handler handler : handlers) {
				
				if (this.myLogger.isLoggable(Level.FINEST)) {
					this.myLogger.finest("invoking handler: " + handler.getClass().getName() + " for event: " + name);
				}
				
				try {
					handler.process(name);
				} catch (Exception e) {
					if (this.myLogger.isLoggable(Level.WARNING)) {
						this.myLogger.log(Level.WARNING, "the handler: " + handler.getClass().getName() + " encountered an exception: " + e.getMessage(), e);
					}
				}
				
			}
		}
	}
}
