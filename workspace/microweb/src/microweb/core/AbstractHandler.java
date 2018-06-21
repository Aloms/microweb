package microweb.core;

import microweb.model.Component;
import microweb.model.Handler;

public abstract class AbstractHandler implements Handler{

	private Component component;
	
	public void setComponent(Component component) {
		this.component = component;
	}
	
	protected Component getComponent() {
		return this.component;
	}
}
