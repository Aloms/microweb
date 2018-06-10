package microweb.model;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Domain {
	public String getName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
}
