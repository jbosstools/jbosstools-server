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
package org.jboss.ide.eclipse.as.core.publishers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.xpl.ModulePackager;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.FileUtil.FileUtilListener;
import org.jboss.ide.eclipse.as.core.util.FileUtil.IFileUtilListener;

/**
 * This class provides a default implementation for packaging different types of
 * flexible projects. It uses the built-in heirarchy of the projects to do so.
 * 
 * @author rob.stryker@jboss.com
 */
public class JstPublisher extends PublishUtil implements IJBossServerPublisher {


	protected IModuleResourceDelta[] delta;
	protected IDeployableServer server;
	protected int publishState = IServer.PUBLISH_STATE_NONE;


	public JstPublisher() {
	}
	
	private boolean serverRequiresZips() {
		return server.zipsWTPDeployments();
	}
	
	public IStatus publishModule(IServer server, IModule[] module, 
			int publishType, IModuleResourceDelta[] delta, IProgressMonitor monitor) throws CoreException {
		IStatus status = null;
		this.server = ServerConverter.getDeployableServer(server);
		this.delta = delta;

		if( serverRequiresZips() ) {
			IJBossServerPublisher delegate = 
				ExtensionManager.getDefault().getZippedPublisher();
			if( delegate != null ) {
				return delegate.publishModule(server, module, publishType, delta, monitor);
			} else {
				// TODO log,  use unzipped instead
			}
		}
		
		boolean deleted = false;
		for( int i = 0; i < module.length; i++ ) {
			if( module[i].isExternal() )
				deleted = true;
		}
		
		if (publishType == REMOVE_PUBLISH ) {
			status = unpublish(this.server, module, monitor);
		} else {
			if( deleted ) {
				publishState = IServer.PUBLISH_STATE_UNKNOWN;
			} else {
				if (publishType == FULL_PUBLISH ) {
					status = fullPublish(module, module[module.length-1], monitor);	
				} else if (publishType == INCREMENTAL_PUBLISH) {
					status = incrementalPublish(module, module[module.length-1], monitor);
				} 
			}
		}
		return status;
	}
		
	
	protected IStatus fullPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IPath deployPath = getDeployPath(moduleTree, server);
		IPath tempDeployPath = getTempDeployFolder(moduleTree, server);
		IModuleResource[] members = getResources(module);
 
		ArrayList<IStatus> list = new ArrayList<IStatus>();
		// if the module we're publishing is a project, not a binary, clean it's folder
		if( !(new Path(module.getName()).segmentCount() > 1 ))
			list.addAll(Arrays.asList(localSafeDelete(deployPath)));

		if( !deployPackaged(moduleTree) && !isBinaryObject(moduleTree)) {
			PublishCopyUtil util = new PublishCopyUtil(server.getServer(), deployPath, tempDeployPath);
			list.addAll(Arrays.asList(util.publishFull(members, deployPath, monitor)));
		}
		else if( isBinaryObject(moduleTree))
			list.addAll(Arrays.asList(copyBinaryModule(moduleTree)));
		else
			list.addAll(Arrays.asList(packModuleIntoJar(moduleTree[moduleTree.length-1], deployPath)));
		
		// adjust timestamps
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				if( pathname.getAbsolutePath().toLowerCase().endsWith(IConstants.EXT_XML))
					return true;
				return false;
			}
		};
		FileUtil.touch(filter, deployPath.toFile(), true);

		if( list.size() > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_FAIL, 
					NLS.bind(Messages.FullPublishFail, module.getName()), null);
			for( int i = 0; i < list.size(); i++ )
				ms.add(list.get(i));
			return ms;
		}

		
		publishState = IServer.PUBLISH_STATE_NONE;
		
		IStatus ret = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_SUCCESS, 
				NLS.bind(Messages.CountModifiedMembers, countMembers(module), module.getName()), null);
		return ret;
	}

	protected IStatus incrementalPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IStatus[] results = new IStatus[] {};
		IPath deployPath = getDeployPath(moduleTree, server);
		IPath tempDeployPath = getTempDeployFolder(moduleTree, server);
		if( !deployPackaged(moduleTree) && !isBinaryObject(moduleTree))
			results = new PublishCopyUtil(server.getServer(), deployPath, tempDeployPath)
						.publishDelta(delta, deployPath, monitor);
		else if( delta.length > 0 ) {
			if( isBinaryObject(moduleTree))
				results = copyBinaryModule(moduleTree);
			else
				results = packModuleIntoJar(moduleTree[moduleTree.length-1], deployPath);
		}
		if( results != null && results.length > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					NLS.bind(Messages.IncrementalPublishFail, module.getName()), null);
			for( int i = 0; i < results.length; i++ )
				ms.add(results[i]);
			return ms;
		}
		IStatus ret = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_SUCCESS, 
				NLS.bind(Messages.CountModifiedMembers, countChanges(delta), module.getName()), null);
		return ret;
	}
	
	protected IStatus unpublish(IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		IModule mod = module[module.length-1];
		IStatus[] errors = localSafeDelete(getDeployPath(module, server));
		if( errors.length > 0 ) {
			publishState = IServer.PUBLISH_STATE_FULL;
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_REMOVE_FAIL,
					NLS.bind(Messages.DeleteModuleFail, mod.getName()), 
					new Exception(Messages.DeleteModuleFail2));
			for( int i = 0; i < errors.length; i++ )
				ms.addAll(errors[i]);
			throw new CoreException(ms);
		}
		IStatus ret = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_REMOVE_SUCCESS, 
				NLS.bind(Messages.ModuleDeleted, mod.getName()), null);
		return ret;
	}


	
	protected IStatus[] copyBinaryModule(IModule[] moduleTree) {
		try {
			IPath deployPath = getDeployPath(moduleTree, server);
			FileUtilListener listener = new FileUtilListener();
			IModuleResource[] members = getResources(moduleTree);
			File source = (File)members[0].getAdapter(File.class);
			if( source == null ) {
				IFile ifile = (IFile)members[0].getAdapter(IFile.class);
				if( ifile != null ) 
					source = ifile.getLocation().toFile();
			}
			if( source != null ) {
				FileUtil.fileSafeCopy(source, deployPath.toFile(), listener);
				return listener.getStatuses();
			} else {
				IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_COPY_BINARY_FAIL,
						NLS.bind(Messages.CouldNotPublishModule,
								moduleTree[moduleTree.length-1]), null);
				return new IStatus[] {s};
			}
		} catch( CoreException ce ) {
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_COPY_BINARY_FAIL,
					NLS.bind(Messages.CouldNotPublishModule,
							moduleTree[moduleTree.length-1]), null);
			return new IStatus[] {s};
		}
	}
	/**
	 * 
	 * @param deployPath
	 * @param event
	 * @return  returns whether an error was found
	 */
	protected IStatus[] localSafeDelete(IPath deployPath) {
        String serverDeployFolder = server.getDeployFolder();
        Assert.isTrue(!deployPath.toFile().equals(new Path(serverDeployFolder).toFile()), 
        		"An attempt to delete your entire deploy folder has been prevented. This should never happen"); //$NON-NLS-1$
        final ArrayList<IStatus> status = new ArrayList<IStatus>();
		IFileUtilListener listener = new IFileUtilListener() {
			public void fileCopied(File source, File dest, boolean result,Exception e) {}
			public void fileDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FILE_DELETE_FAIL, 
							NLS.bind(Messages.DeleteFileError, file.getAbsolutePath()),e));
				}
			}
			public void folderDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FILE_DELETE_FAIL,
							NLS.bind(Messages.DeleteFolderError, file.getAbsolutePath()),e));
				}
			} 
		};
		FileUtil.safeDelete(deployPath.toFile(), listener);
		return (IStatus[]) status.toArray(new IStatus[status.size()]);
	}
	public static boolean deployPackaged(IModule[] moduleTree) {
		if( moduleTree[moduleTree.length-1].getModuleType().getId().equals(IWTPConstants.FACET_UTILITY)) return true;
		if( moduleTree[moduleTree.length-1].getModuleType().getId().equals(IWTPConstants.FACET_APP_CLIENT)) return true;
		return false;
	}
	
	public int getPublishState() {
		return publishState;
	}

	/*
	 * Just package into a jar raw.  Don't think about it, just do it
	 */
	protected IStatus[] packModuleIntoJar(IModule module, IPath destination)throws CoreException {
		String dest = destination.toString();
		ModulePackager packager = null;
		try {
			packager = new ModulePackager(dest, false);
			ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
			IModuleResource[] resources = pm.members();
			for (int i = 0; i < resources.length; i++) {
				doPackModule(resources[i], packager);
			}
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_ASSEMBLE_FAIL,
					"unable to assemble module " + module.getName(), e); //$NON-NLS-1$
			return new IStatus[]{status};
		}
		finally{
			try{
				if( packager != null ) 
					packager.finished();
			}
			catch(IOException e){
				IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_ASSEMBLE_FAIL,
						"unable to assemble module "+ module.getName(), e); //$NON-NLS-1$
				return new IStatus[]{status};
			}
		}
		return new IStatus[]{};
	}

	
	/* Add one file or folder to a jar */
	private void doPackModule(IModuleResource resource, ModulePackager packager) throws CoreException, IOException{
		if (resource instanceof IModuleFolder) {
			IModuleFolder mFolder = (IModuleFolder)resource;
			IModuleResource[] resources = mFolder.members();

			packager.writeFolder(resource.getModuleRelativePath().append(resource.getName()).toPortableString());

			for (int i = 0; resources!= null && i < resources.length; i++) {
				doPackModule(resources[i], packager);
			}
		} else {
			String destination = resource.getModuleRelativePath().append(resource.getName()).toPortableString();
			IFile file = (IFile) resource.getAdapter(IFile.class);
			if (file != null)
				packager.write(file, destination);
			else {
				File file2 = (File) resource.getAdapter(File.class);
				packager.write(file2, destination);
			}
		}
	}
	public boolean accepts(IServer server, IModule[] module) {
		return ModuleCoreNature.isFlexibleProject(module[0].getProject());
	}
}
