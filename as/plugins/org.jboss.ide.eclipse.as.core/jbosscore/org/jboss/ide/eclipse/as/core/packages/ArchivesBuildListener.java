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
package org.jboss.ide.eclipse.as.core.packages;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.other.IArchiveBuildListener;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.as.core.packages.PackageModuleFactory.PackagedModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.stripped.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.FileUtil;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ArchivesBuildListener implements IArchiveBuildListener {

	public static ArchivesBuildListener instance;
	public static final String DEPLOY_SERVERS = "org.jboss.ide.eclipse.as.core.model.PackagesListener.DeployServers";
	public static final String DEPLOY_AFTER_BUILD = "org.jboss.ide.eclipse.as.core.model.PackagesListener.DeployAfterBuild";
	
	public static ArchivesBuildListener getInstance() {
		if( instance == null ) {
			instance = new ArchivesBuildListener();
		}
		return instance;
	}
	
	public ArchivesBuildListener() {
		ArchivesModel.instance().addBuildListener(this);
	}
	
	public void cleanArchive(IArchive pkg) {
		PackagedModuleDelegate del = getModuleDelegate(pkg);
		del.reset();
	}


	public void finishedBuildingArchive(IArchive pkg) {
		if( pkg.isTopLevel() && new Boolean(pkg.getProperty(DEPLOY_AFTER_BUILD)).booleanValue()) {
			publish(pkg);
		}
	}

	public void fileRemoved(IArchive topLevelPackage, IArchiveFileSet fileset, IPath filePath) {
		// make absolute
		IPath filePath2 = ModelUtil.getBaseDestinationFile(fileset, filePath);
		PackagedModuleDelegate del = getModuleDelegate(topLevelPackage);
		del.fileRemoved(filePath2);
	}
	public void fileUpdated(IArchive topLevelPackage, IArchiveFileSet fileset, IPath filePath) {
		// make absolute
		IPath filePath2 = ModelUtil.getBaseDestinationFile(fileset, filePath);
		PackagedModuleDelegate del = getModuleDelegate(topLevelPackage);
		del.fileUpdated(filePath2);
	}
	
	// If we're supposed to auto-deploy, get on it
	protected static void publish(IArchive pkg) {
		String servers = pkg.getProperty(ArchivesBuildListener.DEPLOY_SERVERS);
		publish(pkg, servers, IServer.PUBLISH_INCREMENTAL);
	} 
	public static void publish(IArchive pkg, String servers, int publishType) {
		IModule[] module = getModule(pkg);
		if( module[0] == null ) return; 
		DeployableServerBehavior[] serverBehaviors = ArchivesBuildListener.getServers(servers);
		if( serverBehaviors != null ) {
			for( int i = 0; i < serverBehaviors.length; i++ ) {
				serverBehaviors[i].publishOneModule(publishType, module, ServerBehaviourDelegate.CHANGED, new NullProgressMonitor());
			}
		}
	}
	protected static IModule[] getModule(IArchive node) {
		ModuleFactory factory = ServerPlugin.findModuleFactory("org.jboss.ide.eclipse.as.core.PackageModuleFactory");
		IModule mod = factory.getModule(PackageModuleFactory.getID(node));
		return new IModule[] { mod };
	}
	protected static PackagedModuleDelegate getModuleDelegate(IArchive node) {
		IModule mod = getModule(node)[0];
		return (PackagedModuleDelegate)mod.loadAdapter(PackagedModuleDelegate.class, new NullProgressMonitor());
	}

	protected IDeployableServer getDeployableServerFromBehavior(DeployableServerBehavior dsb) {
		IServer server = dsb.getServer();
		IDeployableServer ids = (IDeployableServer)server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
		return ids;
	}

	public static DeployableServerBehavior[] getServers(String servers) {
		if( servers == null || "".equals(servers))
			return null;
		ArrayList list = new ArrayList();
		String[] byId = servers.split(",");
		for( int i = 0; i < byId.length; i++ ) {
			IServer server = ServerCore.findServer(byId[i]);
			if( server != null ) {
				Object o = server.loadAdapter(DeployableServerBehavior.class, new NullProgressMonitor());
				if( o != null ) {
					list.add((DeployableServerBehavior)o);
				}
			}
		}
		return (DeployableServerBehavior[]) list.toArray(new DeployableServerBehavior[list.size()]);
	}

	/*
	 * If a node is changing from exploded to imploded, or vice versa
	 * make sure to delete the pre-existing file or folder on the server. 
	 */
	public void packageBuildTypeChanged(IArchive topLevelPackage, boolean isExploded) {
		String servers = topLevelPackage.getProperty(ArchivesBuildListener.DEPLOY_SERVERS);
		DeployableServerBehavior[] serverBehaviors = ArchivesBuildListener.getServers(servers);
		if( serverBehaviors != null ) {
			IPath sourcePath, destPath;
			IDeployableServer depServer;
			for( int i = 0; i < serverBehaviors.length; i++ ) {
				sourcePath = topLevelPackage.getArchiveFilePath();
				depServer = getDeployableServerFromBehavior(serverBehaviors[i]);
				destPath = new Path(depServer.getDeployDirectory()).append(sourcePath.lastSegment());
				FileUtil.safeDelete(destPath.toFile());
				FileUtil.fileSafeCopy(sourcePath.toFile(), destPath.toFile());
			}
		}
	}

	public void buildFailed(IArchive pkg, IStatus status) {
		// TODO Auto-generated method stub
		
	}

	public void finishedBuild(IPath project) {
		// TODO Auto-generated method stub
		
	}

	public void finishedCollectingFileSet(IArchiveFileSet fileset) {
		// TODO Auto-generated method stub
		
	}

	public void startedBuild(IPath project) {
		// TODO Auto-generated method stub
		
	}

	public void startedBuildingArchive(IArchive pkg) {
		// TODO Auto-generated method stub
		
	}

	public void startedCollectingFileSet(IArchiveFileSet fileset) {
		// TODO Auto-generated method stub
	}
	public void startedBuildingPackage(IArchive pkg) {
		// TODO Auto-generated method stub
	}

	public void cleanProject(IPath project) {
	}


}
