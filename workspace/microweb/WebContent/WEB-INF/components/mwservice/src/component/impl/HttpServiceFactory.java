package component.impl;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import microweb.core.AbstractFeatureFactory;
import microweb.model.FeatureFactory;
import microweb.services.HttpService;

public class HttpServiceFactory extends AbstractFeatureFactory {

	@Override
	protected void construct(URL pathToConfig) {
		HttpService service = new HttpService() {

			@Override
			public void execute(HttpServletRequest request, HttpServletResponse response) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

}
