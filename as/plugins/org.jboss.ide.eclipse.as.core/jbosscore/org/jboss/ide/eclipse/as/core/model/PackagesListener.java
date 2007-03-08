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

import org.eclipse.core.resources.IProject;
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
import org.jboss.ide.eclipse.as.core.module.PackageModuleFactory;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.stripped.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.packages.core.model.AbstractPackagesBuildListener;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackageNode;
import org.jboss.ide.eclipse.packages.core.model.IPackagesModelListener;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.core.project.build.TruezipUtil;
import org.jboss.ide.eclipse.core.util.ResourceUtil;

import de.schlichtherle.io.ArchiveDetector;
import de.schlichtherle.io.File;

/**
 *
 * @author rob.stryker@jboss.com
 * This class is teh suck. I dont even know whether to keep it
 */
public class PackagesListener extends AbstractPackagesBuildListener implements IPackagesModelListener {

	public static PackagesListener instance;
	public static final String DEPLOY_SERVERS = "org.jboss.ide.eclipse.as.core.model.PackagesListener.DeployServers";
	public static final String DEPLOY_AFTER_BUILD = "org.jboss.ide.eclipse.as.core.model.PackagesListener.DeployAfterBuild";
	
	public static PackagesListener getInstance() {
		if( instance == null ) {
			instance = new PackagesListener();
		}
		return instance;
	}
	
	//Keeping track of build changes
	private HashMap changesOrAdditions = new HashMap();
	private HashMap removals = new HashMap();
	
	
	public PackagesListener() {
		PackagesCore.addPackagesBuildListener(this);
	}
	
	public void startedBuildingPackage(IPackage pkg) {
		if( pkg.isTopLevel() ) {
			changesOrAdditions.put(pkg, new ArrayList());
			removals.put(pkg, new ArrayList());
		}
	}

	public void fileRemoved(IPackage topLevelPackage, IPackageFileSet fileset, IPath filePath) {
		// make absolute
		IPath filePath2 = makeAbsolute(filePath, topLevelPackage); // change
		ArrayList removes = (ArrayList)removals.get(topLevelPackage);
		if( !removes.contains(filePath2)) removes.add(filePath2);
	}
	public void fileUpdated(IPackage topLevelPackage, IPackageFileSet fileset, IPath filePath) {
		// make absolute
		IPath filePath2 = makeAbsolute(filePath, topLevelPackage); // change
		ArrayList changes = (ArrayList)changesOrAdditions.get(topLevelPackage);
		if( !changes.contains(filePath2)) changes.add(filePath2);
	}

	public IPath makeAbsolute(IPath local, IPackage topLevelPackage) {
		IPath file = PackagesCore.getBaseFile(local);
		return ResourceUtil.makeAbsolute(file, topLevelPackage.isDestinationInWorkspace());
	}
	public void finishedBuildingPackage(IPackage pkg) {
		if( pkg.isTopLevel() && new Boolean(pkg.getProperty(DEPLOY_AFTER_BUILD)).booleanValue()) {
			publish(pkg);
			// then clean up what's been changed
			changesOrAdditions.remove(pkg);
			removals.remove(pkg);
		}
	}


	
	// If we're supposed to auto-deploy, get on it
	protected static void publish(IPackage pkg) {
		String servers = pkg.getProperty(PackagesListener.DEPLOY_SERVERS);
		publish(pkg, servers);
	} 
	public static void publish(IPackage pkg, String servers) {
		IModule[] module = getModule(pkg);
		if( module[0] == null ) return; 
		DeployableServerBehavior[] serverBehaviors = PackagesListener.getServers(servers);
		if( serverBehaviors != null ) {
			for( int i = 0; i < serverBehaviors.length; i++ ) {
				serverBehaviors[i].publishOneModule(IServer.PUBLISH_INCREMENTAL, module, ServerBehaviourDelegate.CHANGED, new NullProgressMonitor());
			}
		}
	}
	protected static IModule[] getModule(IPackage node) {
		ModuleFactory factory = ServerPlugin.findModuleFactory("org.jboss.ide.eclipse.as.core.PackageModuleFactory");
		return new IModule[] { factory.getModule(PackageModuleFactory.getID(node)) };
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


	
	// should be called from the publisher to figure out what's changed
	public IPath[] getUpdatedFiles(IPackage pkg) {
		ArrayList list = (ArrayList)changesOrAdditions.get(pkg);
		return list == null ? new IPath[0] : (IPath[]) list.toArray(new IPath[list.size()]);
	}
	public IPath[] getRemovedFiles(IPackage pkg) {
		ArrayList list = (ArrayList)removals.get(pkg);
		return list == null ? new IPath[0] : (IPath[]) list.toArray(new IPath[list.size()]);
	}

	/*
	 * If a node is changing from exploded to imploded, or vice versa
	 * make sure to delete the pre-existing file or folder on the server. 
	 */
	public void packageNodeChanged(IPackageNode changed) {
		if (changed.getNodeType() == IPackageNode.TYPE_PACKAGE
			|| changed.getNodeType() == IPackageNode.TYPE_PACKAGE_REFERENCE)
		{
			IPackage pkg = (IPackage) changed;
			File packageFile = TruezipUtil.getPackageFile(pkg);
			
			if ( (packageFile.getDelegate().isFile() && pkg.isExploded()) 
					|| (packageFile.getDelegate().isDirectory() && !pkg.isExploded())) {
				
				String servers = pkg.getProperty(PackagesListener.DEPLOY_SERVERS);
				DeployableServerBehavior[] serverBehaviors = PackagesListener.getServers(servers);
				if( serverBehaviors != null ) {
					IPath sourcePath, destPath;
					IDeployableServer depServer;
					for( int i = 0; i < serverBehaviors.length; i++ ) {
						sourcePath = pkg.getPackageFilePath();
						depServer = getDeployableServerFromBehavior(serverBehaviors[i]);
						destPath = new Path(depServer.getDeployDirectory()).append(sourcePath.lastSegment());
						boolean success = FileUtil.safeDelete(destPath.toFile());
					}
				}
				
			}
		}
	}
	
	public void packageNodeAdded(IPackageNode added) {	}
	public void packageNodeAttached(IPackageNode attached) {	}
	public void packageNodeRemoved(IPackageNode removed) {	}
	public void projectRegistered(IProject project) { }
}
