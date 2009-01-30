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
package org.jboss.ide.eclipse.archives.core.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XMLBinding.XbException;


/**
 * Manages Archive Model Listeners, build and model changes, for changes
 * in the model
 * @author rstryker
 *
 */
public interface IArchiveModel {
	public static final String DEFAULT_PACKAGES_FILE = ".packages"; //$NON-NLS-1$

	public boolean isProjectRegistered(IPath projectPath);
	public void save(IPath projectPath, IProgressMonitor monitor) throws ArchivesModelException;
	public void save(IArchiveModelRootNode modelNode, IProgressMonitor monitor) throws ArchivesModelException;
	public IArchiveModelRootNode registerProject(IPath projectPath, IProgressMonitor monitor) throws ArchivesModelException;
	public IArchiveModelRootNode registerProject(IPath projectPath, String file, IProgressMonitor monitor) throws ArchivesModelException;
	public void registerProject(IArchiveModelRootNode modelNode, IProgressMonitor monitor);
	public void unregisterProject(IPath projectPath, IProgressMonitor monitor);
	public void unregisterProject(IArchiveModelRootNode modelNode, IProgressMonitor monitor);
	public boolean canReregister(IPath projectPath);
	public boolean canReregister(IPath projectPath, String file);
	public IArchiveModelRootNode getRoot(IPath project);
	public IArchiveModelRootNode[] getModelNodes();
	public boolean accept(IArchiveNodeVisitor visitor);

	public void addBuildListener(IArchiveBuildListener listener);
	public void removeBuildListener(IArchiveBuildListener listener);
	public IArchiveBuildListener[] getBuildListeners();
	public void addModelListener(IArchiveModelListener listener);
	public void removeModelListener(IArchiveModelListener listener);
	public IArchiveModelListener[] getModelListeners();
}
