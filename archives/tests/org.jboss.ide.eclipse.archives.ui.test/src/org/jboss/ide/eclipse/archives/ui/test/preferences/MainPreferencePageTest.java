package org.jboss.ide.eclipse.archives.ui.test.preferences;

import junit.framework.TestCase;

import org.eclipse.jface.preference.PreferenceDialog;
import org.jboss.ide.eclipse.archives.ui.preferences.MainPreferencePage;
import org.jboss.tools.test.util.WorkbenchUtils;

public class MainPreferencePageTest extends TestCase {
	public void testArchivesPreferencePageIsShown() {
		PreferenceDialog dialog = WorkbenchUtils.createPreferenceDialog("org.jboss.ide.eclipse.archives.ui.archivesPreferencePage");
		try {
			dialog.open();
			Object page = dialog.getSelectedPage();
			assertTrue(page instanceof MainPreferencePage);
		} finally {
			dialog.close();
		}
	}
}
