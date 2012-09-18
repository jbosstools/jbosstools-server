/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;

public class JBoss7ModuleStateVerifier implements IServerModuleStateVerifier {
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
					monitor.setCanceled(true);
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

	public boolean isModuleStarted(IServer server, IModule[] module,
			IProgressMonitor monitor) {
		try {
			return isRootModuleStarted(server, module[0], monitor);
		} catch(Exception e ) {
			String er = "Error occurred while checking module state for {0} on server {1}"; //$NON-NLS-1$
			IStatus s = new Status(
					IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(er, module[0].getName(), server.getName()), e);
			ServerLogger.getDefault().log(server, s);
			return false;
		}
	}
	
	private boolean isRootModuleStarted(IServer server, IModule root, IProgressMonitor monitor) throws Exception {
		JBoss7Server jbossServer = ServerConverter.checkedGetJBossServer(server, JBoss7Server.class);
		IJBoss7ManagerService service = JBoss7ManagerUtil.getService(server);
		// TODO does this need to change??
		IPath deployPath = jbossServer.getDeploymentLocation(new IModule[]{root}, true); 
		return isRootModuleStarted(server, root, service, deployPath, monitor);
	}

	private boolean isRootModuleStarted(IServer server, IModule root, 
			IJBoss7ManagerService service, IPath deployPath, IProgressMonitor monitor) throws Exception {
		AS7ManagementDetails details = new AS7ManagementDetails(server);
		boolean done = false;
		if (service.isRunning(details)) { // to avoid asking while server is starting up.
			JBoss7DeploymentState state = service.getDeploymentState(
					details, deployPath.lastSegment());
			done = (state == JBoss7DeploymentState.STARTED);
		}
		return done;
	}

	
	public void waitModuleStarted(IServer server, IModule[] module, IProgressMonitor monitor) {
		try {
			JBoss7Server jbossServer = ServerConverter.checkedGetJBossServer(server, JBoss7Server.class);
			IJBoss7ManagerService service = JBoss7ManagerUtil.getService(server);
			// TODO DOES THIS NEED TO CHANGE?
			IPath deployPath = jbossServer.getDeploymentLocation(new IModule[]{module[0]}, true);
			boolean waitedOnce = false;
			
			while (!monitor.isCanceled()) {
				boolean done = isRootModuleStarted(server, module[0], service, deployPath, monitor);
				if (done) {
					return;
				}
				if(!waitedOnce) {
					String info = "Module {0} on {1} not yet fully deployed. Waiting..."; //$NON-NLS-1$
					IStatus s = new Status( IStatus.INFO, JBossServerCorePlugin.PLUGIN_ID, 
						NLS.bind(info, module[0].getName(), server.getName()),null);
					ServerLogger.getDefault().log(server, s);
				}
				waitedOnce = true;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
					// Ignore, intentional
				}
			}
			
			String warning = "Module {0} on {1} still not ready to be shown in browser. Aborting delay."; //$NON-NLS-1$
			IStatus s = new Status(
					IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(warning, module[0].getName(), server.getName()), null);
			ServerLogger.getDefault().log(server, s);
		} catch (Exception e) {
			String er = "Error occurred while waiting for {0} to start on server {1}"; //$NON-NLS-1$
			IStatus s = new Status(
					IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(er, module[0].getName(), server.getName()), e);
			ServerLogger.getDefault().log(server, s);
		}
	}
}
