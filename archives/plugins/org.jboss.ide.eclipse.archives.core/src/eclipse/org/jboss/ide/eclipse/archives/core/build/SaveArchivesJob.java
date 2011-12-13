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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.ide.eclipse.archives.core.ArchivesCoreMessages;
import org.jboss.ide.eclipse.archives.core.ArchivesCorePlugin;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;

public class SaveArchivesJob extends Job {
	private Runnable runnable;
	private IPath projectPath;
	public SaveArchivesJob(IPath projectPath) {
		this(projectPath, null, null);
	}
	public SaveArchivesJob(IPath projectPath,Runnable runnable, String jobName) {
		super(jobName == null ? ArchivesCoreMessages.SaveArchivesJob : jobName);
		this.runnable = runnable;
		this.projectPath = projectPath;
	}

	protected IStatus run(IProgressMonitor monitor) {
		if( runnable != null ) {
			try {
				runnable.run();
			} catch( Exception e ) {
				IStatus status = new Status(IStatus.ERROR, ArchivesCorePlugin.PLUGIN_ID, ArchivesCoreMessages.ErrorPreSave, e);
				return status;
			}
		}
		try {
			ArchivesModel.instance().getRoot(projectPath).save(new NullProgressMonitor());
		} catch( ArchivesModelException ame ) {
			IStatus status = new Status(IStatus.ERROR, ArchivesCorePlugin.PLUGIN_ID, ArchivesCoreMessages.ErrorUpdatingModel, ame);
			return status;
		}
		return Status.OK_STATUS;
	}

}
