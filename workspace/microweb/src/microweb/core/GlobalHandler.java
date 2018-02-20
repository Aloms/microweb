package microweb.core;

import java.io.IOException;
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
    	String loggerName = this.getClass().getPackage().getName();
    	System.out.println("Logging to " + loggerName);
    	this.logger = Logger.getLogger(loggerName);
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
		System.out.println("path: " + path);
		
	    String topfolder = path.substring(1);
	    System.out.println("topfolder1: " + topfolder);
	    
	    if (topfolder.contains("/")) {
	        topfolder = topfolder.substring(0, topfolder.indexOf("/"));
	    }
	    
	    System.out.println("topfolder2: " + topfolder);

	    
	    this.logger.severe("[" + Level.SEVERE + "] SEVERE message");
	    this.logger.warning("[" + Level.WARNING + "] WARNING message");
	    this.logger.info("[" + Level.INFO  + "] INFO  message");
	    this.logger.config("[" + Level.CONFIG + "] CONFIG message");
	    this.logger.fine("[" + Level.FINE + "] FINE message");
	    this.logger.finer("[" + Level.FINER + "] FINER message");
	    this.logger.finest("[" + Level.FINEST + "] FINEST message");
	    
	    
	    if (topfolder.startsWith("css")) {
	        chain.doFilter(request, response);
	    } else {
	        request.getRequestDispatcher("/Controller" + path).forward(request, response);
	    }
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
