/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.tools.foundation.core.plugin.BaseCorePlugin;
import org.jboss.tools.foundation.core.plugin.log.IPluginLog;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ASWTPToolsPlugin extends BaseCorePlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.wtp.core"; //$NON-NLS-1$
	
	// Usage detection
	public static final String USAGE_COMPONENT_NAME = "server"; //$NON-NLS-1$

	// The shared instance
	private static ASWTPToolsPlugin plugin;

	private UsageEventType newServerEventType;
	
	
	/**
	 * The constructor
	 */
	public ASWTPToolsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		super.registerDebugOptionsListener(PLUGIN_ID, new Trace(this), context);
		
		new Job("Registering Listeners"){
			protected IStatus run(IProgressMonitor monitor) {
				newServerEventType = new UsageEventType(USAGE_COMPONENT_NAME, UsageEventType.getVersion(ASWTPToolsPlugin.this), 
						null, UsageEventType.NEW_ACTION, Messages.UsageEventTypeServerIDLabelDescription, 
						UsageEventType.SUCCESFULL_FAILED_VALUE_DESCRIPTION);
				
				UsageReporter.getInstance().registerEvent(newServerEventType);

				UnitedServerListenerManager.getDefault().addListener(new UnitedServerListener() {
					public void serverAdded(IServer server) {
						UsageReporter.getInstance().trackEvent(newServerEventType.event(server.getServerType().getId()));
					}
					public boolean canHandleServer(IServer server) {
						return true;
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ASWTPToolsPlugin getDefault() {
		return plugin;
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

	/**
	 * Get the IPluginLog for this plugin. This method 
	 * helps to make logging easier, for example:
	 * 
	 *     FoundationCorePlugin.pluginLog().logError(etc)
	 *  
	 * @return IPluginLog object
	 */
	public static IPluginLog pluginLog() {
		return getDefault().pluginLogInternal();
	}

	/**
	 * Get a status factory for this plugin
	 * @return status factory
	 */
	public static StatusFactory statusFactory() {
		return getDefault().statusFactoryInternal();
	}
	
}
