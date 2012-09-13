/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.IInstallableRuntime;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractLocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.ui.IPreferenceKeys;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.IDownloadRuntimes;
import org.jboss.tools.runtime.ui.download.DownloadRuntimes;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Stryker
 */
public class JBossRuntimeWizardFragment extends WizardFragment {

	protected IWizardHandle handle;
	protected boolean beenEntered = false;
	
	
	protected Label nameLabel, homeDirLabel, 
			installedJRELabel, explanationLabel;
	protected Text nameText, homeDirText;
	protected Combo jreCombo;
	protected int jreComboIndex;
	protected Button homeDirButton, jreButton;
	protected Composite nameComposite, homeDirComposite, jreComposite;
	protected Button downloadAndInstallButton;
	protected String name, homeDir;

	// Configuration stuff
	protected Composite configComposite;
	protected Group configGroup;
	protected Label configDirLabel;
	protected Text configDirText;
	protected JBossConfigurationTableViewer configurations;
	protected Button configCopy, configBrowse, configDelete;
	protected String configDirTextVal;

	// jre fields
	protected List<IVMInstall> installedJREs;
	protected String[] jreNames;
	protected int defaultVMIndex;
	protected IVMInstall selectedVM;
	protected String originalName;

	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());

		updateModels();
		createWidgets(main);
		fillWidgets();
		updateWizardHandle(parent);
		return main;
	}
	
	protected void updateModels() {
		// clean state from last time this fragment was used
		// (fragments apparently do not have new instances constructed. ugh)
		homeDir = null;
		updateJREs();
	}
	
	protected void updateWizardHandle(Composite parent) {
		// make modifications to parent
		IRuntime r = (IRuntime) getTaskModel()
			.getObject(TaskModel.TASK_RUNTIME);
		// TODO:  Unify with code in LocalJBossServerRuntime and in getHomeVersionWarning
		String version = r.getRuntimeType().getVersion();
		if( isEAP() && version.startsWith("5."))
			version = "5.x";
		if( !isEAP() && version.startsWith("6."))
			version = "6.x";
		
		handle.setTitle( Messages.rwf_JBossRuntime);
		String description = NLS.bind(
				isEAP() ? Messages.JBEAP_version : Messages.JBAS_version,
				version);
		handle.setImageDescriptor(getImageDescriptor());
		handle.setDescription(description);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.jboss.ide.eclipse.as.doc.user.new_server_runtime"); //$NON-NLS-1$
	}
	
	protected void createWidgets(Composite main) {
		createExplanation(main);
		createNameComposite(main);
		createHomeComposite(main);
		createJREComposite(main);
		createConfigurationComposite(main);
	}

	
	protected boolean isEAP() {
		IRuntime rt = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		return RuntimeUtils.isEAP(rt);
	}
	
	protected ImageDescriptor getImageDescriptor() {
		String imageKey = JBossServerUISharedImages.WIZBAN_JBOSS_LOGO;
		return JBossServerUISharedImages.getImageDescriptor(imageKey);
	}

	protected void fillNameWidgets(IRuntime rt) {
		originalName = rt.getName();
		nameText.setText(originalName);
		name = originalName;
	}
	
	protected void fillHomeDir(IRuntime rt) {
		if( rt.getLocation() == null ) {
			// new runtime creation
			IEclipsePreferences prefs2 = InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
			String value = prefs2.get(IPreferenceKeys.RUNTIME_HOME_PREF_KEY_PREFIX + rt.getRuntimeType().getId(), null);

			String locationDefault = Platform.getOS().equals(Platform.WS_WIN32) 
			? "c:/program files/jboss-" : "/usr/bin/jboss-"; //$NON-NLS-1$ //$NON-NLS-2$
			if( isEAP() )
				locationDefault += "eap-"; //$NON-NLS-1$
			String version = rt.getRuntimeType().getVersion();
			locationDefault += version + ".x"; //$NON-NLS-1$
			homeDir = (value != null && value.length() != 0) ? value : locationDefault;
		} else {
			// old runtime, load from it
			homeDir = rt.getLocation().toOSString();
		}
		homeDirText.setText(homeDir);
		
		boolean isWC = rt instanceof IRuntimeWorkingCopy;
		if( isWC ) {
			((IRuntimeWorkingCopy)rt).setLocation(new Path(homeDir));
		} 
		homeDirText.setEnabled(isWC);
		homeDirButton.setEnabled(isWC);
	}
	
	protected void fillConfigWidgets(IRuntime rt) {
		IJBossServerRuntime jbsrt = getRuntime();
		String dirText = jbsrt.getConfigLocation();
		configDirText.setText(isEmpty(dirText) ? IConstants.SERVER : dirText);
		configurations.setConfiguration(isEmpty(jbsrt.getJBossConfiguration())
				? IConstants.DEFAULT_CONFIGURATION : jbsrt.getJBossConfiguration());
		configurations.getTable().setVisible(true);
	}
	
	protected boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	protected void fillJREWidgets(IRuntime rt) {
		IJBossServerRuntime jbsrt = getRuntime();
		if (jbsrt.isUsingDefaultJRE()) {
			jreCombo.select(0);
		} else {
			IVMInstall install = jbsrt.getHardVM();
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
	
	protected void fillWidgets() {
		IRuntime rt = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if (rt != null) {
			fillNameWidgets(rt);
			fillHomeDir(rt);
			fillConfigWidgets(rt);
			fillJREWidgets(rt);
		}
	}

	protected IJBossServerRuntime getRuntime() {
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

	protected void createExplanation(Composite main) {
		explanationLabel = new Label(main, SWT.WRAP);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 5);
		data.left = new FormAttachment(0, 5);
		data.right = new FormAttachment(100, -5);
		explanationLabel.setLayoutData(data);
		explanationLabel.setText(Messages.rwf_Explanation);
	}

	protected void createNameComposite(Composite main) {
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

	protected void createHomeComposite(Composite main) {
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

		downloadAndInstallButton = new Button(homeDirComposite, SWT.NONE);
		downloadAndInstallButton.setText(Messages.rwf_DownloadRuntime);
		downloadAndInstallButton.addSelectionListener(new DownloadAndInstallListener());
		downloadAndInstallButton.setEnabled(true); 
		
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
		homeDirLabel.setLayoutData(UIUtil.createFormData2(null,0,homeDirText,-5,0,5,null,0));
		homeDirText.setLayoutData(UIUtil.createFormData2(homeDirLabel,5,null,0,0,5,homeDirButton,-5));
		homeDirButton.setLayoutData(UIUtil.createFormData2(homeDirLabel,5,null,0,null,0,100,0));
		downloadAndInstallButton.setLayoutData(UIUtil.createFormData2(0,0,homeDirButton,-5,null,0,100,-5));
	}

	protected class DownloadAndInstallListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent se) {
			IDownloadRuntimes downloader = RuntimeCoreActivator.getDefault().getDownloader();
			if( downloader != null ) {
				HashMap<String, Object> data = new HashMap<String, Object>();
				data.put(DownloadRuntimes.SHELL, downloadAndInstallButton.getShell() );
				IRuntimeType type = getRuntimeType();
				data.put(IDownloadRuntimes.RUNTIME_FILTER, new JBossASDownloadRuntimeFilter(type));
				downloader.execute(data);
			}
		}
	}
	
	
	protected void createJREComposite(Composite main) {
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

	protected void createConfigurationComposite(Composite main) {
		UIUtil u = new UIUtil(); // top bottom left right
		configComposite = new Composite(main, SWT.NONE);
		configComposite.setLayoutData(u.createFormData(
				jreComposite, 10, 100, -5, 0, 5, 100, -5));
		configComposite.setLayout(new FormLayout());
		
		configGroup = new Group(configComposite, SWT.DEFAULT);
		configGroup.setText(Messages.wf_ConfigLabel);
		configGroup.setLayoutData(u.createFormData(
				0, 0, 100, 0, 0, 0, 100, 0));
		configGroup.setLayout(new FormLayout());
		
		configDirLabel = new Label(configGroup, SWT.NONE);
		configDirLabel.setText(Messages.directory);
		configDirText = new Text(configGroup, SWT.BORDER);

		configurations = new JBossConfigurationTableViewer(configGroup,
		SWT.BORDER | SWT.SINGLE);
		
		IRuntime r = (IRuntime) getTaskModel()
				.getObject(TaskModel.TASK_RUNTIME);
		IRuntimeWorkingCopy runtimeWC = r.isWorkingCopy() ? ((IRuntimeWorkingCopy) r)
				: r.createWorkingCopy();
		IJBossServerRuntime srt = (IJBossServerRuntime) runtimeWC.loadAdapter(
				IJBossServerRuntime.class, new NullProgressMonitor());

		if( srt != null && !isEmpty(srt.getJBossConfiguration())) 
			configurations.setConfiguration(srt.getJBossConfiguration());
		
		configBrowse = new Button(configGroup, SWT.DEFAULT);
		configCopy = new Button(configGroup, SWT.DEFAULT);
		configDelete = new Button(configGroup, SWT.DEFAULT);
		configBrowse.setText(Messages.browse);
		configCopy.setText(Messages.copy);
		configDelete.setText(Messages.delete);
		
		// Organize them
		configDirLabel.setLayoutData(u.createFormData(
				2, 5, null, 0, 0, 5, null, 0));
		configDirText.setLayoutData(u.createFormData(
				0, 5, null, 0, configDirLabel, 5, configBrowse, -5));
		configBrowse.setLayoutData(u.createFormData(
				0, 5, null, 0, configurations.getTable(), 5, 100, -5));
		configurations.getTable().setLayoutData(u.createFormData(
				configDirText, 5, 100,-5, 0,5, 80, 0));
		configCopy.setLayoutData(u.createFormData(
				configBrowse, 5, null, 0, configurations.getTable(), 5, 100, -5));
		configDelete.setLayoutData(u.createFormData(
				configCopy, 5, null, 0, configurations.getTable(), 5, 100, -5));
		
		configDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePage();
			} 
		});
		
		configBrowse.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				configBrowsePressed();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		configCopy.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				configCopyPressed();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		configDelete.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				configDeletePressed();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		configurations.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				updateErrorMessage();
				configDelete.setEnabled(!((IStructuredSelection)configurations.getSelection()).isEmpty());
				configCopy.setEnabled(!((IStructuredSelection)configurations.getSelection()).isEmpty());
			}
		});
	}
	
	protected void configBrowsePressed() {
		String folder = new Path(configDirText.getText()).isAbsolute() ? 
				configDirText.getText() : new Path(homeDir).append(configDirText.getText()).toString();
		File file = new File(folder);
		if (!file.exists()) {
			file = null;
		}

		File directory = getDirectory(file, homeDirComposite.getShell());
		if (directory != null) {
			if(directory.getAbsolutePath().startsWith(new Path(homeDir).toString())) {
				String result = directory.getAbsolutePath().substring(homeDir.length());
				configDirText.setText(new Path(result).makeRelative().toString());
			} else {
				configDirText.setText(directory.getAbsolutePath());
			}
		}
	}
	protected void configCopyPressed() {
		CopyConfigurationDialog d = new CopyConfigurationDialog(configCopy.getShell(), homeDir, configDirText.getText(), configurations.getCurrentlySelectedConfiguration());
		if(d.open() == 0 ) {
			IPath source = new Path(configDirText.getText());
			if( !source.isAbsolute())
				source = new Path(homeDir).append(source);
			source = source.append(configurations.getCurrentlySelectedConfiguration());
			
			IPath dest = new Path(d.getNewDest());
			if( !dest.isAbsolute())
				dest = new Path(homeDir).append(dest);
			dest = dest.append(d.getNewConfig());
			dest.toFile().mkdirs();
			FileUtil.copyDir(source.toFile(), dest.toFile());
			configDirText.setText(d.getNewDest());
			configurations.setSelection(new StructuredSelection(d.getNewConfig()));
		}
		
	}
	protected void configDeletePressed() {
        MessageDialog dialog = new MessageDialog(configBrowse.getShell(), 
        		Messages.JBossRuntimeWizardFragment_DeleteConfigTitle, null,
        		Messages.JBossRuntimeWizardFragment_DeleteConfigConfirmation, 
                MessageDialog.WARNING, new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0); // yes is the default
        if(dialog.open() == 0) {
        	String config = configurations.getCurrentlySelectedConfiguration();
        	String configDir = configDirText.getText();
        	File folder;
        	if( !new Path(configDir).isAbsolute())
        		folder = new Path(homeDir).append(configDir).append(config).toFile();
        	else
        		folder = new Path(configDir).append(config).toFile();
        	 
        	FileUtil.completeDelete(folder);
        	configurations.refresh();
        	updatePage();
        }
	}

	// Launchable only from UI thread
	protected void updatePage() {
		String folder;
		if (!isHomeValid()) {
			configurations.getControl().setEnabled(false);
			folder = homeDirText.getText();
		} else {
			IPath p = new Path(configDirText.getText());
			if( p.isAbsolute()) 
				folder = p.toString();
			else 
				folder = new Path(homeDirText.getText()).append(p).toString();
		}
		configurations.setFolder(folder);
		File f = new File(folder);
		configurations.getControl().setEnabled(f.exists() && f.isDirectory());

		int sel = jreCombo.getSelectionIndex();
		int offset = -1;
		if( sel + offset >= 0 )
			selectedVM = installedJREs.get(sel + offset);
		else // if sel < 0 or sel == 0 and offset == -1
			selectedVM = null;
		configDirTextVal = configDirText.getText();
		updateErrorMessage();
		saveDetailsInRuntime();
	}

	protected void updateErrorMessage() {
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

		if( getValidJREs().size() == 0 ) {
			String error = NLS.bind(Messages.rwf_noValidJRE, getRuntime().getExecutionEnvironment().getId());
			return error;
		}
			
		
		if (!isHomeValid())
			return NLS.bind(Messages.rwf_homeMissingFiles, getSystemJarPath());

		if (name == null || name.equals("")) //$NON-NLS-1$
			return Messages.rwf_nameTextBlank;

		if (jreComboIndex < 0)
			return Messages.rwf_NoVMSelected;
		
		if( configurations.getSelection().isEmpty())
			return Messages.JBossRuntimeWizardFragment_MustSelectValidConfig;

		return null;
	}
	
	protected String getWarningString() {
		if( getHomeVersionWarning() != null )
			return getHomeVersionWarning();
		return null;
	}

	protected boolean isHomeValid() {
		if( homeDir == null  || homeDir.length() == 1 || !(new File(homeDir).exists())) return false; //$NON-NLS-1$
		return new Path(homeDir).append("bin").append("run.jar").toFile().exists(); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	protected String getVersionString(File loc) {
		String version = new ServerBeanLoader(loc).getFullServerVersion();
		return version;
	}
	
	protected String getSystemJarPath() {
		return JBossServerType.AS.getSystemJarPath();
	}
	
	protected String getHomeVersionWarning() {
		File loc = new File(homeDir);
		String version = getVersionString(loc);
		IRuntime rt = (IRuntime) getTaskModel().getObject(
				TaskModel.TASK_RUNTIME);
		String v = rt.getRuntimeType().getVersion();
		return getHomeVersionWarning(version, v);
	}

	protected String getHomeVersionWarning(String version, String v) {
		/* 
		 * CHEAP WARNING HACK - 
		 *   EAP 5.0 was started as named 5.0, but is now 5.x.
		 *   So a jar with 5.1 should work here also.  
		 */
		// This really needs to be extracted into an API
		// To determine what server types work for what actual underlying version
		// Unify with the core code in LocalJBossServerRuntime which changes 6.0 to 6.x
		if( isEAP() && v.startsWith("5."))
			v = "5.";
		if( !isEAP() && v.startsWith("6."))
			v = "6.";
		if( !isEAP() && v.startsWith("7."))
			v = "7.";
		if( isEAP() && v.startsWith("6."))
			v = "6.";
		if( !isEAP() && v.startsWith("7."))
			v = "7.";

		return version.startsWith(v) ? null : NLS.bind(Messages.rwf_homeIncorrectVersion, v, version);
	}

	protected void browseHomeDirClicked() {
		File file = homeDir == null ? null : new File(homeDir);
		if (file != null && !file.exists()) {
			file = null;
		}

		File directory = getDirectory(file, homeDirComposite.getShell());
		if (directory != null) {
			homeDir = directory.getAbsolutePath();
			homeDirText.setText(homeDir);
		}
	}

	protected static File getDirectory(File startingDirectory, Shell shell) {
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
				.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage") //$NON-NLS-1$
				.findSubNode(
						"org.eclipse.jdt.debug.ui.preferences.VMPreferencePage"); //$NON-NLS-1$
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
		size = size+1;
		int index = 0;
		jreNames = new String[size];
		jreNames[index++] = NLS.bind(Messages.rwf_DefaultJREForExecEnv, getRuntime().getExecutionEnvironment().getId());
		 
		for (int i = 0; i < installedJREs.size(); i++) {
			IVMInstall vmInstall = installedJREs.get(i);
			jreNames[index++] = vmInstall.getName();
		}
		defaultVMIndex = 0;
	}
	
	
	protected List<IVMInstall> getValidJREs() {
		IRuntime r = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		AbstractLocalJBossServerRuntime jbsrt = (AbstractLocalJBossServerRuntime)r.loadAdapter(AbstractLocalJBossServerRuntime.class, null);
		return Arrays.asList(jbsrt.getValidJREs(getRuntimeType()));
	}
	
	// WST API methods
	public void enter() {
		beenEntered = true;
	}

	public void exit() {
		saveDetailsInRuntime();
	}
	
	protected void saveDetailsInRuntime() {
		IRuntime r = (IRuntime) getTaskModel()
				.getObject(TaskModel.TASK_RUNTIME);
		IRuntimeWorkingCopy runtimeWC = r.isWorkingCopy() ? ((IRuntimeWorkingCopy) r)
				: r.createWorkingCopy();

		if( name != null )
			runtimeWC.setName(name);
		if( homeDir != null)
			runtimeWC.setLocation(new Path(homeDir));
		IJBossServerRuntime srt = (IJBossServerRuntime) runtimeWC.loadAdapter(
				IJBossServerRuntime.class, new NullProgressMonitor());
		if( selectedVM != null )
			srt.setVM(selectedVM);
		if( configurations != null && configurations.getSelectedConfiguration() != null )
			srt.setJBossConfiguration(configurations.getSelectedConfiguration());
		if( configDirText != null )
			srt.setConfigLocation(configDirTextVal);
		getTaskModel().putObject(TaskModel.TASK_RUNTIME, runtimeWC);
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		exit();
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if( rt instanceof IRuntimeWorkingCopy ) {
			IRuntimeWorkingCopy r = (IRuntimeWorkingCopy) rt;
			IRuntime saved = r.save(false, new NullProgressMonitor());
			getTaskModel().putObject(TaskModel.TASK_RUNTIME, saved);
			saveRuntimeLocationInPreferences(saved);
		}
	}
	
	protected void saveRuntimeLocationInPreferences(IRuntime runtime) {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
		prefs.put(IPreferenceKeys.RUNTIME_HOME_PREF_KEY_PREFIX + runtime.getRuntimeType().getId(), homeDir);
		try {
			prefs.flush();
		} catch(BackingStoreException e) {
			// TODO when adding tracing. This is not important enough for an error log entry
		}
	}

	public boolean isComplete() {
		return beenEntered && (getErrorString() == null ? true : false);
	}

	public boolean hasComposite() {
		return true;
	}

	protected IRuntime getRuntime(String runtimeName) {
		if (runtimeName.equals(originalName))
			return null; // name is same as original. No clash.

		IRuntime[] runtimes = ServerCore.getRuntimes();
		for (int i = 0; i < runtimes.length; i++) {
			if (runtimes[i].getName().equals(runtimeName))
				return runtimes[i];
		}
		return null;
	}
	
	
	public static class CopyConfigurationDialog extends TitleAreaDialog {
		private String origHome, origDest, origConfig;
		private String newDest, newConfig;
		private Text destText;
		protected CopyConfigurationDialog(Shell parentShell, String home, 
				String dir, String config) {
			super(new Shell(parentShell));
			origHome = home;
			origDest = dir;
			origConfig = config;
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			Composite main = new Composite(c, SWT.NONE);
			main.setLayoutData(new GridData(GridData.FILL_BOTH));
			main.setLayout(new FormLayout());
 
			setCleanMessage();

			Label nameLabel = new Label(main, SWT.NONE);
			nameLabel.setText(Messages.wf_NameLabel);

			final Text nameText = new Text(main, SWT.BORDER);
			
			Label destLabel = new Label(main, SWT.NONE);
			destLabel.setText(Messages.rwf_DestinationLabel);

			destText = new Text(main, SWT.BORDER);

			Button browse = new Button(main, SWT.PUSH);
			browse.setText(Messages.browse);

			Point nameSize = new GC(nameLabel).textExtent(nameLabel.getText());
			Point destSize = new GC(destLabel).textExtent(destLabel.getText());
			Control wider = nameSize.x > destSize.x ? nameLabel : destLabel;
			
			nameText.setLayoutData(UIUtil.createFormData2(
					0,13,null,0,wider,5,100,-5));
			nameLabel.setLayoutData(UIUtil.createFormData2(
					0,15,null,0,0,5,null,0));
			destText.setLayoutData(UIUtil.createFormData2(
					nameText,5,null,0,wider,5,browse,-5));
			destLabel.setLayoutData(UIUtil.createFormData2(
					nameText,7,null,0,0,5,null,0));
			browse.setLayoutData(UIUtil.createFormData2( 
					nameText,5,null,0,null,0,100,-5));
			
			nameText.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					newConfig = nameText.getText();
					validate();
				}
			});
			destText.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					newDest = destText.getText();
					validate();
				}
			});
			browse.addSelectionListener(new SelectionListener(){
				public void widgetSelected(SelectionEvent e) {
					IPath p = new Path(newDest);
					if( !p.isAbsolute())
						p = new Path(origHome).append(newDest);
					File file = p.toFile();
					if (!file.exists()) { 
						file = null;
					}

					File directory = getDirectory(file, getShell());
					if (directory != null) {
						IPath newP = new Path(directory.getAbsolutePath());
						IPath result;
						if( newP.toOSString().startsWith(new Path(origHome).toOSString()))
							result = newP.removeFirstSegments(new Path(origHome).segmentCount());
						else
							result = newP;
						destText.setText(result.toString());
					}
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			
			
			destText.setText(origDest);
			// we could localize the string _copy, but it would probably cause more trouble than it's worth
			nameText.setText(findNewest(origConfig + "_copy")); // TODO increment //$NON-NLS-1$
			return c;
		}
		
		public void validate() {
			boolean valid = false;
			IPath p = null;
			if( newDest != null && newConfig != null ) {
				p = new Path(newDest);
				if( !p.isAbsolute())
					p = new Path(origHome).append(newDest);
				if( !p.append(newConfig).toFile().exists()) 
					valid = true;

			}
			if( !valid ) {
				if( newDest == null || newConfig == null ) {
					setMessage(Messages.JBossRuntimeWizardFragment_AllFieldsRequired, IMessageProvider.ERROR);
				} else {
					setMessage(Messages.JBossRuntimeWizardFragment_OutputFolderExists + p.append(newConfig).toString(), IMessageProvider.ERROR);
				}
			} else {
				setCleanMessage();
			}
			if( getButton(Dialog.OK) != null ) 
				getButton(Dialog.OK).setEnabled(valid);
		}
		
		protected void setCleanMessage() {
			setMessage(NLS.bind(Messages.rwf_CopyConfigLabel, origConfig, origDest));
		}
		// Only to be used in initializing dialog
		protected String findNewest(String suggested) {
			IPath p = new Path(origDest);
			if( !p.isAbsolute())
				p = new Path(origHome).append(origDest);
			if( p.append(suggested).toFile().exists()) {
				int i = 1;
				while(p.append(suggested + i).toFile().exists())
					i++;
				return suggested + i;
			}
			return suggested;
		}
		
		protected Point getInitialSize() {
			return new Point(500, super.getInitialSize().y);
		}

		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.JBossRuntimeWizardFragment_CopyAConfigShellText);
		}

		public String getNewDest() {
			return newDest;
		}
		
		public String getNewConfig() {
			return newConfig;
		}
	}
	
	public IRuntimeType getRuntimeType() {
		IRuntime r = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		return r.getRuntimeType();
	}
}
