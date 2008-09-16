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
package org.jboss.ide.eclipse.archives.webtools.archivetypes;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.webtools.modules.ModulePackageTypeConverter;

/**
 * @author rob.stryker@redhat.com
 */
public class EarArchiveType extends J2EEArchiveType {
	public final static String ID = "org.jboss.ide.eclipse.as.core.packages.earPackageType";
	public String getAssociatedModuleType() {
		return "jst.ear";
	}

	public String getId() {
		return ID;
	}

	public String getLabel() {
		return "EAR";
	}

	
	public IArchive createDefaultConfiguration(String project, IProgressMonitor monitor) {
		IModule mod = getModule(project);
		if( mod != null ) 
			return createDefaultConfigFromModule(mod, monitor);
		else
			return createDefaultConfiguration2(project, monitor);
	}
	
	public IArchive createDefaultConfiguration2(String projectName,
			IProgressMonitor monitor) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".ear");
		topLevel.setDestinationPath(new Path(project.getName()));
		topLevel.setInWorkspace(true);
		
		fillDefaultConfiguration(projectName, topLevel, monitor);
		return topLevel;
	}

	public IArchive createDefaultConfigFromModule(IModule module,
			IProgressMonitor monitor) {
		IProject project = module.getProject();
		IContainer sourceContainer = project.getFolder(EARCONTENT);
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".ear", sourceContainer);
		topLevel.setDestinationPath(new Path(project.getName()));
		topLevel.setInWorkspace(true);
		
		fillDefaultConfiguration(project.getName(), topLevel, monitor);
		return topLevel;
	}

	public IArchive fillDefaultConfiguration(String projectName, IArchive topLevel, IProgressMonitor monitor) {
		try {
		IModule mod = getModule(projectName);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		if( mod == null ) {
			// add fileset
			IArchiveFolder metainf = addFolder(project, topLevel, METAINF);
			addFileset(project, metainf, new Path(projectName).append(METAINF).toOSString(), null);
			
		} else {
			// now add children
			addFileset(project, topLevel, new Path(project.getName()).append(EARCONTENT).toOSString(), "**/*.*");
			IEnterpriseApplication earModule = (IEnterpriseApplication)mod.loadAdapter(IEnterpriseApplication.class, monitor);
			IModule[] childModules = earModule.getModules();
			for( int i = 0; i < childModules.length; i++ ) {
				IModule child = childModules[i];
				IArchiveType type = ModulePackageTypeConverter.getPackageTypeFor(child);
				IArchive childPack;
				if( type == null ) {
					childPack = createGenericIArchive(child.getProject(), null, child.getProject().getName() + ".jar");
				} else {
					childPack = type.createDefaultConfiguration(child.getProject().getName(), new NullProgressMonitor());
				}
				topLevel.addChild(childPack);
			}
		}

		} catch( ArchivesModelException ame) {}
		return topLevel;
	}
}
