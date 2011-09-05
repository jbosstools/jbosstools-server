package org.jboss.ide.eclipse.as.openshift.core;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.internal.core.utils.UrlBuilder;

public class DomainFactory {

	private UrlBuilder urlBuilder;

	public DomainFactory() {
		this.urlBuilder = new UrlBuilder("https://openshift.redhat.com");
	}

	public Domain create() throws DomainException {
		ModelNode node = new ModelNode();
		node.get("namespace").set("string");
System.err.println(node.toJSONString(false));
		//		new UrlConnectionHttpClient(urlBuilder.path("broker").path("domain").toUrl()).post(node.toJSONString(false));
		return null;
	}
	
}
