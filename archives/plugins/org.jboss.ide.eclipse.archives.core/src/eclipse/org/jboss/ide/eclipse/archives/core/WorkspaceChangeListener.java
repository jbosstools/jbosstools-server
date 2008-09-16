/*
 * JBoss, a division of Red Hat
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
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;

/**
 * Update the model if someone changes the packaging file by hand
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class WorkspaceChangeListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		Comparator c = new Comparator() {
			public int compare(Object o1, Object o2) {
				if( o1 instanceof IProject && o2 instanceof IProject)
					return ((IProject)o1).getLocation().toOSString().compareTo(
							((IProject)o2).getLocation().toOSString());
				return 0;
			}
		};
		final Set<IProject> projects = new TreeSet<IProject>(c);
		
		IResourceDelta delta = event.getDelta();
		try {
			if(delta!=null) {
				delta.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {
						// TODO modify to make sure this file is a descriptor in use / registered
						if( delta.getResource() != null && delta.getResource().getLocation() != null && 
								delta.getResource().getLocation().lastSegment().equals(IArchiveModel.DEFAULT_PACKAGES_FILE)) {
							projects.add(delta.getResource().getProject());
						}
						return true;
					}
				});
			}
		} catch( CoreException ce ) {
		}
		Iterator<IProject> i = projects.iterator();
		while(i.hasNext()) {
			final IProject p = i.next();
			try {
				if( p.getSessionProperty(new QualifiedName(ArchivesCorePlugin.PLUGIN_ID, "localname")) == null ) {
					try {
						ArchivesModel.instance().registerProject(p.getLocation(), new NullProgressMonitor());
						new Job("Refresh Project: " + p.getName()) {
							protected IStatus run(IProgressMonitor monitor) {
								try {
									p.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
								} catch( CoreException e ) {
									IStatus status = new Status(IStatus.WARNING, ArchivesCorePlugin.PLUGIN_ID, "Could not refresh project " + p.getName(), e);
									return status;
								}
								return Status.OK_STATUS;
							}
						}.schedule();
					} catch( ArchivesModelException ame ) {
						ArchivesCore.getInstance().getLogger().log(IArchivesLogger.MSG_ERR, "Could not register project " + p.getName(), ame);
					}
				}
			} catch( CoreException ce ) {
			}
		}
	}

}
