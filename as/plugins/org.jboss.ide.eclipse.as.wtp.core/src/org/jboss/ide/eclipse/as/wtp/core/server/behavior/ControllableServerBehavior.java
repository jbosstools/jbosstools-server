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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

/**
 * @since 3.0 
 */
public class ControllableServerBehavior extends ServerBehaviourDelegate implements IControllableServerBehavior {
	public static final String PROPERTY_PREFIX = "SUBSYSTEM_OVERRIDE_";
	
	
	// A list of pre-defined subsystem categories
	public static final String SUBSYSTEM_PUBLISH = "publish";
	public static final String SUBSYSTEM_MODULES = "modules";
	public static final String SUBSYSTEM_LAUNCH = "launch";
	public static final String SUBSYSTEM_SHUTDOWN = "shutdown";
	
	// The below are not required for all server types
	public static final String SUBSYSTEM_SERVERSTATE = "serverState";
	public static final String SUBSYSTEM_FILESYSTEM = "fs";
	public static final String SUBSYSTEM_MANAGEMENT = "mgmt";
	
	
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
		if( SUBSYSTEM_PUBLISH.equals(system) )
			return getPublishController();
		if( SUBSYSTEM_MODULES.equals(system) )
			return getModuleStateController();
		if( SUBSYSTEM_SHUTDOWN.equals(system) )
			return getShutdownController();
		if( SUBSYSTEM_LAUNCH.equals(system) )
			return getLaunchController();
		return getController(system, null);
	}
	
	/**
	 * Find the default subsystem controller for the given system with 
	 * an environment to be passed to it
	 * 
	 * @param system
	 * @param env
	 * @return
	 * @throws CoreException
	 */
	protected ISubsystemController getController(String system, ControllerEnvironment env) throws CoreException {
		String propOverride = PROPERTY_PREFIX + system;
		String val = getServer().getAttribute(propOverride, (String)null);
		ISubsystemController ret = null;
		if( val != null ) {
			ret = SubsystemModel.getInstance().createControllerForSubsystem(
					getServer(), getServer().getServerType().getId(), system, val, env.getMap());
		} else {
			ret = SubsystemModel.getInstance().createSubsystemController(getServer(), system, env.getMap());
		}
		if( ret == null ) {
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 0, 
					"Unable to locate system " + system + " for server " + getServer().getName(), null));
		}
		return ret;
	}
	
	
	protected IPublishController getPublishController() throws CoreException {
		return (IPublishController)getController(SUBSYSTEM_PUBLISH, null);
	}
	
	protected IModuleStateController getModuleStateController() throws CoreException {
		return (IModuleStateController)getController(SUBSYSTEM_MODULES, null);
	}

	protected IServerShutdownController getShutdownController() throws CoreException {
		return (IServerShutdownController)getController(SUBSYSTEM_SHUTDOWN, null);
	}
	
	protected ILaunchServerController getLaunchController() throws CoreException {
		return (ILaunchServerController)getController(SUBSYSTEM_LAUNCH, null);
	}
	
	
	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		getPublishController().publishFinish(this, monitor);
	}
	@Override
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		getPublishController().publishModule(this, kind, deltaKind, module, monitor);
	}

	@Override
	protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
		getPublishController().publishServer(kind, monitor);
	}
	
	@Override
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		getPublishController().publishStart(this, monitor);
	}

	

	@Override
	public boolean canRestartModule(IModule[] module) {
		try {
			return getModuleStateController().canRestartModule(module);
		} catch(CoreException ce) {
			return false;
		}
	}
	
	@Override
	public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		getModuleStateController().startModule(module, monitor);
	}

	@Override
	public void stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		getModuleStateController().stopModule(module, monitor);
	}
	
	@Override
	public void restartModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		getModuleStateController().restartModule(module, monitor);
	}

	
	// TODO launch polling
	public void setServerStarting() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STARTING);
		}
		pollServer(IServerStatePoller.SERVER_UP);
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
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
	
	public void setServerStopped() {
		synchronized(serverStateLock) {
			setServerState(IServer.STATE_STOPPED);
		}
		IModule[] mods = getServer().getModules();
		setModulesStopped(new IModule[]{}, mods);
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
			getShutdownController().stop(force);
		} catch(CoreException ce) {
			// TODO log
		}
	}
	
	@Override
	public IStatus canStop() {
		try {
			return getShutdownController().canStop();
		} catch( CoreException ce) {
			return ce.getStatus();
		}
	}
	
	
	// TODO search via subsystems
	protected void pollServer(final boolean expectedState) {
		// IF shutting down a process started OUTSIDE of eclipse, force use the web poller, 
		// since there's no process watch for shutdowns
//		if( expectedState == IServerStatePoller.SERVER_DOWN && !isProcessRunning() ) {
//			IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, getServer());
//			// Only override the poller if it is currently set to the process terminated poller
//			if( poller.getPollerType().getId().equals(ProcessTerminatedPoller.POLLER_ID))
//				poller = PollThreadUtils.getPoller(WebPortPoller.WEB_POLLER_ID);
//			pollServer(expectedState, poller);
//		} else {
//			IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, getServer());
//			pollServer(expectedState, poller);
//		}
	}

}
