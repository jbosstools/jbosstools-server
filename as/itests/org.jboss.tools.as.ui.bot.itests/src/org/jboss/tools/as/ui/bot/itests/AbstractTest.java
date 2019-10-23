/*******************************************************************************
 * Copyright (c) 2007-2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests;

import org.eclipse.reddeer.eclipse.jdt.debug.ui.jres.JREsPreferencePage;
import org.eclipse.reddeer.eclipse.wst.server.ui.RuntimePreferencePage;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.junit.AfterClass;

/**
 * The purpose of this class is to set up/clean up environment to help isolate tests and to avoid code duplication.
 * @author jkopriva@redhat.com
 *
 */
public abstract class AbstractTest {
	
	@AfterClass
	public static void cleanUpAfterTest() {
		//Close all shells if test fails (could interfere next tests)
		WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
	}
	
	public static void deleteRuntimes() {
		WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		RuntimePreferencePage runtimePreferencePage = new RuntimePreferencePage(preferences);
		preferences.select(runtimePreferencePage);
		runtimePreferencePage.removeAllRuntimes();
		preferences.ok();
	}
    
    public static void addJRE(String name, String path) {
		WorkbenchPreferenceDialog dialog = new WorkbenchPreferenceDialog();
		dialog.open();
		JREsPreferencePage page = new JREsPreferencePage(dialog);
		dialog.select(page);
		page.addJRE(path, name);
		dialog.ok();
    }
    
    public static void removeJRE(String name) {
		WorkbenchPreferenceDialog dialog = new WorkbenchPreferenceDialog();
		dialog.open();
		JREsPreferencePage page = new JREsPreferencePage(dialog);
		dialog.select(page);
		page.deleteJRE(name);
		dialog.ok();
    }
}
