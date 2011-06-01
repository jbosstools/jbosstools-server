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
import java.util.HashMap;
import java.util.Iterator;
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
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBoss7ServerBehavior extends JBossServerBehavior {

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

	protected void pollServer(final boolean expectedState) {
		if( pollThread != null ) {
			pollThread.cancel();
		}
		//IServerStatePoller poller = PollThreadUtils.getPoller(JBoss7ManagerServicePoller.POLLER_ID, expectedState, getServer());
		IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, getServer());
		this.pollThread = new PollThread(expectedState, poller, this);
		pollThread.start();
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
		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(getServer());
		Object o = beh.getPublishData(JBoss7JSTPublisher.MARK_DO_DEPLOY);
		if (o != null && (o instanceof ArrayList<?>)) {
			List<IPath> l = (List<IPath>) o;
			int size = l.size();
			monitor.beginTask("Completing Publishes", size + 1); //$NON-NLS-1$
			Iterator<IPath> i = l.iterator();
			IPath p;
			while (i.hasNext()) {
				DeploymentMarkerUtils.addDoDeployMarker(method, getServer(), i.next(), new SubProgressMonitor(monitor,
						1));
			}
			super.publishFinish(new SubProgressMonitor(monitor, 1));
		} else
			super.publishFinish(monitor);
	}

	protected IJBoss7ManagerService getService() throws Exception {
		if (service == null) {
			service = JBoss7ManagerUtil.getService(getServer());
		}
		return service;
	}

	private boolean isServerRunning(String host) throws Exception {
		try {
			return getService().getServerState(host) == JBoss7ServerState.RUNNING;
		} catch (JBoss7ManangerConnectException e) {
			return false;
		}
	}

	public void stop(boolean force) {
		
		try {
			if (force) {
				if( serverProcess != null )
					serverProcess.terminate();
			} else {
				serverStopping();
				// TODO: for now only local, implement for remote afterwards
				if (isServerRunning(getServer().getHost())) {
					// The service and Poller will make sure the server is down
					getService().stop(getServer().getHost()); 
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
}
