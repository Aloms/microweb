package microweb.core;

import java.net.URL;

import microweb.model.Component;
import microweb.model.ExtensionFactory;

public abstract class AbstractExtensionFactory implements ExtensionFactory {

	private Component component;
	
	public Component getComponent() {
		return component;
	}

	@Override
	public void initialise(Component component) {
		this.component = component;
	}

}
