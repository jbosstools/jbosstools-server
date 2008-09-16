package org.jboss.ide.eclipse.archives.core.build;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.ide.eclipse.archives.core.ArchivesCorePlugin;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;

public class RegisterArchivesJob extends Job {
	private IProject[] projects;
	private Runnable callback;
	public RegisterArchivesJob(IProject[] projects, Runnable callback) {
		super("Register Project Archives");
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
