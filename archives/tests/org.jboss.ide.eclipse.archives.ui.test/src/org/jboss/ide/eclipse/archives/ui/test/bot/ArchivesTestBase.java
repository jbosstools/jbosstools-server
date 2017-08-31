/*******************************************************************************
 * Copyright (c) 2010-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui.test.bot;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.eclipse.jdt.ui.wizards.JavaProjectWizard;
import org.eclipse.reddeer.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.eclipse.ui.perspectives.JavaPerspective;
import org.eclipse.reddeer.eclipse.ui.views.log.LogView;
import org.eclipse.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.archives.reddeer.archives.ui.NewJarDialog;
import org.jboss.tools.archives.reddeer.archives.ui.ProjectArchivesExplorer;
import org.jboss.tools.archives.reddeer.archives.ui.ProjectArchivesView;
import org.junit.BeforeClass;

/**
 * 
 * @author jjankovi
 *
 */
@OpenPerspective(JavaPerspective.class)
@CleanWorkspace
public class ArchivesTestBase{

	protected static ProjectExplorer projectExplorer = new ProjectExplorer();
	protected static ProjectArchivesView view = new ProjectArchivesView();
	protected static final Logger log = Logger.getLogger(ArchivesTestBase.class);
	
	@BeforeClass
	public static void maximizeWorkbench(){
		new WorkbenchShell().maximize();
	}
	
	protected ProjectArchivesView openProjectArchivesView() {
		view.open();
		return view;
	}
	
	protected static ProjectArchivesView viewForProject(String projectName) {
		view.open();
		projectExplorer.open();
		projectExplorer.getProject(projectName).select();
		view.open();
		return view;
	}
	
	protected ProjectArchivesExplorer explorerForProject(String projectName) {
		return new ProjectArchivesExplorer(projectName);
	}
	
	protected void assertArchiveIsInView(String project, ProjectArchivesView view, String archiveName) {
		view.open();
		assertTrue(view.getProject(project).hasArchive(archiveName));
	}
	
	protected void assertArchiveIsNotInView(String project, ProjectArchivesView view, String archiveName) {
		view.open();
		assertFalse(view.getProject(project).hasArchive(archiveName));
	}
	
	protected void assertArchiveIsInExplorer(ProjectArchivesExplorer explorer, 
			String archiveName) {
		try {
			explorer.getArchive(archiveName);
		} catch (Exception sle) {
			fail("'" + archiveName + "' is not in archives explorer but it should!");
		}
	}
	
	protected void assertArchiveIsNotInExplorer(ProjectArchivesExplorer explorer, 
			String archiveName) {
		try {
			explorer.getArchive(archiveName);
			fail("'" + archiveName + "' is in archives explorer but it should not!");
		} catch (Exception sle) {
		}
	}
	
	protected static void deleteErrorView() {
		LogView lw = new LogView();
		lw.open();
		lw.deleteLog();
	}
	
	protected static void createJavaProject(String projectName) {
		JavaProjectWizard javaProject = new JavaProjectWizard();
		javaProject.open();
		
		NewJavaProjectWizardPageOne javaWizardPage = new NewJavaProjectWizardPageOne(javaProject);
		javaWizardPage.setProjectName(projectName);
		
		javaProject.finish();
	}
	
	protected static void addArchivesSupport(String projectName) {
		addRemoveArchivesSupport(projectName, true);
	}
	
	protected static void removeArchivesSupport(String projectName) {
		addRemoveArchivesSupport(projectName, false);
	}
	
	private static void addRemoveArchivesSupport(String projectName, boolean add) {
		ProjectExplorer pe = new ProjectExplorer();
		pe.open();
		pe.getProject(projectName).select();
		if (add) {
			new ContextMenuItem("Configure", "Add Project Archives Support").select();
		} else {
			new ContextMenuItem("Configure", "Remove Project Archives Support").select();
		}
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
	}
	
	protected static void createArchive(NewJarDialog dialog, String archiveName, boolean standardCompression) {
		dialog.setArchiveName(archiveName);
		if (standardCompression) {
			dialog.setZipStandardArchiveType();
		} else {
			dialog.setNoCompressionArchiveType();
		}
		dialog.finish();
	}
	
	protected static void createArchive(String project, String archiveName, boolean standardCompression) {
		view = viewForProject(project);
		NewJarDialog dialog = view.getProject(project).newJarArchive();
		dialog.setArchiveName(archiveName);
		if (standardCompression) {
			dialog.setZipStandardArchiveType();
		} else {
			dialog.setNoCompressionArchiveType();
		}
		dialog.finish();
	}
	
}
