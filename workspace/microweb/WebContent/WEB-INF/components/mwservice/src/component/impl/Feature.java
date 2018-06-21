package component.impl;

import microweb.core.AbstractHandler;
import microweb.core.ComponentException;
import microweb.model.Component;
import microweb.model.Handler;

public class Feature extends AbstractHandler{

	@Override
	public void process(String event) {
		this.getComponent().getLogger().fine("Doing handler stuff, by: " + this.getClass().getName() + ", on event: " + event);
	}
}
