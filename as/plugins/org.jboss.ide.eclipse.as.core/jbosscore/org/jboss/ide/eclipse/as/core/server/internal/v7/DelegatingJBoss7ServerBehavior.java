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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.JSTPublisherXMLToucher;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

public class DelegatingJBoss7ServerBehavior extends DelegatingServerBehavior {

	public static final String MARK_DO_DEPLOY = "org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher.markUndeploy"; //$NON-NLS-1$

	private IProcess serverProcess;
	private IJBoss7ManagerService service;
	private IDebugEventSetListener serverProcessListener;
	private PollThread pollThread;

	private static HashMap<String, Class> delegateClassMap;
	static {
		delegateClassMap = new HashMap<String, Class>();
		delegateClassMap.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, LocalJBoss7BehaviorDelegate.class);
	}

	public static void addDelegateMapping(String s, Class c) {
		delegateClassMap.put(s, c);
	}

	protected HashMap<String, Class> getDelegateMap() {
		return delegateClassMap;
	}
	
	@Override
	public boolean shouldSuspendScanner() {
		return false;
	}

	@Override
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfig, IProgressMonitor monitor)
			throws CoreException {
		// TODO: implement setup for RSE launch delegate too
		new LocalJBoss7StartupConfigurator(getServer()).configure(launchConfig);
	}

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
		// Handle the dodeploy
		createDoDeployMarkers(monitor);
		super.publishFinish(new SubProgressMonitor(monitor, 1));
	}

	private void createDoDeployMarkers(IProgressMonitor monitor) throws CoreException {
		if (!hasMarkedDoDeploy()) {
			return;
		}
		List<IPath> paths = getMarkedDoDeploy();
		monitor.beginTask("Completing Publishes", paths.size() + 1); //$NON-NLS-1$
		createDoDeployMarker(paths, monitor);
	}
	
	@Override
	public void restartModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		IDeployableServer ds = ServerConverter.getDeployableServer(getServer());
		if( ds == null ) 
			return;

		IJBossServerPublishMethod method = getOrCreatePublishMethod();
		IPath depPath = PublishUtil.getDeployPath(method, module, ds);
		createDoDeployMarker(new IPath[]{depPath}, monitor);
	}


	protected IJBoss7ManagerService getService() throws Exception {
		if (service == null) {
			service = JBoss7ManagerUtil.getService(getServer());
		}
		return service;
	}

	private boolean isServerRunning(String host, int port) throws Exception {
		try {
			return getService().getServerState(host, port) == JBoss7ServerState.RUNNING;
		} catch (JBoss7ManangerConnectException e) {
			return false;
		}
	}

	@Override
	public void stop(boolean force) {
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(getServer())) {
			super.setServerStopped();
			return;
		}
		try {
			if (force) {
				if( serverProcess != null )
					serverProcess.terminate();
			} else {
				setServerStopping();
				String host = getServer().getHost();
				JBoss7Server server = ServerConverter.checkedGetJBossServer(getServer(), JBoss7Server.class);
				int mgmtPort = server.getManagementPort();
				// TODO: for now only local, implement for remote afterwards
				if (isServerRunning(host, mgmtPort)) {
					// The service and Poller will make sure the server is down
					getService().stop(host, mgmtPort); 
					return;
				} else {
					if( serverProcess != null && !serverProcess.isTerminated()) {
						serverProcess.terminate();
					}
				}
			}
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, MessageFormat.format(Messages.JBoss7ServerBehavior_could_not_stop, getServer().getName()), e);
			JBossServerCorePlugin.getDefault().getLog().log(status);
		}
		setServerStopped();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (service != null) {
			service.dispose();
		}
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
	private void createDoDeployMarker(IPath[] paths, IProgressMonitor monitor) throws CoreException {
		List<IPath> allPaths = Arrays.asList(paths);
		createDoDeployMarker(allPaths, monitor);
	}
	private void createDoDeployMarker(List<IPath> paths, IProgressMonitor monitor) throws CoreException {
		DeploymentMarkerUtils.addDoDeployMarker(getOrCreatePublishMethod(), getServer(), paths, monitor);
	}
}
