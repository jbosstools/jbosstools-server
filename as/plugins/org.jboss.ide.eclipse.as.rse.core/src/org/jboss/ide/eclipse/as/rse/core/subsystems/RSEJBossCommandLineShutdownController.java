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
package org.jboss.ide.eclipse.as.rse.core.subsystems;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerHomeValidationUtility;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchConfigProperties;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

public class RSEJBossCommandLineShutdownController extends RSECommandLineShutdownController
		implements IServerShutdownController {

	// we remove scanners before stopping
	public void stopImpl(boolean force) {
		try {
			new ServerHomeValidationUtility().validateServerHome(getServer(), true);
		} catch(CoreException ce) {
			throw new RuntimeException(ce);
		}
		
		removeScanners();
		super.stopImpl(force);
	}
	
	protected void removeScanners() {
		boolean removeScanners = getServer().getAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, true);
		if( removeScanners ) {
			JBossExtendedProperties properties = (JBossExtendedProperties)getServer().loadAdapter(JBossExtendedProperties.class, null);
			if( properties != null ) {
				properties.getDeploymentScannerModifier().removeAddedDeploymentScanners(getServer());
			}		
		}
	}
	

	@Override
	protected String getShutdownCommand(IServer server) throws CoreException {
		ILaunchConfiguration config = getServer().getLaunchConfiguration(false, new NullProgressMonitor());
		return new RSELaunchConfigProperties().getShutdownCommand(config, getDefaultShutdownCommand(server));
	}
	
	@Override
	protected String getDefaultShutdownCommand(IServer server) {
		JBossServer jbs = (JBossServer)ServerConverter.getJBossServer(getServer());
		String defaultArgs = jbs.getExtendedProperties().getDefaultLaunchArguments().getDefaultStopArgs();
		return defaultArgs;
	}

	protected PollThread getPollThread() {
		return (PollThread)getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.POLL_THREAD);
	}
	
	protected void clearPollThread() {
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.POLL_THREAD, null);
	}

	
	@Override
	protected boolean isServerStarted() {
		return PollThreadUtils.isServerStarted(getServer()).isOK();
	}

	@Override
	protected void beforeCommandExecuted() {
		// launch poll thread
		PollThreadUtils.pollServer(getServer(), IServerStatePoller.SERVER_DOWN);
	}
	
	@Override
	protected void afterCommandExecuted() {
		// Smart server, lets the poller handle this, does nothing
	}

	protected void handleShutdownFailed() {
		// Cancel / clear poll thread
		if( getPollThread() != null ) {
			getPollThread().cancel();
			clearPollThread();
		}
		super.handleShutdownFailed();
	}
}
