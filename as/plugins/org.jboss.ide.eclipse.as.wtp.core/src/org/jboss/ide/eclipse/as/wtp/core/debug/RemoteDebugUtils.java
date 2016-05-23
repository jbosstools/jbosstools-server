/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.debug;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;

public class RemoteDebugUtils {
	public static final String ATTACH_DEBUGGER = "org.jboss.ide.eclipse.as.core.server.launch.DebugLaunchConstants.ATTACH_DEBUGGER"; //$NON-NLS-1$
	public static final String DEBUG_PORT = "org.jboss.ide.eclipse.as.core.server.launch.DebugLaunchConstants.DEBUG_PORT"; //$NON-NLS-1$
	public static final int DEFAULT_DEBUG_PORT = 8787;

	private ILaunchManager launchManager;
	public static RemoteDebugUtils get() {
		return get(DebugPlugin.getDefault().getLaunchManager());
	}
	
	/** For testing purposes **/
	public static RemoteDebugUtils get(ILaunchManager launchManager) {
		return new RemoteDebugUtils(launchManager);
	}
	
	private RemoteDebugUtils(ILaunchManager launchManager) {
		this.launchManager = launchManager;
	}
	
	public ILaunchConfiguration getRemoteDebuggerLaunchConfiguration(IServer server) throws CoreException {
		ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType(ID_REMOTE_JAVA_APPLICATION);
		ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations(launchConfigurationType);
		String name = getRemoteDebuggerLaunchConfigurationName(server);
		Stream.of(launchConfigs);
		Optional<ILaunchConfiguration> maybeLaunch = 
				Stream.of(launchConfigs)
				.filter(lc -> name.equals(lc.getName()))
				.findFirst();
		return maybeLaunch.orElse(null);
	}
	
	public ILaunchConfigurationWorkingCopy createRemoteDebuggerLaunchConfiguration(IServer server) throws CoreException {
		String name = getRemoteDebuggerLaunchConfigurationName(server);
		ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType(ID_REMOTE_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null, name);
		return workingCopy;
	}
	
	public void setupRemoteDebuggerLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProject project, int debugPort, String host) throws CoreException {
		String portString = String.valueOf(debugPort);
	    workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
		Map<String, String> connectMap = new HashMap<>(2);
		connectMap.put("port", portString); //$NON-NLS-1$
		connectMap.put("hostname", host); //$NON-NLS-1$ //$NON-NLS-2$
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, connectMap);
		if(project != null) { 
		   workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		}
	}
	
	public boolean isRunning(ILaunchConfiguration launchConfiguration, int localDebugPort) {
		boolean isRunning = getLaunches()
				.filter(l -> !l.isTerminated() && launchMatches(l, launchConfiguration, localDebugPort))
				.findFirst().isPresent();
		return isRunning;
	}
	
	
	private boolean launchMatches(ILaunch l, ILaunchConfiguration launchConfiguration, int localDebugPort) {
		return Objects.equals(l.getLaunchConfiguration(), launchConfiguration);
	}

	public static String getRemoteDebuggerLaunchConfigurationName(IServer server) {
		String name ="Remote debugger to "+server.getName();
		return name;
	}
	
	public void terminateRemoteDebugger(IServer server) throws CoreException {
		ILaunchConfiguration launchConfig = getRemoteDebuggerLaunchConfiguration(server);
		if (launchConfig == null) {
			return;
		}
		List<IStatus> errors = new ArrayList<>();
		getLaunches().filter(l -> launchConfig.equals(l.getLaunchConfiguration()))
					 .filter(l -> l.canTerminate())
					 .forEach(l -> terminate(l, errors));
		
		if (!errors.isEmpty()) {
			MultiStatus status = new MultiStatus(ASWTPToolsPlugin.PLUGIN_ID, IStatus.ERROR, errors.toArray(new IStatus[errors.size()]), "Failed to terminate remote launch configuration", null);
			throw new CoreException(status);
		}
	}
	
	private void terminate(ILaunch launch, Collection<IStatus> errors) {
		try {
			launch.terminate();
		} catch (DebugException e) {
			errors.add(e.getStatus());
		}
	}

	private Stream<ILaunch> getLaunches() {
		return Stream.of(launchManager.getLaunches());
	}
	

	public IServerListener createAttachDebuggerListener() {
		return new IServerListener() {
			public void serverChanged(ServerEvent event) {
				if( UnitedServerListener.serverSwitchesToState(event, IServer.STATE_STARTED)) {
					event.getServer().removeServerListener(this);
					IServer s = event.getServer();
					int debugPort = getDebugPort(s);
					try {
						attachRemoteDebugger(event.getServer(), debugPort, new NullProgressMonitor());
					} catch(CoreException ce) {
						ASWTPToolsPlugin.pluginLog().logError(ce);
					}
				} else if( UnitedServerListener.serverSwitchesToState(event, IServer.STATE_STOPPED)) {
					event.getServer().removeServerListener(this);
				}
			}
		};
	}
	
	protected int getDebugPort(IServer server) {
		String debugPort = server.getAttribute(DEBUG_PORT, Integer.toString(DEFAULT_DEBUG_PORT));
		int port = -1;
		try {
			port = Integer.parseInt(debugPort);
		} catch(NumberFormatException nfe) {
			port = DEFAULT_DEBUG_PORT;
		}
		if( port < 1 )
			port = DEFAULT_DEBUG_PORT;
		return port;
	}

	public ILaunch attachRemoteDebugger(IServer server, IProgressMonitor monitor) 
			throws CoreException {
		return attachRemoteDebugger(server, getDebugPort(server), monitor);
	}

	public ILaunch attachRemoteDebugger(IServer server, int localDebugPort, IProgressMonitor monitor) 
			throws CoreException {
		monitor.subTask("Attaching remote debugger");
		ILaunch ret = null;
		RemoteDebugUtils debugUtils = RemoteDebugUtils.get();
		ILaunchConfiguration debuggerLaunchConfig = debugUtils.getRemoteDebuggerLaunchConfiguration(server);
		ILaunchConfigurationWorkingCopy workingCopy;
		if (debuggerLaunchConfig == null) {
			workingCopy = debugUtils.createRemoteDebuggerLaunchConfiguration(server);
		} else {
			if (debugUtils.isRunning(debuggerLaunchConfig, localDebugPort)) {
				return null;
			}
			workingCopy = debuggerLaunchConfig.getWorkingCopy();
		}
				
		debugUtils.setupRemoteDebuggerLaunchConfiguration(workingCopy, null, localDebugPort, server.getHost());
		debuggerLaunchConfig = workingCopy.doSave();
		boolean launched = false;
		try {
			ret = debuggerLaunchConfig.launch("debug", new NullProgressMonitor());
			launched = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!launched){
			throw toCoreException("Unable to start a remote debugger to localhost:"+localDebugPort);
		}
		
	    monitor.worked(10);
	    return ret;
	}

	private CoreException toCoreException(String msg, Exception e) {
		return new CoreException(StatusFactory.errorStatus(ASWTPToolsPlugin.PLUGIN_ID, msg, e));
	}
	
	private CoreException toCoreException(String msg) {
		return toCoreException(msg, null);
	}
}
