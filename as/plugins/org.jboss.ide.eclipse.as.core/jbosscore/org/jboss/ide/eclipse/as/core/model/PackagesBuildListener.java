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
import java.util.Comparator;
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
 * This class is teh suck. I dont even know whether to keep it
 */
//public class PackagesBuildListener implements IPackagesBuildListener {
public class PackagesBuildListener {

//	public static PackagesBuildListener instance;
//
//	
//	
//	private HashMap projectServerToEvent = new HashMap();
//	public PackagesBuildListener() {
//		if( instance != null ) {
//			instance.remove();
//		}
//		instance = this;
//		PackagesCore.addPackagesBuildListener(this);
//	}
//	
//	public void remove() {
//		PackagesCore.removePackagesBuildListener(this);
//	}
//	
//
//	
//
//	public void startedBuildingPackage(IPackage pkg) {
//		IServer[] servers = getServersWhoCare(pkg);
//		for( int i = 0; i < servers.length; i++ ) {
//			ProjectServerPair projectKey = new ProjectServerPair(pkg.getProject(), servers[i]);
//			EventLogTreeItem item = (EventLogTreeItem)projectServerToEvent.get(projectKey);
//			if( item == null ) {
//				EventLogRoot root = EventLogModel.getModel(servers[i]).getRoot();
//				projectServerToEvent.put(pkg, root);
//				item = root;
//				EventLogModel.markChanged(root);
//			}
//			
//			PackagesBuildListenerEventLogItem child = 
//				new PackagesBuildListenerEventLogItem(item, PACKAGE_BUILD_STARTED);
//			child.setProperty("package.name", pkg.getName());
//			
//			EventLogModel.markChanged(item);
//		}
//	}
//
//	public void finishedBuildingPackage(IPackage pkg) {
//		IServer[] servers = getServersWhoCare(pkg);
//		for( int i = 0; i < servers.length; i++ ) {
//			ProjectServerPair projectKey = new ProjectServerPair(pkg.getProject(), servers[i]);
//			EventLogTreeItem item = (EventLogTreeItem)projectServerToEvent.get(projectKey);
//			if( item == null ) {
//				item = (EventLogTreeItem)projectServerToEvent.get(pkg);
//				if( item == null ) {
//					EventLogRoot root = EventLogModel.getModel(servers[i]).getRoot();
//					item = root;
//					projectServerToEvent.put(pkg, item);
//					EventLogModel.markChanged(root);
//				}
//			}
//			PackagesBuildListenerEventLogItem child = 
//				new PackagesBuildListenerEventLogItem(item, PACKAGE_BUILD_FINISHED);
//			child.setProperty("package.name", pkg.getName());
//			
//			EventLogModel.markChanged(item);
//			projectServerToEvent.remove(pkg);
//		}
//	}
//
//	public void buildFailed(IPackage pkg, IStatus status) {
//		IServer[] servers = getServersWhoCare(pkg);
//		for( int i = 0; i < servers.length; i++ ) {
//			ProjectServerPair projectKey = new ProjectServerPair(pkg.getProject(), servers[i]);
//			EventLogTreeItem item = (EventLogTreeItem)projectServerToEvent.get(projectKey);
//			if( item == null ) {
//				item = (EventLogTreeItem)projectServerToEvent.get(pkg);
//				if( item == null ) {
//					EventLogRoot root = EventLogModel.getModel(servers[i]).getRoot();
//					item = root; 
//					projectServerToEvent.put(pkg, item);
//					EventLogModel.markChanged(root);
//				}
//			}
//			PackagesBuildListenerEventLogItem child = 
//				new PackagesBuildListenerEventLogItem(item, PACKAGE_BUILD_FINISHED);
//			child.setProperty("package.name", pkg.getName());
//			
//			EventLogModel.markChanged(item);
//			projectServerToEvent.remove(pkg);
//		}
//	}
//
//	public void startedCollectingFileSet(IPackageFileSet fileset) {
//	}
//
//	public void finishedCollectingFileSet(IPackageFileSet fileset) {
//	}
//
//	
//	public void startedBuild(IProject project) {
//		IServer[] serversThatCare = getServersWhoCare(project);
//		
//		// Create a top level event for each of these guys who care
//		for( int i = 0; i < serversThatCare.length; i++ ) {
//			ProjectServerPair key = new ProjectServerPair(project, serversThatCare[i]);
//			EventLogRoot root = EventLogModel.getModel(serversThatCare[i]).getRoot();
//			projectServerToEvent.put(key, root);
//			
//			PackagesBuildListenerEventLogItem child = 
//				new PackagesBuildListenerEventLogItem(root, PROJECT_BUILD_STARTED);
//			child.setProperty("project.name", project.getName());
//			
//			EventLogModel.markChanged(root);
//		}
//	}
//
//
//	public void finishedBuild(IProject project) {
//		IServer[] serversThatCare = getServersWhoCare(project);
//		
//		// Create a top level event for each of these guys who care
//		for( int i = 0; i < serversThatCare.length; i++ ) {
//			ProjectServerPair key = new ProjectServerPair(project, serversThatCare[i]);
//			EventLogTreeItem buildRoot = (EventLogTreeItem)projectServerToEvent.get(key); 
//			PackagesBuildListenerEventLogItem val = 
//				new PackagesBuildListenerEventLogItem(buildRoot, PROJECT_BUILD_FINISHED);
//			val.setProperty("project.name", project.getName());
//			EventLogModel.markChanged(buildRoot);
//			
//			// cleanup
//			projectServerToEvent.remove(key);
//		}
//	}
//
//	
//	
//	
//	
//	
//	
//	/*
//	 * If a server cares about any one package, then it cares the building is starting
//	 */
//	protected IServer[] getServersWhoCare(IProject project) {
//		Set set = new TreeSet(new Comparator() {
//			public int compare(Object o1, Object o2) {
//				if( o1 instanceof IServer && o2 instanceof IServer) {
//					return ((IServer)o1).getId().compareTo(((IServer)o2).getId());
//				}
//				return 0;
//			} });
//		
//		IPackage[] packs = PackagesCore.getProjectPackages(project, new NullProgressMonitor());
//		for( int i = 0; i < packs.length; i++ )
//			set.addAll(Arrays.asList(getServersWhoCare(packs[i])));
//		return (IServer[]) set.toArray(new IServer[set.size()]);
//	}
//	
//	// TODO later
//	protected IServer[] getServersWhoCare(IPackage pack) {
//		return new IServer[] { };
//		//return JBossServerCore.getIServerJBossServers();
//	}
	
	
//	protected class ProjectServerPair {
//		public IProject project;
//		public IServer server;
//		public ProjectServerPair(IProject project, IServer server) {
//			this.project = project; this.server = server;
//		}
//		public int hashCode() {
//			return project.hashCode() + server.hashCode();
//		}
//		public boolean equals(Object other) {
//			if( !(other instanceof ProjectServerPair )) {
//				return false;
//			}
//			ProjectServerPair p2 = (ProjectServerPair)other;
//			return p2.project == project && p2.server == server;
//		}
//	}
	public static final String EVENT_TYPE = "org.jboss.ide.eclipse.as.core.model.Packages";
	
	public static final String PROJECT_BUILD_STARTED = "org.jboss.ide.eclipse.as.core.model.Packages.projectBuildStarted";
	public static final String PROJECT_BUILD_FINISHED = "org.jboss.ide.eclipse.as.core.model.Packages.projectBuildFinished";
	public static final String PACKAGE_BUILD_STARTED = "org.jboss.ide.eclipse.as.core.model.Packages.packageBuildStarted";
	public static final String PACKAGE_BUILD_FINISHED = "org.jboss.ide.eclipse.as.core.model.Packages.packageBuildFinished";
	public static final String PACKAGE_BUILD_FAILED = "org.jboss.ide.eclipse.as.core.model.Packages.packageBuildFinished";
	public static final String FILESET_START = "org.jboss.ide.eclipse.as.core.model.Packages.FilesetStarted";
	public static final String FILESET_FINISHED = "org.jboss.ide.eclipse.as.core.model.Packages.FilesetFinished";
	
	
	public static final String PROJECT_NAME = "project.name";
	public static final String PACKAGE_NAME = "package.name";
	public static final String FILESET_INCLUDES_PATTERN = "fileset.includes";
	public static final String FILESET_EXCLUDES_PATTERN = "fileset.excludes";
	public static final String FILESET_DESTINATION_FILENAME = "fileset.destination.filename";
	public static final String FILESET_FILE_PATH = "fileset.file.path";
	public static final String FILESET_PROJECT = "fileset.project";
	public static final String FILESET_SOURCE = "fileset.source";
	public static final String FILESET_SOURCE_FOLDER = "fileset.source.folder";
	public static final String FILESET_SOURCE_PROJECT = "fileset.source.project";
	
	public static class PackagesBuildListenerEventLogItem extends EventLogTreeItem {

		public PackagesBuildListenerEventLogItem(SimpleTreeItem parent,
				String specificType) {
			super(parent, EVENT_TYPE, specificType);
			setProperty(DATE, new Long(new Date().getTime()));
		}
		
	}
	
	public static class PackagesPublisherBuildListener implements IPackagesBuildListener {
		private EventLogTreeItem parent;
		public PackagesPublisherBuildListener(EventLogTreeItem parent) {
			this.parent = parent;
		}
		public void startedBuild(IProject project) {
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(parent, PackagesBuildListener.PROJECT_BUILD_STARTED);
			child.setProperty(PROJECT_NAME, project.getName());
		}
		public void startedBuildingPackage(IPackage pkg) {
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(parent, PackagesBuildListener.PACKAGE_BUILD_STARTED);
			child.setProperty(PACKAGE_NAME, pkg.getName());
		}
		public void buildFailed(IPackage pkg, IStatus status) {
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(parent, PackagesBuildListener.PACKAGE_BUILD_FAILED);
			child.setProperty(PACKAGE_NAME, pkg.getName());
		}
		public void finishedBuildingPackage(IPackage pkg) {
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(parent, PackagesBuildListener.PACKAGE_BUILD_FINISHED);
			child.setProperty(PACKAGE_NAME, pkg.getName());
		}
		public void finishedBuild(IProject project) {
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(parent, PackagesBuildListener.PROJECT_BUILD_FINISHED);
			child.setProperty(PROJECT_NAME, project.getName());
		}
		
		
		public void startedCollectingFileSet(IPackageFileSet fileset) {
			fileset(fileset, PackagesBuildListener.FILESET_START);
		}
		public void finishedCollectingFileSet(IPackageFileSet fileset) {
			fileset(fileset, PackagesBuildListener.FILESET_FINISHED);
		}

		protected void fileset(IPackageFileSet fileset, String type) {
			PackagesBuildListenerEventLogItem child = 
				new PackagesBuildListenerEventLogItem(parent, type);
			child.setProperty(FILESET_INCLUDES_PATTERN, fileset.getIncludesPattern());
			child.setProperty(FILESET_EXCLUDES_PATTERN, fileset.getDestinationFilename());
			child.setProperty(FILESET_DESTINATION_FILENAME, fileset.getDestinationFilename());
			if( fileset.getFilePath() != null )
				child.setProperty(FILESET_FILE_PATH, fileset.getFilePath().toOSString());
			if( fileset.getProject() != null ) 
				child.setProperty(FILESET_PROJECT, fileset.getProject().getName());
			if( fileset.getSourceContainer() != null )
				child.setProperty(FILESET_SOURCE, fileset.getSourceContainer().getFullPath().toOSString());
			if( fileset.getSourceFolder() != null )
				child.setProperty(FILESET_SOURCE_FOLDER, fileset.getSourceFolder().toOSString());
			if( fileset.getSourceProject() != null )
				child.setProperty(FILESET_SOURCE_PROJECT, fileset.getSourceProject().getName());
		}
	}

}
