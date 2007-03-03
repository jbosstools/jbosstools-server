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
import org.jboss.ide.eclipse.packages.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackageFolder;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class WarPackageType extends ObscurelyNamedPackageTypeSuperclass {
	public static final String WAR_PACKAGE_TYPE = "org.jboss.ide.eclipse.as.core.packages.warPackage";

	public String getAssociatedModuleType() {
		return "jst.web";
	}

	public IPackage createDefaultConfiguration(IProject project, IProgressMonitor monitor) {
		IModule mod = getModule(project);
		if( mod == null ) 
			return createDefaultConfiguration2(project, monitor);
		else
			return createDefaultConfigFromModule(mod, monitor);
	}
	
	protected IPackage createDefaultConfiguration2(IProject project, IProgressMonitor monitor) {
		IPackage topLevel = createGenericIPackage(project, null, project.getName() + ".war");
		return fillDefaultConfiguration(project, topLevel, monitor);
	}
	
	public IPackage fillDefaultConfiguration(IProject project, IPackage topLevel, IProgressMonitor monitor) {
		topLevel.setDestinationContainer(project);
		IPackageFolder webinf = addFolder(project, topLevel, WEBINF);
		IPackageFolder lib = addFolder(project, webinf, LIB);
		IPackageFolder classes = addFolder(project, webinf, CLASSES);
		addWebinfFileset(project, webinf);
		addLibFileset(project, lib);
		addClassesFileset(project, classes);
		return topLevel;
	}
	
	protected void addClassesFileset(IProject project, IPackageFolder folder) {
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
	protected void addWebinfFileset(IProject project, IPackageFolder folder) {
		IPath projectPath = project.getLocation();
		DirectoryScanner scanner = 
			DirectoryScannerFactory.createDirectoryScanner(projectPath, "**/web.xml", null);
		String[] files = scanner.getIncludedFiles();
		// just take the first
		if( files.length > 0 ) {
			IPath path = new Path(files[0]);
			path = path.removeLastSegments(1); // remove the file name
			path.removeFirstSegments(projectPath.segmentCount()-1); // leave project name
			addFileset(project, folder, path.toOSString(), "**/*");			
		}
	}
	protected void addLibFileset(IProject project, IPackageFolder folder) {
		addFileset(project, folder, project.getName(), "**/*.jar");  // add default jars
		
		// now add referenced projects
		IJavaProject jp = JavaCore.create(project);
		if( jp != null ) {
			try {
				IClasspathEntry[] entries = jp.getRawClasspath();
				for( int i = 0; i < entries.length; i++ ) {
					System.out.println(entries[i].getContentKind());
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
	protected void createLibFromProject(IProject project, IPackageFolder folder) {
		IPackage pack = createGenericIPackage(project, null, project.getName() + ".jar");
		folder.addChild(pack);
	}

	protected IPackage createDefaultConfigFromModule(IModule mod, IProgressMonitor monitor) {
		try {
			IProject project = mod.getProject();

			IPackage topLevel = createGenericIPackage(project, null, project.getName() + ".war");
			topLevel.setDestinationContainer(project);
			IPackageFolder webinf = addFolder(project, topLevel, WEBINF);
			IPackageFolder metainf = addFolder(project, topLevel, METAINF);
			IPackageFolder lib = addFolder(project, metainf, LIB);
			addFileset(project, webinf, 
					new Path(project.getName()).append(WEBCONTENT).append(WEBINF).toOSString(), null);

			IWebModule webModule = (IWebModule)mod.loadAdapter(IWebModule.class, monitor);
			IModule[] childModules = webModule.getModules();
			
			for (int i = 0; i < childModules.length; i++) {
				IModule child = childModules[i];
				// package each child and add
				lib.addChild(createGenericIPackage(child.getProject(), null, child.getProject().getName() + ".jar"));
			}
			return topLevel;
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}
}