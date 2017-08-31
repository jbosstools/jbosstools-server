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

import static org.junit.Assert.fail;

import org.eclipse.reddeer.eclipse.ui.dialogs.PropertyDialog;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.swt.impl.clabel.DefaultCLabel;
import org.eclipse.reddeer.swt.impl.link.DefaultLink;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.archives.reddeer.archives.ui.MainPreferencePage;
import org.junit.Test;

/**
 * Tests global and local archive preferences
 * 
 * @author jjankovi
 *
 */
public class ArchivePreferencesTest extends ArchivesTestBase {

	private static String projectName = "ArchivePreferencesTest";
	
	@Test
	public void testArchivePreferences() {
		testGlobalArchivePreferences();
		testLocalArchivePreferences();
	}

	private void testGlobalArchivePreferences() {
		WorkbenchPreferenceDialog preferenceDialog = new WorkbenchPreferenceDialog();
		MainPreferencePage archivesPreferencePage = new MainPreferencePage(preferenceDialog);
		preferenceDialog.open();
		preferenceDialog.select(archivesPreferencePage);
		checkAllSettingsInArchivePreferencePage(archivesPreferencePage);
		preferenceDialog.ok();
	
	}

	private void testLocalArchivePreferences() {
		createJavaProject(projectName);
		addArchivesSupport(projectName);
		ProjectExplorer pExplorer = new ProjectExplorer();
		pExplorer.open();
		
		PropertyDialog pd = pExplorer.getProject(projectName).openProperties();
		pd.select("Project Archives");
		new DefaultLink(pd, "Configure Workspace Settings...").click();
		WorkbenchPreferenceDialog wd = new WorkbenchPreferenceDialog();
		try {
			new DefaultCLabel(wd, "Project Archives");
			wd.cancel();
			pd.cancel();
		}catch (Exception wnfe) {
			fail("Archive global preferences page was not invoked");
		}
	}

	private void checkAllSettingsInArchivePreferencePage(
			MainPreferencePage archivesPreferencePage) {
		archivesPreferencePage.enableDefaultExcludes(
				archivesPreferencePage.isIncrementalBuilderEnabled());
		
		archivesPreferencePage.showBuildErrorDialog(
				archivesPreferencePage.isBuildErrorDialogShown());
		
		archivesPreferencePage.showOutputPathNextToPackages(
				archivesPreferencePage.isOutputPathNextToPackagesShown());
		
		archivesPreferencePage.showRootDirectoryOfFilesets(
				archivesPreferencePage.isRootDirectoryOfFilesetsShown());
		
		archivesPreferencePage.showProjectAtTheRoot(
				archivesPreferencePage.isProjectAtTheRootShown());
		
		archivesPreferencePage.showAllProjectsThatContainPackages(
				archivesPreferencePage.areAllProjectsThatContainPackagesShown());
		
		archivesPreferencePage.showNodeInAllProjects(
				archivesPreferencePage.isNodeInAllProjectShown());
		
		archivesPreferencePage.enableDefaultExcludes(
				archivesPreferencePage.isDefaultExcludesEnabled());
	}
	
}
