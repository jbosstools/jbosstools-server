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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyButtonCommand;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyCommand;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.StandardModuleRestartBehaviorController;

public class ModuleRestartSection extends ServerEditorSection {

	public ModuleRestartSection() {
	}
	private Button customizePattern;
	private Text restartPatternText;
	private SelectionListener checkboxListener;
	private ModifyListener textListener;
	protected ServerAttributeHelper helper; 
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		createUI(parent);
		DeployableServer ds = (DeployableServer)ServerConverter.getDeployableServer(server.getOriginal());
		String defaultPattern = ds.getDefaultModuleRestartPattern();
		String pattern = server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, defaultPattern);
		String useDef = server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN, Boolean.TRUE.toString());
		customizePattern.setSelection(new Boolean(useDef).booleanValue());
		restartPatternText.setEnabled(!new Boolean(useDef).booleanValue());
		restartPatternText.setText(pattern == null ? defaultPattern : pattern);
		addListeners();
	}
	
	protected void createUI(Composite parent) {
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText("Application Reload Behavior");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new FormLayout());
		Label desc = toolkit.createLabel(composite, "Customize application restart behavior on changes to project resources");
		desc.setLayoutData(FormDataUtility.createFormData2(0, 5, null, 0, 0, 5, null, 0));
		
		customizePattern = toolkit.createButton(composite, "Use default pattern", SWT.CHECK);
		customizePattern.setLayoutData(FormDataUtility.createFormData2(desc, 5, null, 0, 0, 5, null, 0));
		Label l = toolkit.createLabel(composite, "Force module restart on following regex pattern: ");
		l.setLayoutData(FormDataUtility.createFormData2(customizePattern, 5, null, 0, 0, 5, null, 0));
		restartPatternText = toolkit.createText(composite, "");
		restartPatternText.setLayoutData(FormDataUtility.createFormData2(l, 5, null, 0, 0, 5, 100, -5));
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}
	
	protected void addListeners() {
		checkboxListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetCustomizePatternCommand(server));
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		textListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetCustomPatternCommand(server));
			}
		};

		this.customizePattern.addSelectionListener(checkboxListener);
		this.restartPatternText.addModifyListener(textListener);
	}

	public class SetCustomizePatternCommand extends ServerWorkingCopyPropertyButtonCommand {
		public SetCustomizePatternCommand(IServerWorkingCopy server) {
			super(server, "Use default regular expression",  
					customizePattern, customizePattern.getSelection(), StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN, checkboxListener, true);
		}
		public void execute() {
			super.execute();
			restartPatternText.setEnabled(!customizePattern.getSelection());
		}
		public void undo() {
			super.undo();
			restartPatternText.setEnabled(!customizePattern.getSelection());
		}
	}
	
	public class SetCustomPatternCommand extends ServerWorkingCopyPropertyCommand {
		public SetCustomPatternCommand(IServerWorkingCopy server) {
			super(server, "Modify Module Restart Pattern",  
					restartPatternText, restartPatternText.getText(), 
					IDeployableServer.ORG_JBOSS_TOOLS_AS_RESTART_FILE_PATTERN, 
					textListener);
		}
		public void undo() {
			super.undo();
			validate();
		}
		public void execute() {
			super.execute();
			validate();
		}
		protected void validate() {
			try {
				Pattern.compile(restartPatternText.getText(), Pattern.CASE_INSENSITIVE);
				setErrorMessage(null); 
			} catch(PatternSyntaxException pse) {
				setErrorMessage("Invalid Restart Pattern: " + restartPatternText.getText());
			}
		}
	}
}
