/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards.composite;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.debug.ui.jres.JREsPreferencePage;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractLocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.wizards.ServerProfileWizardFragment;

public class JREComposite extends Composite {

	private TaskModel taskModel;
	private Label installedJRELabel;
	private Combo jreCombo;
	private Button jreButton;

	private int defaultVMIndex;
	private List<IVMInstall> installedJREs;
	private String[] jreNames;
	private int jreComboIndex;
	protected IVMInstall selectedVM;
	
	public static interface IJRECompositeListener {
		public void vmChanged(JREComposite comp);
	}
	
	private IJRECompositeListener listener;
	
	public JREComposite(Composite parent, int style, TaskModel tm) {
		super(parent, style);
		this.taskModel = tm;
		updateJREs();
		createJREComposite(this);
		fillJREWidgets(getRuntimeFromTaskModel());
	}
	
	public IVMInstall getSelectedVM() {
		return selectedVM;
	}
	
	public void setListener(IJRECompositeListener listener) {
		this.listener = listener;
	}
	
	protected void createJREComposite(Composite main) {
		setLayout(new FormLayout());

		// Create Internal Widgets
		installedJRELabel = new Label(this, SWT.NONE);
		installedJRELabel.setText(Messages.wf_JRELabel);

		jreCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		jreCombo.setItems(jreNames);
		if( defaultVMIndex != -1 )
			jreCombo.select(defaultVMIndex);
		
		jreButton = new Button(this, SWT.NONE);
		jreButton.setText(Messages.wf_JRELabel);

		// Add action listeners
		jreButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				jreButtonPressed();
			}
		});

		jreCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				vmChanged();
			}
		});

		// Set Layout Data
		FormData labelData = new FormData();
		FormData comboData = new FormData();
		FormData buttonData = new FormData();

		labelData.left = new FormAttachment(0, 0);
		installedJRELabel.setLayoutData(labelData);

		comboData.left = new FormAttachment(0, 5);
		comboData.right = new FormAttachment(jreButton, -5);
		comboData.top = new FormAttachment(installedJRELabel, 5);
		jreCombo.setLayoutData(comboData);

		buttonData.top = new FormAttachment(installedJRELabel, 5);
		buttonData.right = new FormAttachment(100, 0);
		jreButton.setLayoutData(buttonData);

	}
	
	protected void jreButtonPressed() {
		String currentVM = jreCombo.getText();
		if (showPreferencePage()) {
			updateJREs();
			jreCombo.setItems(jreNames);
			jreCombo.setText(currentVM);
			if (jreCombo.getSelectionIndex() == -1)
				jreCombo.select(defaultVMIndex);
			jreComboIndex = jreCombo.getSelectionIndex();
			vmChanged();
		}
	}
	
	// Other
	protected boolean showPreferencePage() {
		PreferenceManager manager = PlatformUI.getWorkbench()
				.getPreferenceManager();
		IPreferenceNode node = manager
				.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage") //$NON-NLS-1$
				.findSubNode(
						"org.eclipse.jdt.debug.ui.preferences.VMPreferencePage"); //$NON-NLS-1$
		PreferenceManager manager2 = new PreferenceManager();
		manager2.addToRoot(node);
		final PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				JREsPreferencePage.ID, 
				new String[] {}, 
				null);
		if (dialog.open() == Window.OK)
			return true;
		return false;
	}
	
	// JRE methods
	protected void updateJREs() {
		defaultVMIndex = 0;

		// get all valid JVMs
		IVMInstall runtimesInstall = getStoredJRE(getRuntimeFromTaskModel());
		
		installedJREs = getValidJREs();
		// get names
		int size = installedJREs.size();
		int index = 0;
		jreNames = new String[size+1];
		jreNames[index++] = NLS.bind(Messages.rwf_DefaultJREForExecEnv, 
				getExecutionEnvironmentId());
		 
		for (int i = 0; i < installedJREs.size(); i++) {
			IVMInstall vmInstall = installedJREs.get(i);
			if( vmInstall.equals(runtimesInstall)) {
				defaultVMIndex = index;
			}
			jreNames[index++] = vmInstall.getName();
		}
	}
	
	protected void fillJREWidgets(IRuntime rt) {
		if (isUsingDefaultJRE(rt)) {
			jreCombo.select(0);
		} else {
			IVMInstall install = getStoredJRE(rt);
			if( install != null ) {
				selectedVM = install;
				String vmName = install.getName();
				String[] jres = jreCombo.getItems();
				for (int i = 0; i < jres.length; i++) {
					if (vmName.equals(jres[i]))
						jreCombo.select(i);
				}
			}
		}
		jreComboIndex = jreCombo.getSelectionIndex();
		if( jreCombo.getSelectionIndex() < 0 && jreCombo.getItemCount() > 0)
			jreCombo.select(0);
		
		boolean isWC = rt instanceof IRuntimeWorkingCopy;
		jreCombo.setEnabled(isWC);
		jreButton.setEnabled(isWC);
	}
	
	
	protected IRuntime getRuntimeFromTaskModel() {
		IRuntime r = (IRuntime) taskModel.getObject(TaskModel.TASK_RUNTIME);
		if( r == null ) {
			r = (IRuntime) taskModel.getObject(ServerProfileWizardFragment.TASK_CUSTOM_RUNTIME);
		}
		return r;
	}
	
	private void vmChanged() {
		jreComboIndex = jreCombo.getSelectionIndex();
		int offset = -1;
		if( jreComboIndex + offset >= 0 )
			selectedVM = installedJREs.get(jreComboIndex + offset);
		else // if sel < 0 or sel == 0 and offset == -1
			selectedVM = null;
		
		if( listener != null ) {
			listener.vmChanged(this);
		}
	}
	
	/*
	 * Below are methods that a subclass may override if they have
	 * their own way of getting access to a list of vm's etc. 
	 */
	
	public IExecutionEnvironment getExecutionEnvironment() {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return jbsrt.getExecutionEnvironment();
	}
	
	protected String getExecutionEnvironmentId() {
		IExecutionEnvironment env = getExecutionEnvironment();
		return env == null ? null : env.getId();
	}

	protected boolean isUsingDefaultJRE(IRuntime rt) {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return jbsrt.isUsingDefaultJRE();
	}
	
	protected IVMInstall getStoredJRE(IRuntime rt) {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return jbsrt.getHardVM();
	}

	public List<IVMInstall> getValidJREs() {
		IRuntime r = getRuntimeFromTaskModel();
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return Arrays.asList(jbsrt.getValidJREs(getRuntimeFromTaskModel().getRuntimeType()));
	}
	
}
