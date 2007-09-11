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
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.eclipse.wst.server.core.util.PublishUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.NestedPublishInfo;
import org.jboss.ide.eclipse.as.core.server.xpl.ModulePackager;
import org.jboss.ide.eclipse.as.core.util.FileUtil;

/**
 * This class provides a default implementation for packaging different types of
 * flexible projects. It uses the built-in heirarchy of the projects to do so.
 * 
 * @author rob.stryker@jboss.com
 */
public class JstPublisher extends PackagesPublisher {

	public static final int BUILD_FAILED_CODE = 100;
	public static final int PACKAGE_UNDETERMINED_CODE = 101;

	protected IModuleResourceDelta[] delta;

	public JstPublisher(IServer server, EventLogTreeItem context) {
		super(server, context);
	}

	public void setDelta(IModuleResourceDelta[] delta) {
		this.delta = delta;
	}

	public IStatus publishModule(int kind, int deltaKind,
			int modulePublishState, IModule module, IProgressMonitor monitor)
			throws CoreException {
		IStatus status = null;

		IPath root = new Path(server.getDeployDirectory());
		if (ServerBehaviourDelegate.REMOVED == deltaKind) {
			status = unpublish(server, module, monitor);
		} else if (kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN) {
			status = fullPublishPackIntoFolder(module, root, monitor);	
		} else if (kind == IServer.PUBLISH_INCREMENTAL) {
			status = incrementalPublish(new ArrayList(), root, module, monitor);
		} 
		return status;
	}
		
	protected void fullWebPublish(IModule module, IPath root, IProgressMonitor monitor ) throws CoreException {
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
		IModuleResource[] members = md.members();
		IWebModule webmodule = (IWebModule)module.loadAdapter(IWebModule.class, monitor);
		IPath moduleDeployPath = root.append(webmodule.getContextRoot() + ".war");
		FileUtil.safeDelete(moduleDeployPath.toFile());
		PublishUtil.publishSmart(members, moduleDeployPath, monitor);
		IWebModule webModule = (IWebModule)module.loadAdapter(IWebModule.class, monitor);
		IModule[] childModules = webModule.getModules();
		for (int i = 0; i < childModules.length; i++) {
			IModule module2 = childModules[i];
			packModuleIntoJar(module, webModule.getURI(module2), moduleDeployPath);
		}
	}
	protected void fullEarPublish(IModule module, IPath root, IProgressMonitor monitor) throws CoreException {
		if( module instanceof DeletedModule ) {
			// TODO FIX ME
			return;
		} 
		
		
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
		IModuleResource[] members = md.members();
		IEnterpriseApplication earModule = (IEnterpriseApplication)module.loadAdapter(IEnterpriseApplication.class, monitor);
		IPath moduleDeployPath = root.append(module.getProject().getName() + ".ear");
		FileUtil.safeDelete(moduleDeployPath.toFile());
		PublishUtil.publishFull(members, moduleDeployPath, monitor);
		IModule[] childModules = earModule.getModules();
		for (int i = 0; i < childModules.length; i++) {
			IModule module2 = childModules[i];
			String uri = earModule.getURI(module2);
			if(uri==null){
				IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0,	"unable to assemble module null uri",null ); //$NON-NLS-1$
				throw new CoreException(status);
			}
			if( module2.getModuleType().getId().equals("jst.utility")) {
				packModuleIntoJar(module2, uri, moduleDeployPath);
			} else {
				fullPublishPackIntoFolder(module2, moduleDeployPath, monitor);
			}
		}
	}
	
	protected IStatus fullPublishPackIntoFolder(IModule module, IPath root, IProgressMonitor monitor) throws CoreException {
		if( module.getModuleType().getId().equals("jst.web")) {
			fullWebPublish(module, root, monitor);
		} else if( module.getModuleType().getId().equals("jst.ear")) {
			fullEarPublish(module, root, monitor);
		} else {
			ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
			IModuleResource[] members = md.members();
			IPath moduleDeployPath = root.append(module.getProject().getName() + ".jar");
			FileUtil.safeDelete(moduleDeployPath.toFile());
			PublishUtil.publishSmart(members, moduleDeployPath, monitor);
		}
		return null;
	}

	private boolean hasDelta( IModule module, ArrayList moduleTree ) {
        return getDelta(module, moduleTree).length > 0;
    }
	
	public IModuleResourceDelta[] getDelta(IModule module, ArrayList moduleTree) {
		IModuleResourceDelta[] deltas;
		if( moduleTree.size() == 0 ) {
	        final IModule[] modules ={module}; 
	        deltas = ((Server)server.getServer()).getPublishedResourceDelta( modules );
		} else {
			deltas = NestedPublishInfo.getDefault().getServerPublishInfo(server.getServer()).getDelta(moduleTree, module);
		}
		return deltas;
	}

	
	protected void packModuleIntoJar(IModule module, String deploymentUnitName, IPath destination)throws CoreException {
		String dest = destination.append(deploymentUnitName).toString();
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
	

	protected IStatus incrementalPublish(ArrayList moduleTree, IPath root, IModule module, IProgressMonitor monitor) throws CoreException {
		if( module.getModuleType().getId().equals("jst.web")) {
			incrementalWarPublish(moduleTree, module, root, monitor);
		} else if( module.getModuleType().getId().equals("jst.ear")) {
			incrementalEarPublish(moduleTree, module, root, monitor);
		} else {
			// cannot incrementally publish something unknown 
			// just package it
			packModuleIntoJar(module, module.getName() + ".jar", root);
		}
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID,
				IStatus.OK, "", null);
	}
	
	protected void incrementalWarPublish(ArrayList moduleTree, IModule module, IPath root, IProgressMonitor monitor) throws CoreException {
		ArrayList newTree = new ArrayList();
		newTree.addAll(moduleTree); newTree.add(module);

		IModule[] modules = new IModule[] {module};
        IModuleResourceDelta[] deltas = getDelta(module, moduleTree);
        //((Server)server.getServer()).getPublishedResourceDelta( modules );
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
		IModuleResource[] members = md.members();
		IWebModule webmodule = (IWebModule)module.loadAdapter(IWebModule.class, monitor);
		IPath moduleDeployPath = root.append(webmodule.getContextRoot() + ".war");
        PublishUtil.publishDelta(deltas, moduleDeployPath, monitor);
		IWebModule webModule = (IWebModule)module.loadAdapter(IWebModule.class, monitor);
		IModule[] childModules = webModule.getModules();
		for (int i = 0; i < childModules.length; i++) {
			IModule module2 = childModules[i];
			// if a lib .jar needs to be repacked, repack the whole thing
			if( hasDelta(module2, newTree))
				packModuleIntoJar(module, webModule.getURI(module2), moduleDeployPath);
		}
		if( moduleTree.size() > 0 ) {
			// published it
			NestedPublishInfo.getDefault().getServerPublishInfo(server.getServer()).getPublishInfo(moduleTree, module).setResources(md.members());
		}
	}
	
	protected void incrementalEarPublish(ArrayList moduleTree, IModule module, IPath root, IProgressMonitor monitor) throws CoreException {
		ArrayList newTree = new ArrayList();
		newTree.addAll(moduleTree); 
		newTree.add(module);

        IModuleResourceDelta[] deltas = getDelta(module, moduleTree); 
        //((Server)server.getServer()).getPublishedResourceDelta( modules );
		IEnterpriseApplication earModule = (IEnterpriseApplication)module.loadAdapter(IEnterpriseApplication.class, monitor);
		IPath moduleDeployPath = root.append(module.getProject().getName() + ".ear");
        PublishUtil.publishDelta(deltas, moduleDeployPath, monitor);
		IModule[] childModules = earModule.getModules();
		for (int i = 0; i < childModules.length; i++) {
			IModule childModule = childModules[i];
			String uri = earModule.getURI(childModule);
			if(uri==null){
				IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0,	"unable to assemble module null uri",null ); //$NON-NLS-1$
				throw new CoreException(status);
			}
			
			incrementalPublish(newTree, root, childModule, monitor);
		}

	}

	protected IStatus unpublish(IDeployableServer jbServer, IModule module,
			IProgressMonitor monitor) throws CoreException {

		IPath root = new Path(jbServer.getDeployDirectory());
		if( module.getModuleType().getId().equals("jst.web")) {
			// copy the module first
			ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
			IModuleResource[] members = md.members();
			IWebModule webmodule = (IWebModule)module.loadAdapter(IWebModule.class, monitor);
			IPath moduleDeployPath = root.append(webmodule.getContextRoot() + ".war");
			FileUtil.completeDelete(moduleDeployPath.toFile());
		} else if( module.getModuleType().getId().equals("jst.ear")) {
			ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
			IModuleResource[] members = md.members();
			IEnterpriseApplication earModule = (IEnterpriseApplication)module.loadAdapter(IEnterpriseApplication.class, monitor);
			IPath moduleDeployPath = root.append(module.getProject().getName() + ".ear");
			FileUtil.completeDelete(moduleDeployPath.toFile());
		}

		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID,
				IStatus.OK, "", null);
	}

}
