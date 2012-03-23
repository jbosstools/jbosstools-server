/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunnable;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunner;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class JBossServerJMXRunner implements IServerJMXRunner {

	public void run(IServer server, final IServerJMXRunnable runnable) throws CoreException {
		IJMXRunnable runnable2 = new IJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				runnable.run(connection);
			}
		};
		try {
			JBossJMXConnectionProviderModel.getDefault().run(server, runnable2);
		} catch(JMXException jmxe) {
			// TODO wrap and log
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, jmxe.getMessage(), jmxe));
		}
	}

	public void beginTransaction(IServer server, Object lock) {
		AbstractJBossJMXConnectionProvider provider = JBossJMXConnectionProviderModel.getDefault().getProvider(server);
		if( provider != null && provider.hasClassloaderRepository())
			provider.getClassloaderRepository().addConcerned(server, lock);
	}

	public void endTransaction(IServer server, Object lock) {
		AbstractJBossJMXConnectionProvider provider = JBossJMXConnectionProviderModel.getDefault().getProvider(server);
		if( provider != null && provider.hasClassloaderRepository())
			provider.getClassloaderRepository().removeConcerned(server, lock);
	}
}
