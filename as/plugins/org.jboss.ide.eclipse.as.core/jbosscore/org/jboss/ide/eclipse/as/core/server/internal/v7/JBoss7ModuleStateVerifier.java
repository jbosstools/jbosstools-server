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

import org.eclipse.core.runtime.CoreException;
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
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

public class JBoss7ModuleStateVerifier extends AbstractSubsystemController implements IModuleStateController, IServerModuleStateVerifier {
	public void waitModuleStarted(IServer server, IModule[] module, final int maxDelay) {
		final NullProgressMonitor monitor = new NullProgressMonitor();
		Thread t = new Thread(){
			public void run() {
				try {
					Thread.sleep(maxDelay);
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
		return getModuleState(server, module, monitor) == IServer.STATE_STARTED;
	}
	
	public int getModuleState(IServer server, IModule[] module,
			IProgressMonitor monitor) {
		try {
			return getRootModuleState(server, module[0], monitor);
		} catch(Exception e ) {
			String er = "Error occurred while checking module state for {0} on server {1}"; //$NON-NLS-1$
			IStatus s = new Status(
					IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(er, module[0].getName(), server.getName()), e);
			ServerLogger.getDefault().log(server, s);
			return IServer.STATE_UNKNOWN;
		}
	}
	
	private int getRootModuleState(IServer server, IModule root, IProgressMonitor monitor) throws Exception {
		IJBoss7ManagerService service = JBoss7ManagerUtil.getService(server);
		return getRootModuleState(server, root, service, getDeploymentName(server, root), monitor);
	}
	
	private String getDeploymentName(IServer server, IModule module) throws Exception {
		return getDeploymentOutputName(server, module);
	}

	private String getDeploymentOutputName(IServer server, IModule module) {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
		if( beh != null ) {
			try {
				IModule[] moduleToTest = new IModule[]{module};
				IModuleDeployPathController controller = (IModuleDeployPathController)beh.getController(IModuleDeployPathController.SYSTEM_ID);
				return controller.getOutputName(moduleToTest);
			} catch(CoreException ce) {
				// TODO log
				return null;
			}
		} 
		return null;
	}
	
	private boolean isRootModuleStarted(IServer server, IModule root, 
			IJBoss7ManagerService service, String deploymentName, IProgressMonitor monitor) throws Exception {
		return getRootModuleState(server, root, service, deploymentName, monitor) == IServer.STATE_STARTED;
	}
	
	private int getRootModuleState(IServer server, IModule root, 
			IJBoss7ManagerService service, String deploymentName, IProgressMonitor monitor) throws Exception {
		AS7ManagementDetails details = new AS7ManagementDetails(server);
		int ret = IServer.STATE_UNKNOWN;
		if (service.isRunning(details)) { // to avoid asking while server is starting up.
			JBoss7DeploymentState state = service.getDeploymentState(
					details, deploymentName);
			if( state == JBoss7DeploymentState.STARTED)
				return IServer.STATE_STARTED;
			if( state == JBoss7DeploymentState.STOPPED)
				return IServer.STATE_STOPPED;
		}
		return ret;
	}

	
	public void waitModuleStarted(IServer server, IModule[] module, IProgressMonitor monitor) {
		try {
			IJBoss7ManagerService service = JBoss7ManagerUtil.getService(server);
			String deploymentName = getDeploymentName(server, module[0]);
			boolean waitedOnce = false;
			
			while (!monitor.isCanceled()) {
				boolean done = isRootModuleStarted(server, module[0], service, deploymentName, monitor);
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
	

	@Override
	public int stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException  {
		return changeModuleStateTo(module, IServer.STATE_STOPPED, monitor);	
	}
	
	@Override
	public int startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		return changeModuleStateTo(module, IServer.STATE_STARTED, monitor);
	}
	
	public int changeModuleStateTo(IModule[] module, int state, IProgressMonitor monitor) throws CoreException {
		AS7ManagementDetails details = new AS7ManagementDetails(getServer());
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(getServer());
		IModuleDeployPathController controller = (IModuleDeployPathController)beh.getController(IModuleDeployPathController.SYSTEM_ID);
		String deploymentName =  controller.getOutputName(module);

		try {
			IJBoss7ManagerService service = JBoss7ManagerUtil.getService(getServer());
			if( service == null ) {
				throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Management service for server not found.")); //$NON-NLS-1$
			} else if(state == IServer.STATE_STARTED ) {
				service.deploySync(details, deploymentName, null, false, monitor);
			} else if (state == IServer.STATE_STOPPED) {
				service.undeploySync(details, deploymentName, false, monitor);
			} else {
				throw new IllegalArgumentException("Only states IServer.STATE_STARTED and IServer.STATE_STOPPED are supported"); //$NON-NLS-1$
			}
			return state;
		} catch(JBoss7ManangerException j7me) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, j7me.getMessage(),j7me));
		}
	}

	@Override
	public int restartModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		stopModule(module, monitor);
		return startModule(module, monitor);
	}

	@Override
	public boolean canRestartModule(IModule[] module) {
		if( module.length == 1 ) 
			return true;
		return false;
	}

	@Override
	public int getModuleState(IModule[] module, IProgressMonitor monitor) {
		return getModuleState(getServer(), module, monitor);
	}

	@Override
	public boolean isModuleStarted(IModule[] module, IProgressMonitor monitor) {
		return isModuleStarted(getServer(), module, monitor);
	}

	@Override
	public void waitModuleStarted(IModule[] module, IProgressMonitor monitor) {
		waitModuleStarted(getServer(), module, monitor);
	}

	@Override
	public void waitModuleStarted(IModule[] module, int maxDelay) {
		waitModuleStarted(getServer(), module, maxDelay);
	}
}
