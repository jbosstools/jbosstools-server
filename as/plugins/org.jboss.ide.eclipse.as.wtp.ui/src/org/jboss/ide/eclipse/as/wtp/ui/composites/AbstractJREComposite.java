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
package org.jboss.ide.eclipse.as.wtp.ui.composites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.debug.ui.jres.ExecutionEnvironmentsPreferencePage;
import org.eclipse.jdt.internal.debug.ui.jres.JREsPreferencePage;
import org.eclipse.jdt.internal.launching.environments.EnvironmentsManager;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.wtp.core.util.VMInstallUtil;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;

public abstract class AbstractJREComposite extends Composite {

	protected static final String JAVA_PREF_PAGE_ID = "org.eclipse.jdt.ui.preferences.JavaBasePreferencePage";
	protected static final String JRE_PREF_PAGE_PATH = JAVA_PREF_PAGE_ID + Path.SEPARATOR + JREsPreferencePage.ID;
	protected static final String EXEC_ENV_PREF_PAGE_PATH = JRE_PREF_PAGE_PATH + Path.SEPARATOR + ExecutionEnvironmentsPreferencePage.ID;
	
	
	private TaskModel taskModel;
	private Group installedJREGroup;
	private Combo alternateJRECombo;
	private Combo execEnvironmentCombo;
	
	private Button execenvRadio, vmRadio;
	private Button environmentsButton, installedJREsButton;

	private List<IVMInstall> compatibleJREs, allJREs;
	private String[] jreNames;
	protected IVMInstall selectedVM;
	protected IExecutionEnvironment selectedExecutionEnvironment;
	
	private IExecutionEnvironment[] validExecutionEnvironments;
	private String[] validExecutionEnvironmentNames;
	
	private IJRECompositeListener listener;
	private ModifyListener comboModifyListener;
	public AbstractJREComposite(Composite parent, int style, TaskModel tm) {
		super(parent, style);
		this.taskModel = tm;
		createJREComposite(this);
	}
	
	protected TaskModel getTaskModel() {
		return taskModel;
	}
	
	public void setListener(IJRECompositeListener listener) {
		this.listener = listener;
	}
	
	protected void createJREComposite(Composite main) {
		setLayout(new FillLayout());
		createWidgets();
		loadModel();
		fillWidgets();
		addInternalListeners();
		vmChanged();
	}
	
	protected void addInternalListeners() {
		SelectionListener sl = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				radioClicked();
			}
		};
		
		execenvRadio.addSelectionListener(sl);
		vmRadio.addSelectionListener(sl);
		
		
		comboModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				vmChanged();
			}
		};
		execEnvironmentCombo.addModifyListener(comboModifyListener);
		alternateJRECombo.addModifyListener(comboModifyListener);
		

		environmentsButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				showPreferencePage(EXEC_ENV_PREF_PAGE_PATH); 
			}
		});
		installedJREsButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				jreButtonPressed();
			}
		});
		 
		
	}
	
	private void radioClicked() {
		execEnvironmentCombo.setEnabled(execenvRadio.getSelection());
		alternateJRECombo.setEnabled(!execenvRadio.getSelection());
		vmChanged();
	}
	
	protected void refreshWidgets() {
		
		// First handle the execition environments
		int selIndex = execEnvironmentCombo == null ? -1 : execEnvironmentCombo.getSelectionIndex();
		execEnvironmentCombo.setItems(validExecutionEnvironmentNames);
		if( selIndex != -1 ) 
			execEnvironmentCombo.select(selIndex);
		else 
			execEnvironmentCombo.deselectAll();
		
		// Handle specific JREs
		alternateJRECombo.setItems(jreNames);
		// Refresh selection of vm
		int ind = selectedVM == null ? -1 : getDisplayableJREList().indexOf(selectedVM);
		if( ind != -1 )
			alternateJRECombo.select(ind);
		else
			alternateJRECombo.deselectAll();
		
	}
	
	protected void fillWidgets() {
		execEnvironmentCombo.setItems(validExecutionEnvironmentNames);
		IExecutionEnvironment toSelect = selectedExecutionEnvironment != null ? selectedExecutionEnvironment : getStoredExecutionEnvironment();
		int selIndex = -1;
		if( toSelect != null ) {
			String id = toSelect.getId();
			selIndex = Arrays.asList(validExecutionEnvironmentNames).indexOf(id);
		}
		execEnvironmentCombo.select(selIndex == -1 ? 0 : selIndex);
		
		alternateJRECombo.setItems(jreNames);
		
		selIndex = -1;
		IVMInstall hardSelected = selectedVM != null ? selectedVM : getStoredJRE();
		execenvRadio.setSelection(hardSelected == null);
		vmRadio.setSelection(hardSelected != null);
		if( hardSelected != null ) {
			String name = hardSelected.getName();
			selIndex = Arrays.asList(jreNames).indexOf(name);
		}
		alternateJRECombo.select(selIndex == -1 ? 0 : selIndex);
		
		
		execEnvironmentCombo.setEnabled(execenvRadio.getSelection());
		alternateJRECombo.setEnabled(vmRadio.getSelection());
	}
	
	protected IVMInstall[] getCompatibleVMs() {
		IExecutionEnvironment minimum = getMinimumExecutionEnvironment();
		IExecutionEnvironment maximum = getMaximumExecutionEnvironment();
		return VMInstallUtil.getValidJREs(minimum, maximum);
	}
	
	protected void loadModel() {
		// first load all possible exec-envs.
		IExecutionEnvironment min = getMinimumExecutionEnvironment();
		validExecutionEnvironments = findAllValidEnvironments(min);
		validExecutionEnvironmentNames = new String[validExecutionEnvironments.length];
		for( int i = 0; i < validExecutionEnvironments.length; i++ ) {
			validExecutionEnvironmentNames[i] = validExecutionEnvironments[i].getId();
		}
		
		// Now load all possible jres
		compatibleJREs = new ArrayList<IVMInstall>();
		IVMInstall[] compat = getCompatibleVMs();
		compatibleJREs.addAll(Arrays.asList(compat));
		
		
		allJREs = new ArrayList<IVMInstall>();
		allJREs.addAll(Arrays.asList(compat));
		
		// Now add all other JREs
		IVMInstall[] allFromUtil = VMInstallUtil.getAllVMInstalls();
		ArrayList<IVMInstall> noncompliant = new ArrayList<IVMInstall>();
		noncompliant.addAll(Arrays.asList(allFromUtil));
		noncompliant.removeAll(Arrays.asList(compat));
		
		allJREs.addAll(noncompliant);

		List<IVMInstall> toDisplay = getDisplayableJREList();
		jreNames = new String[toDisplay.size()];
		for( int i = 0; i < toDisplay.size(); i++ ) {
			jreNames[i] = toDisplay.get(i).getName();
		}
	}
	
	protected List<IVMInstall> getDisplayableJREList() {
		return includeIncompatibleJREs() ? allJREs : compatibleJREs;
	}
	
	protected boolean includeIncompatibleJREs() {
		return true;
	}
	
	/**
	 * Find all valid execution environments
	 * @param env
	 * @return
	 */
	protected IExecutionEnvironment[] findAllValidEnvironments(IExecutionEnvironment min) {
		return findAllValidEnvironments(min, getMaximumExecutionEnvironment());
	}
	
	protected IExecutionEnvironment[] findAllValidEnvironments(IExecutionEnvironment minimum, IExecutionEnvironment maximum) {
		return VMInstallUtil.findAllValidEnvironments(minimum, maximum);
	}
	
	private IExecutionEnvironment[] findSuperEnvironments(IExecutionEnvironment minimum) {
		return VMInstallUtil.findSuperEnvironments(minimum);
	}
	
	protected void createWidgets() {
		// Create Internal Widgets
		installedJREGroup = new Group(this, SWT.NONE);
		installedJREGroup.setText(Messages.wf_JRELabel);
		installedJREGroup.setLayout(new GridLayout(3, true));
		
		GridData comboData = new GridData();
		comboData.grabExcessHorizontalSpace = true;
		comboData.horizontalAlignment = SWT.FILL;
		
		GridData buttonData = new GridData();
		buttonData.grabExcessHorizontalSpace = true;
		buttonData.horizontalAlignment = SWT.FILL;

		
		execenvRadio = new Button(installedJREGroup, SWT.RADIO);
		execenvRadio.setText("Execution Environment: ");
		execEnvironmentCombo = new Combo(installedJREGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
		execEnvironmentCombo.setLayoutData(comboData);
		
		environmentsButton = new Button(installedJREGroup, SWT.NONE);
		environmentsButton.setText("Environments...");
		
		vmRadio = new Button(installedJREGroup, SWT.RADIO);
		vmRadio.setText("Alternate JRE: ");
		alternateJRECombo = new Combo(installedJREGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
		alternateJRECombo.setLayoutData(comboData);
		installedJREsButton = new Button(installedJREGroup, SWT.NONE);
		installedJREsButton.setText("Installed JREs...");
		
		environmentsButton.setLayoutData(buttonData);
		installedJREsButton.setLayoutData(buttonData);
		
	}
	
	protected void jreButtonPressed() {
		showPreferencePage(JRE_PREF_PAGE_PATH); 
		// Need to refresh available jvms
		execEnvironmentCombo.removeModifyListener(comboModifyListener);
		alternateJRECombo.removeModifyListener(comboModifyListener);
		loadModel();
		refreshWidgets();
		execEnvironmentCombo.addModifyListener(comboModifyListener);
		alternateJRECombo.addModifyListener(comboModifyListener);
		vmChanged();
	}
	
	// Other
	/**
	 * This method should not be used, as it does not indicate 
	 * which preference page should be shown. 
	 * @return
	 */
	@Deprecated
	protected boolean showPreferencePage() {
		return showPreferencePage(JRE_PREF_PAGE_PATH);
	}
	
	private void vmChanged() {
		// The hard vm is null if the proper radio isn't selected; otherwise use what's selected
		int vmIndex = !vmRadio.getSelection() ? -1 : alternateJRECombo.getSelectionIndex();
		selectedVM = vmIndex == -1 ? null : getDisplayableJREList().get(vmIndex);
		
		int execenvIndex = !execenvRadio.getSelection() ? -1 : execEnvironmentCombo.getSelectionIndex();
		selectedExecutionEnvironment = execenvIndex == -1 ? null : validExecutionEnvironments[execenvIndex];
		
		if( listener != null ) {
			listener.vmChanged(this);
		}
	}
	
	/**
	 * Get the current runtime from the task model
	 * @return
	 */
	protected abstract IRuntime getRuntimeFromTaskModel();
	
	/**
	 * Get the minimum execution environment for the runtime type 
	 * @return
	 */
	public abstract IExecutionEnvironment getMinimumExecutionEnvironment();
	
	/**
	 * Get the maximum execution environment for the runtime type, or null if no max set 
	 * @return
	 */
	public IExecutionEnvironment getMaximumExecutionEnvironment() {
		return null;  // Only has an impl to avoid API breakage
	}

	/**
	 * Get the execution environment currently stored in the runtime
	 * @return
	 */
	public abstract IExecutionEnvironment getStoredExecutionEnvironment();
	
	/**
	 * Get the execution environment currently selected 
	 * @return
	 */
	public IExecutionEnvironment getSelectedExecutionEnvironment() {
		return selectedExecutionEnvironment;
	}

	/**
	 * Get the currently stored JRE
	 * @return
	 */
	protected abstract IVMInstall getStoredJRE();
	

	/**
	 * Get the VM selected in this composite, or null if the user opts to use an execution environment
	 * @return
	 */
	public IVMInstall getSelectedVM() {
		return selectedVM;
	}
	
	/**
	 * Is the selected VM in the list of VMs compatible with the required execution environment?
	 * @return
	 */
	public boolean selectedVMisCompatible() {
		// If selectedVM is null, they've chosen an execution environment
		if( selectedVM == null ) {
			return VMInstallUtil.hasValidJRE(getMinimumExecutionEnvironment(), getMaximumExecutionEnvironment(), selectedExecutionEnvironment);
		} else {
			return compatibleJREs.contains(selectedVM);
		}
	}
	
	/**
	 * Get all valid JRE's that can be manually chosen which will fit this runtime
	 * @return
	 */
	public abstract List<IVMInstall> getValidJREs();
	
	protected boolean showPreferencePage(String pageId) {
		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();
		IPreferenceNode node = manager.find(pageId);
		PreferenceManager manager2 = new PreferenceManager();
		manager2.addToRoot(node);
		PreferenceDialog dialog = new PreferenceDialog(getShell(), manager2);
		dialog.setSelectedNode(pageId);
		dialog.create();
		dialog.setMessage(node.getLabelText());
		return (dialog.open() == Window.OK);
	}
}
