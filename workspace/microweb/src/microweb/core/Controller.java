package microweb.core;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Controller
 */
//@WebServlet("/*")
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String TEMPLATE_PATH = "/WEB-INF/templates";
	
	Logger logger;
	
	//private Properties config;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Controller() {
        super();

        //this.config = new Properties();
        this.logger = Logger.getLogger(this.getClass().getPackage().getName());
        
    }
    
    
    

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String contextPath = request.getContextPath();
		String requestUrl = request.getRequestURL().toString();
		String requestUri = request.getRequestURI();
		
		
		
		
		String microwebContext = Util.getConfig().getProperty("microweb-context");
		String controller = Util.getConfig().getProperty("controller");
		
		logger.finest("contextPath:" + contextPath);
		logger.finest("requestUrl:" + requestUrl);
		logger.finest("requestUri:" + requestUri);
		logger.finest("microwebContext:" + microwebContext);
		logger.finest("controller:" + controller);
		
		if (Util.isController(requestUri)) {
			logger.finest("Controller called");
			
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

			pageRegistry.put("AdminConsole", TEMPLATE_PATH + "/microweb/pages/admin-console.jsp");
			
			if (action.equals("Page")) {
				
				String template = pageRegistry.getProperty(name);
				
				if (template == null || template.equals("") || template.trim().equals("")) {
					throw new RuntimeException("No configuration for action: " + name);
				}
				
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(template);
				dispatcher.forward(request, response);
				return;
			}
		} else {
			logger.finest("Controller was not called");
		}
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher( "/WEB-INF/templates/main.jsp" );
		dispatcher.forward( request, response );
	}




	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
