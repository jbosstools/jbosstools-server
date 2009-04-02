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
package org.jboss.ide.eclipse.archives.core;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModel;

/**
 * Update the model if someone changes the packaging file by hand
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class WorkspaceChangeListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		// The comparator
		Comparator c = new Comparator() {
			public int compare(Object o1, Object o2) {
				if( o1 instanceof IProject && o2 instanceof IProject)
					return ((IProject)o1).getLocation().toOSString().compareTo(
							((IProject)o2).getLocation().toOSString());
				return 0;
			}
		};
		
		// Unregister a project if a project is being deleted
		if (event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			IResource resource = event.getResource();
			if (resource instanceof IProject) {
				final IProject project = (IProject) resource;
				IResource packages = project.findMember(IArchiveModel.DEFAULT_PACKAGES_FILE);
				if (ArchivesModel.instance().isProjectRegistered(project.getLocation()) || packages != null) {
					unregister(project);
				}
			}
		}
		
		// Recurse delta to find a packages file, add to set
		final Set<IProject> projects = new TreeSet<IProject>(c);
		IResourceDelta delta = event.getDelta();
		try {
			if(delta!=null) {
				delta.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {
						// TODO modify to make sure this file is a descriptor in use / registered
						if( delta.getResource() != null && delta.getResource().getLocation() != null &&
								delta.getResource().getLocation().lastSegment().equals(IArchiveModel.DEFAULT_PACKAGES_FILE)) {
							if( delta.getResource().getProject() != null ) 
								projects.add(delta.getResource().getProject());
						}
						return true;
					}
				});
			}
		} catch( CoreException ce ) {
		}
		
		// If we're deleting the .packages file or closing the project, unregister
		if (event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Iterator<IProject> i = projects.iterator();
			while(i.hasNext()) {
				final IProject p = i.next();
				unregister(p);
			}
		} else {
			Iterator<IProject> i = projects.iterator();
			while(i.hasNext()) {
				final IProject p = i.next();
				try {
					if( p.getSessionProperty(new QualifiedName(ArchivesCorePlugin.PLUGIN_ID, "localname")) == null ) { //$NON-NLS-1$
						try {
							ArchivesModel.instance().registerProject(p.getLocation(), new NullProgressMonitor());
							new Job(ArchivesCore.bind(ArchivesCoreMessages.RefreshProject, p.getName())) {
							protected IStatus run(IProgressMonitor monitor) {
								try {
									p.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
									} catch( CoreException e ) {
										IStatus status = new Status(IStatus.WARNING, ArchivesCorePlugin.PLUGIN_ID,
											ArchivesCore.bind(ArchivesCoreMessages.RefreshProjectFailed, p.getName()),e);
										return status;
									}
									return Status.OK_STATUS;
								}
							}.schedule();
						} catch( ArchivesModelException ame ) {
						ArchivesCore.getInstance().getLogger().log(IStatus.ERROR,
								ArchivesCore.bind(ArchivesCoreMessages.RegisterProjectFailed, p.getName()), ame);
						}
					}
				} catch( CoreException ce ) {
				}
			}
		}
	}

	protected void unregister(final IProject project) {
		WorkspaceJob job = new WorkspaceJob(ArchivesCoreMessages.UnregisterProject) {
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				ArchivesModel.instance().unregisterProject(
						project.getLocation(), new NullProgressMonitor());
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
}
