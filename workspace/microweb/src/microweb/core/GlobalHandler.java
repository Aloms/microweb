package microweb.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		Map<String, String> sitesRegistry = new HashMap<String, String>();
		sitesRegistry.put("prep", "prep");
		sitesRegistry.put("lounge", "lounge");
		sitesRegistry.put("website2", "website2");
		
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
					this.logger.finer("found " + site + " in site registry");
				} else {
					this.logger.finer("could not find " + site + " in site registry");
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
