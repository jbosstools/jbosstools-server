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
package org.jboss.ide.eclipse.as.core.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.module.PackageModuleFactory;
import org.jboss.ide.eclipse.as.core.module.PackageModuleFactory.PackagedModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.stripped.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.core.util.ResourceUtil;
import org.jboss.ide.eclipse.packages.core.model.AbstractPackagesBuildListener;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackagesListener extends AbstractPackagesBuildListener {

	public static PackagesListener instance;
	public static final String DEPLOY_SERVERS = "org.jboss.ide.eclipse.as.core.model.PackagesListener.DeployServers";
	public static final String DEPLOY_AFTER_BUILD = "org.jboss.ide.eclipse.as.core.model.PackagesListener.DeployAfterBuild";
	
	public static PackagesListener getInstance() {
		if( instance == null ) {
			instance = new PackagesListener();
		}
		return instance;
	}
	
	public PackagesListener() {
		PackagesCore.addPackagesBuildListener(this);
	}
	
	public void startedBuildingPackage(IPackage pkg) {
		System.out.println("starting pkg: " + pkg.getName());
	}

	public void fileRemoved(IPackage topLevelPackage, IPackageFileSet fileset, IPath filePath) {
		// make absolute
		IPath filePath2 = makeAbsolute(filePath, topLevelPackage); // change
		PackagedModuleDelegate del = getModuleDelegate(topLevelPackage);
		del.fileRemoved(filePath2);
	}
	public void fileUpdated(IPackage topLevelPackage, IPackageFileSet fileset, IPath filePath) {
		// make absolute
		IPath filePath2 = makeAbsolute(filePath, topLevelPackage); // change
		PackagedModuleDelegate del = getModuleDelegate(topLevelPackage);
		del.fileUpdated(filePath2);
	}

	public IPath makeAbsolute(IPath local, IPackage topLevelPackage) {
		IPath file = PackagesCore.getBaseFile(local);
		return ResourceUtil.makeAbsolute(file, topLevelPackage.isDestinationInWorkspace());
	}
	public void finishedBuildingPackage(IPackage pkg) {
		System.out.println("finishedBuildingPackage started: " + pkg.getName());
		if( pkg.isTopLevel() && new Boolean(pkg.getProperty(DEPLOY_AFTER_BUILD)).booleanValue()) {
			publish(pkg);
		}
		System.out.println("done publishing from finishedBuildingPackage: " + pkg.getName());
	}


	
	// If we're supposed to auto-deploy, get on it
	protected static void publish(IPackage pkg) {
		String servers = pkg.getProperty(PackagesListener.DEPLOY_SERVERS);
		publish(pkg, servers, IServer.PUBLISH_INCREMENTAL);
	} 
	public static void publish(IPackage pkg, String servers, int publishType) {
		IModule[] module = getModule(pkg);
		if( module[0] == null ) return; 
		DeployableServerBehavior[] serverBehaviors = PackagesListener.getServers(servers);
		if( serverBehaviors != null ) {
			for( int i = 0; i < serverBehaviors.length; i++ ) {
				serverBehaviors[i].publishOneModule(publishType, module, ServerBehaviourDelegate.CHANGED, new NullProgressMonitor());
			}
		}
	}
	protected static IModule[] getModule(IPackage node) {
		ModuleFactory factory = ServerPlugin.findModuleFactory("org.jboss.ide.eclipse.as.core.PackageModuleFactory");
		IModule mod = factory.getModule(PackageModuleFactory.getID(node));
		return new IModule[] { mod };
	}
	protected static PackagedModuleDelegate getModuleDelegate(IPackage node) {
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
	public void packageBuildTypeChanged(IPackage topLevelPackage, boolean isExploded) {
		String servers = topLevelPackage.getProperty(PackagesListener.DEPLOY_SERVERS);
		DeployableServerBehavior[] serverBehaviors = PackagesListener.getServers(servers);
		if( serverBehaviors != null ) {
			IPath sourcePath, destPath;
			IDeployableServer depServer;
			for( int i = 0; i < serverBehaviors.length; i++ ) {
				sourcePath = topLevelPackage.getPackageFilePath();
				depServer = getDeployableServerFromBehavior(serverBehaviors[i]);
				destPath = new Path(depServer.getDeployDirectory()).append(sourcePath.lastSegment());
				FileUtil.safeDelete(destPath.toFile());
				FileUtil.fileSafeCopy(sourcePath.toFile(), destPath.toFile());
			}
		}
	}
}
