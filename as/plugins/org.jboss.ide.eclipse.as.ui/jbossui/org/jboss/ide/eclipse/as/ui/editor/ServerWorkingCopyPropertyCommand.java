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

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServerWorkingCopy;


/**
 * Replaced by class of the same name in 
 * org.jboss.ide.eclipse.as.wtp.ui.editor package
 * inside org.jboss.ide.eclipse.as.wtp.ui plugin
 * 
 * @since 2.3
 * @deprecated
 */
public class ServerWorkingCopyPropertyCommand 
	extends org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyCommand {
	
	public ServerWorkingCopyPropertyCommand(IServerWorkingCopy wc, String commandName, 
			Text text, String newVal, String attributeKey, ModifyListener listener) {
		super(wc, commandName, text, newVal, attributeKey, listener);
	}
	public ServerWorkingCopyPropertyCommand(IServerWorkingCopy wc, String commandName, 
				Text text, String newVal, String attributeKey, ModifyListener listener, String defaultVal) {
		super(wc, commandName, text, newVal, attributeKey, listener, defaultVal);
	}
}