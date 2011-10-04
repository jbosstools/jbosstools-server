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
package org.jboss.ide.eclipse.as.core;

import java.util.Hashtable;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.core.server.internal.ServerListener;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class JBossServerCorePlugin extends Plugin  {
	//The shared instance.
	private static JBossServerCorePlugin plugin;
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.core"; //$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public JBossServerCorePlugin() {
		super();
		plugin = this;
	}
	public static JBossServerCorePlugin getInstance() {
		return plugin;
	}

	public IExtension[] getExtensions (String extensionPoint) {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg.getExtensionPoint(extensionPoint);
		IExtension[] extensions = ep.getExtensions();
		
		return extensions;
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// Start the array of models that need to be started
		UnitedServerListenerManager.getDefault();
		UnitedServerListenerManager.getDefault().addListener(XPathModel.getDefault());
		UnitedServerListenerManager.getDefault().addListener(ServerListener.getDefault());
		// It's unsafe to use FacetedProjectFramework in start method in the same thread. If may cause a deadlock. See https://issues.jboss.org/browse/JBIDE-9802
		Job job = new Job("Adding JBoss4xEarFacetInstallListener") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				FacetedProjectFramework.addListener(JBoss4xEarFacetInstallListener.getDefault(), IFacetedProjectEvent.Type.POST_INSTALL);
				return Status.OK_STATUS;
			}
		};
		job.schedule();

		// register the debug options listener
		final Hashtable<String, String> props = new Hashtable<String, String>(4);
		props.put(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
		context.registerService(DebugOptionsListener.class.getName(), new Trace(), props);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		UnitedServerListenerManager.getDefault().removeListener(ServerListener.getDefault());
		UnitedServerListenerManager.getDefault().removeListener(XPathModel.getDefault());
		FacetedProjectFramework.removeListener(JBoss4xEarFacetInstallListener.getDefault());
	}

	/**
	 * Returns the shared instance.
	 */
	public static JBossServerCorePlugin getDefault() {
		return plugin;
	}
	
	public static BundleContext getContext() {
		return plugin.getBundle().getBundleContext();
	}

	public static IPath getServerStateLocation(IServer server) {
		return ServerUtil.getServerStateLocation(server);
	}

	public static IPath getServerStateLocation(String serverID) {
		return ServerUtil.getServerStateLocation(serverID);
	}
	
	public static IPath getGlobalSettingsLocation() {
		return JBossServerCorePlugin.getDefault().getStateLocation().append(".global"); //$NON-NLS-1$
	}
	
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void log(int severity, String message, Throwable e) {
        log(new Status(severity, PLUGIN_ID, 0, message, e));
    }
    

    public static void log(Throwable e) {
    	log(e.getMessage(), e);
    }

    public static void log(String message, Throwable e) {
        log(IStatus.ERROR, message, e);
    }

}
