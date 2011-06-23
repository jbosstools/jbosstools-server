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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class JBoss7ServerBehavior extends JBossServerBehavior {

	public static final String MARK_DO_DEPLOY = "org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7JSTPublisher.markUndeploy"; //$NON-NLS-1$

	private IProcess serverProcess;
	private IJBoss7ManagerService service;
	private IDebugEventSetListener serverProcessListener;
	private PollThread pollThread;

	public static final String DEFAULT_CP_PROVIDER_ID = "org.jboss.ide.eclipse.as.core.server.internal.launch.serverClasspathProvider"; //$NON-NLS-1$

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

	private JBoss7Server getJBoss7Server() {
		return (JBoss7Server)getServer().loadAdapter(JBoss7Server.class, new NullProgressMonitor());
	}
	
	public boolean shouldSuspendScanner() {
		return false;
	}

	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfig, IProgressMonitor monitor)
			throws CoreException {
		IServer server = getServer();
		IRuntime runtime = server.getRuntime();
		IJBossServerRuntime serverRuntime = (IJBossServerRuntime) runtime.loadAdapter(IJBossServerRuntime.class, null);
		new JBoss7RuntimeLaunchConfigurator(launchConfig).apply(server, runtime, serverRuntime);
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
/* fix for JBIDE-
*		if (method == null)
*			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
*		try {
*			int result = method.publishModule(this, kind, deltaKind, module, monitor);
*			setModulePublishState(module, result);
*		} catch(CoreException e) {
*			setModulePublishState(module, IServer.PUBLISH_STATE_FULL);
*			throw e;
*		}
*/
		if (method == null)
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
		int result = method.publishModule(this, kind, deltaKind, module, monitor);
		setModulePublishState(module, result);
	}

	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		if (method == null)
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
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

	public void stop(boolean force) {
		String tmp = getServer().getAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, (String)null);
		Boolean b = tmp == null ? new Boolean(false) : new Boolean(tmp);
		if( b.booleanValue()) {
			super.setServerStopped();
			return;
		}
		try {
			if (force) {
				terminateProcess();
			} else {
				serverStopping();
				// TODO: for now only local, implement for remote afterwards
// disabled because of JBIDE-9173
				try {
					if (isServerRunning(getServer().getHost(), getJBoss7Server().getManagementPort())) {
						// The service and Poller will make sure the server is down
						getService().stop(getServer().getHost(), getJBoss7Server().getManagementPort()); 
						return;
					} else {
						terminateProcess();
					}
				} catch(Exception e) {
					terminateProcess();
				}
//				terminateProcess();
			}
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, MessageFormat.format(Messages.JBoss7ServerBehavior_could_not_stop, getServer().getName()), e);
			JBossServerCorePlugin.getDefault().getLog().log(status);
		}
		setServerStopped();
	}

	private void terminateProcess() throws DebugException {
		if( serverProcess != null && !serverProcess.isTerminated()) {
			serverProcess.terminate();
		}
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

	private void createDoDeployMarker(List<IPath> paths, IProgressMonitor monitor) throws CoreException {
		DeploymentMarkerUtils.addDoDeployMarker(method, getServer(), paths, monitor);
	}
	
}
