package microweb.model;

import java.net.URL;

public interface ExtensionFactory {

	public static final String FACTORY_METHOD_NAME = "create";
	
	public void initialise(Component component);
	
	public Component getComponent();
	
	public <T extends Extension> T create(URL config, String extensionType);
}
