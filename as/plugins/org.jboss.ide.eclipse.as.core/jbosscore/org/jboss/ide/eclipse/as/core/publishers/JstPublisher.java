/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.publishers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.eclipse.wst.server.core.util.PublishUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.DeletedEvent;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.xpl.ModulePackager;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.FileUtil.IFileUtilListener;

/**
 * This class provides a default implementation for packaging different types of
 * flexible projects. It uses the built-in heirarchy of the projects to do so.
 * 
 * @author rob.stryker@jboss.com
 */
public class JstPublisher implements IJBossServerPublisher {

	public static final int BUILD_FAILED_CODE = 100;
	public static final int PACKAGE_UNDETERMINED_CODE = 101;

	protected IModuleResourceDelta[] delta;
	protected IDeployableServer server;
	protected EventLogTreeItem eventRoot;
	protected int publishState = IServer.PUBLISH_STATE_NONE;


	public JstPublisher(IDeployableServer server, EventLogTreeItem context) {
		this.server = server;
		eventRoot = context;
	}
	public JstPublisher(IServer server, EventLogTreeItem eventContext) {
		this( ServerConverter.getDeployableServer(server), eventContext);
	}
	public void setDelta(IModuleResourceDelta[] delta) {
		this.delta = delta;
	}

	protected String getModulePath(IModule[] module ) {
		String modulePath = "";
		for( int i = 0; i < module.length; i++ ) {
			modulePath += module[i].getName() + Path.SEPARATOR;
		}
		modulePath = modulePath.substring(0, modulePath.length()-1);
		return modulePath;
	}
	
	public IStatus publishModule(int kind, int deltaKind,
			int modulePublishState, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		String modulePath = getModulePath(module);

		IStatus status = null;
		boolean deleted = false;
		for( int i = 0; i < module.length; i++ ) {
			if( module[i] instanceof DeletedModule )
				deleted = true;
		}
		
		if (ServerBehaviourDelegate.REMOVED == deltaKind) {
			status = unpublish(server, module, monitor);
		} else if (kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN) {
			if( deleted ) 
				throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "The module cannot be published because it cannot be located. (" + modulePath + ")"));
			status = fullPublish(module, module[module.length-1], monitor);	
		} else if (kind == IServer.PUBLISH_INCREMENTAL || kind == IServer.PUBLISH_AUTO) {
			if( deleted ) 
				throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "The module cannot be published because it cannot be located. (" + modulePath + ")"));
			status = incrementalPublish(module, module[module.length-1], monitor);
		} 
		return status;
	}
		
	
	protected IStatus fullPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IPath deployPath = getDeployPath(moduleTree);
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
		IModuleResource[] members = md.members();
		localSafeDelete(deployPath, eventRoot);
		IStatus[] results = new IStatus[0];
		if( !deployPackaged(moduleTree))
			results = PublishUtil.publishFull(members, deployPath, monitor);
		else
			results = packModuleIntoJar(moduleTree[moduleTree.length-1], getDeployPath(moduleTree));
		
		int length = results == null ? 0 : results.length;
		for( int i = 0; i < length; i++ ) {
			new PublisherEventLogger.PublishUtilStatusWrapper(eventRoot, results[i]);
		}
		if( length != 0 ) EventLogModel.markChanged(eventRoot);
		
		// adjust timestamps
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				if( pathname.getAbsolutePath().toLowerCase().endsWith(".xml"))
					return true;
				return false;
			}
		};
		FileUtil.touch(filter, deployPath.toFile(), true);
		return null;
	}

	protected IStatus incrementalPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IStatus[] results = new IStatus[] {};
		if( !deployPackaged(moduleTree))
			results = PublishUtil.publishDelta(delta, getDeployPath(moduleTree), monitor);
		else if( delta.length > 0 )
			results = packModuleIntoJar(moduleTree[moduleTree.length-1], getDeployPath(moduleTree));
		
		int length = results == null ? 0 : results.length;
		for( int i = 0; i < length; i++ ) {
			new PublisherEventLogger.PublishUtilStatusWrapper(eventRoot, results[i]);
		}
		if( length != 0 ) EventLogModel.markChanged(eventRoot);

		return null;
	}
	
	protected IStatus unpublish(IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		boolean error = localSafeDelete(getDeployPath(module), eventRoot);
		if( error ) {
			publishState = IServer.PUBLISH_STATE_FULL;
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Unable to delete module from server. (" + getModulePath(module) + ")", new Exception("Some files were not removed from the server")));
		}
		return null;
	}

	protected IPath getDeployPath(IModule[] moduleTree) {
		IPath root = new Path( server.getDeployDirectory() );
		String type, name;
		IProject project;
		for( int i = 0; i < moduleTree.length; i++ ) {
			type = moduleTree[i].getModuleType().getId();
			project = moduleTree[i].getProject();
			name = project == null ? moduleTree[i].getName() : project.getName();
			if( "jst.ear".equals(type)) 
				root = root.append(name + ".ear");
			else if( "jst.web".equals(type)) 
				root = root.append(name + ".war");
			else if( "jst.utility".equals(type) && i >= 1 && "jst.web".equals(moduleTree[i-1].getModuleType().getId())) 
				root = root.append("WEB-INF").append("lib").append(name + ".jar");			
			else
				root = root.append(name + ".jar");
		}
		return root;
	}
	
	/**
	 * 
	 * @param deployPath
	 * @param event
	 * @return  returns whether an error was found
	 */
	protected boolean localSafeDelete(IPath deployPath, final EventLogTreeItem event) {
		final Boolean[] errorFound = new Boolean[] { new Boolean(false)};
		IFileUtilListener listener = new IFileUtilListener() {
			public void fileCoppied(File source, File dest, boolean result,Exception e) {}
			public void fileDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					errorFound[0] = new Boolean(true);
					new DeletedEvent(event, file, result, e);
					EventLogModel.markChanged(event);
				}
			}
			public void folderDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					errorFound[0] = new Boolean(true);
					new DeletedEvent(event, file, result, e);
					EventLogModel.markChanged(event);
				}
			} 
		};
		FileUtil.safeDelete(deployPath.toFile(), listener);
		return errorFound[0].booleanValue();
	}
	protected boolean deployPackaged(IModule[] moduleTree) {
		if( moduleTree[moduleTree.length-1].getModuleType().getId().equals("jst.utility")) return true;
		if( moduleTree[moduleTree.length-1].getModuleType().getId().equals("jst.appclient")) return true;
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
			IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0,
					"unable to assemble module", e); //$NON-NLS-1$
			throw new CoreException(status);
		}
		finally{
			try{
				packager.finished();
			}
			catch(IOException e){
				IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0,
						"unable to assemble module", e); //$NON-NLS-1$
				throw new CoreException(status);
			}
		}
		return null;
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
}
