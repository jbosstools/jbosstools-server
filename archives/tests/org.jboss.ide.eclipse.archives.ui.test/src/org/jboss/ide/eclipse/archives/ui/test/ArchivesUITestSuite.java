package org.jboss.ide.eclipse.archives.ui.test;

import org.jboss.ide.eclipse.archives.ui.test.preferences.MainPreferencePageTest;
import org.jboss.ide.eclipse.archives.ui.test.wizards.FilesetWizardTest;
import org.jboss.ide.eclipse.archives.ui.test.wizards.NewJARWizardTest;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesCommonView;
import org.jboss.tools.test.util.ProjectImportTestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ArchivesUITestSuite {
	public static final String TEST_SUITE_BUNDLE_ID = "org.jboss.ide.eclipse.archives.ui.test";
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MainPreferencePageTest.class);
		suite.addTestSuite(ProjectArchivesCommonView.class);
		suite.addTestSuite(FilesetWizardTest.class);
		suite.addTestSuite(NewJARWizardTest.class);
		return new ProjectImportTestSetup(suite, TEST_SUITE_BUNDLE_ID,"projects/archive-test", "archive-test");
	}
}

