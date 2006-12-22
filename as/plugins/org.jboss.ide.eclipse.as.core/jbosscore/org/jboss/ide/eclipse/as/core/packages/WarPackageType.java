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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackageFolder;
import org.jboss.ide.eclipse.packages.core.model.IPackageNode;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.core.model.types.IPackageType;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class WarPackageType extends ObscurelyNamedPackageTypeSuperclass {

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
		topLevel.setDestinationContainer(project);
		IPackageFolder webinf = addFolder(project, topLevel, WEBINF);
		IPackageFolder metainf = addFolder(project, topLevel, METAINF);
		IPackageFolder lib = addFolder(project, metainf, LIB);
		addFileset(project, metainf, WEBCONTENT + Path.SEPARATOR + METAINF, null);
		addFileset(project, webinf, WEBCONTENT + Path.SEPARATOR + WEBINF, null);
		return topLevel;
	}

	protected IPackage createDefaultConfigFromModule(IModule mod, IProgressMonitor monitor) {
		try {
			IProject project = mod.getProject();

			IPackage topLevel = createGenericIPackage(project, null, project.getName() + ".war");
			topLevel.setDestinationContainer(project);
			IPackageFolder webinf = addFolder(project, topLevel, WEBINF);
			IPackageFolder metainf = addFolder(project, topLevel, METAINF);
			IPackageFolder lib = addFolder(project, metainf, LIB);
			addFileset(project, metainf, WEBCONTENT + Path.SEPARATOR + METAINF, null);
			addFileset(project, webinf, WEBCONTENT + Path.SEPARATOR + WEBINF, null);

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