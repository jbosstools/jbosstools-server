/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IUserPrompter;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.AbstractStartJavaServerLaunchDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.ServerHotCodeReplaceListener;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.jmx.core.IConnectionFacade;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

/**
 * This is a launch configuration delegate for use with local jboss servers. 
 * It will launch the configuration, update server state, and 
 * register a process termination listener. 
 * 
 * We also kick off the polling mechanism from here as part of the launch
 */
public class StandardLocalJBossStartLaunchDelegate extends
	AbstractStartJavaServerLaunchDelegate {
	

	protected void logStatus(IServer server, IStatus stat) {
		ServerLogger.getDefault().log(server, stat);
	}
	
	protected void initiatePolling(IServer server) {
		// Initiate Polling!
		PollThreadUtils.pollServer(server, IServerStatePoller.SERVER_UP);
	}
	/*
	 * A solution needs to be found here. 
	 * Should ideally use the poller that the server says is its poller,
	 * but some pollers such as timeout poller 
	 */
	protected IStatus isServerStarted(IServer server) {
		return PollThreadUtils.isServerStarted(server);
	}
	
	protected void validateServerStructure(IServer server) throws CoreException {
		IControllableServerBehavior jbsBehavior = JBossServerBehaviorUtils.getControllableBehavior(server);

		Trace.trace(Trace.STRING_FINEST, "Verifying server structure"); //$NON-NLS-1$
		JBossExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getJBossExtendedProperties(server);
		IStatus status = props.verifyServerStructure();
		if( !status.isOK() ) {
			((ControllableServerBehavior)jbsBehavior).setServerStopped();
			throw new CoreException(status);
		}
		
		Trace.trace(Trace.STRING_FINEST, "Verifying jdk is available if server requires jdk"); //$NON-NLS-1$
		boolean requiresJDK = props.requiresJDK();
		if( requiresJDK) {
			IRuntime rt = server.getRuntime();
			IJBossServerRuntime rt2 = RuntimeUtils.getJBossServerRuntime(rt);
			IVMInstall vm = rt2.getVM();
			
			if( !JavaUtils.isJDK(vm)) {
				// JBIDE-14568 do not BLOCK launch, but log error
				Trace.trace(Trace.STRING_FINEST, "The VM to launch server '" +  //$NON-NLS-1$
						server.getName() + "' does not appear to be a JDK: " + vm.getInstallLocation().getAbsolutePath()); //$NON-NLS-1$
				IStatus stat = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						NLS.bind(Messages.launch_requiresJDK, 
								server.getName(),
								vm.getInstallLocation().getAbsolutePath()));
				logStatus(server, stat);
			}
		}
	}

	@Override
	protected void cancelPolling(IServer server) {
		IControllableServerBehavior jbsBehavior = JBossServerBehaviorUtils.getControllableBehavior(server);
		Object pt = ((ControllableServerBehavior)jbsBehavior).getSharedData(IDeployableServerBehaviorProperties.POLL_THREAD);
		if( pt != null ) {
			PollThreadUtils.cancelPolling(null, (PollThread)pt);
		}
	}
	
	protected IUserPrompter getPrompter() {
		return JBossServerCorePlugin.getDefault().getPrompter();
	}

	protected boolean addCustomHotcodeReplaceLogic(IServer server) {
		return server.getAttribute(ServerHotCodeReplaceListener.PROPERTY_HOTCODE_REPLACE_OVERRIDE, true);
	}
	
	@Override
	protected IJavaHotCodeReplaceListener getHotCodeReplaceListener(final IServer server, ILaunch launch) {
		if( addCustomHotcodeReplaceLogic(server)) {
			return new ServerHotCodeReplaceListener(server, launch) {
				protected void postPublish(IJavaDebugTarget target, IModule[] modules) {
					waitModulesStarted(modules);
					removeBreakpoints(target);
					executeJMXGarbageCollection(server, modules);
					addBreakpoints(target);
				}
			};
		}
		return null;
	}
	
	protected void addBreakpoints(IJavaDebugTarget target) {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			target.breakpointAdded(breakpoints[i]);
		}
	}

	protected void removeBreakpoints(IJavaDebugTarget target) {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			target.breakpointRemoved(breakpoints[i], null);
		}
	}

	protected void executeJMXGarbageCollection(IServer server, IModule[] modules) {
		IJBossServer jbs = (IJBossServer) server.loadAdapter(IJBossServer.class, null);
		if (jbs instanceof IConnectionFacade) {
			IConnectionWrapper wrap = ((IConnectionFacade) jbs).getJMXConnection();
			try {
				wrap.run(new IJMXRunnable() {
					public void run(MBeanServerConnection connection) throws Exception {
						final MemoryMXBean memoryBean = ManagementFactory.newPlatformMXBeanProxy(connection,
								ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
						memoryBean.gc();
						memoryBean.gc();
					}
				});
			} catch (JMXException e) {
				JBossServerCorePlugin.log(
						StatusFactory.errorStatus(JBossServerCorePlugin.PLUGIN_ID, 
						"Error executing garbage collection on server after publish", e)); //$NON-NLS-1$
			}
		}
	}

}
