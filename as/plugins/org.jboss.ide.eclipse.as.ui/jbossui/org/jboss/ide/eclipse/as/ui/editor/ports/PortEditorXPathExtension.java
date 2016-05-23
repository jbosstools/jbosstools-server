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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.dialogs.ChangePortDialog;
import org.jboss.ide.eclipse.as.ui.dialogs.ChangePortDialog.ChangePortDialogInfo;

public abstract class PortEditorXPathExtension extends PortEditorExtension {
	protected Button detect;
	protected Link link;
	protected String currentXPathKey, detectXPathKey, defaultXPath;
	protected String currentXPath;
	protected int defaultValue;
	
	public PortEditorXPathExtension(String labelText, String currentXPathKey, 
			String detectXPathKey, String overrideValueKey, String defaultXPath,
			int defaultValue,
			String changeValueCommandName) {
		super(labelText, overrideValueKey, defaultValue, changeValueCommandName);
		this.currentXPathKey = currentXPathKey;
		this.detectXPathKey = detectXPathKey;
		this.defaultXPath = defaultXPath;
	}

	@Override
	protected void createUI(Composite parent) {
		label = new Label(parent, SWT.NONE);
		text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		
		detect = new Button(parent, SWT.CHECK);
		link = new Link(parent, SWT.NONE);
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).minSize(80, 10).grab(true, false).applyTo(text);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(detect);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(link);
		
		label.setText(labelText);
		detect.setText(Messages.EditorAutomaticallyDetectPort);
		link.setText("<a href=\"\">" + Messages.Configure + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	protected void initialize() {
		boolean shouldDetect = helper.getAttribute(detectXPathKey, true);
		detect.setSelection(shouldDetect);
		detect.setEnabled(defaultXPath != null);
		link.setEnabled(shouldDetect);
		text.setEnabled(!shouldDetect);
		text.setEditable(!shouldDetect);
		currentXPath = helper.getAttribute(currentXPathKey, defaultXPath);
		if( shouldDetect ) {
			text.setText(PortSection.findPortWithDefault(helper.getServer(), new Path(currentXPath), defaultValue, discoverOffset()));
		} else
			text.setText(helper.getAttribute(overrideValueKey, "")); //$NON-NLS-1$
	}

	@Override
	protected void addListeners() {
		super.addListeners();
		detect.addListener(SWT.Selection, listener);
		link.addListener(SWT.Selection, createLinkListener());
	}

	protected Listener createLinkListener() {
		return new Listener() {
			public void handleEvent(Event event) {
				ChangePortDialog dialog = getDialog();
				int result = dialog.open();
				if( result == Dialog.OK) {
					currentXPath = dialog.getSelection();
					section.execute(getCommand());
				}
				if( dialog.isModified() ) {
					initialize();
					validate();
				}
				text.setFocus();
			}
		};
	}
	public ChangePortDialog getDialog() {
		return new ChangePortDialog(section.getShell(), getDialogInfo());
	}
	
	@Override
	public ServerCommand getCommand() {
		return new SetPortXPathCommand(helper.getWorkingCopy(), helper, changeValueCommandName,
				overrideValueKey, detectXPathKey,currentXPathKey, defaultXPath, this);
	}
	protected ChangePortDialogInfo getDialogInfo() {
		ChangePortDialogInfo info = new ChangePortDialogInfo();
		info.port = labelText;
		info.defaultValue = defaultXPath;
		info.server = helper.getWorkingCopy().getOriginal();
		info.currentXPath = currentXPath;
		return info;
	}

	@Override
	public void validate() {
		decoration.hide();
		String v = null;
		String errorText;
		if( detect.getSelection()) {
			v = PortSection.findPort(helper.getServer(), new Path(defaultXPath));
			errorText = "This port cannot be automatically located. A default value is being displayed";
		} else {
			v = text.getText();
			errorText = "The empty string is not a valid port.";
		}
		if( "".equals(v)) {
			decoration.setDescriptionText(errorText);
			decoration.show();
		}
	}
}