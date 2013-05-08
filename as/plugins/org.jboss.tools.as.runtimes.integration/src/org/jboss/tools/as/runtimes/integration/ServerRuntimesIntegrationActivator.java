/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.as.runtimes.integration;

import org.jboss.tools.common.log.BasePlugin;
import org.osgi.framework.BundleContext;

public class ServerRuntimesIntegrationActivator extends BasePlugin {

	public static final String PLUGIN_ID = "org.jboss.tools.as.runtimes.integration";
	private static BundleContext context;
	private static ServerRuntimesIntegrationActivator DEFAULT;
	
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
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		ServerRuntimesIntegrationActivator.context = null;
	}
}
