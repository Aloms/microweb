package microweb.core;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.sun.media.jfxmedia.logging.Logger;

/**
 * Servlet Filter implementation class GlobalHandler
 */
@WebFilter("/*")
public class GlobalHandler implements Filter {

    /**
     * Default constructor. 
     */
    public GlobalHandler() {
        // TODO Auto-generated constructor stub
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
