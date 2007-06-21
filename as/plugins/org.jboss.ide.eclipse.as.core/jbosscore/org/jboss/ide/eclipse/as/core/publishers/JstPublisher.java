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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

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
import org.eclipse.wst.server.core.internal.ModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.packages.ModulePackageTypeConverter;
import org.jboss.ide.eclipse.as.core.packages.ProjectArchiveStorer;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.PublishEvent;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.PublisherFileUtilListener;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;

/**
 *  This class provides a default implementation for 
 *  packaging different types of flexible projects. It uses 
 *  the built-in heirarchy of the projects to do so. 
 *  
 * @author rob.stryker@jboss.com
 */
public class JstPublisher extends PackagesPublisher {
	
	private static HashMap moduleToArchiveMap = new HashMap();
	
	public static final int BUILD_FAILED_CODE = 100;
	public static final int PACKAGE_UNDETERMINED_CODE = 101;
	
	protected IModuleResourceDelta[] delta;

	public JstPublisher(IServer server, EventLogTreeItem context) {
		super(server, context);
	}


	public void setDelta(IModuleResourceDelta[] delta) {
		this.delta = delta;
	}

	public IStatus publishModule(int kind, int deltaKind, int modulePublishState,
			IModule module, IProgressMonitor monitor) throws CoreException {
		IStatus status = null;
		if(ServerBehaviourDelegate.REMOVED == deltaKind){
        	status = unpublish(server, module, kind, deltaKind, modulePublishState, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind || kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	status = publish(server, module, kind, deltaKind, modulePublishState, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind && kind == IServer.PUBLISH_INCREMENTAL ){
        	status = publish(server, module, kind, deltaKind, modulePublishState, monitor);
        }
		return status;
	}

	protected IStatus publish(IDeployableServer jbServer, IModule module, int kind, 
						int deltaKind, int modulePublishState, IProgressMonitor monitor) throws CoreException {
		PublishEvent event = PublisherEventLogger.createSingleModuleTopEvent(eventRoot, module, kind, deltaKind);
		EventLogModel.markChanged(eventRoot);
		boolean incremental = shouldPublishIncremental(module, kind, deltaKind, modulePublishState);

		IArchive topLevel = getTopPackage(module, jbServer.getDeployDirectory(), monitor);

		
		if( topLevel != null ) {
			try {
				if( !incremental ) 
					new ArchiveBuildDelegate().fullArchiveBuild(topLevel);
				else {
					Set addedChanged = createDefaultTreeSet();
					Set removed = createDefaultTreeSet();
					fillDelta(delta, addedChanged, removed);
					new ArchiveBuildDelegate().incrementalBuild(topLevel, addedChanged, removed);
				}
			} catch( Exception e ) {
				return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, BUILD_FAILED_CODE, "", e);
			}
		} else {
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, PACKAGE_UNDETERMINED_CODE, "", null);
		}
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IStatus.OK, "", null);
	}

	protected IStatus unpublish(IDeployableServer jbServer, IModule module, 
						int kind, int deltaKind, int modulePublishKind, IProgressMonitor monitor) throws CoreException {
		PublishEvent event = PublisherEventLogger.createSingleModuleTopEvent(eventRoot, module, kind, deltaKind);
		
		IArchive topLevel = getTopPackage(module, jbServer.getDeployDirectory(), monitor);
		if( topLevel != null ) {
			IPath path = topLevel.getArchiveFilePath();
			FileUtil.safeDelete(path.toFile(), new PublisherFileUtilListener(event));
		} else if( module.getProject() == null ){
			// this is a problem. All I know is the module name, aka project name. Not it's suffix on the server.
		} else {
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, PACKAGE_UNDETERMINED_CODE, "", null);
		}
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IStatus.OK, "", null);
	}


	protected IArchive getTopPackage(IModule module, String deployDir, IProgressMonitor monitor) {
		if( moduleToArchiveMap.containsKey(module.getId())) {
			return (IArchive)moduleToArchiveMap.get(module.getId());
		}
		
		IProject project = module.getProject();
		IArchive top = ProjectArchiveStorer.getArchiveFor(project);
		if( top == null ) {
			top = createTopPackage(module, deployDir, monitor);
			if( top != null ) {
				ProjectArchiveStorer.storeArchive(project, top);
				moduleToArchiveMap.put(module.getId(), top);
			}
		}
		return top;
	}
	
	protected IArchive createTopPackage(IModule module, String deployDir, IProgressMonitor monitor) {
		IArchiveType type = ModulePackageTypeConverter.getPackageTypeFor(module);
		if( type != null && module.getProject() != null ) {
    		IArchive topLevel = type.createDefaultConfiguration(module.getProject().getName(), monitor);
    		topLevel.setDestinationPath(new Path(deployDir));
    		topLevel.setInWorkspace(false);
    		return topLevel;
		} 
		return null;
	}
	
	protected void fillDelta(IModuleResourceDelta[] delta, Set addedChanged, Set removed) {
		for( int i = 0; i < delta.length; i++ ) {
			fillDelta(delta[i], addedChanged, removed);
		}
	}
	protected void fillDelta(IModuleResourceDelta delta, Set addedChanged, Set removed) {
		IModuleResourceDelta[] children = delta.getAffectedChildren();
		if( children != null ) {
			for( int i = 0; i < children.length; i++ ) {
				fillDelta(children[i], addedChanged, removed);
			}
		}
		handleResource(delta.getKind(), delta.getModuleResource(), addedChanged, removed);
	}
	
	protected void handleResource(int kind, IModuleResource resource, Set addedChanged, Set removed) {
		if( resource instanceof ModuleFile ) {
			ModuleFile mf = (ModuleFile)resource;
			IFile f = (IFile)resource.getAdapter(IFile.class);
			IPath p = null;
			if( f == null ) {
				IFile ifile = (IFile)resource.getAdapter(IFile.class);
				if( ifile != null )
					p = ifile.getLocation();
			} else {
				p = f.getLocation();
			}
			
			if( p != null ) {
				if( kind == IModuleResourceDelta.ADDED || kind == IModuleResourceDelta.CHANGED) {
					addedChanged.add(p);
				} else if( kind == IModuleResourceDelta.REMOVED) {
					removed.add(p);
				}
			}
		}
	}
	protected TreeSet createDefaultTreeSet() {
		return new TreeSet(new Comparator () {
			public int compare(Object o1, Object o2) {
				if (o1.equals(o2)) return 0;
				else return -1;
			}
		});		
	}

}
