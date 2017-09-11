package org.jboss.tools.as.ui.bot.itests.reddeer.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.reddeer.eclipse.wst.server.ui.RuntimePreferencePage;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.as.ui.bot.itests.parametized.CleanEnvironmentUtils;
import org.jboss.tools.as.ui.bot.itests.reddeer.ui.RuntimeDetectionPreferencePage;
import org.jboss.tools.as.ui.bot.itests.reddeer.ui.SearchingForRuntimesDialog;
import org.jboss.tools.runtime.core.model.RuntimePath;
import org.jboss.tools.runtime.ui.RuntimeUIActivator;

/**
 * Provides useful methods that can be used by its descendants. 
 * 
 * @author Lucia Jelinkova
 *
 */
public class RuntimeDetectionUtility {

	public static SearchingForRuntimesDialog addPath(String path){
		RuntimeUIActivator.getDefault().getModel().addRuntimePath(new RuntimePath(new File(path).getAbsolutePath()));
		WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		RuntimeDetectionPreferencePage runtimeDetectionPage = new RuntimeDetectionPreferencePage(preferences);
		preferences.select(runtimeDetectionPage);
		if(!runtimeDetectionPage.getAllPaths().contains(path)) {
			preferences.cancel();
			preferences.open();
			preferences.select(runtimeDetectionPage);
		}
		return runtimeDetectionPage.search();
	}

	public static SearchingForRuntimesDialog searchFirstPath(){
		WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		RuntimeDetectionPreferencePage runtimeDetectionPage = new RuntimeDetectionPreferencePage(preferences);
		preferences.select(runtimeDetectionPage);
		return runtimeDetectionPage.search();
	}

	public static void removeAllPaths(){
		CleanEnvironmentUtils.cleanPaths();
	}

	public static void removeAllServerRuntimes(){
		CleanEnvironmentUtils.cleanServerRuntimes();
	}


	public static void assertServerRuntimesNumber(int expected) {
		WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		RuntimePreferencePage runtimePreferencePage = new RuntimePreferencePage(preferences);
		preferences.select(runtimePreferencePage);
		List<org.eclipse.reddeer.eclipse.wst.server.ui.Runtime> runtimes = 
				runtimePreferencePage.getServerRuntimes();
		assertThat("Expected are " + expected + " runtimes but there are:\n"
				+ Arrays.toString(runtimes.toArray()), runtimes.size(), is(expected));
		preferences.ok();
	}
}
