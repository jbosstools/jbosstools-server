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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackageFolder;
import org.jboss.ide.eclipse.packages.core.model.IPackageNode;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.core.model.types.AbstractPackageType;
import org.jboss.ide.eclipse.packages.core.model.types.IPackageType;

/**
 *
 * @author rob.stryker@jboss.com
 */
public abstract class ObscurelyNamedPackageTypeSuperclass extends AbstractPackageType {
	protected static final String METAINF = "META-INF";
	protected static final String WEBINF = "WEB-INF";
	protected static final String CLASSES = "classes";
	protected static final String LIB = "lib";
	protected static final String WEBCONTENT = "WebContent";
	protected static final String EARCONTENT = "EarContent";
	protected static final String EJBMODULE = "ejbModule";


	protected boolean isModuleType(IModule module, String moduleTypeId){	
		if(module.getModuleType()!=null && moduleTypeId.equals(module.getModuleType().getId()))
			return true;
		return false;
	}

	protected IModule getModule(IProject project) {
		IModuleArtifact moduleArtifact = ServerPlugin.loadModuleArtifact(project);
		return moduleArtifact == null ? null : moduleArtifact.getModule();
	}
	
	// Find the source folder, then create the IPackage appropriately
	public static IPackage createGenericIPackage(IProject project, String deployDirectory, String packageName) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
			Assert.isNotNull(javaProject);
			
			IPath sourcePath;
			try {
				sourcePath = javaProject.getOutputLocation();
			} catch (JavaModelException e) {
				e.printStackTrace();
				return null;
			}
			sourcePath = sourcePath.removeFirstSegments(1);
			IContainer sourcePathContainer;
			if( sourcePath.segmentCount() == 0 ) 
				sourcePathContainer = project;
			else
				sourcePathContainer = project.getFolder(sourcePath);
			return createGenericIPackage(project, deployDirectory, packageName, sourcePathContainer);
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	} 
	
	// Create a detached package with some generic settings
	public static IPackage createGenericIPackage(IProject project, String deployDirectory, String packageName, IContainer sourceContainer) {
		IPackage jar = PackagesCore.createDetachedPackage(project, true);
			
		if( deployDirectory != null ) {
			jar.setDestinationPath(new Path(deployDirectory));
			jar.setExploded(false);
		} else {
			jar.setDestinationContainer(project);
			jar.setExploded(false);
		}
		jar.setName(packageName);
			
		IPackageFileSet classes = PackagesCore.createDetachedPackageFileSet(project);
		classes.setIncludesPattern("**/*");
		classes.setSourceContainer(sourceContainer);
		jar.addChild(classes);
		return jar;
	}

	
	protected static IPackageFolder addFolder(IProject project, IPackageNode parent, String name) {
		IPackageFolder folder = PackagesCore.createPackageFolder(project);
		folder.setName(name);
		parent.addChild(folder);
		return folder;
	}
	protected static IPackageFileSet addFileset(IProject project, IPackageNode parent, String sourcePath, String includePattern) {
		IPackageFileSet fs = PackagesCore.createPackageFileSet(project);
		Assert.isNotNull(project);
		IJavaProject javaProject = JavaCore.create(project);
		Assert.isNotNull(javaProject);

		IContainer sourceContainer;
		if( sourcePath != null && !sourcePath.equals("")) {
			sourceContainer = project.getFolder(new Path(sourcePath));
		} else {
			sourceContainer = project;
		}

		fs.setSourceContainer(sourceContainer);
		fs.setIncludesPattern(  includePattern == null ?  "**/*" : includePattern );
		parent.addChild(fs);
		return fs;
	}
	
	public int getSupportFor(IProject project) {
		String modType = getAssociatedModuleType();
		IModule module = getModule(project);

		if( modType == null )  // we require no module type
			return IPackageType.SUPPORT_FULL;
			
		if( module == null ) // project is not a module
			return IPackageType.SUPPORT_FULL;

		// module and modType match
		if( modType.equals(module.getModuleType().getId())) 
			return IPackageType.SUPPORT_FULL;

		// it's some other module type. Not Best Match
		return IPackageType.SUPPORT_CONDITIONAL; 
	}
	public abstract String getAssociatedModuleType();
	
}
