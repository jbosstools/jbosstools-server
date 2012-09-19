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
package org.jboss.ide.eclipse.as.core.server.internal;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunnable;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;

public class JBossLT6ModuleStateVerifier implements IServerModuleStateVerifier {
	public JBossLT6ModuleStateVerifier() {
		// Nothing
	}
	public boolean isModuleStarted(final IServer server, final IModule[] module,
			final IProgressMonitor monitor) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		IServerJMXRunnable r = new IServerJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				result[0] = checkDeploymentStarted(server, module, connection, monitor);
			}
		};
		try {
			ExtensionManager.getDefault().getJMXRunner().run(server, r);
		} catch( CoreException jmxe ) {
			IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.RESUME_DEPLOYMENT_SCANNER, Messages.JMXResumeScannerError, jmxe);
			ServerLogger.getDefault().log(server, status);
		} finally {
			ExtensionManager.getDefault().getJMXRunner().endTransaction(server, this);
		}
		return result[0];
	}

	public void waitModuleStarted(IServer server, IModule[] module, int maxDelay) {
		final NullProgressMonitor monitor = new NullProgressMonitor();
		Thread t = new Thread(){
			public void run() {
				try {
					Thread.sleep(20000);
				} catch(InterruptedException ie) {
					return;
				}
				synchronized(monitor) {
					monitor.setCanceled(false);
				}
			}
		};
		t.start();
		
		// synchronous call to wait
		waitModuleStarted(server, module, monitor);
		
		// call is over, can notify the thread to go finish itself
		synchronized(monitor) {
			if( !monitor.isCanceled() )
				t.interrupt();
		}
	}
	public void waitModuleStarted(IServer server, IModule[] module,
			IProgressMonitor monitor) {
		waitJMX(server, module);
	}
	
	protected void waitJMX(final IServer server, final IModule[] module) {
		IServerJMXRunnable r = new IServerJMXRunnable() {
			public void run(MBeanServerConnection connection) throws Exception {
				jmxWaitForDeploymentStarted(server, module, connection, null);
			}
		};
		try {
			ExtensionManager.getDefault().getJMXRunner().run(server, r);
		} catch( CoreException jmxe ) {
			IStatus status = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.RESUME_DEPLOYMENT_SCANNER, Messages.JMXResumeScannerError, jmxe);
			ServerLogger.getDefault().log(server, status);
		} finally {
			ExtensionManager.getDefault().getJMXRunner().endTransaction(server, this);
		}
	}

	protected void jmxWaitForDeploymentStarted(final IServer server, final IModule[] module,
			final MBeanServerConnection connection, IProgressMonitor monitor) throws Exception {
		monitor = monitor == null ? new NullProgressMonitor() : monitor;
		monitor.beginTask("Ensuring Deployments are Loaded", 10000); //$NON-NLS-1$
		while( !monitor.isCanceled()) {
			boolean done = checkDeploymentStarted(server, module, connection, monitor);
			if( done ) {
				monitor.done();
				return;
			}
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {
				// Intentional ignore
			}
		}
	}

	protected boolean checkDeploymentStarted(final IServer server, final IModule[] module,
			final MBeanServerConnection connection, IProgressMonitor monitor) throws Exception {
		String typeId = module[module.length-1].getModuleType().getId();
		if( typeId.equals("wst.web") || typeId.equals("jst.web")) { //$NON-NLS-1$ //$NON-NLS-2$
			return checkWebModuleStarted(server, module, connection);
		}
		return true;
	}	
	
	protected boolean checkWebModuleStarted(IServer server, IModule[] module, MBeanServerConnection connection) throws Exception {
		if( module.length > 1) {
			return checkNestedWebModuleStarted(server, module, connection);
		} else {
			return checkStandaloneWebModuleStarted(server, module, connection);
		}
	}
	
	protected boolean checkNestedWebModuleStarted(IServer server, IModule[] module, MBeanServerConnection connection) throws Exception {
		String n = module[module.length-1].getName();
		String mbeanName = "jboss.deployment:id=\"jboss.web.deployment:war=/" + n + "\",type=Component";  //$NON-NLS-1$//$NON-NLS-2$
		String stateAttribute = "State"; //$NON-NLS-1$
		Object result = getAttributeResult(connection, mbeanName, stateAttribute);
		if( result == null || !result.toString().equals("DEPLOYED"))  //$NON-NLS-1$
			return false;
		return true;
	}

	protected boolean checkStandaloneWebModuleStarted(IServer server, IModule[] module, MBeanServerConnection connection) throws Exception {
		String n = module[module.length-1].getName();
		String mbeanName = "jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=WebModule,name=//localhost/" + n; //$NON-NLS-1$
		String stateAttribute = "state"; //$NON-NLS-1$
		Object result = getAttributeResult(connection, mbeanName, stateAttribute);
		if(result == null || !(result instanceof Integer) || ((Integer)result).intValue() != 1 ) {
			return false;
		}
		return true;
	}

	protected Object getAttributeResult(final MBeanServerConnection connection, String mbeanName, String stateAttribute) throws Exception {
		ObjectName on = new ObjectName(mbeanName);
		try {
			return connection.getAttribute(on, stateAttribute);
		} catch(InstanceNotFoundException infe) {
			return false;
		}
	}
}
