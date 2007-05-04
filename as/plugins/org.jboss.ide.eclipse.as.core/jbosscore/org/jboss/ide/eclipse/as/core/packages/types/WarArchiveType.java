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
package org.jboss.ide.eclipse.as.core.packages.types;

import org.apache.tools.ant.DirectoryScanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class WarArchiveType extends J2EEArchiveType {
	public static final String WAR_PACKAGE_TYPE = "org.jboss.ide.eclipse.as.core.packages.warPackage";

	public String getAssociatedModuleType() {
		return "jst.web";
	}

	public IArchive createDefaultConfiguration(IProject project, IProgressMonitor monitor) {
		IModule mod = getModule(project);
		if( mod == null ) 
			return createDefaultConfiguration2(project, monitor);
		else
			return createDefaultConfigFromModule(mod, monitor);
	}
	
	protected IArchive createDefaultConfiguration2(IProject project, IProgressMonitor monitor) {
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".war");
		return fillDefaultConfiguration(project, topLevel, monitor);
	}
	
	public IArchive fillDefaultConfiguration(IProject project, IArchive topLevel, IProgressMonitor monitor) {
		IModule mod = getModule(project);
//		topLevel.setDestinationPath(new Path(project.getName()));
//		topLevel.setInWorkspace(true);
		IArchiveFolder webinf = addFolder(project, topLevel, WEBINF);
		IArchiveFolder lib = addFolder(project, webinf, LIB);
		IArchiveFolder classes = addFolder(project, webinf, CLASSES);
		addReferencedProjectsAsLibs(project, lib);
		addClassesFileset(project, classes);

		if( mod == null ) {
			addWebinfFileset(project, webinf);
			addLibFileset(project, lib, true);
		} else {
			addWebContentFileset(project, topLevel);
		}
		return topLevel;
	}
	
	// For modules only
	protected void addWebContentFileset(IProject project, IArchiveNode packageRoot) {
		IPath projectPath = project.getLocation();
		DirectoryScanner scanner = 
			DirectoryScannerFactory.createDirectoryScanner(projectPath, "**/WEB-INF/web.xml", null, true);
		String[] files = scanner.getIncludedFiles();
		// just take the first
		if( files.length > 0 ) {
			IPath path = new Path(files[0]);
			path = path.removeLastSegments(2); // remove the file name
			path = new Path(project.getName()).append(path); // pre-pend project name to make workspace-relative
			addFileset(project, packageRoot, path.toOSString(), "**/*");			
		}
	}
	protected void addClassesFileset(IProject project, IArchiveFolder folder) {
		IJavaProject jp = JavaCore.create(project);
		if( jp != null ) {
			try {
				IPath outputLoc = project.getWorkspace().getRoot().getLocation();
				outputLoc = outputLoc.append(jp.getOutputLocation());
				addFileset(project, folder, jp.getOutputLocation().toOSString(), "**/*.class");
			} catch( JavaModelException jme ) {
			}
		}
	}
	protected void addWebinfFileset(IProject project, IArchiveFolder folder) {
		IPath projectPath = project.getLocation();
		DirectoryScanner scanner = 
			DirectoryScannerFactory.createDirectoryScanner(projectPath, "**/web.xml", null, true);
		String[] files = scanner.getIncludedFiles();
		// just take the first
		if( files.length > 0 ) {
			IPath path = new Path(files[0]);
			path = path.removeLastSegments(1); // remove the file name
			path = new Path(project.getName()).append(path); // pre-pend project name to make workspace-relative
			addFileset(project, folder, path.toOSString(), "**/*");			
		}
	}
	
	// Lib support
	protected void addLibFileset(IProject project, IArchiveFolder folder, boolean includeTopLevelJars) {
		addFileset(project, folder, project.getName(), "**/*.jar");  // add default jars
	}
	protected void addReferencedProjectsAsLibs(IProject project, IArchiveFolder folder) {
		IJavaProject jp = JavaCore.create(project);
		if( jp != null ) {
			try {
				IClasspathEntry[] entries = jp.getRawClasspath();
				for( int i = 0; i < entries.length; i++ ) {
					if( entries[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) {
						IPath path = entries[i].getPath();
						IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
						if( res instanceof IProject ) {
							createLibFromProject((IProject)res, folder);
						}
					}
				}
			} catch( JavaModelException jme ) {
				jme.printStackTrace();
			}
		}
	}
	
	
	protected void createLibFromProject(IProject project, IArchiveFolder folder) {
		IArchive pack = createGenericIArchive(project, null, project.getName() + ".jar");
		folder.addChild(pack);
	}

	protected IArchive createDefaultConfigFromModule(IModule mod, IProgressMonitor monitor) {
		try {
			IProject project = mod.getProject();

			// create the stub
			IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".war");
			topLevel.setDestinationPath(new Path(project.getName()));
			topLevel.setInWorkspace(true);
			
			// add lib folder so we can add libraries
			IArchiveFolder webinf = addFolder(project, topLevel, WEBINF);
			IArchiveFolder lib = addFolder(project, webinf, LIB);
			
			addFileset(project, topLevel, new Path(project.getName()).append(WEBCONTENT).toOSString(), null);

			
			// package each child and add to lib folder
			IWebModule webModule = (IWebModule)mod.loadAdapter(IWebModule.class, monitor);
			IModule[] childModules = webModule.getModules();
			for (int i = 0; i < childModules.length; i++) {
				IModule child = childModules[i];
				lib.addChild(createGenericIArchive(child.getProject(), null, child.getProject().getName() + ".jar"));
			}
			return topLevel;
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	public String getId() {
		return WAR_PACKAGE_TYPE;
	}

	public String getLabel() {
		return "WAR";
	}
}