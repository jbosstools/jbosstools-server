/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.ui.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.rse.core.RSEJbossLaunchConstants;
import org.jboss.ide.eclipse.as.ui.editor.PortSection;import org.jboss.ide.eclipse.as.ui.editor.ModuleRestartSection.SetOverrideHotcodeReplaceCommand;
import org.jboss.ide.eclipse.as.ui.editor.PortSection.IPortEditorExtension;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyButtonCommand;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyTextCommand;

public class DebugPortSection implements IPortEditorExtension {
	private ServerAttributeHelper helper;
	private PortSection section;
	
	public DebugPortSection() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setServerAttributeHelper(ServerAttributeHelper helper) {
		this.helper = helper;
	}

	@Override
	public void setSection(PortSection section) {
		this.section = section;
	}

	@Override
	public void createControl(Composite parent) {
		String profile = ServerProfileModel.getProfile(helper.getOriginal());
		if( profile.contains("rse")) {
			final Button check = new Button(parent,SWT.CHECK);
			check.setText("Launch remote debugger");
			GridData gd = new GridData();
			gd.horizontalSpan = 4;
			check.setLayoutData(gd);
			Label l2 = new Label(parent, SWT.NONE);
			l2.setText("Debug Port");
			final Text portText = new Text(parent, SWT.BORDER);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).minSize(80, 10).grab(true, false).applyTo(portText);
			
			portText.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent e) {
					if( e.text == null || e.text.equals(""))
						return;
					try {
						Integer i = Integer.parseInt(e.text);
						if( i.intValue() == 0 ) {
							// do not allow port 0
							e.doit = false;
						}
					} catch( NumberFormatException nfe ) {
						e.doit = false;
					}
				}
			});

			Boolean b = helper.getWorkingCopy().getAttribute(RSEJbossLaunchConstants.ATTACH_DEBUGGER, true);
			check.setSelection(b.booleanValue());
			check.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					ServerWorkingCopyPropertyButtonCommand command = new ServerWorkingCopyPropertyButtonCommand(
							helper.getWorkingCopy(), "Enable debugging", check, check.getSelection(), 
							RSEJbossLaunchConstants.ATTACH_DEBUGGER, this);
					section.execute(command);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
			
			String dbugPort = helper.getWorkingCopy().getAttribute(RSEJbossLaunchConstants.DEBUG_PORT, new Integer(RSEJbossLaunchConstants.DEFAULT_DEBUG_PORT).toString());
			portText.setText(dbugPort);
			portText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					ServerWorkingCopyPropertyTextCommand command = new ServerWorkingCopyPropertyTextCommand(
							helper.getWorkingCopy(), "Change Debug Port", portText, portText.getText(), 
							RSEJbossLaunchConstants.DEBUG_PORT, new Integer(RSEJbossLaunchConstants.DEFAULT_DEBUG_PORT).toString(), this);
					section.execute(command);
				}
			});
		}
	}

	@Override
	public String getValue() {
		// irrelevant / unused
		return null;
	}

	@Override
	public void refresh() {
		// unaffected by port offset
	}

}
