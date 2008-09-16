package org.jboss.ide.eclipse.archives.core.build;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
		super(jobName == null ? "Save Archives Job" : jobName);
		this.runnable = runnable;
		this.projectPath = projectPath;
	}

	protected IStatus run(IProgressMonitor monitor) {
		if( runnable != null ) { 
			try {
				runnable.run();
			} catch( Exception e ) {
				IStatus status = new Status(IStatus.ERROR, ArchivesCorePlugin.PLUGIN_ID, "Problem executing pre-save runnable", e);
				return status;
			}
		}
		try {
			ArchivesModel.instance().save(projectPath, new NullProgressMonitor());
		} catch( ArchivesModelException ame ) {
			IStatus status = new Status(IStatus.ERROR, ArchivesCorePlugin.PLUGIN_ID, "Problem saving archives model", ame);
			return status;
		}
		return Status.OK_STATUS;
	}

}
