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
