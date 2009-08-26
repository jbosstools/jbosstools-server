/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.JBossServerType;
import org.jboss.ide.eclipse.as.core.util.ServerBeanLoader;
import org.jboss.ide.eclipse.as.ui.IPreferenceKeys;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * @author Stryker
 */
public class JBossRuntimeWizardFragment extends WizardFragment {

	private IWizardHandle handle;
	private boolean beenEntered = false;
	
	
	private Label nameLabel, homeDirLabel, installedJRELabel, configLabel,
			explanationLabel;
	private Text nameText, homeDirText;
	private Combo jreCombo;
	private int jreComboIndex;
	private Button homeDirButton, jreButton;
	private Composite nameComposite, homeDirComposite, jreComposite,
			configComposite;
	private String name, homeDir, config;

	// jre fields
	protected ArrayList<IVMInstall> installedJREs;
	protected String[] jreNames;
	protected int defaultVMIndex;
	private IVMInstall selectedVM;
	private JBossConfigurationTableViewer configurations;
	private String originalName;

	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());

		updateJREs();
		createExplanation(main);
		createNameComposite(main);
		createHomeComposite(main);
		createJREComposite(main);
		createConfigurationComposite(main);

		fillWidgets();

		// make modifications to parent
		IRuntime r = (IRuntime) getTaskModel()
			.getObject(TaskModel.TASK_RUNTIME);
		String version = r.getRuntimeType().getVersion();
		if( r.isWorkingCopy() && ((IRuntimeWorkingCopy)r).getOriginal() == null) 
			handle.setTitle(Messages.rwf_Title1);
		else 
			handle.setTitle(Messages.rwf_Title2);
		String description = NLS.bind(
				isEAP() ? Messages.JBEAP_version : Messages.JBAS_version,
				version);
		handle.setImageDescriptor(getImageDescriptor());
		handle.setDescription(description);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.jboss.ide.eclipse.as.doc.user.new_server_runtime");
		return main;
	}

	protected boolean isEAP() {
		IRuntime rt = (IRuntime) getTaskModel().getObject(
				TaskModel.TASK_RUNTIME);
		return rt.getRuntimeType().getId().startsWith("org.jboss.ide.eclipse.as.runtime.eap.");
	}
	
	protected ImageDescriptor getImageDescriptor() {
		String imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_LOGO;
		return JBossServerUISharedImages.getImageDescriptor(imageKey);
	}

	private void fillWidgets() {
		boolean canEdit = true;

		// STUPID ECLIPSE BUG https://bugs.eclipse.org/bugs/show_bug.cgi?id=263928
		IRuntime r = (IRuntime) getTaskModel()
			.getObject(TaskModel.TASK_RUNTIME);
		String oldName = r.getName();
		if( r.isWorkingCopy() && ((IRuntimeWorkingCopy)r).getOriginal() == null) {
			String newName = oldName.replace("Enterprise Application Platform", "EAP");
			newName = LocalJBossServerRuntime.getNextRuntimeName(newName);
			((IRuntimeWorkingCopy)r).setName(newName);
		}
		
		IJBossServerRuntime rt = getRuntime();
		if (rt != null) {
			originalName = rt.getRuntime().getName();
			nameText.setText(rt.getRuntime().getName());
			name = rt.getRuntime().getName();
			Preferences prefs = JBossServerUIPlugin.getDefault().getPluginPreferences();
			String value = prefs.getString(IPreferenceKeys.RUNTIME_HOME_PREF_KEY_PREFIX + rt.getRuntime().getRuntimeType().getId());
			homeDir = (value != null && value.length() != 0) ? value : rt.getRuntime().getLocation().toOSString();
			homeDirText.setText(homeDir);
			((IRuntimeWorkingCopy)r).setLocation(new Path(homeDir));
			config = rt.getJBossConfiguration();
			configurations.setConfiguration(config);
			configLabel.setText(Messages.wf_ConfigLabel);

			if (rt.isUsingDefaultJRE()) {
				jreCombo.select(0);
			} else {
				IVMInstall install = rt.getVM();
				String vmName = install.getName();
				String[] jres = jreCombo.getItems();
				for (int i = 0; i < jres.length; i++) {
					if (vmName.equals(jres[i]))
						jreCombo.select(i);
				}
			}
			jreComboIndex = jreCombo.getSelectionIndex();
			homeDirText.setEditable(canEdit);
			homeDirButton.setEnabled(canEdit);
			configurations.getTable().setVisible(canEdit);
		}
	}

	private IJBossServerRuntime getRuntime() {
		IRuntime r = (IRuntime) getTaskModel()
				.getObject(TaskModel.TASK_RUNTIME);
		IJBossServerRuntime ajbsrt = null;
		if (r != null) {
			ajbsrt = (IJBossServerRuntime) r
					.loadAdapter(IJBossServerRuntime.class,
							new NullProgressMonitor());
		}
		return ajbsrt;
	}

	private void createExplanation(Composite main) {
		explanationLabel = new Label(main, SWT.WRAP);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 5);
		data.left = new FormAttachment(0, 5);
		data.right = new FormAttachment(100, -5);
		explanationLabel.setLayoutData(data);
		explanationLabel.setText(Messages.rwf_Explanation);
	}

	private void createNameComposite(Composite main) {
		// Create our name composite
		nameComposite = new Composite(main, SWT.NONE);

		FormData cData = new FormData();
		cData.left = new FormAttachment(0, 5);
		cData.right = new FormAttachment(100, -5);
		cData.top = new FormAttachment(explanationLabel, 10);
		nameComposite.setLayoutData(cData);

		nameComposite.setLayout(new FormLayout());

		// create internal widgets
		nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText(Messages.wf_NameLabel);

		nameText = new Text(nameComposite, SWT.BORDER);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				updatePage();
			}
		});

		// organize widgets inside composite
		FormData nameLabelData = new FormData();
		nameLabelData.left = new FormAttachment(0, 0);
		nameLabel.setLayoutData(nameLabelData);

		FormData nameTextData = new FormData();
		nameTextData.left = new FormAttachment(0, 5);
		nameTextData.right = new FormAttachment(100, -5);
		nameTextData.top = new FormAttachment(nameLabel, 5);
		nameText.setLayoutData(nameTextData);
	}

	private void createHomeComposite(Composite main) {
		// Create our composite
		homeDirComposite = new Composite(main, SWT.NONE);

		FormData cData = new FormData();
		cData.left = new FormAttachment(0, 5);
		cData.right = new FormAttachment(100, -5);
		cData.top = new FormAttachment(nameComposite, 10);
		homeDirComposite.setLayoutData(cData);

		homeDirComposite.setLayout(new FormLayout());

		// Create Internal Widgets
		homeDirLabel = new Label(homeDirComposite, SWT.NONE);
		homeDirLabel.setText(Messages.wf_HomeDirLabel);
		homeDirText = new Text(homeDirComposite, SWT.BORDER);
		homeDirButton = new Button(homeDirComposite, SWT.NONE);
		homeDirButton.setText(Messages.browse);

		// Add listeners
		homeDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				homeDir = homeDirText.getText();
				updatePage();
			}
		});

		homeDirButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				browseHomeDirClicked();
			}

			public void widgetSelected(SelectionEvent e) {
				browseHomeDirClicked();
			}

		});

		// Set Layout Data
		FormData labelData = new FormData();
		FormData textData = new FormData();
		FormData buttonData = new FormData();

		labelData.left = new FormAttachment(0, 0);
		homeDirLabel.setLayoutData(labelData);

		textData.left = new FormAttachment(0, 5);
		textData.right = new FormAttachment(homeDirButton, -5);
		textData.top = new FormAttachment(homeDirLabel, 5);
		homeDirText.setLayoutData(textData);

		buttonData.top = new FormAttachment(homeDirLabel, 5);
		buttonData.right = new FormAttachment(100, 0);
		homeDirButton.setLayoutData(buttonData);
	}

	private void createJREComposite(Composite main) {
		// Create our composite
		jreComposite = new Composite(main, SWT.NONE);

		FormData cData = new FormData();
		cData.left = new FormAttachment(0, 5);
		cData.right = new FormAttachment(100, -5);
		cData.top = new FormAttachment(homeDirComposite, 10);
		jreComposite.setLayoutData(cData);

		jreComposite.setLayout(new FormLayout());

		// Create Internal Widgets
		installedJRELabel = new Label(jreComposite, SWT.NONE);
		installedJRELabel.setText(Messages.wf_JRELabel);

		jreCombo = new Combo(jreComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		jreCombo.setItems(jreNames);
		if( defaultVMIndex != -1 )
			jreCombo.select(defaultVMIndex);
		
		jreButton = new Button(jreComposite, SWT.NONE);
		jreButton.setText(Messages.wf_JRELabel);

		// Add action listeners
		jreButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String currentVM = jreCombo.getText();
				if (showPreferencePage()) {
					updateJREs();
					jreCombo.setItems(jreNames);
					jreCombo.setText(currentVM);
					if (jreCombo.getSelectionIndex() == -1)
						jreCombo.select(defaultVMIndex);
					jreComboIndex = jreCombo.getSelectionIndex();
					updateErrorMessage();
				}
			}
		});

		jreCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				updatePage();
			}

			public void widgetSelected(SelectionEvent e) {
				updatePage();
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

	private void createConfigurationComposite(Composite main) {
		configComposite = new Composite(main, SWT.NONE);

		FormData cData = new FormData();
		cData.left = new FormAttachment(0, 5);
		cData.right = new FormAttachment(100, -5);
		cData.top = new FormAttachment(jreComposite, 10);
		cData.bottom = new FormAttachment(100, -5);
		configComposite.setLayoutData(cData);

		configComposite.setLayout(new FormLayout());

		configLabel = new Label(configComposite, SWT.NONE);
		configLabel.setText(Messages.wf_ConfigLabel);

		configurations = new JBossConfigurationTableViewer(configComposite,
				SWT.BORDER | SWT.SINGLE);

		FormData labelData = new FormData();
		labelData.left = new FormAttachment(0, 5);
		configLabel.setLayoutData(labelData);

		FormData viewerData = new FormData();
		viewerData.left = new FormAttachment(0, 5);
		viewerData.right = new FormAttachment(100, -5);
		viewerData.top = new FormAttachment(configLabel, 5);
		viewerData.bottom = new FormAttachment(100, -5);

		configurations.getTable().setLayoutData(viewerData);

		configurations.getTable().addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				updatePage();
			}

			public void widgetSelected(SelectionEvent e) {
				updatePage();
			}

		});

	}

	private void updatePage() {
		updateErrorMessage();
		if (!isHomeValid()) {
			configurations.getControl().setEnabled(false);
			configurations.setJBossHome(homeDirText.getText());
		} else {
			configurations.getControl().setEnabled(true);
			if( !homeDirText.getText().equals(configurations.getInput())) {
				configurations.setJBossHome(homeDirText.getText());
				configurations.setConfiguration(IJBossServerConstants.DEFAULT_SERVER_NAME);
			}
		}

		int sel = jreCombo.getSelectionIndex();
		if (sel > 0)
			selectedVM = installedJREs.get(sel-1);
		else
			selectedVM = null;
	}

	private void updateErrorMessage() {
		String error = getErrorString();
		if (error == null) {
			String warn = getWarningString();
			if( warn != null )
				handle.setMessage(warn, IMessageProvider.WARNING);
			else
				handle.setMessage(null, IMessageProvider.NONE);
		} else
			handle.setMessage(error, IMessageProvider.ERROR);
	}

	protected String getErrorString() {
		if (nameText == null) {
			// not yet initialized. no errors
			return null;
		}

		if (getRuntime(name) != null) {
			return Messages.rwf_NameInUse;
		}

		if (!isHomeValid())
			return Messages.rwf_homeMissingFiles;

		if (name == null || name.equals(""))
			return Messages.rwf_nameTextBlank;

		if (homeDir == null || homeDir.equals(""))
			return Messages.rwf_homeDirBlank;

		if (jreComboIndex < 0)
			return Messages.rwf_NoVMSelected;

		return null;
	}
	
	private String getWarningString() {
		if( getHomeVersionWarning() != null )
			return getHomeVersionWarning();
		return null;
	}

	protected boolean isHomeValid() {
		if( homeDir == null  || !(new File(homeDir).exists())) return false;
		return new Path(homeDir).append("bin").append("run.jar").toFile().exists();
	}
	
	protected String getHomeVersionWarning() {
		String version = new ServerBeanLoader().getFullServerVersion(new File(homeDir, JBossServerType.AS.getSystemJarPath()));
		IRuntime rt = (IRuntime) getTaskModel().getObject(
				TaskModel.TASK_RUNTIME);
		String v = rt.getRuntimeType().getVersion();
		return version.startsWith(v) ? null : NLS.bind(Messages.rwf_homeIncorrectVersion, v, version);
	}

	private void browseHomeDirClicked() {
		File file = new File(homeDir);
		if (!file.exists()) {
			file = null;
		}

		File directory = getDirectory(file, homeDirComposite.getShell());
		if (directory != null) {
			homeDir = directory.getAbsolutePath();
			homeDirText.setText(homeDir);
		}
	}

	protected File getDirectory(File startingDirectory, Shell shell) {
		DirectoryDialog fileDialog = new DirectoryDialog(shell, SWT.OPEN);
		if (startingDirectory != null) {
			fileDialog.setFilterPath(startingDirectory.getPath());
		}

		String dir = fileDialog.open();
		if (dir != null) {
			dir = dir.trim();
			if (dir.length() > 0) {
				return new File(dir);
			}
		}
		return null;
	}

	// Other
	protected boolean showPreferencePage() {
		PreferenceManager manager = PlatformUI.getWorkbench()
				.getPreferenceManager();
		IPreferenceNode node = manager
				.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage")
				.findSubNode(
						"org.eclipse.jdt.debug.ui.preferences.VMPreferencePage");
		PreferenceManager manager2 = new PreferenceManager();
		manager2.addToRoot(node);
		final PreferenceDialog dialog = new PreferenceDialog(jreButton
				.getShell(), manager2);
		final boolean[] result = new boolean[] { false };
		BusyIndicator.showWhile(jreButton.getDisplay(), new Runnable() {
			public void run() {
				dialog.create();
				if (dialog.open() == Window.OK)
					result[0] = true;
			}
		});
		return result[0];
	}

	// JRE methods
	protected void updateJREs() {
		// get all installed JVMs
		installedJREs = getValidJREs();
		// get names
		int size = installedJREs.size();
		size = shouldIncludeDefaultJRE() ? size+1 : size;
		int index = 0;
		jreNames = new String[size];
		if( shouldIncludeDefaultJRE())
			jreNames[index++] = "Default JRE"; //$NON-NLS-1$
		 
		for (int i = 0; i < installedJREs.size(); i++) {
			IVMInstall vmInstall = installedJREs.get(i);
			jreNames[index++] = vmInstall.getName();
		}
		defaultVMIndex = shouldIncludeDefaultJRE() ? 0 : 
			jreNames.length > 0 ? 0 : -1;
	}
	
	protected boolean shouldIncludeDefaultJRE() {
		return true;
	}
	
	protected ArrayList<IVMInstall> getValidJREs() {
		ArrayList<IVMInstall> valid = new ArrayList<IVMInstall>();
		IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
		int size = vmInstallTypes.length;
		for (int i = 0; i < size; i++) {
			IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
			int size2 = vmInstalls.length;
			for (int j = 0; j < size2; j++) {
				valid.add(vmInstalls[j]);
			}
		}
		return valid;
	}

	// WST API methods
	public void enter() {
		beenEntered = true;
	}

	public void exit() {
		IRuntime r = (IRuntime) getTaskModel()
				.getObject(TaskModel.TASK_RUNTIME);
		IRuntimeWorkingCopy runtimeWC = r.isWorkingCopy() ? ((IRuntimeWorkingCopy) r)
				: r.createWorkingCopy();

		runtimeWC.setName(name);
		runtimeWC.setLocation(new Path(homeDir));
		IJBossServerRuntime srt = (IJBossServerRuntime) runtimeWC.loadAdapter(
				IJBossServerRuntime.class, new NullProgressMonitor());
		srt.setVM(selectedVM);
		srt.setJBossConfiguration(configurations.getSelectedConfiguration());
		getTaskModel().putObject(TaskModel.TASK_RUNTIME, runtimeWC);
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		exit();
		IRuntimeWorkingCopy r = (IRuntimeWorkingCopy) getTaskModel().getObject(
				TaskModel.TASK_RUNTIME);
		IRuntime saved = r.save(false, new NullProgressMonitor());
		Preferences prefs = JBossServerUIPlugin.getDefault().getPluginPreferences();
		prefs.setValue(IPreferenceKeys.RUNTIME_HOME_PREF_KEY_PREFIX + saved.getRuntimeType().getId(), homeDir);

		getTaskModel().putObject(TaskModel.TASK_RUNTIME, saved);
	}

	public boolean isComplete() {
		return beenEntered && (getErrorString() == null ? true : false);
	}

	public boolean hasComposite() {
		return true;
	}

	private IRuntime getRuntime(String runtimeName) {
		if (runtimeName.equals(originalName))
			return null; // name is same as original. No clash.

		IRuntime[] runtimes = ServerCore.getRuntimes();
		for (int i = 0; i < runtimes.length; i++) {
			if (runtimes[i].getName().equals(runtimeName))
				return runtimes[i];
		}
		return null;
	}
}