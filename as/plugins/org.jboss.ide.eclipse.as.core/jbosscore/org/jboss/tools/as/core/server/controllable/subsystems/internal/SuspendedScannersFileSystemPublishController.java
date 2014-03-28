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
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDeploymentScannerModifier;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;

/**
 * A filesystem publish controller which also suspends and resumes 
 * the deployment scanners for a server before and after each publish.
 * 
 * This is used between AS 3 and 6.
 */
public class SuspendedScannersFileSystemPublishController extends
		StandardFileSystemPublishController {
	
	private IDeploymentScannerModifier getScannerModifier() {
		JBossExtendedProperties properties = (JBossExtendedProperties)getServer().loadAdapter(JBossExtendedProperties.class, null);
		if( properties != null ) {
			IDeploymentScannerModifier modifier = properties.getDeploymentScannerModifier();
			return modifier;
		}
		return null;
	}
	
	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
		super.publishStart(monitor);
		if( getServer().getServerState() == IServer.STATE_STARTED ) {
			IDeploymentScannerModifier mod = getScannerModifier();
			if( mod != null ) {
				mod.suspendScanners(getServer());
			}
		}
	}

	@Override
	public void publishFinish(IProgressMonitor monitor) throws CoreException {
		// Resume deployment scanners
		if( getServer().getServerState() == IServer.STATE_STARTED ) {
			IDeploymentScannerModifier mod = getScannerModifier();
			if( mod != null ) {
				mod.resumeScanners(getServer());
			}
		}
		super.publishFinish(monitor);
	}

}
