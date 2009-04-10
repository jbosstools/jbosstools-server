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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;
import org.jboss.ide.eclipse.archives.webtools.Messages;

/**
 *
 * @author rob.stryker@jboss.com
 */
public abstract class J2EEArchiveType implements IArchiveType {
	public static final String METAINF = "META-INF"; //$NON-NLS-1$
	public static final String WEBINF = "WEB-INF";//$NON-NLS-1$
	public static final String CLASSES = "classes";//$NON-NLS-1$
	public static final String LIB = "lib";//$NON-NLS-1$
	public static final String WEBCONTENT = "WebContent";//$NON-NLS-1$
	public static final String EARCONTENT = "EarContent";//$NON-NLS-1$
	public static final String EJBMODULE = "ejbModule";//$NON-NLS-1$


	protected boolean isModuleType(IModule module, String moduleTypeId){
		if(module.getModuleType()!=null && moduleTypeId.equals(module.getModuleType().getId()))
			return true;
		return false;
	}

	protected IModule getModule(String projectName) {
		IModuleArtifact moduleArtifacts[] = ServerPlugin.getModuleArtifacts(getProject(projectName));

		if (moduleArtifacts != null && moduleArtifacts.length > 0)
			return moduleArtifacts[0].getModule();
		else return null;
	}

	protected IProject getProject(String projectName) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}
	// Find the source folder, then create the IPackage appropriately
	public static IArchive createGenericIArchive(IProject project, String deployDirectory, String packageName) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
			Assert.isNotNull(javaProject);

			IPath sourcePath;
			try {
				sourcePath = javaProject.getOutputLocation();
			} catch (JavaModelException e) {
				return null;
			}
			sourcePath = sourcePath.removeFirstSegments(1);
			IContainer sourcePathContainer;
			if( sourcePath.segmentCount() == 0 )
				sourcePathContainer = project;
			else
				sourcePathContainer = project.getFolder(sourcePath);
			return createGenericIArchive(project, deployDirectory, packageName, sourcePathContainer);
		} catch( Exception e ) {
			IntegrationPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, Messages.ExceptionUnexpectedException, e));
		}
		return null;
	}

	// Create a detached package with some generic settings
	public static IArchive createGenericIArchive(IProject project, String deployDirectory, String packageName, IContainer sourceContainer) {
		IArchive jar = ArchivesCore.getInstance().getNodeFactory().createArchive();

		if( deployDirectory != null ) {
			jar.setDestinationPath(new Path(deployDirectory));
			jar.setInWorkspace(ResourcesPlugin.getWorkspace().getRoot().getLocation().isPrefixOf(new Path(deployDirectory)));
			jar.setExploded(false);
		} else {
			jar.setDestinationPath(project.getFullPath());
			jar.setInWorkspace(true);
			jar.setExploded(false);
		}
		jar.setName(packageName);
		return jar;
	}


	public static IArchiveFolder addFolder(IProject project,
			IArchiveNode parent, String name) throws ArchivesModelException {
		IArchiveFolder folder = ArchivesCore.getInstance().getNodeFactory().createFolder();
		folder.setName(name);
		parent.addChild(folder);
		return folder;
	}
	public static IArchiveStandardFileSet addFileset(IProject project, IArchiveNode parent,
			String sourcePath, String includePattern) throws ArchivesModelException {
		IArchiveStandardFileSet fs = ArchivesCore.getInstance().getNodeFactory().createFileset();
		Assert.isNotNull(project);
		IJavaProject javaProject = JavaCore.create(project);
		Assert.isNotNull(javaProject);

		IContainer sourceContainer;
		if( sourcePath != null && !sourcePath.equals("")) { //$NON-NLS-1$
			Path p = new Path(sourcePath);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			sourceContainer = p.segmentCount() != 1 ? (IContainer)root.getFolder(p) : root.getProject(p.segment(0));
		} else {
			sourceContainer = project;
		}

		fs.setRawSourcePath(sourceContainer.getFullPath().toString());
		fs.setInWorkspace(true);
		fs.setIncludesPattern(  includePattern == null ?  "**/*" : includePattern ); //$NON-NLS-1$
		parent.addChild(fs);
		return fs;
	}

	public abstract String getAssociatedModuleType();

	/*
	 * Creates a directory scanner for some global path
	 */
	public static DirectoryScannerExtension createDirectoryScanner (String rawPath, String includes, String excludes, boolean scan) {
		return DirectoryScannerFactory.createDirectoryScanner(rawPath, null, includes, excludes, null, false, 1, scan);
	}

}
