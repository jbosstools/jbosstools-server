/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.ui.editor.messages"; //$NON-NLS-1$
	public static String ModuleRestartSection_arbCustomPatternCommand;
	public static String ModuleRestartSection_arbDefaultPatternCommand;
	public static String ModuleRestartSection_arbDefaultPatternLabel;
	public static String ModuleRestartSection_arbDesc;
	public static String ModuleRestartSection_arbRegexLabel;
	public static String ModuleRestartSection_hcrBehaviorCommand;
	public static String ModuleRestartSection_hcrFailureBehavior;
	public static String ModuleRestartSection_hcrOverrideCommand;
	public static String ModuleRestartSection_interceptHCR;
	public static String ModuleRestartSection_invalidRegex;
	public static String ModuleRestartSection_title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
