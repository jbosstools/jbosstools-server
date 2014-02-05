/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.rse.core.IFileServiceProvider;
import org.jboss.ide.eclipse.as.rse.core.RSEFrameworkUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.ui.subsystems.IExploreBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

public class RSEExploreBehavior extends AbstractSubsystemController implements IExploreBehavior {
	public void openExplorer(IServer server, IModule[] module) {
		IPath remoteFolder = null;
		try {
			IControllableServerBehavior cs = JBossServerBehaviorUtils.getControllableBehavior(server);
			if( module == null ) {
				IDeploymentOptionsController controller = (IDeploymentOptionsController)cs.getController(IDeploymentOptionsController.SYSTEM_ID);
				remoteFolder = new RemotePath(controller.getDeploymentsRootFolder(true), controller.getPathSeparatorCharacter());
			} else {
				IModuleDeployPathController controller = (IModuleDeployPathController)cs.getController(IModuleDeployPathController.SYSTEM_ID);
				remoteFolder = controller.getDeployDirectory(module);
			}
			
			IFilesystemController fsController = (IFilesystemController)cs.getController(IFilesystemController.SYSTEM_ID);
			if( fsController != null && fsController instanceof IFileServiceProvider) {
				IFileService fs = ((IFileServiceProvider)fsController).getFileService();
				IFileServiceSubSystem fsSubsystem = ((IFileServiceProvider)fsController).getFileServiceSubSystem();

				String connectionName = RSEUtils.getRSEConnectionName(server);
				IHost host = RSEFrameworkUtils.findHost(connectionName);
				RSEFrameworkUtils.ensureActiveConnection(server, 
						RSEFrameworkUtils.findFileTransferSubSystem(host), new NullProgressMonitor());
				IHostFile file = fs.getFile(remoteFolder.removeLastSegments(1).toOSString(), remoteFolder.lastSegment(), new NullProgressMonitor());
				String path = remoteFolder.toString();
				
				IRemoteFile rf = fsSubsystem.getRemoteFileObject(path, null);
				
				SystemShowInTableAction act = new SystemShowInTableAction(Display.getDefault().getActiveShell()); 
				act.setSelectedObject(rf);
				act.run();
			}
		} catch(CoreException ce) {
			RSEUIPlugin.log(ce);
		} catch(SystemMessageException sme) {
			RSEUIPlugin.log(sme);
		}
		
	}

	@Override
	public boolean canExplore(IServer server, IModule[] module) {
		return true;
	}
}
