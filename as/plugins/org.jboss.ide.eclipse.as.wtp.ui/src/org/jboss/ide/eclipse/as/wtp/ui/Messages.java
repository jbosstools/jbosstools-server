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
package org.jboss.ide.eclipse.as.wtp.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.wtp.ui.messages"; //$NON-NLS-1$
	public static String MODULE_EXISTS_ERROR;
	public static String DESTINATION_INVALID;
	public static String DESTINATION_ARCHIVE_SHOULD_END_WITH;
	public static String RESOURCE_EXISTS_ERROR;
	public static String IS_READ_ONLY;
	public static String Export_LabelDestination;
	public static String Export_WizardTitle;
	public static String Export_PageTitle;
	public static String Export_PageDescription;
	public static String Export_LabelProject;
	public static String Export_LabelBrowse;
	public static String Export_OverwriteCheckbox;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
