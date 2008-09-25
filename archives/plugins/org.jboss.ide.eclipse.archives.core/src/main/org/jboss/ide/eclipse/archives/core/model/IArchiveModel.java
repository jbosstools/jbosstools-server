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
