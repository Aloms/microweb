package microweb.model;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpExtension extends Extension{
	
	public static final HttpMethod HTTP_DELETE = new HttpMethod() {};
	public static final HttpMethod HTTP_GET = new HttpMethod() {};
	public static final HttpMethod HTTP_HEAD = new HttpMethod() {};
	public static final HttpMethod HTTP_OPTIONS = new HttpMethod() {};
	public static final HttpMethod HTTP_PATCH = new HttpMethod() {};
	public static final HttpMethod HTTP_POST = new HttpMethod() {};
	public static final HttpMethod HTTP_PUT = new HttpMethod() {};
	

	public interface HttpMethod {
		
	}
	
	public void execute(HttpServletRequest request, HttpServletResponse response);

}
