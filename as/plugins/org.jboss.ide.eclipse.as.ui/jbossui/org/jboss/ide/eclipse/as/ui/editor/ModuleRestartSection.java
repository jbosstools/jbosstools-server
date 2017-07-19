/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
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
import org.eclipse.swt.widgets.Combo;
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
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.ServerHotCodeReplaceListener;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyButtonCommand;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyCommand;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;
import org.jboss.ide.eclipse.as.wtp.ui.editor.ServerWorkingCopyPropertyComboCommand;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.StandardModuleRestartBehaviorController;

public class ModuleRestartSection extends ServerEditorSection {

	public ModuleRestartSection() {
	}
	private Button useDefaultPattern, overrideHotcodeButton;
	private Combo hotcodeCombo;
	private Text restartPatternText;
	private SelectionListener checkboxListener;
	private ModifyListener comboListener;
	private SelectionListener hotcodeCheckboxListener;
	
	private ModifyListener textListener;
	protected ServerAttributeHelper helper; 
	
	
	private String[] hotcodeReplaceStrings;
	private int[] hotcodeReplaceCodes;
	
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
		boolean useDefB = new Boolean(useDef).booleanValue();
		useDefaultPattern.setSelection(useDefB);
		restartPatternText.setEnabled(!useDefB);
		restartPatternText.setText((pattern == null || useDefB) ? defaultPattern : pattern);
		
		boolean customizeHotcode = server.getAttribute(ServerHotCodeReplaceListener.PROPERTY_HOTCODE_REPLACE_OVERRIDE, true);
		int customizedBehavior = server.getAttribute(ServerHotCodeReplaceListener.PROPERTY_HOTCODE_BEHAVIOR, ServerHotCodeReplaceListener.PROMPT);
		overrideHotcodeButton.setSelection(customizeHotcode);
		hotcodeCombo.setEnabled(customizeHotcode);
		hotcodeCombo.select(customizedBehavior);
		addListeners();
	}
	
	protected void createUI(Composite parent) {
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText(Messages.ModuleRestartSection_title);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new FormLayout());
		Label desc = toolkit.createLabel(composite, Messages.ModuleRestartSection_arbDesc);
		desc.setLayoutData(FormDataUtility.createFormData2(0, 5, null, 0, 0, 5, null, 0));
		
		Label l = toolkit.createLabel(composite, Messages.ModuleRestartSection_arbRegexLabel);
		l.setLayoutData(FormDataUtility.createFormData2(desc, 5, null, 0, 0, 5, null, 0));
		useDefaultPattern = toolkit.createButton(composite, Messages.ModuleRestartSection_arbDefaultPatternLabel, SWT.CHECK);
		useDefaultPattern.setLayoutData(FormDataUtility.createFormData2(l, 5, null, 0, 0, 5, null, 0));
		restartPatternText = toolkit.createText(composite, ""); //$NON-NLS-1$
		restartPatternText.setLayoutData(FormDataUtility.createFormData2(useDefaultPattern, 5, null, 0, 0, 5, 100, -5));
		
		
		overrideHotcodeButton = toolkit.createButton(composite, Messages.ModuleRestartSection_interceptHCR, SWT.CHECK);
		overrideHotcodeButton.setLayoutData(FormDataUtility.createFormData2(restartPatternText, 5, null, 0, 0, 5, 100, -5));
		
		Label hotcodeComboLabel = toolkit.createLabel(composite, Messages.ModuleRestartSection_hcrFailureBehavior);
		hotcodeComboLabel.setLayoutData(FormDataUtility.createFormData2(overrideHotcodeButton, 5, null, 0, 0, 5, null, 0));
		
		hotcodeCombo = new Combo(composite, SWT.READ_ONLY);
		hotcodeCombo.setLayoutData(FormDataUtility.createFormData2(overrideHotcodeButton, 5, null, 0, hotcodeComboLabel, 5, 100, -5));
		hotcodeReplaceStrings = new String[]{
				org.jboss.ide.eclipse.as.wtp.ui.Messages.hcrShowDialog,	
				org.jboss.ide.eclipse.as.wtp.ui.Messages.hcrRestartModules,
				org.jboss.ide.eclipse.as.wtp.ui.Messages.hcrTerminate,
				org.jboss.ide.eclipse.as.wtp.ui.Messages.hcrRestartServer,
				org.jboss.ide.eclipse.as.wtp.ui.Messages.hcrContinue
		};
		hotcodeCombo.setItems(hotcodeReplaceStrings);
		hotcodeReplaceCodes = new int[]{
				ServerHotCodeReplaceListener.PROMPT,
				ServerHotCodeReplaceListener.RESTART_MODULE,
				ServerHotCodeReplaceListener.TERMINATE,
				ServerHotCodeReplaceListener.RESTART_SERVER,
				ServerHotCodeReplaceListener.CONTINUE
		};
		
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

		this.useDefaultPattern.addSelectionListener(checkboxListener);
		this.restartPatternText.addModifyListener(textListener);
		
		
		// Hotcode replace
		hotcodeCheckboxListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				execute(new SetOverrideHotcodeReplaceCommand(server));
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		this.overrideHotcodeButton.addSelectionListener(hotcodeCheckboxListener);
		
		// Hotcode replace
		comboListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetHotcodeReplaceBehaviorCommand(server));
			}
		};
		this.hotcodeCombo.addModifyListener(comboListener);

		
	}

	public class SetOverrideHotcodeReplaceCommand extends ServerWorkingCopyPropertyButtonCommand {
		public SetOverrideHotcodeReplaceCommand(IServerWorkingCopy server) {
			super(server, Messages.ModuleRestartSection_hcrOverrideCommand,  
					overrideHotcodeButton, overrideHotcodeButton.getSelection(), 
					ServerHotCodeReplaceListener.PROPERTY_HOTCODE_REPLACE_OVERRIDE, 
					hotcodeCheckboxListener, true);
		}
		public void execute() {
			super.execute();
			updateCombo();
		}
		public void undo() {
			super.undo();
			updateCombo();
		}
		private void updateCombo() {
			hotcodeCombo.setEnabled(overrideHotcodeButton.getSelection());
		}
	}
	
	
	
	public class SetCustomizePatternCommand extends ServerWorkingCopyPropertyButtonCommand {
		public SetCustomizePatternCommand(IServerWorkingCopy server) {
			super(server, Messages.ModuleRestartSection_arbDefaultPatternCommand,  
					useDefaultPattern, useDefaultPattern.getSelection(), StandardModuleRestartBehaviorController.PROPERTY_USE_DEFAULT_RESTART_PATTERN, checkboxListener, true);
		}
		public void execute() {
			super.execute();
			updatePatternText();
		}
		public void undo() {
			super.undo();
			updatePatternText();
		}
		private void updatePatternText() {
			restartPatternText.setEnabled(!useDefaultPattern.getSelection());
			restartPatternText.removeModifyListener(textListener);
			String newPattern = useDefaultPattern.getSelection() ? StandardModuleRestartBehaviorController.RESTART_DEFAULT_FILE_PATTERN : 
				server.getAttribute(StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN,StandardModuleRestartBehaviorController.RESTART_DEFAULT_FILE_PATTERN);
			restartPatternText.setText(newPattern);
			restartPatternText.addModifyListener(textListener);
		}
	}
	
	public class SetCustomPatternCommand extends ServerWorkingCopyPropertyCommand {
		public SetCustomPatternCommand(IServerWorkingCopy server) {
			super(server, Messages.ModuleRestartSection_arbCustomPatternCommand,  
					restartPatternText, restartPatternText.getText(), 
					StandardModuleRestartBehaviorController.PROPERTY_RESTART_FILE_PATTERN, 
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
				setErrorMessage(Messages.ModuleRestartSection_invalidRegex + restartPatternText.getText());
			}
		}
	}
	
	
	public class SetHotcodeReplaceBehaviorCommand extends ServerWorkingCopyPropertyComboCommand {
		public SetHotcodeReplaceBehaviorCommand(IServerWorkingCopy server) {
			super(server, Messages.ModuleRestartSection_hcrBehaviorCommand,  hotcodeCombo,   
					hotcodeCombo.getSelectionIndex() == -1 ? "" : Integer.toString(hotcodeReplaceCodes[hotcodeCombo.getSelectionIndex()]), //$NON-NLS-1$
					ServerHotCodeReplaceListener.PROPERTY_HOTCODE_BEHAVIOR, 
					comboListener);
		}	
		protected String getStringForValue(String value) {
			for( int i = 0; i < hotcodeReplaceCodes.length; i++ ) {
				if( Integer.toString(hotcodeReplaceCodes[i]).equals(value)) {
					return hotcodeReplaceStrings[i];
				}
			}
			return value;
		}
	}
}
