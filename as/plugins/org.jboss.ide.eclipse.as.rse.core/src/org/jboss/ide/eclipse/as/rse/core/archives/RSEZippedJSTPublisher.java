/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.wst.common.project.facet.core.util.internal.ProgressMonitorUtil;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.webtools.modules.WTPZippedPublisher;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.publishers.AbstractServerToolsPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
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
		
		// Locally zip it up into the remote tmp folder
		IStatus sup = super.publishModule(method, server, module, publishType, delta, 
				AbstractServerToolsPublisher.getSubMon(monitor, 50));
		if( !sup.isOK() ) {
			monitor.done();
			return sup;
		}
		
		monitor.setTaskName("Publishing to remote server (dummy)");
		
		// set up needed vars
		IDeployableServer server2 = ServerConverter.getDeployableServer(server);
		String remoteTempDeployRoot = getDeployRoot(module, ServerConverter.getDeployableServer(server));
		RSEPublishMethod method2 = (RSEPublishMethod)method;
		IPath sourcePath = PublishUtil.getDeployPath(module, remoteTempDeployRoot);
		IModule lastMod = module[module.length-1];
		IPath destFolder = RSEPublishMethod.findModuleFolderWithDefault(lastMod, server2, method2.getRemoteRootFolder());
		//IPath tempDestFolder = RSEPublishMethod.findModuleFolderWithDefault(lastMod, server2, method2.getRemoteTemporaryFolder());
		String name = sourcePath.lastSegment();
		
		// Now transfer the file to RSE
		IFileService fs = method2.getFileService();
		IFileServiceSubSystem system = method2.getFileServiceSubSystem();
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		if( !sup.isOK())
			results.add(sup);
		
		try {
			method2.getFileService().upload(sourcePath.toFile(), destFolder.toString(), name, true, null, null, 
					AbstractServerToolsPublisher.getSubMon(monitor, 150));
		} catch( SystemMessageException sme ) {
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, sme.getMessage(), sme);
			results.add(s);
		}

		monitor.done();
		if( results != null && results.size() > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					NLS.bind(Messages.IncrementalPublishFail, module[0].getName()), null);
			IStatus[] results2 = results.toArray(new IStatus[results.size()]);
			for( int i = 0; i < results.size(); i++ )
				ms.add(results2[i]);
			return ms;
		}

		return sup;
	}
}
