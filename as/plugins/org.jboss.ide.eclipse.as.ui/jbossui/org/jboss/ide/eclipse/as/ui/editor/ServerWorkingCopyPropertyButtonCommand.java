/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wst.server.core.IServerWorkingCopy;
/**
 * Replaced by class of the same name in 
 * org.jboss.ide.eclipse.as.wtp.ui.editor package
 * inside org.jboss.ide.eclipse.as.wtp.ui plugin
 * 
 * @since 2.3
 * @deprecated
 */
public class ServerWorkingCopyPropertyButtonCommand extends 
	org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyButtonCommand {

	public ServerWorkingCopyPropertyButtonCommand(IServerWorkingCopy wc,
			String commandName, Button button, boolean newVal,
			String attributeKey, SelectionListener listener) {
		super(wc, commandName, button, newVal, attributeKey, listener);
	}
}
