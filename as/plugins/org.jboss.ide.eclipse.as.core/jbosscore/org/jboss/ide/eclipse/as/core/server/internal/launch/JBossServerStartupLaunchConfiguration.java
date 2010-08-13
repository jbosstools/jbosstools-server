/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBossServerStartupLaunchConfiguration extends AbstractJBossLaunchConfigType {
	public static interface StartLaunchDelegate {
		public void actualLaunch(JBossServerStartupLaunchConfiguration launchConfig,
				ILaunchConfiguration configuration, 
				String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
		
		public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException;
		public void preLaunch(ILaunchConfiguration configuration, 
				String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
		public void postLaunch(ILaunchConfiguration configuration, String mode,
				ILaunch launch, IProgressMonitor monitor) throws CoreException;
		
	}
	
	private StartLaunchDelegate del = null;
	protected StartLaunchDelegate getDelegate(ILaunchConfiguration configuration) throws CoreException {
		if( del != null )
			return del;
		IServer server = ServerUtil.getServer(configuration);
		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(server);
		IJBossServerPublishMethodType type = beh.createPublishMethod().getPublishMethodType();
		if( type.getId().equals(LocalPublishMethod.LOCAL_PUBLISH_METHOD)) {
			del = new LocalJBossServerStartupLaunchUtil();
		}
		return del;
	}
	public void actualLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getDelegate(configuration).actualLaunch(this, configuration, mode, launch, monitor);
	}
	
	public void superActualLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.actualLaunch(configuration, mode, launch, monitor);
	}	
	/*
	 * Ensures that the working directory and classpath are 100% accurate.
	 * Merges proper required params into args and vm args
	 */
	
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return getDelegate(configuration).preLaunchCheck(configuration, mode, monitor);
	}

	public void preLaunch(ILaunchConfiguration configuration, 
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getDelegate(configuration).preLaunch(configuration, mode, launch, monitor);
	}

	public void postLaunch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		getDelegate(configuration).postLaunch(configuration, mode, launch, monitor);
	}
}
