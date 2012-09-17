/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;

/**
 * A wrapper for pollers
 * @author Rob Stryker rob.stryker@redhat.com
 *
 */
public class ServerStatePollerType implements IServerStatePollerType {
	private IConfigurationElement el;
	public ServerStatePollerType(IConfigurationElement el) {
		this.el = el;
	}
	public boolean supportsStartup() {
		return Boolean.parseBoolean(el.getAttribute("supportsStartup")); //$NON-NLS-1$
	}
	public boolean supportsShutdown() {
		return Boolean.parseBoolean(el.getAttribute("supportsShutdown")); //$NON-NLS-1$
	}
	public String getName() {
		return el.getAttribute("name"); //$NON-NLS-1$
	}
	public String getId() {
		return el.getAttribute("id"); //$NON-NLS-1$
	}
	public String getServerTypes() {
		return el.getAttribute("serverTypes"); //$NON-NLS-1$
	}
	
	public IServerStatePoller createPoller() {
		try {
			return (IServerStatePoller)el.createExecutableExtension("class"); //$NON-NLS-1$
		} catch( CoreException e ) {
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.CannotLoadServerPoller, el.getAttribute("name")), e); //$NON-NLS-1$
			JBossServerCorePlugin.getDefault().getLog().log(s);
		}
		return null;
	}
}
