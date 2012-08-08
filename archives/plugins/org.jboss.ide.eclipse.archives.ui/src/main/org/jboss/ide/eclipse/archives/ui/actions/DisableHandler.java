package org.jboss.ide.eclipse.archives.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.ArchivesCorePlugin;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModel;
import org.jboss.ide.eclipse.archives.core.project.ArchivesNature;
import org.jboss.ide.eclipse.archives.core.project.ProjectUtils;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;

public class DisableHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getCurrentSelectionChecked(event);
		final Object e = selection.getFirstElement();
		if (e instanceof IProject) {
			String pName = ((IProject)e).getName();
			final String jobName = NLS.bind(ArchivesUIMessages.DisableProjectArchivesJob, pName); 
			new Job(jobName) {
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask(jobName, 300);
					CoreException ce = null;
					try {
						SubProgressMonitor deletionMonitor = new SubProgressMonitor(monitor, 100);
						IResource r = ((IProject)e).getFile(IArchiveModel.DEFAULT_PACKAGES_FILE);
						if( r != null && r.exists() ) 
							r.delete(true, deletionMonitor);
						
					} catch(CoreException ce2) {
						ce = ce2;
					}
					
					IPath loc = ((IProject) e).getLocation();
					SubProgressMonitor mon1 = new SubProgressMonitor(monitor, 100);
					ProjectUtils.removeProjectNature(((IProject) e), ArchivesNature.NATURE_ID, mon1);

					SubProgressMonitor mon2 = new SubProgressMonitor(monitor, 100);
					ArchivesModel.instance().unregisterProject(loc, mon2);
					
					return ce == null ? Status.OK_STATUS : new Status(IStatus.ERROR, ArchivesCorePlugin.PLUGIN_ID, 
							"Unable to fully remove project archives support."); //$NON-NLS-1$
				}
			}.schedule();
		}
		return null;	}

}
