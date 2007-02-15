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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogRoot;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.module.PackageModuleFactory;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.stripped.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackagesBuildListener;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;

/**
 *
 * @author rob.stryker@jboss.com
 * This class is teh suck. I dont even know whether to keep it
 */
public class PackagesBuildListener implements IPackagesBuildListener {
//public class PackagesBuildListener {

	public static PackagesBuildListener instance;
	public static final String DEPLOY_SERVERS = "org.jboss.ide.eclipse.as.core.model.PackagesBuildListener.DeployServers";
	public static final String AUTO_DEPLOY = "org.jboss.ide.eclipse.as.core.model.PackagesBuildListener.AutoDeploy";
	
	public static PackagesBuildListener getInstance() {
		if( instance == null ) {
			instance = new PackagesBuildListener();
		}
		return instance;
	}
	public PackagesBuildListener() {
		PackagesCore.addPackagesBuildListener(this);
	}
	
	public void remove() {
		PackagesCore.removePackagesBuildListener(this);
	}

	public void buildFailed(IPackage pkg, IStatus status) {
		System.out.println("build failed");
	}

	public void finishedBuild(IProject project) {
		System.out.println("finished build");
	}

	public void finishedBuildingPackage(IPackage pkg) {
		
		if( new Boolean(pkg.getProperty(AUTO_DEPLOY)).booleanValue()) {
			publish(pkg);
		}
	}

	public static void publish(IPackage pkg) {
		String servers = pkg.getProperty(PackagesBuildListener.DEPLOY_SERVERS);
		IModule[] module = getModule(pkg);
		if( module[0] == null ) return; 
		DeployableServerBehavior[] serverBehaviors = PackagesBuildListener.getServers(servers);
		if( serverBehaviors != null ) {
			for( int i = 0; i < serverBehaviors.length; i++ ) {
				serverBehaviors[i].publishOneModule(IServer.PUBLISH_FULL, module, ServerBehaviourDelegate.ADDED, new NullProgressMonitor());
			}
		}
	}
	protected static IModule[] getModule(IPackage node) {
		ModuleFactory factory = ServerPlugin.findModuleFactory("org.jboss.ide.eclipse.as.core.PackageModuleFactory");
		return new IModule[] { factory.getModule(PackageModuleFactory.getID(node)) };
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

	public void finishedCollectingFileSet(IPackageFileSet fileset) {
		System.out.println("finished collecting fileset");
	}

	public void startedBuild(IProject project) {
		System.out.println("build started");
	}

	public void startedBuildingPackage(IPackage pkg) {
		System.out.println("started building package");
	}

	public void startedCollectingFileSet(IPackageFileSet fileset) {
		System.out.println("started collecting fileset");
	}

}
