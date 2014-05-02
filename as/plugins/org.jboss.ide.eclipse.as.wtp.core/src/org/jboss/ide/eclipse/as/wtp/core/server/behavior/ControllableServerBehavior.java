/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

/**
 * @since 3.0 
 */
public class ControllableServerBehavior extends ServerBehaviourDelegate implements IControllableServerBehavior {
	public static final String PROPERTY_PREFIX = "SUBSYSTEM_OVERRIDE_";
	
	
	/**
	 * A shared location to store data relevant to multiple subsystems
	 */
	protected HashMap<String, Object> sharedData = new HashMap<String, Object>();
	protected final Object serverStateLock = new Object();
	
	public synchronized Object getSharedData(String key) {
		return sharedData.get(key);
	}
	
	public synchronized void putSharedData(String key, Object o) {
		sharedData.put(key, o);
	}
	
	/**
	 * Find the default subsystem controller for the given system
	 * @param system
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController getController(String system) throws CoreException {
		if( SYSTEM_PUBLISH.equals(system) )
			return getPublishController();
		if( SYSTEM_MODULES.equals(system) )
			return getModuleStateController();
		if( SYSTEM_SHUTDOWN.equals(system) )
			return getShutdownController();
		if( SYSTEM_LAUNCH.equals(system) )
			return getLaunchController();
		return getController(system, null);
	}
	
	/**
	 * Find the default subsystem controller for the given system with 
	 * an environment to be passed to it.  This method will also check the server
	 * for hard-coded overrides on which subsystem impl to choose, if they are set on the server. 
	 * 
	 * @param system
	 * @param env
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController getController(String system, ControllerEnvironment env) throws CoreException {
		ISubsystemController ret = getOverrideController(system, env);
		if( ret == null ) {
			// Otherwise, just get one from the model
			ret = SubsystemModel.getInstance().createSubsystemController(getServer(), system, env == null ? null : env.getMap());
		}
		if( ret == null ) {
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 0, 
					"Unable to locate system " + system + " for server " + getServer().getName(), null));
		}
		return ret;
	}
	
	
	protected ISubsystemController getOverrideController(String system, ControllerEnvironment env) throws CoreException {
		// FIrst check if the server has a hard-coded subsystem to choose for this system
		String propOverride = PROPERTY_PREFIX + system;
		String val = getServer().getAttribute(propOverride, (String)null);
		ISubsystemController ret = null;
		Map<String,Object> envMap =  env == null ? null : env.getMap();
		if( val != null ) {
			ret = SubsystemModel.getInstance().createControllerForSubsystem(
					getServer(), getServer().getServerType().getId(), system, val, envMap);
		}
		return ret;
	}
	
	protected IPublishController getPublishController() throws CoreException {
		return (IPublishController)getController(SYSTEM_PUBLISH, null);
	}
	
	protected IModuleStateController getModuleStateController() throws CoreException {
		return (IModuleStateController)getController(SYSTEM_MODULES, null);
	}

	protected IServerShutdownController getShutdownController() throws CoreException {
		return (IServerShutdownController)getController(SYSTEM_SHUTDOWN, null);
	}
	
	protected ILaunchServerController getLaunchController() throws CoreException {
		return (ILaunchServerController)getController(SYSTEM_LAUNCH, null);
	}
	
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		ILaunchServerController controller = getLaunchController();
		controller.setupLaunchConfiguration(workingCopy, monitor);
	}

	
	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		IPublishController controller = getPublishController();
		controller.publishFinish(monitor);
	}
	@Override
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		IPublishController controller = getPublishController();
		int result = controller.publishModule( kind, deltaKind, module, monitor);
		setModulePublishState(module, result);
	}

	@Override
	protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
		IPublishController controller = getPublishController();
		controller.publishServer(kind, monitor);
	}
	
	@Override
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		IPublishController controller = getPublishController();
		controller.publishStart(monitor);
	}

	

	@Override
	public boolean canRestartModule(IModule[] module) {
		try {
			IModuleStateController controller = getModuleStateController();
			return controller.canRestartModule(module);
		} catch(CoreException ce) {
			ASWTPToolsPlugin.log(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, "Unable to locate a module state controller for the given server"));
		}
		return false;
	}
	
	@Override
	public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		IModuleStateController controller = getModuleStateController();
		int newState = controller.startModule(module, monitor);
		setModuleState(module, newState);
	}

	@Override
	public void stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		IModuleStateController controller = getModuleStateController();
		int newState = controller.stopModule(module, monitor);
		setModuleState(module, newState);
	}
	
	@Override
	public void restartModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		IModuleStateController controller = getModuleStateController();
		int newState = controller.restartModule(module, monitor);
		setModuleState(module, newState);
	}

	public void setServerStarting() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STARTING);
		}
	}

	public void setServerStarted() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STARTED);
		}
	}

	public void setServerStopping() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STOPPING);
		}
	}
	
	public void setServerStopped() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STOPPED);
		}
		setModulesStopped(new IModule[]{}, getServer().getModules());
	}
	
	protected void setModulesStopped(IModule[] parent, IModule[] children) {
		for( int i = 0; i < children.length; i++ ) {
			IModule[] combined = combine(parent, children[i]);
			setModuleState(combined, IServer.STATE_UNKNOWN);
			
			// Javadoc says this should not be null, but Server.java clearly returns null in some cases
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=411812
			IModule[] children2 =  getServer().getChildModules(combined, new NullProgressMonitor());
			setModulesStopped(combined, children2);
		}
	}

	/**
	 * Simply append the child module to the end of the list of modules
	 * @param module
	 * @param newMod
	 * @return
	 */
	private IModule[] combine(IModule[] module, IModule newMod) {
		IModule[] retval = new IModule[module.length + 1];
		for( int i = 0; i < module.length; i++ )
			retval[i]=module[i];
		retval[retval.length-1] = newMod;
		return retval;
	}	

	
	
	@Override
	public void stop(boolean force) {
		try {
			IServerShutdownController con = getShutdownController();
			if( con != null ) {
				con.stop(force);
			} else {
				// TODO LOG ERROR
			}
		} catch(CoreException ce) {
			// TODO log
		}
	}
	
	@Override
	public IStatus canStart(String launchMode) {
		try {
			ILaunchServerController con = getLaunchController();
			if( con != null ) {
				return con.canStart(launchMode);
			} else {
				return Status.CANCEL_STATUS;
			}
		} catch(CoreException ce) {
			return ce.getStatus();
		}
	}
	
	@Override
	public IStatus canStop() {
		try {
			IServerShutdownController con = getShutdownController();
			if( con != null ) {
				return con.canStop();
			} else {
				// TODO LOG ERROR, return an error status
				return Status.CANCEL_STATUS;
			}
		} catch(CoreException ce) {
			return ce.getStatus();
		}
	}

	public void setRunMode(String mode) {
		setMode(mode);
	}

}
