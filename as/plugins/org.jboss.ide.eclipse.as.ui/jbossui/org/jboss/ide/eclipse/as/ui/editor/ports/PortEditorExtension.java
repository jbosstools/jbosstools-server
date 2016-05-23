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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;

public abstract class PortEditorExtension implements IPortEditorExtension {
	protected Text text;
	protected Label label;
	protected String labelText, overrideValueKey;
	protected String changeValueCommandName;
	protected ServerAttributeHelper helper;
	protected Listener listener;
	protected PortSection section;
	protected int defaultValue;
	protected ControlDecoration decoration;
	
	public PortEditorExtension(String labelText, String overrideValueKey, 
			int defaultValue, String changeValueCommandName) {
		this.labelText = labelText;
		this.overrideValueKey = overrideValueKey;
		this.changeValueCommandName = changeValueCommandName;
		this.defaultValue = defaultValue;
	}
	public void setServerAttributeHelper(ServerAttributeHelper helper) {
		this.helper = helper;
	}
	public void setSection(PortSection section) {
		this.section = section;
	}
	public void createControl(Composite parent) {
		createUI(parent);
		initialize();
		addListeners();
		
		decoration = new ControlDecoration(text,
				SWT.LEFT | SWT.TOP);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
		decoration.setImage(fieldDecoration.getImage());
		validate();
	}

	protected void createUI(Composite parent) {
		label = new Label(parent, SWT.NONE);
		text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		
		boolean typeHasRuntime = helper.getServer().getServerType().hasRuntime();
		boolean hasRuntime = helper.getServer().getRuntime() != null;
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).minSize(80, 10).grab(true, false).applyTo(text);
		
		label.setText(labelText);
	}
	protected void initialize() {
		boolean editable = getEditable();
		text.setEnabled(editable);
		text.setEditable(editable);
		text.setText(helper.getAttribute(overrideValueKey, Integer.toString(defaultValue)));
	}
	
	protected boolean getEditable() {
		return true;
	}
	
	protected int discoverOffset() {
		return section.getPortOffset();
	}
	public void refresh() {
		initialize();
	}
	protected void addListeners() {
		listener = new Listener() {
			public void handleEvent(Event event) {
				listenerEvent(event);
			}
		};
		text.addListener(SWT.Modify, listener);
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if( e.text == null || e.text.equals(""))
					return;
				try {
					Integer i = Integer.parseInt(e.text);
				} catch( NumberFormatException nfe ) {
					e.doit = false;
				}
			}
		});
	}
	protected void listenerEvent(Event event) {
		section.execute(getCommand());
	}

	public ServerCommand getCommand() {
		return new SetPortCommand(helper.getWorkingCopy(), helper, changeValueCommandName,
				overrideValueKey, this);
	}
	
	public String getValue() {
		return text.getText();
	}
	public void validate() {
		// subclasses override
		decoration.hide();
		String v  = text.getText();
		String errorText = "The empty string is not a valid port.";
		if( "".equals(v)) {
			decoration.setDescriptionText(errorText);
			decoration.show();
		}
	}
}