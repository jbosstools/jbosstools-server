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

import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.jboss.ide.eclipse.as.core.publishers.JSTPublisherXMLToucher;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

public class JBossLT6ModuleStateVerifier extends AbstractSubsystemController implements IModuleStateController, IServerModuleStateVerifier {
	// Dependencies
	
	/*
	 * The deployment options gives us access to things like
	 * where the deployment root dir for a server should be,
	 * or whether the server prefers zipped settings
	 */
	private IDeploymentOptionsController deploymentOptions;
	
	/*
	 * The deploy path controller helps us to discover
	 * a module's root deployment directory
	 */
	private IModuleDeployPathController deployPathController;
	
	/*
	 * A filesystem controller gives us access to 
	 * a way to transfer individual files
	 */
	private IFilesystemController filesystemController;
	
	public JBossLT6ModuleStateVerifier() {
		// Nothing
	}
	

	/*
	 * Get the system for deployment options such as zipped or not
	 * We must pass in a custom environment here. 
	 */
	protected IDeploymentOptionsController getDeploymentOptions() throws CoreException {
		if( deploymentOptions == null ) {
			deploymentOptions = (IDeploymentOptionsController)findDependency(IDeploymentOptionsController.SYSTEM_ID);
		}
		return deploymentOptions;
	}
	
	/*
	 * get the system for deploy path for a given module
	 */
	protected IModuleDeployPathController getDeployPathController() throws CoreException {
		if( deployPathController == null ) {
			Map<String, Object> env = new HashMap<String, Object>(getEnvironment());
			env.put(IModuleDeployPathController.ENV_DEPLOYMENT_OPTIONS_CONTROLLER, getDeploymentOptions());
			deployPathController = (IModuleDeployPathController)findDependency(IModuleDeployPathController.SYSTEM_ID, getServer().getServerType().getId(), env);
		}
		return deployPathController;
	}

	/*
	 * get the filesystem controller for transfering files
	 */
	protected IFilesystemController getFilesystemController() throws CoreException {
		if( filesystemController == null ) {
			filesystemController = (IFilesystemController)findDependency(IFilesystemController.SYSTEM_ID);
		}
		return filesystemController;
	}

	
	
	public boolean isModuleStarted(final IServer server, final IModule[] module,
			final IProgressMonitor monitor) {
		return getModuleState(server, module, monitor) == IServer.STATE_STARTED;
	}
	
	public int getModuleState(final IServer server, final IModule[] module,
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
		// Leaving the jboss < 6 impl old and basic because I don't 
		// have the time to really dig into what else the jboss app server
		// can reply for deployment state at this time. 
		return result[0] ? IServer.STATE_STARTED : IServer.STATE_STOPPED;
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

	@Override
	public boolean canRestartModule(IModule[] module) {
		if( module.length == 1 ) 
			return true;
		return false;
	}

	@Override
	public int startModule(IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		// This impl is unable to stop or start a module; only restart		
		return getServer().getModuleState(module);
	}

	@Override
	public int stopModule(IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		// This impl is unable to stop or start a module; only restart		
		return getServer().getModuleState(module);
	}


	private IPath getModuleDeployRoot(IModule[] module) throws CoreException {
		// Find dependency will throw a CoreException if an object is not found, rather than return null
		IDeploymentOptionsController opts = getDeploymentOptions();
		IModuleDeployPathController depPath = getDeployPathController();
		return new RemotePath(depPath.getDeployDirectory(module), 
				opts.getPathSeparatorCharacter());
	}
	
	@Override
	public int restartModule(IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		IPath archiveDestination = getModuleDeployRoot(module);
		IFilesystemController controller = getFilesystemController();

		if( ServerModelUtilities.isBinaryModule(module[module.length-1]) || getDeploymentOptions().prefersZippedDeployments()) {
			controller.touchResource(archiveDestination, monitor);
		} else {
			JSTPublisherXMLToucher.getInstance().touch(archiveDestination, module[0], controller);
		}
		return IServer.STATE_STARTED;
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
