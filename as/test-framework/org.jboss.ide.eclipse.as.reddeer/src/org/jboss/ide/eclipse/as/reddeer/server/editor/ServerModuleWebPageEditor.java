/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.server.editor;

import org.eclipse.reddeer.common.matcher.RegexMatcher;

/**
 * This editor is displayed when user clicks on a specific server 
 * module and selects Show in -> Web Browser
 * 
 * Note: this should be replaced once WebPageEditor is implemented in RD
 * @author Lucia Jelinkova
 *
 */
public class ServerModuleWebPageEditor extends AbstractEditorWithBrowser {

	public ServerModuleWebPageEditor(String moduleName) {
		super(new RegexMatcher(".*" + moduleName + ".*"));
	}
}
