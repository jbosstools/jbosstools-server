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
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.xpl.ModulePackager;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishUtil;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.FileUtil.FileUtilListener;
import org.jboss.ide.eclipse.as.core.util.FileUtil.IFileUtilListener;

/**
 * This class provides a default implementation for packaging different types of
 * flexible projects. It uses the built-in heirarchy of the projects to do so.
 * 
 * @author rob.stryker@jboss.com
 */
public class JstPublisher implements IJBossServerPublisher {


	protected IModuleResourceDelta[] delta;
	protected IDeployableServer server;
	protected int publishState = IServer.PUBLISH_STATE_NONE;


	public JstPublisher() {
	}

	protected String getModulePath(IModule[] module ) {
		String modulePath = "";
		for( int i = 0; i < module.length; i++ ) {
			modulePath += module[i].getName() + Path.SEPARATOR;
		}
		modulePath = modulePath.substring(0, modulePath.length()-1);
		return modulePath;
	}
	
	public IStatus publishModule(IServer server, IModule[] module, 
			int publishType, IModuleResourceDelta[] delta, IProgressMonitor monitor) throws CoreException {
		IStatus status = null;
		this.server = ServerConverter.getDeployableServer(server);
		this.delta = delta;
		
		boolean deleted = false;
		for( int i = 0; i < module.length; i++ ) {
			if( module[i] instanceof DeletedModule )
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
		IPath deployPath = getDeployPath(moduleTree);
		ModuleDelegate md = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, monitor);
		IModuleResource[] members = md.members();
 
		ArrayList<IStatus> list = new ArrayList<IStatus>();
		// if the module we're publishing is a project, not a binary, clean it's folder
		if( !(new Path(module.getName()).segmentCount() > 1 ))
			list.addAll(Arrays.asList(localSafeDelete(deployPath)));

		if( !deployPackaged(moduleTree) && !isBinaryObject(moduleTree))
			list.addAll(Arrays.asList(new PublishUtil(server.getServer()).publishFull(members, deployPath, monitor)));
		else if( isBinaryObject(moduleTree))
			list.addAll(Arrays.asList(copyBinaryModule(moduleTree)));
		else
			list.addAll(Arrays.asList(packModuleIntoJar(moduleTree[moduleTree.length-1], deployPath)));
		
		
		// adjust timestamps
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				if( pathname.getAbsolutePath().toLowerCase().endsWith(".xml"))
					return true;
				return false;
			}
		};
		FileUtil.touch(filter, deployPath.toFile(), true);


		if( list.size() > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_FAIL, 
					"Full Publish Failed for module " + module.getName(), null);
			for( int i = 0; i < list.size(); i++ )
				ms.add(list.get(i));
			return ms;
		}


		publishState = IServer.PUBLISH_STATE_NONE;
		
		IStatus ret = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_SUCCESS, 
				countMembers(module) + " files modified in module " + module.getName(), null);
		return ret;
	}

	protected IStatus incrementalPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IStatus[] results = new IStatus[] {};
		IPath deployPath = getDeployPath(moduleTree);
		if( !deployPackaged(moduleTree) && !isBinaryObject(moduleTree))
			results = new PublishUtil(server.getServer()).publishDelta(delta, deployPath, monitor);
		else if( delta.length > 0 ) {
			if( isBinaryObject(moduleTree))
				results = copyBinaryModule(moduleTree);
			else
				results = packModuleIntoJar(moduleTree[moduleTree.length-1], deployPath);
		}
		if( results != null && results.length > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					"Incremental Publish Failed for module " + module.getName(), null);
			for( int i = 0; i < results.length; i++ )
				ms.add(results[i]);
			return ms;
		}
		IStatus ret = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_SUCCESS, 
				countChanges(delta) + " files modified in module " + module.getName(), null);
		return ret;
	}
	
	protected IStatus unpublish(IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		IModule mod = module[module.length-1];
		IStatus[] errors = localSafeDelete(getDeployPath(module));
		if( errors.length > 0 ) {
			publishState = IServer.PUBLISH_STATE_FULL;
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_REMOVE_FAIL,
					"Unable to delete module " + mod.getName(), new Exception("Some files were not removed from the server"));
			for( int i = 0; i < errors.length; i++ )
				ms.addAll(errors[i]);
			throw new CoreException(ms);
		}
		IStatus ret = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_REMOVE_SUCCESS, 
				mod.getName() + " removed.", null);
		return ret;
	}

	protected IPath getDeployPath(IModule[] moduleTree) {
		IPath root = new Path( server.getDeployFolder() );
		String type, name;
		for( int i = 0; i < moduleTree.length; i++ ) { 
			type = moduleTree[i].getModuleType().getId();
			name = moduleTree[i].getName();
			
			if( new Path(name).segmentCount() > 1 )
				// we strongly suspect this is a binary object and not a project
				return root.append(new Path(name).lastSegment());
			if( "jst.ear".equals(type)) 
				root = root.append(name + ".ear");
			else if( "jst.web".equals(type)) 
				root = root.append(name + ".war");
			else if( "jst.utility".equals(type) && i == 1 && "jst.ear".equals(moduleTree[i-1].getModuleType().getId()))
				root = root.append(getUtilityInEarProjectURI(moduleTree));
			else if( "jst.utility".equals(type) && i >= 1 && "jst.web".equals(moduleTree[i-1].getModuleType().getId())) 
				root = root.append("WEB-INF").append("lib").append(name + ".jar");			
			else if( "jst.connector".equals(type)) {
				root = root.append(name + ".rar");
			} else if( "jst.jboss.esb".equals(type)){
				root = root.append(name + ".esb");
			}else
				root = root.append(name + ".jar");
		}
		return root;
	}
	
	
	// hack to fix JBIDE-4629, the case of a util project directly inside an ear project
	protected String getUtilityInEarProjectURI(IModule[] moduleTree) {
		IProject earProject = moduleTree[0].getProject();
		IVirtualComponent earComponent = ComponentCore.createComponent(earProject);
    	IVirtualComponent utilComponent = ComponentCore.createComponent(moduleTree[1].getProject());
    	String aURI = null;
    	if (utilComponent!=null && earComponent!=null && J2EEProjectUtilities.isEARProject(earProject)) {
			EARArtifactEdit earEdit = null;
			try {
				earEdit = EARArtifactEdit.getEARArtifactEditForRead(earComponent);
				if (earEdit != null) {
					IVirtualReference [] refs = earComponent.getReferences();
					for(int i=0; i<refs.length; i++){
						if(refs[i].getReferencedComponent().equals(utilComponent)){
							IPath path = refs[i].getRuntimePath();
							path = path == null ? new Path("/") : path;
							return path.append(refs[i].getArchiveName()).toString();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (earEdit != null)
					earEdit.dispose();
			}
    	}

		
		return moduleTree[1].getName() + ".jar";
	}
	
	protected boolean isBinaryObject(IModule[] moduleTree) {
		String name;
		for( int i = 0; i < moduleTree.length; i++ ) {
			name = moduleTree[i].getName();
			if( new Path(name).segmentCount() > 1 )
				// we strongly suspect this is a binary object and not a project
				return true;
		}
		return false;
	}
	
	protected IStatus[] copyBinaryModule(IModule[] moduleTree) {
		try {
			IPath deployPath = getDeployPath(moduleTree);
			FileUtilListener listener = new FileUtilListener();
			ModuleDelegate deployable =(ModuleDelegate)moduleTree[moduleTree.length-1].loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
			IModuleResource[] members = deployable.members();
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
						"Could not publish module " + moduleTree[moduleTree.length-1], null);
				return new IStatus[] {s};
			}
		} catch( CoreException ce ) {
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_COPY_BINARY_FAIL,
					"Could not publish module " + moduleTree[moduleTree.length-1], ce);
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
        Assert.isTrue(!deployPath.toFile().equals(new Path(serverDeployFolder).toFile()), "An attempt to delete your entire deploy folder has been prevented. This should never happen");
        final ArrayList<IStatus> status = new ArrayList<IStatus>();
		IFileUtilListener listener = new IFileUtilListener() {
			public void fileCopied(File source, File dest, boolean result,Exception e) {}
			public void fileDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FILE_DELETE_FAIL, 
							"Attempt to delete " + file.getAbsolutePath() + " failed",e));
				}
			}
			public void folderDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FILE_DELETE_FAIL,
							"Attempt to delete " + file.getAbsolutePath() + " failed",e));
				}
			} 
		};
		FileUtil.safeDelete(deployPath.toFile(), listener);
		return (IStatus[]) status.toArray(new IStatus[status.size()]);
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
	
	protected int countChanges(IModuleResourceDelta[] deltas) {
		IModuleResource res;
		int count = 0;
		if( deltas == null ) return 0;
		for( int i = 0; i < deltas.length; i++ ) {
			res = deltas[i].getModuleResource();
			if( res != null && res instanceof IModuleFile)
				count++;
			count += countChanges(deltas[i].getAffectedChildren());
		}
		return count;
	}

	protected int countMembers(IModule module) {
		try {
			ModuleDelegate delegate = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
			return delegate == null ? 0 : countMembers(delegate.members());
		} catch( CoreException ce ) {}
		return 0;
	}
	protected int countMembers(IModuleResource[] resources) {
		int count = 0;
		if( resources == null ) return 0;
		for( int i = 0; i < resources.length; i++ ) {
			if( resources[i] instanceof IModuleFile ) {
				count++;
			} else if( resources[i] instanceof IModuleFolder ) {
				count += countMembers(((IModuleFolder)resources[i]).members());
			}
		}
		return count;
	}

	public boolean accepts(IServer server, IModule[] module) {
		return ModuleCoreNature.isFlexibleProject(module[0].getProject());
	}
}
