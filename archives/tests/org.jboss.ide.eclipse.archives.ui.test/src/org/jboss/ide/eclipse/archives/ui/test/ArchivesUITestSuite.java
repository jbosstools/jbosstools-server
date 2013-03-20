/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.ide.eclipse.archives.ui.test.preferences.MainPreferencePageTest;
import org.jboss.ide.eclipse.archives.ui.test.views.ProjectsArchiveViewTest;
import org.jboss.ide.eclipse.archives.ui.test.wizards.NewJARWizardTest;
import org.jboss.tools.test.util.ProjectImportTestSetup;

public class ArchivesUITestSuite {
	public static final String TEST_SUITE_BUNDLE_ID = "org.jboss.ide.eclipse.archives.ui.test";
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MainPreferencePageTest.class);
		suite.addTestSuite(ProjectsArchiveViewTest.class);
		//suite.addTestSuite(FilesetWizardTest.class);
		suite.addTestSuite(NewJARWizardTest.class);
		return new ProjectImportTestSetup(suite, TEST_SUITE_BUNDLE_ID,"projects/archives-test", "archives-test");
	}
}