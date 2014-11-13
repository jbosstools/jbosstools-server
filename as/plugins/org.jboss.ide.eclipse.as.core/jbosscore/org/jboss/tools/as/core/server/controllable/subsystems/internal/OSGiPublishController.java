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
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.PluginExportOperation;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPrimaryPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishControllerDelegate;
import org.jboss.tools.as.core.server.controllable.util.PublishControllerUtility;

public class OSGiPublishController extends AbstractSubsystemController implements IPublishControllerDelegate {

	protected IStatus scheduleExportJob(IProject project, String destFolder, String name, IProgressMonitor monitor) {
		final FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = true;
		info.useJarFormat = true;
		info.exportSource = false;
		info.exportSourceBundle = true;
		info.allowBinaryCycles = true;
		info.useWorkspaceCompiledClasses = false;
		info.destinationDirectory = destFolder;
		info.zipFileName = name;
		info.items = new Object[]{PluginRegistry.findModel(project)};
		info.signingInfo = null; //fPage.useJARFormat() ? fPage.getSigningInfo() : null;
		info.qualifier = null; //fPage.getQualifier();

		final CustomPluginExportOperation job = new CustomPluginExportOperation(info, "Exporting Plugin: " + project.getName()); //$NON-NLS-1$
		IStatus s = job.run2(monitor);
		return s;
	}
	
	private class CustomPluginExportOperation extends PluginExportOperation {
		public CustomPluginExportOperation(FeatureExportInfo info, String name) {
			super(info, name);
		}
		
		public IStatus run2(IProgressMonitor monitor) {
			return super.run(monitor);
		}
	}
	
	private IPath getMetadataTemporaryLocation(IServer server) {
		IPath deployRoot = JBossServerCorePlugin.getServerStateLocation(server).
			append(IJBossToolingConstants.TEMP_REMOTE_DEPLOY).makeAbsolute();
		deployRoot.toFile().mkdirs();
		return deployRoot;
	}


	@Override
	public int publishModule(int kind, int deltaKind, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		int publishType = PublishControllerUtility.getPublishType(getServer(), module, kind, deltaKind);
		if( publishType == PublishControllerUtility.REMOVE_PUBLISH){
			return removeModule(module, monitor);
		}
		
		IDeployableServer server2 = ServerConverter.getDeployableServer(getServer());
		IPath metadataLoc = getMetadataTemporaryLocation(getServer());
		if( metadataLoc.toFile().exists())
			metadataLoc.toFile().mkdirs();
		
		IPath presumedSourcePath = PublishUtil.getModuleNestedDeployPath(module, metadataLoc.toOSString(), server2);
		String name = presumedSourcePath.lastSegment();
		IPath realSourcePathFolder = presumedSourcePath.removeLastSegments(1).append("plugins"); //$NON-NLS-1$
		IProject project = module[module.length-1].getProject();
		// Run the export job
		IStatus result = scheduleExportJob(project, metadataLoc.toOSString(), name, monitor);
		if( result.isOK() ) {
			String[] plugins = realSourcePathFolder.toFile().list();
			if( plugins.length > 0 ) {
				// Our zipped file is here
				IPath src = realSourcePathFolder.append(plugins[0]);
				
				int ret = transferBuiltModule(module, src, monitor);
				// Then cleanup this plugins folder >=[
				src.toFile().delete();
				return ret;
			}
		}

		// We don't know how it published
		return IServer.PUBLISH_STATE_UNKNOWN;
	}

	private int removeModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
		IPrimaryPublishController pc = JBossServerBehaviorUtils.getController(getServer(), IPublishController.SYSTEM_ID, IPrimaryPublishController.class);
		if( pc != null) {
			return pc.removeModule(module, monitor);
		}
		return IServer.PUBLISH_STATE_UNKNOWN;
	}
	
	private int transferBuiltModule(IModule[] module, IPath srcFile, IProgressMonitor monitor) throws CoreException {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(getServer());
		if( beh != null ) {
			IPublishController pc = (IPublishController)beh.getController(IPublishController.SYSTEM_ID);
			if( pc instanceof IPrimaryPublishController) {
				return ((IPrimaryPublishController)pc).transferBuiltModule(module, srcFile, monitor);
			}
		}
		return IServer.PUBLISH_STATE_UNKNOWN;
	}
	
}
