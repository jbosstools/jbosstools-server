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
	public static String ModuleRestartSection_0;
	public static String ModuleRestartSection_1;
	public static String ModuleRestartSection_2;
	public static String ModuleRestartSection_3;
	public static String ModuleRestartSection_5;
	public static String ModuleRestartSection_appReloadCommandName;
	public static String ModuleRestartSection_appReloadPatternCommandName;
	public static String ModuleRestartSection_hcrBehaviorCommandName;
	public static String ModuleRestartSection_hcrComboLabel;
	public static String ModuleRestartSection_hcrOverrideCommandName;
	public static String ModuleRestartSection_invalidPatternError;
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
	
	
	public static String ServerAlreadyStartedDialog_Message;
	public static String ServerAlreadyStartedDialog_Title;
	public static String ServerAlreadyStartedDialog_Desc;
	public static String ServerAlreadyStartedDialog_Connect;
	public static String ServerAlreadyStartedDialog_Launch;

	
	public static String rwf_DefaultJREForExecEnv;
	public static String wf_JRELabel;
	public static String wf_HomeDirLabel;
	public static String browse;
	public static String wf_NameLabel;
	public static String rwf_NameInUse;
	public static String rwf_noValidJRE;
	public static String rwf_nameTextBlank;
	public static String rwf_jboss7homeNotValid;
	public static String rwf_homeIncorrectVersionError; 
	public static String rwf_incompatibleJRE;
	public static String rwf_incompatibleJREMinMax;

	
	public static String HotCodeReplaceObsolete_Title;
	public static String HotCodeReplaceFailed_Title;
	public static String HotCodeReplaceHeader;
	public static String HotCodeReplaceDesc;
	public static String RememberChoiceServer;
	public static String hcrRestartServer;
	public static String hcrRestartModules;
	public static String hcrContinue;
	public static String hcrShowDialog;
	public static String hcrTerminate;

	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
