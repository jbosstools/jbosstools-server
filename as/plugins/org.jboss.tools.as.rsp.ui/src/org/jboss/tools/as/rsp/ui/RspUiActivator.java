/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.foundation.core.plugin.log.IPluginLog;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;
import org.jboss.tools.foundation.ui.plugin.BaseUIPlugin;
import org.jboss.tools.foundation.ui.plugin.BaseUISharedImages;
import org.jboss.tools.usage.event.UsageEventType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class RspUiActivator extends BaseUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.rsp.ui"; //$NON-NLS-1$
	
	// Usage detection
	public static final String USAGE_COMPONENT_NAME = "server"; //$NON-NLS-1$

	// The shared instance
	private static RspUiActivator plugin;

	private UsageEventType newServerEventType;
	private UsageEventType newRemoteServerEventType;
	
	
	/**
	 * The constructor
	 */
	public RspUiActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
//		newServerEventType = new UsageEventType(USAGE_COMPONENT_NAME, UsageEventType.getVersion(ASWTPToolsPlugin.this), 
//				null, UsageEventType.NEW_ACTION, Messages.UsageEventTypeServerIDLabelDescription, 
//				Messages.UsageEventTypeServerIDLabelDescription);
//		UsageReporter.getInstance().registerEvent(newServerEventType);
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
	public static RspUiActivator getDefault() {
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

	protected BaseUISharedImages createSharedImages() {
		return new RspSharedImages(getBundle());
	}
	
	private static class RspSharedImages extends BaseUISharedImages {
		public RspSharedImages(Bundle pluginBundle) {
			super(pluginBundle);
			addImage("community-12x24.png", "icons/community-12x24.png");
			addImage("community-24x24.png", "icons/community-24x24.png");
			addImage("felix-24x24.png", "icons/felix-24x24.png");
			addImage("glassfish-24x24.png", "icons/glassfish-24x24.png");
			addImage("jar_obj.gif", "icons/jar_obj.gif");
			addImage("jboss-24x24.png", "icons/jboss-24x24.png");
			addImage("jbossas7_ligature-24x24.png", "icons/jbossas7_ligature-24x24.png");
			addImage("jboss.eap-24x24.png", "icons/jboss.eap-24x24.png");
			addImage("jetty-24x24.png", "icons/jetty-24x24.png");
			addImage("karaf-24x24.png", "icons/karaf-24x24.png");
			addImage("openshift_extension-24x24.png", "icons/openshift_extension-24x24.png");
			addImage("openshift_extension.png", "icons/openshift_extension.png");
			addImage("rsp_ui_icon.png", "icons/rsp_ui_icon.png");
			addImage("server-dark-24x24.png", "icons/server-dark-24x24.png");
			addImage("server-light-24x24.png", "icons/server-light-24x24.png");
			addImage("tomcat-24x24.png", "icons/tomcat-24x24.png");
			addImage("wildfly_icon-24x24.png", "icons/wildfly_icon-24x24.png");
		}
	}

}
	