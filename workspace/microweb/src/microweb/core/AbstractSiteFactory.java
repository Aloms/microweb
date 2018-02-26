package microweb.core;

import microweb.model.Node;
import microweb.model.Redirect;
import microweb.model.Section;

public class AbstractSiteFactory {

	protected abstract class NodeImpl implements Node {
		
		
		private int type;
		
		private String name;
		private String uri;
		
		protected NodeImpl(String name, int type) {
			this.name = name;
			this.type = type;
		}
		
		public int getType() {
			return type;
		}
		public String getName() {
			return name;
		}
		public String getUri() {
			return uri;
		}
		void setUri(String uri) {
			this.uri = uri;
		}
		
		
	}
	
	public class SectionImpl extends NodeImpl implements Section {

		
		private String template;
		private String canonicalUri;
		
		protected SectionImpl(String name) {
			super(name, Node.SECTION);
		}
		
		public String getTemplate() {
			return template;
		}
		
		void setTemplate(String template) {
			this.template = template;
		}
		
		public String getCanonicalUri() {
			return canonicalUri;
		}
		void setCanonicalUri(String canonicalUri) {
			this.canonicalUri = canonicalUri;
		}
		
	}
	
	public class RedirectImpl extends NodeImpl implements Redirect {
		
		private String redirect;

		RedirectImpl(String name) {
			super(name, Node.REDIRECT);
		}
		
		public String getRedirect() {
			return redirect;
		}

		void setRedirect(String redirect) {
			this.redirect = redirect;
		}
		
		
	}

}
