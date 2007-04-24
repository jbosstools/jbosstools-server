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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.types.IArchiveType;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class EarArchiveType extends J2EEArchiveType {
	public static final String EAR_PACKAGE_TYPE = "org.jboss.ide.eclipse.as.core.packages.earPackage";

	public String getAssociatedModuleType() {
		return "jst.ear";
	}

	public IArchive createDefaultConfiguration(IProject project, IProgressMonitor monitor) {
		IModule mod = getModule(project);
		if( mod != null ) 
			return createDefaultConfigFromModule(mod, monitor);
		else
			return createDefaultConfiguration2(project, monitor);
	}
	
	public IArchive createDefaultConfiguration2(IProject project,
			IProgressMonitor monitor) {
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".war");
		topLevel.setDestinationPath(project.getLocation(), true);//setDestinationContainer(project);
		IArchiveFolder metainf = addFolder(project, topLevel, METAINF);
		IArchiveFolder lib = addFolder(project, metainf, LIB);
		addFileset(project, metainf, new Path(project.getName()).append(METAINF).toOSString(), null);
		return topLevel;
	}

	public IArchive createDefaultConfigFromModule(IModule module,
			IProgressMonitor monitor) {
		IProject project = module.getProject();
		IContainer sourceContainer = project.getFolder(EARCONTENT);

		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".ear", sourceContainer);
		addFileset(project, topLevel, new Path(project.getName()).append(EARCONTENT).toOSString(), "**/*.*");
		
		// now add children
		IEnterpriseApplication earModule = (IEnterpriseApplication)module.loadAdapter(IEnterpriseApplication.class, monitor);
		IModule[] childModules = earModule.getModules();
		for( int i = 0; i < childModules.length; i++ ) {
			IModule child = childModules[i];
			IArchiveType type = ModulePackageTypeConverter.getPackageTypeFor(child);
			IArchive childPack;
			if( type == null ) {
				childPack = createGenericIArchive(child.getProject(), null, child.getProject().getName() + ".jar");
			} else {
				childPack = type.createDefaultConfiguration(child.getProject(), new NullProgressMonitor());
			}
			topLevel.addChild(childPack);
		}

		return topLevel;
	}

	public String getId() {
		return EAR_PACKAGE_TYPE;
	}

	public String getLabel() {
		return "EAR";
	}
}
