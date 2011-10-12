/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.express.internal.ui.wizard.projectimport;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * @author Andre Dietisheim <adietish@redhat.com>
 * 
 */
public class GeneralProjectImportOperation extends AbstractProjectImportOperation {

	public GeneralProjectImportOperation(File projectDirectory) {
		super(projectDirectory);
	}

	public List<IProject> importToWorkspace(IProgressMonitor monitor)
			throws CoreException, InterruptedException {
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject(getProjectDirectory().getName());
		overwriteExistingProject(project, monitor);
		return Collections.singletonList(project);
	}

	private void importToNewProject(File projectDirectory, IProject project, IProgressMonitor monitor)
			throws CoreException, InvocationTargetException, InterruptedException {
		project.create(monitor);
		project.open(monitor);
		ImportOperation operation =
				new ImportOperation(
						project.getFullPath()
						, projectDirectory
						, FileSystemStructureProvider.INSTANCE
						, new IOverwriteQuery() {
							public String queryOverwrite(String file) {
								return IOverwriteQuery.ALL;
							}
						});
		operation.setCreateContainerStructure(false);
		operation.run(monitor);
	}

	private void overwriteExistingProject(final IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (project == null
				|| !project.exists()) {
			return;
		}

		final boolean[] overwrite = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				overwrite[0] = MessageDialog.openQuestion(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Overwrite project?",
						NLS.bind(
								"A project \"{0}\" already exists in the workspace.\n"
										+ "If you want to import the OpenShift \"{0}\", the project in your workspace will "
										+ "get overwritten and may not be recovered.\n\n"
										+ "Are you sure that you want to overwrite the project \"{0}\" in your workspace?",
								project.getName()));
			}

		});
		if (overwrite[0]) {
			project.delete(true, true, monitor);
		}
	}
}
