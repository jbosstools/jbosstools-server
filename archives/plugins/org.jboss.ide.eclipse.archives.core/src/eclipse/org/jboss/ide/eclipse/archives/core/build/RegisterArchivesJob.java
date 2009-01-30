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
package org.jboss.ide.eclipse.archives.core.build;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.ide.eclipse.archives.core.ArchivesCoreMessages;
import org.jboss.ide.eclipse.archives.core.ArchivesCorePlugin;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;

public class RegisterArchivesJob extends Job {
	private IProject[] projects;
	private Runnable callback;
	public RegisterArchivesJob(IProject[] projects, Runnable callback) {
		super(ArchivesCoreMessages.RegisterProject);
		this.projects = projects;
		this.callback = callback;
	}

	protected IStatus run(IProgressMonitor monitor) {
		// register the projects
		for( int i = 0; i < projects.length; i++ ) {
			try {
				ArchivesModel.instance().registerProject(projects[i].getLocation(), monitor);
			} catch( ArchivesModelException ame ) {
				IStatus status = new Status(IStatus.ERROR, ArchivesCorePlugin.PLUGIN_ID, ame.getMessage(), ame);
				return status;
			}
		}

		callback.run();
		return Status.OK_STATUS;
	}

}
