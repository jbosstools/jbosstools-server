package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;

public class ServerStatePollerType {
	private IConfigurationElement el;
	public ServerStatePollerType(IConfigurationElement el) {
		this.el = el;
	}
	public boolean supportsStartup() {
		return Boolean.parseBoolean(el.getAttribute("supportsStartup"));
	}
	public boolean supportsShutdown() {
		return Boolean.parseBoolean(el.getAttribute("supportsShutdown"));
	}
	public String getName() {
		return el.getAttribute("name");
	}
	public String getId() {
		return el.getAttribute("id");
	}
	public IServerStatePoller createPoller() {
		try {
			return (IServerStatePoller)el.createExecutableExtension("class");
		} catch( Exception e ) {
		}
		return null;
	}
}
