/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerDetailsController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;

public class ServerHomeValidationUtility {
	private ISubsystemController findDependencyFromBehavior(IServer server, String system) throws CoreException {
		IControllableServerBehavior beh = getControllableBehavior(server);
		return beh == null ? null : beh.getController(system);
	}
	
	private IControllableServerBehavior getControllableBehavior(IServer server) {
		if( server != null ) {
			ServerBehaviourDelegate del = (ServerBehaviourDelegate)server.loadAdapter(ServerBehaviourDelegate.class, null);
			if( del instanceof ControllableServerBehavior)
				return (IControllableServerBehavior)del;
		}
		return null;
	}
	
	public IStatus validateServerHome(IServer server) throws CoreException {
		return validateServerHome(server, false);
	}
	
	public IStatus validateServerHome(IServer server, boolean alwaysThrow) throws CoreException {

		IFilesystemController fs = (IFilesystemController)findDependencyFromBehavior(server, IFilesystemController.SYSTEM_ID);
		IServerDetailsController det = (IServerDetailsController)findDependencyFromBehavior(server, IServerDetailsController.SYSTEM_ID);
		IStatus s = validateServerHome(server, det, fs);
		if( alwaysThrow && !s.isOK()) {
			throw new CoreException(s);
		}
		return s;
	}
	
	public IStatus validateServerHome(IServer server, IServerDetailsController details, 
			IFilesystemController fs) throws CoreException {
		if( details != null ) {
			String serverHome = details.getProperty(IServerDetailsController.PROP_SERVER_HOME);
			if( serverHome == null || serverHome.isEmpty()) {
				return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						NLS.bind("The home directory for \"{0}\" cannot be empty.", server.getName())); //$NON-NLS-1$
			}
			String sep2 = details.getProperty(IServerDetailsController.SEPARATOR_CHAR);
			if( sep2 == null || sep2.length() != 1 ) {
				return (new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						NLS.bind("The separator character for \"{0}\" is invalid: " + sep2, server.getName()))); //$NON-NLS-1$
			}
			IPath remoteHome = new RemotePath(serverHome, sep2.charAt(0));
			if( !fs.exists(remoteHome, new NullProgressMonitor())) {
				return (new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						NLS.bind("The home directory for \"{0}\" does not exist: " + serverHome, server.getName()))); //$NON-NLS-1$
			}
		}
		return Status.OK_STATUS;
	}
}
