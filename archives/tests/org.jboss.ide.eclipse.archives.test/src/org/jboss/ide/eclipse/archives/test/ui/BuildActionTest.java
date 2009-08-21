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
package org.jboss.ide.eclipse.archives.test.ui;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.ide.eclipse.archives.ui.actions.BuildAction;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.WrappedProject;
import org.jboss.tools.test.util.ResourcesUtils;

public class BuildActionTest extends TestCase {
	private IProject project;
	private boolean waiting = true;
	private boolean scheduled = false;
	private JobChangeAdapter jobChangeAdapter;
	private CoreException ce;
	protected void setUp() throws Exception {
		project = ResourcesUtils.importProject(ArchivesTest.PLUGIN_ID,
				"inputs" + Path.SEPARATOR + "projects" + Path.SEPARATOR + "JBIDE2099");
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		Job.getJobManager().addJobChangeListener(getJobChangeAdapter());
	}

	protected void tearDown() throws Exception {
		ResourcesUtils.deleteProject("JBIDE2099");
	}

	protected JobChangeAdapter getJobChangeAdapter() {
		jobChangeAdapter = new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if( event.getJob().getClass().getName().startsWith("org.jboss.ide.eclipse.archives.ui.actions.BuildAction")) {
					IStatus result = event.getResult();
					System.out.println(result);
					if( !result.isOK())
						ce = new CoreException(result);
					waiting = false;
				}
			}
			public void scheduled(IJobChangeEvent event) {
				if( event.getJob().getClass().getName().startsWith("org.jboss.ide.eclipse.archives.ui.actions.BuildAction")) {
					scheduled = true;
				}
			}
		};
		return jobChangeAdapter;
	}
	
	public void testBuildAction() throws Exception {
		ArchivesModel.instance().registerProject(project.getLocation(), new NullProgressMonitor());
		BuildAction action = new BuildAction();
		waiting = true;
		
		action.run(project);
		waitForGo();

		ISelection sel = new StructuredSelection(new Object[]{project});
		action.selectionChanged(null, sel);
		action.run(null);
		waitForGo();
		
		
		
		action.run(new WrappedProject(project, 0));
		waitForGo();

		sel = new StructuredSelection(new Object[] { new WrappedProject(project,0)});
		action.selectionChanged(null, sel);
		action.run(null);
		waitForGo();
		
		
		
		IArchiveModelRootNode root = ArchivesModel.instance().getRoot(project.getLocation());
		IArchiveNode[] children = root.getAllChildren();
		action.run(children[0]);
		waitForGo();
		
		sel = new StructuredSelection(children);
		action.selectionChanged(null, sel);
		action.run(null);
		waitForGo();
	}

	protected void waitForGo() throws Exception {
		if( !scheduled ) 
			fail("Job not scheduled");
		while(waiting) 
			try {
				Thread.sleep(500);
			} catch(InterruptedException ie) {}
		
		// finished, now start waiting again
		waiting = true;
		scheduled = false;
		
		if( ce != null ) {
			IStatus s = ce.getStatus();
			String message = s.getMessage() + '\n';
			if( s instanceof MultiStatus ) {
				IStatus[] children = ((MultiStatus)s).getChildren();
				for( int i = 0; i < children.length; i++ ) 
					message += children[i].getMessage() + '\n';
			}
			fail(message);
		}
	}
}
