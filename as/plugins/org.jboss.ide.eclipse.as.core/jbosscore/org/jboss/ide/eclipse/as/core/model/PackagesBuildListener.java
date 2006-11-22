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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogRoot;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackagesBuildListener;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackagesBuildListener implements IPackagesBuildListener {

	public static PackagesBuildListener instance;

	
	
	private HashMap projectServerToEvent = new HashMap();
	public PackagesBuildListener() {
		if( instance != null ) {
			instance.remove();
		}
		instance = this;
		PackagesCore.addPackagesBuildListener(this);
	}
	
	public void remove() {
		PackagesCore.removePackagesBuildListener(this);
	}
	

	

	public void startedBuildingPackage(IPackage pkg) {
		IServer[] servers = getServersWhoCare(pkg);
		for( int i = 0; i < servers.length; i++ ) {
			ProjectServerPair projectKey = new ProjectServerPair(pkg.getProject(), servers[i]);
			EventLogTreeItem item = (EventLogTreeItem)projectServerToEvent.get(projectKey);
			if( item == null ) {
				EventLogRoot root = EventLogModel.getModel(servers[i]).getRoot();
				projectServerToEvent.put(pkg, root);
				item = root;
				EventLogModel.markChanged(root);
			}
			
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(item, PACKAGE_BUILD_STARTED);
			child.setProperty("package.name", pkg.getName());
			
			EventLogModel.markChanged(item);
		}
	}

	public void finishedBuildingPackage(IPackage pkg) {
		IServer[] servers = getServersWhoCare(pkg);
		for( int i = 0; i < servers.length; i++ ) {
			ProjectServerPair projectKey = new ProjectServerPair(pkg.getProject(), servers[i]);
			EventLogTreeItem item = (EventLogTreeItem)projectServerToEvent.get(projectKey);
			if( item == null ) {
				item = (EventLogTreeItem)projectServerToEvent.get(pkg);
				if( item == null ) {
					EventLogRoot root = EventLogModel.getModel(servers[i]).getRoot();
					item = root;
					projectServerToEvent.put(pkg, item);
					EventLogModel.markChanged(root);
				}
			}
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(item, PACKAGE_BUILD_FINISHED);
			child.setProperty("package.name", pkg.getName());
			
			EventLogModel.markChanged(item);
			projectServerToEvent.remove(pkg);
		}
	}

	public void buildFailed(IPackage pkg, IStatus status) {
		IServer[] servers = getServersWhoCare(pkg);
		for( int i = 0; i < servers.length; i++ ) {
			ProjectServerPair projectKey = new ProjectServerPair(pkg.getProject(), servers[i]);
			EventLogTreeItem item = (EventLogTreeItem)projectServerToEvent.get(projectKey);
			if( item == null ) {
				item = (EventLogTreeItem)projectServerToEvent.get(pkg);
				if( item == null ) {
					EventLogRoot root = EventLogModel.getModel(servers[i]).getRoot();
					item = root; 
					projectServerToEvent.put(pkg, item);
					EventLogModel.markChanged(root);
				}
			}
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(item, PACKAGE_BUILD_FINISHED);
			child.setProperty("package.name", pkg.getName());
			
			EventLogModel.markChanged(item);
			projectServerToEvent.remove(pkg);
		}
	}

	public void startedCollectingFileSet(IPackageFileSet fileset) {
	}

	public void finishedCollectingFileSet(IPackageFileSet fileset) {
	}

	
	public void startedBuild(IProject project) {
		IServer[] serversThatCare = getServersWhoCare(project);
		
		// Create a top level event for each of these guys who care
		for( int i = 0; i < serversThatCare.length; i++ ) {
			ProjectServerPair key = new ProjectServerPair(project, serversThatCare[i]);
			EventLogRoot root = EventLogModel.getModel(serversThatCare[i]).getRoot();
			projectServerToEvent.put(key, root);
			
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(root, PROJECT_BUILD_STARTED);
			child.setProperty("project.name", project.getName());
			
			EventLogModel.markChanged(root);
		}
	}


	public void finishedBuild(IProject project) {
		IServer[] serversThatCare = getServersWhoCare(project);
		
		// Create a top level event for each of these guys who care
		for( int i = 0; i < serversThatCare.length; i++ ) {
			ProjectServerPair key = new ProjectServerPair(project, serversThatCare[i]);
			EventLogTreeItem buildRoot = (EventLogTreeItem)projectServerToEvent.get(key); 
			PackagesBuildListenerEventLogItem val = 
				new PackagesBuildListenerEventLogItem(buildRoot, PROJECT_BUILD_FINISHED);
			val.setProperty("project.name", project.getName());
			EventLogModel.markChanged(buildRoot);
			
			// cleanup
			projectServerToEvent.remove(key);
		}
	}

	
	
	
	
	
	
	/*
	 * If a server cares about any one package, then it cares the building is starting
	 */
	protected IServer[] getServersWhoCare(IProject project) {
		Set set = new TreeSet();
		IPackage[] packs = PackagesCore.getProjectPackages(project, new NullProgressMonitor());
		for( int i = 0; i < packs.length; i++ )
			set.addAll(Arrays.asList(getServersWhoCare(packs[i])));
		return (IServer[]) set.toArray(new IServer[set.size()]);
	}
	
	// TODO later
	protected IServer[] getServersWhoCare(IPackage pack) {
		return JBossServerCore.getIServerJBossServers();
	}
	
	public static final String EVENT_TYPE = "org.jboss.ide.eclipse.as.core.model.Packages";
	
	public static final String PROJECT_BUILD_STARTED = "org.jboss.ide.eclipse.as.core.model.Packages.projectBuildStarted";
	public static final String PROJECT_BUILD_FINISHED = "org.jboss.ide.eclipse.as.core.model.Packages.projectBuildFinished";
	public static final String PACKAGE_BUILD_STARTED = "org.jboss.ide.eclipse.as.core.model.Packages.packageBuildStarted";
	public static final String PACKAGE_BUILD_FINISHED = "org.jboss.ide.eclipse.as.core.model.Packages.packageBuildFinished";
	public static final String PACKAGE_BUILD_FAILED = "org.jboss.ide.eclipse.as.core.model.Packages.packageBuildFinished";
	
	
	protected class PackagesBuildListenerEventLogItem extends EventLogTreeItem {

		public PackagesBuildListenerEventLogItem(SimpleTreeItem parent,
				String specificType) {
			super(parent, EVENT_TYPE, specificType);
			setProperty(DATE, new Long(new Date().getTime()));
		}
		
	}
	
	protected class ProjectServerPair {
		public IProject project;
		public IServer server;
		public ProjectServerPair(IProject project, IServer server) {
			this.project = project; this.server = server;
		}
		public int hashCode() {
			return project.hashCode() + server.hashCode();
		}
		public boolean equals(Object other) {
			if( !(other instanceof ProjectServerPair )) {
				return false;
			}
			ProjectServerPair p2 = (ProjectServerPair)other;
			return p2.project == project && p2.server == server;
		}
	}
	
}
