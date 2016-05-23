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

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Link;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;

public class SetPortXPathCommand extends SetPortCommand {
	String overrideAttribute, overridePathAttribute;
	String defaultPath;
	boolean preOverride;
	Button button;
	String xpath;
	Link link;
	public SetPortXPathCommand(IServerWorkingCopy server, ServerAttributeHelper helper, String name,
			String textAttribute, String overrideAttribute, String overridePathAttribute,
			String pathDefault, PortEditorXPathExtension ext) { //Text text, Button button, String xpath, Listener listener) {
		super(server, helper, name, textAttribute, ext);
		this.overrideAttribute = overrideAttribute;
		this.overridePathAttribute = overridePathAttribute;
		this.defaultPath = pathDefault;
		this.button = ext.detect;
		this.xpath = ext.currentXPath;
		this.link = ext.link;
	}

	public void execute() {
		preText = helper.getAttribute(textAttribute, (String)null);
		if( preText == null )
			preText = text.getText();
		prePath = helper.getAttribute(overridePathAttribute, (String)defaultPath);
		preOverride = helper.getAttribute(overrideAttribute, true);
		helper.setAttribute(textAttribute, text.getText());
		helper.setAttribute(overrideAttribute, button.getSelection());
		link.setEnabled(button.getSelection());
		helper.setAttribute(overridePathAttribute, xpath);

		text.setEnabled(!button.getSelection());
		text.setEditable(!button.getSelection());
		if( button.getSelection() ) {
			text.removeListener(SWT.Modify, listener);
			text.setText(PortSection.findPortWithDefault(helper.getServer(), new Path(xpath), this.defVal, ext.discoverOffset()));
			text.addListener(SWT.Modify, listener);
		}
		validate();
	}

	public void undo() {
		// set new values
		helper.setAttribute(textAttribute, preText);
		helper.setAttribute(overrideAttribute, preOverride);
		link.setEnabled(preOverride);
		helper.setAttribute(overridePathAttribute, prePath);
		
		// update ui
		text.removeListener(SWT.Modify, listener);
		button.removeListener(SWT.Selection, listener);

		button.setSelection(preOverride);
		text.setText(preText == null ? "" : preText); //$NON-NLS-1$
		text.setEnabled(!preOverride);
		text.setEditable(!preOverride);
		button.addListener(SWT.Selection, listener);
		text.addListener(SWT.Modify, listener);
		validate();
	}
	
	private void validate() {
		ext.validate();
	}
}