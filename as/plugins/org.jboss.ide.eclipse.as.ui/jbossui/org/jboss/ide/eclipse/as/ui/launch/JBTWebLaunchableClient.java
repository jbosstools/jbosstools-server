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
package org.jboss.ide.eclipse.as.ui.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ClientDelegate;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.ExtensionManager.IServerJMXRunnable;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.internal.JBossLaunchAdapter.JBTCustomHttpLaunchable;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7Server;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.server.v7.management.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class JBTWebLaunchableClient extends ClientDelegate {

	public JBTWebLaunchableClient() {
		// TODO Auto-generated constructor stub
	}	
	
	public boolean supports(IServer server, Object launchable, String launchMode) {
		return (launchable instanceof JBTCustomHttpLaunchable);
	}

	protected boolean isJMXServer(IServer server) {
		JBossServer jbs = ServerConverter.getJBossServer(server);
		return jbs != null && server.getServerState() == IServer.STATE_STARTED
				&&  jbs.hasJMXProvider() && ExtensionManager.getDefault().getJMXRunner() != null;
	}

	public IStatus launch(final IServer server, final Object launchable, final String launchMode, final ILaunch launch) {
		new Thread() {
			public void run() {
				launch2(server, launchable, launchMode, launch);
			}
		}.start();
		return null;  // intentional null return
	}
	
	public IStatus launch2(IServer server, Object launchable, String launchMode, ILaunch launch) {
		final JBTCustomHttpLaunchable http = (JBTCustomHttpLaunchable) launchable;
		wait(server, http.getModule());
		Display.getDefault().asyncExec(new Runnable(){
			public void run() {
				try {
					IWorkbenchBrowserSupport browserSupport = JBossServerUIPlugin.getDefault().getWorkbench().getBrowserSupport();
					IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
					browser.openURL(http.getURL());
				} catch (Exception e) {
					JBossServerUIPlugin.getDefault().getLog().log(
							new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, "Unable to open web browser", e)); //$NON-NLS-1$
				}
			}
		});
		return null;
	}
	
	protected void wait(final IServer server, final IModule module) {
		if( isJMXServer(server)) {
			waitJMX(server, module);
		} else if( ServerUtil.isJBoss7(server)) {
			waitJBoss7(server, module);
		}
	}
	
	protected void waitJBoss7(final IServer server, final IModule module) {
		try {
			JBoss7Server jbossServer = ServerConverter.checkedGetJBossServer(server, JBoss7Server.class);
			IJBoss7ManagerService service = JBoss7ManagerUtil.getService(server);
			IPath deployPath = PublishUtil.getDeployPath(new IModule[]{module}, jbossServer);
			long time = new Date().getTime();
			long endTime = time + getMaxDelay();
			while( new Date().getTime() < endTime ) {
				JBoss7DeploymentState state = service.getDeploymentState(
						new AS7ManagementDetails(server),
						deployPath.lastSegment());
				boolean done = (state == JBoss7DeploymentState.STARTED);
				if( done ) {
					return;
				}
				try {
					Thread.sleep(2000);
				} catch(InterruptedException ie) {}
			}
		} catch (Exception e) {
			IStatus s = new Status(
					IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind("Could not acquire the management service for this JBoss installation", 
							server.getName()), e);
			JBossServerUIPlugin.log(s.getMessage(), e);
		}

	}
	
	protected void waitJMX(final IServer server, final IModule module) {
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
	
	protected void jmxWaitForDeploymentStarted(final IServer server, final IModule module,
			final MBeanServerConnection connection, IProgressMonitor monitor) throws Exception {
		monitor = monitor == null ? new NullProgressMonitor() : monitor;
		monitor.beginTask("Ensuring Deployments are Loaded", 10000); //$NON-NLS-1$
		long time = new Date().getTime();
		long endTime = time + getMaxDelay();
		while( new Date().getTime() < endTime ) {
			boolean done = checkDeploymentStarted(server, module, connection, monitor);
			if( done ) {
				monitor.done();
				return;
			}
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {}
		}
	}
	
	protected long getMaxDelay() {
		return 20000;
	}

	protected boolean checkDeploymentStarted(final IServer server, final IModule module,
			final MBeanServerConnection connection, IProgressMonitor monitor) throws Exception {
		String typeId;
		typeId = module.getModuleType().getId();
		
		if( typeId.equals("wst.web") || typeId.equals("jst.web")) { //$NON-NLS-1$ //$NON-NLS-2$
			String mbeanName = null;
			IModule earParent = findEarParent(server, module);
			String stateAttribute;
			Object result;
			if( earParent == null ) {
				mbeanName = "jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=WebModule,name=//localhost/" + module.getName(); //$NON-NLS-1$
				stateAttribute = "state"; //$NON-NLS-1$
				result = getAttributeResult(connection, mbeanName, stateAttribute);
				if(result == null || !(result instanceof Integer) || ((Integer)result).intValue() != 1 ) {
					return false;
				}
			} else {
				mbeanName = "jboss.deployment:id=\"jboss.web.deployment:war=/" + module.getName() + "\",type=Component";  //$NON-NLS-1$//$NON-NLS-2$
				stateAttribute = "State"; //$NON-NLS-1$
				result = getAttributeResult(connection, mbeanName, stateAttribute);
				if( result == null || !result.toString().equals("DEPLOYED"))  //$NON-NLS-1$
					return false;
			}
		}
		return true;
	}	
	
	private Object getAttributeResult(final MBeanServerConnection connection, String mbeanName, String stateAttribute) throws Exception {
		ObjectName on = new ObjectName(mbeanName);
		try {
			return connection.getAttribute(on, stateAttribute);
		} catch(InstanceNotFoundException infe) {
			return false;
		}
	}
	
	private IModule findEarParent(IServer server, IModule module) {
		try {
			IModule[] deployed = server.getModules();
			ArrayList<IModule> deployedAsList = new ArrayList<IModule>();
			deployedAsList.addAll(Arrays.asList(deployed));
			IModule[] possibleParents = server.getRootModules(module, new NullProgressMonitor());
			for( int i = 0; i < possibleParents.length; i++ ) {
				if( possibleParents[i].getModuleType().getId().equals("jst.ear") && deployedAsList.contains(possibleParents[i]))
					return possibleParents[i];
			}
		} catch(CoreException ce) {
		}
		return null;
	}
}
