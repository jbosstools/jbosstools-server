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
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.eclipse.wst.server.core.util.PublishUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.xpl.ModulePackager;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

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

	public IStatus publishModule(int kind, int deltaKind,
			int modulePublishState, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		IStatus status = null;

		if (ServerBehaviourDelegate.REMOVED == deltaKind) {
			status = unpublish(server, module, monitor);
		} else if (kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN) {
			status = fullPublish(module, module[module.length-1], monitor);	
		} else if (kind == IServer.PUBLISH_INCREMENTAL || kind == IServer.PUBLISH_AUTO) {
			status = incrementalPublish(module, module[module.length-1], monitor);
		} 
		return status;
	}
		
	
	protected IStatus fullPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IPath deployPath = getDeployPath(moduleTree);
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
		IModuleResource[] members = md.members();
		FileUtil.safeDelete(deployPath.toFile());
		if( !deployPackaged(moduleTree))
			PublishUtil.publishFull(members, deployPath, monitor);
		else
			packModuleIntoJar(moduleTree[moduleTree.length-1], getDeployPath(moduleTree));
			
		return null;
	}

	protected IStatus incrementalPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		if( !deployPackaged(moduleTree))
			PublishUtil.publishDelta(delta, getDeployPath(moduleTree), monitor);
		else if( delta.length > 0 )
			packModuleIntoJar(moduleTree[moduleTree.length-1], getDeployPath(moduleTree));
		
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID,
				IStatus.OK, "", null);
	}
	
	protected IStatus unpublish(IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		IPath path = getDeployPath(module);
		FileUtil.completeDelete(path.toFile());
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID,
				IStatus.OK, "", null);
	}

	protected IPath getDeployPath(IModule[] moduleTree) {
		IPath root = new Path( server.getDeployDirectory() );
		String type;
		for( int i = 0; i < moduleTree.length; i++ ) {
			type = moduleTree[i].getModuleType().getId();
			if( "jst.ear".equals(type)) 
				root = root.append(moduleTree[i].getProject().getName() + ".ear");
			else if( "jst.web".equals(type)) 
				root = root.append(moduleTree[i].getProject().getName() + ".war");
			else if( "jst.utility".equals(type) && i >= 1 && "jst.web".equals(moduleTree[i-1].getModuleType().getId())) 
				root = root.append("WEB-INF").append("lib").append(moduleTree[i].getProject().getName() + ".jar");			
			else
				root = root.append(moduleTree[i].getProject().getName() + ".jar");
		}
		return root;
	}
	
	protected boolean deployPackaged(IModule[] moduleTree) {
		if( moduleTree[moduleTree.length-1].getModuleType().getId().equals("jst.utility")) return true;
		return false;
	}
	
	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}

	/*
	 * Just package into a jar raw.  Don't think about it, just do it
	 */
	protected void packModuleIntoJar(IModule module, IPath destination)throws CoreException {
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
				//unhandled
			}
		}
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
