/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;

/**
 * A wrapper for pollers
 * @author Rob Stryker rob.stryker@redhat.com
 *
 */
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
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					"Error instantiating Server Poller: " + el.getAttribute("name"), e);
			JBossServerCorePlugin.getDefault().getLog().log(s);
		}
		return null;
	}
}
