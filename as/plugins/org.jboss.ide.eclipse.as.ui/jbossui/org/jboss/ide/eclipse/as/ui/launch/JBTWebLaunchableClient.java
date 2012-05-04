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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.model.ClientDelegate;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.JBossLaunchAdapter.JBTCustomHttpLaunchable;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class JBTWebLaunchableClient extends ClientDelegate {

	public JBTWebLaunchableClient() {
	}	
	
	public boolean supports(IServer server, Object launchable, String launchMode) {
		return (launchable instanceof JBTCustomHttpLaunchable);
	}

	public IStatus launch(final IServer server, final Object launchable, final String launchMode, final ILaunch launch) {
		if( server.getServerState() == IServer.STATE_STOPPED || server.getServerState() == IServer.STATE_STOPPING)
			return Status.CANCEL_STATUS;
		
		new Thread() {
			public void run() {
				launch2(server, launchable, launchMode, launch);
			}
		}.start();
		return null;  // intentional null return
	}
	
	public IStatus launch2(IServer server, Object launchable, String launchMode, ILaunch launch) {
		final JBTCustomHttpLaunchable http = (JBTCustomHttpLaunchable) launchable;
		wait(server, http.getModuleTree(server));
		
		if(server.getServerState() == server.STATE_STARTED) {
		Display.getDefault().asyncExec(new Runnable(){
			public void run() {
				openBrowser(http.getURL());
			}
		});
		} else {
			JBossServerUIPlugin.getDefault().getLog().log(
					new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, 
							"Server stopped before before browser could be opened.", null));
		}
		return null;
	}
	
	protected void wait(final IServer server, final IModule[] module) {
		// Wait for the server to be started. No remote polling necessary. 
		// Framework poller is handling that. Here just wait in the state event framework
		waitServerStarted(server);
		
		// Now check if we are able to poll the server on module state or not
		ServerExtendedProperties props = (ServerExtendedProperties)server
				.loadAdapter(ServerExtendedProperties.class, new NullProgressMonitor());
		if( props != null && props.canVerifyRemoteModuleState()) {
			IServerModuleStateVerifier verifier = props.getModuleStateVerifier();
			if( verifier != null ) {
				// we can verify the remote state, so go do it, so go wait for the module to be deployed
				verifier.waitModuleStarted(server, module, 20000);
			}
		}
	}
	
	private void waitServerStarted(final IServer server) {
		final Object lock = new Object();
		IServerListener listener = new IServerListener() {
			public void serverChanged(ServerEvent event) {
				synchronized(lock) {
					if( server.getServerState() != IServer.STATE_STARTING ) {
						lock.notifyAll();
					}
				}
			}
		};
		synchronized(lock) {
			server.addServerListener(listener);
			if( server.getServerState() == IServer.STATE_STARTING) {
				try {
					lock.wait();
				} catch(InterruptedException ie) {
					// ignore
				}
			}
		}
		server.removeServerListener(listener);
	}
	
	/*
	 * Stolen from BrowserUtil
	 */
	private static final String BROWSER_COULD_NOT_OPEN_BROWSER = "Unable to open web browser"; //$NON-NLS-1$
	public static void checkedCreateInternalBrowser(String url, String browserId, String pluginId, ILog log) {
		try {
			openUrl(url, PlatformUI.getWorkbench().getBrowserSupport().createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, browserId, null, null), pluginId, log);
		} catch (PartInitException e) {
			IStatus errorStatus = createErrorStatus(pluginId, BROWSER_COULD_NOT_OPEN_BROWSER, e, url);
			log.log(errorStatus);
		}
	}

	private static IStatus createErrorStatus(String pluginId, String message, Throwable e,
			Object... messageArguments) {
		String formattedMessage = null;
		if (message != null) {
			formattedMessage = MessageFormat.format(message, messageArguments);
		}
		return new Status(Status.ERROR, pluginId, Status.ERROR, formattedMessage, e);
	}

	public static void checkedCreateExternalBrowser(String url, String pluginId, ILog log) {
		try {
			openUrl(url, PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser(), pluginId, log);
		} catch (PartInitException e) {
			IStatus errorStatus = createErrorStatus(pluginId, BROWSER_COULD_NOT_OPEN_BROWSER, e, url);
			log.log(errorStatus);
		}
	}

	public static void openUrl(String url, IWebBrowser browser, String pluginId, ILog log) {
		try {
			browser.openURL(new URL(url));
		} catch (PartInitException e) {
			IStatus errorStatus = createErrorStatus(pluginId, BROWSER_COULD_NOT_OPEN_BROWSER, e, url);
			log.log(errorStatus);
		} catch (MalformedURLException e) {
			IStatus errorStatus = createErrorStatus(pluginId, BROWSER_COULD_NOT_OPEN_BROWSER, e,
					url);
			log.log(errorStatus);
		}
	}

	public static void openBrowser(URL url) {
		try {
			IWorkbenchBrowserSupport browserSupport = JBossServerUIPlugin.getDefault().getWorkbench().getBrowserSupport();
			IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
			browser.openURL(url);
		} catch (Exception e) {
			JBossServerUIPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, "Unable to open web browser", e)); //$NON-NLS-1$
		}
	}
	
}
