/*************************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.as.runtimes.integration.internal;

import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.jboss.tools.as.runtimes.integration.Messages;
import org.jboss.tools.as.runtimes.integration.ServerRuntimesIntegrationActivator;
import org.jboss.tools.as.runtimes.integration.internal.DriverUtility.DriverUtilityException;

public class DriverRuntimeLifecycleListener implements IRuntimeLifecycleListener {
	private static DriverRuntimeLifecycleListener instance = new DriverRuntimeLifecycleListener();
	public static DriverRuntimeLifecycleListener getDefault() {
		return instance;
	}
	
	
	@Override
	public void runtimeAdded(IRuntime runtime) {
		try {
			new DriverUtility().createDriver(runtime.getLocation().toOSString(), runtime.getRuntimeType());
		}catch (DriverUtilityException e) {
			ServerRuntimesIntegrationActivator.pluginLog().logError(Messages.JBossRuntimeStartup_Cannott_create_new_DTP_Connection_Profile,e);
		}

	}

	@Override
	public void runtimeChanged(IRuntime runtime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runtimeRemoved(IRuntime runtime) {
		// TODO Auto-generated method stub

	}

}
