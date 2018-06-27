package microweb.model;

public interface ExtensionFactory {

	public static final String FACTORY_METHOD_NAME = "create";
	
	public void initialise(Component component);
	
	public Component getComponent();
}
