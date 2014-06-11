/*************************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.as.runtimes.integration;

import org.jboss.tools.foundation.core.plugin.BaseCorePlugin;
import org.jboss.tools.foundation.core.plugin.log.IPluginLog;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;
import org.osgi.framework.BundleContext;

public class ServerRuntimesIntegrationActivator extends BaseCorePlugin {
	public static final String USAGE_COMPONENT_NAME = "server"; //$NON-NLS-1$
	public static final String USAGE_SERVER_ID_LABEL_DESCRIPTION = Messages.UsageEventTypeServerIDLabelDescription;
	private static final String DETECT_ACTION_NAME = "detect"; //$NON-NLS-1$

	public static final String PLUGIN_ID = "org.jboss.tools.as.runtimes.integration";
	private static BundleContext context;
	private static ServerRuntimesIntegrationActivator DEFAULT;

	private UsageEventType newDetectedServerEventType;

	public static ServerRuntimesIntegrationActivator getDefault() {
		return DEFAULT;
	}
	
	public static BundleContext getBundleContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		ServerRuntimesIntegrationActivator.context = bundleContext;
		DEFAULT = this;

		newDetectedServerEventType = new UsageEventType(USAGE_COMPONENT_NAME, UsageEventType.getVersion(this), null, DETECT_ACTION_NAME, USAGE_SERVER_ID_LABEL_DESCRIPTION);
		UsageReporter.getInstance().registerEvent(newDetectedServerEventType);
	}

	/**
	 * Reports a new event of server creation via runtime detection
	 * @param serverTypeId
	 */
	public void trackNewDetectedServerEvent(String serverTypeId) {
		UsageReporter.getInstance().trackEvent(newDetectedServerEventType.event(serverTypeId));
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		ServerRuntimesIntegrationActivator.context = null;
	}
	
	public static IPluginLog pluginLog() {
		return getDefault().pluginLogInternal();
	}
}
