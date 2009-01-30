/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
	public final static String ID = "org.jboss.ide.eclipse.as.core.packages.earPackageType"; //$NON-NLS-1$
	private static final String EXTENSION = ".ear"; //$NON-NLS-1$
	private static final String JAR_EXTENSION = ".jar"; //$NON-NLS-1$

	public String getAssociatedModuleType() {
		return "jst.ear"; //$NON-NLS-1$
	}

	public String getId() {
		return ID;
	}

	public String getLabel() {
		return "EAR"; //$NON-NLS-1$
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
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + EXTENSION);
		topLevel.setDestinationPath(new Path(project.getName()));
		topLevel.setInWorkspace(true);

		fillDefaultConfiguration(projectName, topLevel, monitor);
		return topLevel;
	}

	public IArchive createDefaultConfigFromModule(IModule module,
			IProgressMonitor monitor) {
		IProject project = module.getProject();
		IContainer sourceContainer = project.getFolder(EARCONTENT);
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + EXTENSION, sourceContainer);
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
			addFileset(project, topLevel, new Path(project.getName()).append(EARCONTENT).toOSString(), "**/*.*"); //$NON-NLS-1$
			IEnterpriseApplication earModule = (IEnterpriseApplication)mod.loadAdapter(IEnterpriseApplication.class, monitor);
			IModule[] childModules = earModule.getModules();
			for( int i = 0; i < childModules.length; i++ ) {
				IModule child = childModules[i];
				IArchiveType type = ModulePackageTypeConverter.getPackageTypeFor(child);
				IArchive childPack;
				if( type == null ) {
					childPack = createGenericIArchive(child.getProject(), null, child.getProject().getName() + JAR_EXTENSION);
				} else {
					if( new Path(childModules[i].getName()).segmentCount() > 1 )
						continue;
					childPack = type.createDefaultConfiguration(child.getProject().getName(), new NullProgressMonitor());
				}
				topLevel.addChild(childPack);
			}
		}

		} catch( ArchivesModelException ame) {}
		return topLevel;
	}
}
