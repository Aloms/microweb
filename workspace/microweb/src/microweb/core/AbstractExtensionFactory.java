package microweb.core;

import java.net.URL;
import java.util.logging.Logger;

import microweb.model.Component;
import microweb.model.ExtensionFactory;

public abstract class AbstractExtensionFactory implements ExtensionFactory {

	private Component component;
	private Logger logger;
	
	public Component getComponent() {
		return component;
	}

	@Override
	public void initialise(Component component) {
		this.component = component;
		this.logger = this.getComponent().getLogger();
	}
	
	protected Logger getLogger() {
		return this.logger;
	}

}
