/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;

/**
 * 
 * @deprecated
 */
public class DelegatingJBoss7ServerBehavior extends DelegatingServerBehavior {

	public static final String MARK_DO_DEPLOY = "org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher.markUndeploy"; //$NON-NLS-1$

	private IProcess serverProcess;
	private IDebugEventSetListener serverProcessListener;
	private PollThread pollThread;

	public void setProcess(IProcess process) {
		this.serverProcess = process;
		initDebugListener(process);
	}

	private void initDebugListener(IProcess process) {
		DebugPlugin.getDefault().addDebugEventListener(serverProcessListener = new JBossServerLifecycleListener());
	}

	private void disposeServerProcessListener() {
		if( serverProcessListener != null ) {
			DebugPlugin.getDefault().removeDebugEventListener(serverProcessListener);
			serverProcess = null;
			if( pollThread != null )
				pollThread.cancel();
		}
	}

	@Override
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		super.publishModule(kind, deltaKind, module, monitor);
	}

	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "PublishFinish in DelegatingJBoss7ServerBehavior"); //$NON-NLS-1$
		// Handle the dodeploy
		try {
			createDoDeployMarkers(monitor);
		} finally {
			super.publishFinish(new SubProgressMonitor(monitor, 1));
		}
	}

	private void createDoDeployMarkers(IProgressMonitor monitor) throws CoreException {
		if (!hasMarkedDoDeploy()) {
			return;
		}
		List<IPath> paths = getMarkedDoDeploy();
		Trace.trace(Trace.STRING_FINER, "Marking " + paths.size() + " dodeploy files.");  //$NON-NLS-1$//$NON-NLS-2$
		monitor.beginTask("Completing Publishes", paths.size() + 1); //$NON-NLS-1$
		createDoDeployMarker(getOrCreatePublishMethod(), paths, monitor);
	}
	
	@Override
	public void stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException  {
		changeModuleStateTo(module, IServer.STATE_STOPPED, monitor);	
	}
	
	@Override
	public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		changeModuleStateTo(module, IServer.STATE_STARTED, monitor);
	}
	
	public void changeModuleStateTo(IModule[] module, int state, IProgressMonitor monitor) throws CoreException {
		AS7ManagementDetails details = new AS7ManagementDetails(getServer());
		IJBossServer jbs = ServerConverter.getJBossServer(getServer());
		String deploymentName = jbs.getDeploymentLocation(module, true).lastSegment();
		int preState = getServer().getModuleState(module);
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
			setModuleState(module, state);
		} catch(JBoss7ManangerException j7me) {
			setModuleState(module,preState);
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, j7me.getMessage(),j7me));
		}
	}

	@Override
	public void restartModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		stopModule(module, monitor);
		startModule(module, monitor);
	}

	@Override
	public void stop(boolean force) {
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(getServer())) {
			super.setServerStopped();
			return;
		}
		super.stop(force);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void setServerStopped() {
		disposeServerProcessListener();
		logServerStopped();
		super.setServerStopped();
	}

	private void logServerStopped() {
		IStatus status = new Status(IStatus.INFO,
				JBossServerCorePlugin.PLUGIN_ID, IEventCodes.BEHAVIOR_PROCESS_TERMINATED,
				Messages.TERMINATED, null);
		ServerLogger.getDefault().log(getServer(), status);
	}

	private class JBossServerLifecycleListener implements IDebugEventSetListener {

		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent event : events) {
				if (serverProcess != null && serverProcess.equals(event.getSource())
						&& event.getKind() == DebugEvent.TERMINATE) {
					setServerStopped();
					break;
				}
			}
		}
	}
	
	public void markDoDeploy(IPath path) {
		Object o = getPublishData(MARK_DO_DEPLOY);
		if(!(o instanceof List<?>)) {
			o = new ArrayList<IPath>();
			setPublishData(MARK_DO_DEPLOY, o);
		}
		List<IPath> list = (List<IPath>)o;
		if( !list.contains(path)) {
			list.add(path);
		}
		
	}

	public boolean hasMarkedDoDeploy() {
		return getMarkedDoDeploy().size() > 0;
	}
	
	private List<IPath> getMarkedDoDeploy() {
		Object o = getPublishData(MARK_DO_DEPLOY);
		if (!(o instanceof List<?>)) {
			return Collections.emptyList();
		}
		return (List<IPath>) o;
	}
	
	private void createDoDeployMarker(IJBossServerPublishMethod method, List<IPath> paths, IProgressMonitor monitor) throws CoreException {
		if( method == null )
			method = createPublishMethod();
		DeploymentMarkerUtils.addDoDeployMarker(method, getServer(), paths, monitor);
	}
}
