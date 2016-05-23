/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor.ports;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;

public class SetPortCommand extends ServerCommand {
	ServerAttributeHelper helper;
	String textAttribute;
	String preText, prePath;
	Text text;
	Listener listener;
	PortSection pSection;
	int defVal;
	PortEditorExtension ext;
	
	public SetPortCommand(IServerWorkingCopy server, ServerAttributeHelper helper, String name,
			String textAttribute, PortEditorExtension ext) {
		super(server, name);
		this.helper = helper;
		this.textAttribute = textAttribute;
		this.text = ext.text;
		this.listener = ext.listener;
		this.pSection = ext.section;
		this.defVal = ext.defaultValue;
		this.ext = ext;
	}

	public void execute() {
		preText = helper.getAttribute(textAttribute, (String)null);
		if( preText == null )
			preText = text.getText();
		helper.setAttribute(textAttribute, text.getText());
		validate();
	}

	public void undo() {
		// set new values
		helper.setAttribute(textAttribute, preText);
		
		// update ui
		text.removeListener(SWT.Modify, listener);
		text.setText(preText == null ? "" : preText); //$NON-NLS-1$
		text.addListener(SWT.Modify, listener);
		validate();
	}
	
	private void validate() {
		ext.validate();
	}
}