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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
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
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.ServerType;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.util.JBossConfigurationTableViewer;

/**
 * @author Marshall
 */
public abstract class AbstractJBossWizardFragment extends WizardFragment {
	
	//private final static int UNKNOWN_CHANGED = 0;
	private final static int NAME_CHANGED = 1;
	private final static int HOME_CHANGED = 2;
	private final static int JRE_CHANGED = 3;
	private final static int CONFIG_CHANGED = 4;
	
	private final static int SEVERITY_ALL = 1;
	private final static int SEVERITY_MAJOR = 2;
	
	private IWizardHandle handle;
	private Label nameLabel, homeDirLabel, installedJRELabel, configLabel; 
	private Text nameText, homeDirText;
	private Combo jreCombo;
	private Button homeDirButton, jreButton;
	private Composite nameComposite, homeDirComposite, jreComposite, configComposite;
	private String name, homeDir, jre, config;

	// jre fields
	protected ArrayList installedJREs;
	protected String[] jreNames;
	protected int defaultVMIndex;
	private JBossConfigurationTableViewer configurations;

	
	private IVMInstall selectedVM;
	private JBossServer server;
	private JBossServerRuntime runtime;
	

	
	protected void debug (String message)
	{
		System.out.println("[jboss-wizard-fragment] " + message);
	}
	
	public Composite createComposite(Composite parent, IWizardHandle handle)
	{
		this.handle = handle;
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		
		
		updateJREs();
		createNameComposite(main);
		createHomeComposite(main);
		createJREComposite(main);
		createConfigurationComposite(main);

		
		
		// make modifications to parent
		handle.setTitle(Messages.createWizardTitle);
		handle.setDescription(Messages.createWizardDescription);
		handle.setImageDescriptor (getImageDescriptor());
		return main;
	}

	private void createNameComposite(Composite main) {
		// Create our name composite
		nameComposite = new Composite(main, SWT.NONE);
		
		FormData cData = new FormData();
		cData.left = new FormAttachment(0,5);
		cData.right = new FormAttachment(100,-5);
		cData.top = new FormAttachment(0, 10);
		nameComposite.setLayoutData(cData);
		
		nameComposite.setLayout(new FormLayout());

		
		// create internal widgets
		nameLabel = new Label(nameComposite, SWT.None);
		nameLabel.setText(Messages.wizardFragmentNameLabel);
		
		nameText = new Text(nameComposite, SWT.BORDER);
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updatePage(NAME_CHANGED);
			} 
			
		});
		
		// organize widgets inside composite
		FormData nameLabelData = new FormData();
		nameLabelData.left = new FormAttachment(0,0);
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
		cData.left = new FormAttachment(0,5);
		cData.right = new FormAttachment(100,-5);
		cData.top = new FormAttachment(nameComposite, 10);
		homeDirComposite.setLayoutData(cData);

		homeDirComposite.setLayout(new FormLayout());
		
		
		// Create Internal Widgets
		homeDirLabel = new Label(homeDirComposite, SWT.NONE);
		homeDirLabel.setText(Messages.wizardFragmentHomeDirLabel);
		
		homeDirText = new Text(homeDirComposite, SWT.BORDER);
		
		homeDirButton = new Button(homeDirComposite, SWT.NONE);
		homeDirButton.setText(Messages.browse);

		
		// Add listeners
		homeDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePage(HOME_CHANGED);
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
		
		labelData.left = new FormAttachment(0,0);
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
		cData.left = new FormAttachment(0,5);
		cData.right = new FormAttachment(100,-5);
		cData.top = new FormAttachment(homeDirComposite, 10);
		jreComposite.setLayoutData(cData);

		jreComposite.setLayout(new FormLayout());
		
		
		// Create Internal Widgets
		installedJRELabel = new Label(jreComposite, SWT.NONE);
		installedJRELabel.setText(Messages.wizardFragmentJRELabel);
		
		jreCombo = new Combo(jreComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		jreCombo.setItems(jreNames);
		jreCombo.select(defaultVMIndex);
		
		jreButton = new Button(jreComposite, SWT.NONE);
		jreButton.setText(Messages.installedJREs);
		
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
				}
			}
		});

		jreCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				updatePage(CONFIG_CHANGED);
			}

			public void widgetSelected(SelectionEvent e) {
				updatePage(CONFIG_CHANGED);
			} 
		} );
		
		// Set Layout Data
		FormData labelData = new FormData();
		FormData comboData = new FormData();
		FormData buttonData = new FormData();
		
		labelData.left = new FormAttachment(0,0);
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
		cData.left = new FormAttachment(0,5);
		cData.right = new FormAttachment(100,-5);
		cData.top = new FormAttachment(jreComposite, 10);
		configComposite.setLayoutData(cData);

		configComposite.setLayout(new FormLayout());
		
		
		configLabel = new Label(configComposite, SWT.NONE);
		configLabel.setText(Messages.wizardFragmentConfigLabel);

		configurations = new JBossConfigurationTableViewer(configComposite, 
				SWT.BORDER | SWT.SINGLE);
		
		FormData labelData = new FormData();
		labelData.left = new FormAttachment(0,5);
		configLabel.setLayoutData(labelData);
		
		FormData viewerData = new FormData();
		viewerData.left = new FormAttachment(0, 5);
		viewerData.right = new FormAttachment(100, -5);
		viewerData.top = new FormAttachment(configLabel, 5);
		configurations.getTable().setLayoutData(viewerData);
		
		configurations.getTable().addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				updatePage(CONFIG_CHANGED);
			}

			public void widgetSelected(SelectionEvent e) {
				updatePage(CONFIG_CHANGED);
			} 
			
		} );

	}

	private void updatePage(int changed) {
		switch( changed ) {
			case NAME_CHANGED:
				updateErrorMessage(SEVERITY_MAJOR);
				break;
			case HOME_CHANGED:
				if (! new File(homeDirText.getText()).exists()) {
					configurations.getControl().setEnabled(false);
				} else {
					// No errors, clear the message and update the available configurations
					configurations.setJBossHome(homeDirText.getText());
					configurations.setDefaultConfiguration("default");

					// update config variable
					int index = configurations.getTable().getSelectionIndex();
					config = configurations.getTable().getItem(index).getText();
				}
				updateErrorMessage(SEVERITY_MAJOR);

				break;
			case JRE_CHANGED:
				int sel = jreCombo.getSelectionIndex();
				selectedVM = (IVMInstall) installedJREs.get(sel);
				break;
			case CONFIG_CHANGED:
				int index = configurations.getTable().getSelectionIndex();
				config = configurations.getTable().getItem(index).getText();
				break;
			default:
				break;
		}
	}

	private void updateErrorMessage(int severity) {
		String error = getErrorString(severity);
		if( error == null ) {
			handle.setMessage(null, IMessageProvider.NONE);
			return;
		}

		handle.setMessage(error, IMessageProvider.ERROR);
	}
	
	private String getErrorString(int severity) {
		if( getJbossServerFolder(nameText.getText()).exists() ) {
			return Messages.serverNameInUse;
		}
		
		if ( homeDirText.getText() != "" && !new File(homeDirText.getText()).exists()) {
			return Messages.invalidDirectory;
		}
		
		if( severity == SEVERITY_MAJOR ) return null;
		
		// now give minor warnings
		if( nameText.getText().equals("")) 
			return Messages.nameTextBlank;

		if( homeDirText.getText().equals("")) 
			return Messages.homeDirBlank;


		return null;

	}

	
	private void browseHomeDirClicked() {
		File file = new File(homeDirText.getText());
		if (! file.exists()) {
			file = null;
		}
		
		File directory = getDirectory(file, homeDirComposite.getShell());
		if (directory == null) {
			return;
		}
		
		homeDirText.setText(directory.getAbsolutePath());
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
		PreferenceManager manager = PlatformUI.getWorkbench().getPreferenceManager();
		IPreferenceNode node = manager.find("org.eclipse.jdt.ui.preferences.JavaBasePreferencePage").findSubNode("org.eclipse.jdt.debug.ui.preferences.VMPreferencePage");
		PreferenceManager manager2 = new PreferenceManager();
		manager2.addToRoot(node);
		final PreferenceDialog dialog = new PreferenceDialog(jreButton.getShell(), manager2);
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
		installedJREs = new ArrayList();
		IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
		int size = vmInstallTypes.length;
		for (int i = 0; i < size; i++) {
			IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
			int size2 = vmInstalls.length;
			for (int j = 0; j < size2; j++) {
				installedJREs.add(vmInstalls[j]);
			}
		}
		
		// get names
		size = installedJREs.size();
		jreNames = new String[size];
		for (int i = 0; i < size; i++) {
			IVMInstall vmInstall = (IVMInstall) installedJREs.get(i);
			jreNames[i] = vmInstall.getName();
		}
		
		selectedVM = JavaRuntime.getDefaultVMInstall();
		defaultVMIndex = installedJREs.indexOf(selectedVM);
		

	}
	
	// WST API methods
	public void enter() {
		
		IRuntime r = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		IRuntimeWorkingCopy runtimeWC = r.createWorkingCopy();
		runtime = (JBossServerRuntime) runtimeWC.loadAdapter(JBossServerRuntime.class, null);
		
		

	}

	public void exit() {
		name = nameText.getText();
		homeDir = homeDirText.getText();
		jre = jreCombo.getText();
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		
		IFolder folder = getJbossServerFolder(name);
		if( !folder.exists()) {
			folder.create(true,true, new NullProgressMonitor());
		}
		serverWC.setServerConfiguration(folder);
		serverWC.setName(name);


		server = (JBossServer) serverWC.getAdapter(JBossServer.class);
		if( server == null ) {
			server = (JBossServer) serverWC.loadAdapter(JBossServer.class, new NullProgressMonitor());
		}

		ServerAttributeHelper helper = new ServerAttributeHelper(server, serverWC);
		helper.setServerHome(homeDir);
		helper.setJbossConfiguration(config);
		
		try {
			serverWC.save(false, new NullProgressMonitor());
			server.setRuntime(runtime);
			runtime.setVMInstall(selectedVM);
		} catch( Exception e ) {
			System.out.println("DYING HERE");
			e.printStackTrace();
		}
		
	}

	public boolean isComplete() {
		return getErrorString(SEVERITY_ALL) == null ? true : false;
	}

	protected abstract ImageDescriptor getImageDescriptor();

	public boolean hasComposite() {
		return true;
	}


	
	private IFolder getJbossServerFolder(String serverName) {
		try {
			return ServerType.getServerProject().getFolder(serverName);
		} catch( CoreException e) {
			return null;
		}
	}
}