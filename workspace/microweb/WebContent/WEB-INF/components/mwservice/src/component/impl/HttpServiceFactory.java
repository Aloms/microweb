package component.impl;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import component.model.HttpService;
import microweb.core.AbstractExtensionFactory;
import microweb.model.Extension;
import microweb.model.ExtensionFactory;

public class HttpServiceFactory extends AbstractExtensionFactory {

	@Override
	public <T extends Extension> T create(URL config, String extensionType) {
		if (extensionType.equals("HttpService")) {
			return (T) new HttpService(){

				@Override
				public void execute(HttpServletRequest request, HttpServletResponse response) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public String getName() {
					// TODO Auto-generated method stub
					return null;
				}};
		}
		return null;
	}

	/*
	public HttpService create(URL config, Class ) {
		if (this.getLogger().isLoggable(Level.FINEST)) {
			this.getLogger().log(Level.FINEST, "Constructing instance of " + HttpService.class.getCanonicalName() + " from config: " + config.toExternalForm());
		}
		
		return null;
	}
	*/
	
	/*
	public <T> T create(URL config, T t) {
		if (this.getLogger().isLoggable(Level.FINEST)) {
			this.getLogger().log(Level.FINEST, "Constructing instance of " + HttpService.class.getCanonicalName() + " from config: " + config.toExternalForm());
		}
		
		return null;
	}
	*/
	
	/*
	public <T extends Extension> T callFriend(URL config, Class<T> type) {
		
		if (this.getLogger().isLoggable(Level.FINEST)) {
			this.getLogger().log(Level.FINEST, "Constructing instance of " + HttpService.class.getCanonicalName() + " from config: " + config.toExternalForm());
		}
		
		if (type.equals(HttpService.class)) {
			return type.cast(new HttpService(){

				@Override
				public void execute(HttpServletRequest request, HttpServletResponse response) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public String getName() {
					// TODO Auto-generated method stub
					return null;
				}});
		}
		
		return null;
	    
	}
	*/
	
	
	
}
