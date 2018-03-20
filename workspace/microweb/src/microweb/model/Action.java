package microweb.model;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Action {

	public void doAction(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException;
}
