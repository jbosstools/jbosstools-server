/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core.archives;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.services.clientserver.messages.SystemElementNotFoundException;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.webtools.modules.WTPZippedPublisher;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.rse.core.RSEPublishMethod;

/**
 * This class is in charge of RSE zipped publishing for flexible projects.
 * It extends the functionality of the local zipped publishing class
 * by uploading the file after building it in a temporary directory
 */
public class RSEZippedJSTPublisher extends WTPZippedPublisher {

	protected String getPublishMethod() {
		return RSEPublishMethod.RSE_ID;
	}
	
	/**
	 * Here we put the deployment first in a temporary remote deploy folder
	 * Then during the publishModule call, we'll also upload it to remote machine
	 */
	protected String getDeployRoot(IModule[] module, IDeployableServer ds) {
		IPath deployRoot = JBossServerCorePlugin.getServerStateLocation(ds.getServer()).
			append(IJBossServerConstants.TEMP_REMOTE_DEPLOY).makeAbsolute();
		deployRoot.toFile().mkdirs();
		return deployRoot.toString();
	}
	
	@Override
	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		
		String taskName = "Publishing " + module[0].getName();
		monitor.beginTask(taskName, 200); //$NON-NLS-1$
		monitor.setTaskName(taskName);
		if( module.length > 1 ) {
			monitor.done();
			return null;
		}
		
		monitor.setTaskName("Publishing to remote server " + server.getName());
		
		// set up needed vars
		IDeployableServer server2 = ServerConverter.getDeployableServer(server);
		String remoteTempDeployRoot = getDeployRoot(module, ServerConverter.getDeployableServer(server));
		IPath sourcePath = PublishUtil.getDeployPath(module, remoteTempDeployRoot, server2);
		IPath destFolder = PublishUtil.getDeployPath(method, module, server2);
		String name = sourcePath.lastSegment();
		IStatus result = null;		
		RSEPublishMethod method2 = (RSEPublishMethod)method;
				
		// Am I a removal? If yes, remove me, and return
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
			result = removeRemoteDeployment(method2, sourcePath, destFolder, name, monitor);
		} else if( publishType != IJBossServerPublisher.NO_PUBLISH ){
			// Locally zip it up into the remote tmp folder
			result = super.publishModule(method, server, module, publishType, delta, 
					AbstractServerToolsPublisher.getSubMon(monitor, 50));
			if( !result.isOK() ) {
				monitor.done();
			} else {
				result = remoteFullPublish(method, sourcePath, destFolder, name, 
						AbstractServerToolsPublisher.getSubMon(monitor, 150));
			}
		}

		monitor.done();
		if( result != null ) {
			return result;
		}

		return Status.OK_STATUS;
	}
	
	private IStatus remoteFullPublish( IJBossServerPublishMethod method, IPath sourcePath, 
			IPath destFolder, String name, IProgressMonitor monitor) {
		// Now transfer the file to RSE
		RSEPublishMethod method2 = (RSEPublishMethod)method;
		try {
			removeRemoteDeployment(method2, sourcePath, destFolder, name, new NullProgressMonitor());
			method2.getFileService().upload(sourcePath.toFile(), destFolder.toString(), name, true, null, null, 
					AbstractServerToolsPublisher.getSubMon(monitor, 150));
		} catch( SystemMessageException sme ) {
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, sme.getMessage(), sme);
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}

	private IStatus removeRemoteDeployment( IJBossServerPublishMethod method, IPath sourcePath, 
			IPath destFolder, String name, IProgressMonitor monitor) {
		// Now transfer the file to RSE
		RSEPublishMethod method2 = (RSEPublishMethod)method;
		try {
			method2.getFileService().delete(destFolder.toString(), name, monitor);
		} catch( SystemElementNotFoundException senfe ) {
			/* Ignore intentionally... file already does not exist on remote server */
			return Status.OK_STATUS;
		} catch( SystemMessageException sme ) {
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, sme.getMessage(), sme);
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}
}
