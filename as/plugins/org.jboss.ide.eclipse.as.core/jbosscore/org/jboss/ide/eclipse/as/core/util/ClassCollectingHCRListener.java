/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.ServerHotCodeReplaceListener;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.jmx.core.IConnectionFacade;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public final class ClassCollectingHCRListener extends ServerHotCodeReplaceListener {

	public ClassCollectingHCRListener(IServer server, ILaunch launch) {
		super(server, launch);
	}

	@Override
	protected void postPublish(IJavaDebugTarget target, IModule[] modules) {
		IServer server = getServer();
		waitModulesStarted(modules);
		removeBreakpoints(target);
		executeJMXGarbageCollection(server, modules);
		addBreakpoints(target);
	}
	
	
	private void addBreakpoints(IJavaDebugTarget target) {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			target.breakpointAdded(breakpoints[i]);
		}
	}

	private void removeBreakpoints(IJavaDebugTarget target) {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			target.breakpointRemoved(breakpoints[i], null);
		}
	}

	private void executeJMXGarbageCollection(IServer server, IModule[] modules) {
		IConnectionFacade jbs = (IConnectionFacade) server.loadAdapter(IConnectionFacade.class, null);
		if (jbs instanceof IConnectionFacade) {
			IConnectionWrapper wrap = ((IConnectionFacade) jbs).getJMXConnection();
			try {
				if (!wrap.isConnected()) {
					wrap.connect();
				}
				wrap.run(new IJMXRunnable() {
					public void run(MBeanServerConnection connection) throws Exception {
						Object ret = connection.invoke(new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME), "gc", new Object[0], new String[0]); //$NON-NLS-1$
						Object ret2 = connection.invoke(new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME), "gc", new Object[0], new String[0]); //$NON-NLS-1$
					}
				});
			} catch (JMXException | IOException e) {
				JBossServerCorePlugin.log(
						StatusFactory.errorStatus(JBossServerCorePlugin.PLUGIN_ID, 
						"Error executing garbage collection on server after publish", e)); //$NON-NLS-1$
			} 
		}
	}
}