package microweb.core;

import java.net.URL;

import microweb.model.Component;
import microweb.model.FeatureFactory;

public abstract class AbstractFeatureFactory implements FeatureFactory {

	private Component component;
	
	protected Component getComponent() {
		return component;
	}

	@Override
	public void initialise(Component component) {
		this.component = component;
	}
	
	protected abstract void construct(URL pathToConfig);

}
