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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchive;

/**
 *
 * @author rob.stryker@redhat.com
 */
public class EjbArchiveType extends J2EEArchiveType {

	public final static String ID = "org.jboss.ide.eclipse.as.core.packages.ejbPackageType"; //$NON-NLS-1$
	public String getAssociatedModuleType() {
		return "jst.ejb"; //$NON-NLS-1$
	}

	public String getId() {
		return ID;
	}

	public String getLabel() {
		return "EJB JAR"; //$NON-NLS-1$
	}

	public IArchive createDefaultConfiguration(String projectName, IProgressMonitor monitor) {
		IProject project = getProject(projectName);
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".jar"); //$NON-NLS-1$
		return fillDefaultConfiguration(project, topLevel, monitor);
	}

	public IArchive fillDefaultConfiguration(String projectName, IArchive topLevel, IProgressMonitor monitor) {
		return fillDefaultConfiguration(getProject(projectName), topLevel, monitor);
	}
	public IArchive fillDefaultConfiguration(IProject project, IArchive topLevel, IProgressMonitor monitor) {

		IModule mod = getModule(project.getName());

		if( mod == null ) {
			// TODO fix me
		} else {
			if( mod.getModuleType().getId().equals("jst.ejb") ) { //$NON-NLS-1$
				try {
					IJavaProject proj = JavaCore.create(project);
					IPath outputLoc = proj.getOutputLocation();
					addFileset(project, topLevel, outputLoc.toOSString(), null);
				} catch( JavaModelException jmde) {
				} catch( ArchivesModelException ame ) {}
			}
		}
		return topLevel;
	}

}
