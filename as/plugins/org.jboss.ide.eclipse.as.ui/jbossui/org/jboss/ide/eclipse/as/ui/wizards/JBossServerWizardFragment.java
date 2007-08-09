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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerType;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.runtime.server.AbstractJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;

public class JBossServerWizardFragment extends WizardFragment {
	//private final static int UNKNOWN_CHANGED = 0;
	private final static int NAME_CHANGED = 1;
	private final static int HOME_CHANGED = 2;
	private final static int JRE_CHANGED = 3;
	private final static int CONFIG_CHANGED = 4;
	
	private final static int SEVERITY_ALL = 1;
	private final static int SEVERITY_MAJOR = 2;
	
	private IWizardHandle handle;
	private Label nameLabel, homeDirLabel, installedJRELabel, configLabel, explanationLabel; 
	private Text nameText, homeDirText, configText;
	private Combo jreCombo;
	private Composite nameComposite, homeDirComposite, jreComposite, configComposite;
	private Group g;
	private String name, config;

	// jre fields
	protected ArrayList installedJREs;
	protected String[] jreNames;
	protected int defaultVMIndex;

	
	private IVMInstall selectedVM;
	private JBossServer server;
	private IJBossServerRuntime runtime;
	

	
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
		createExplanationLabel(main);
		createNameComposite(main);
		
		g = new Group(main, SWT.NONE);
		g.setText(Messages.runtimeInformation);
		g.setLayout(new FormLayout());
		FormData groupData = new FormData();
		groupData.left = new FormAttachment(0,5);
		groupData.right = new FormAttachment(100, -5);
		groupData.top = new FormAttachment(nameComposite, 5);
		g.setLayoutData(groupData);
		
		createHomeComposite(g);
		createJREComposite(g);
		createConfigurationComposite(g);

		// make modifications to parent
		handle.setTitle(Messages.createWizardTitle);
		handle.setDescription(Messages.createWizardDescription);
		handle.setImageDescriptor (getImageDescriptor());
		
		return main;
	}
	
	public ImageDescriptor getImageDescriptor() {
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		String id = rt.getRuntimeType().getId();
		String imageKey = "";
		if( id.equals("org.jboss.ide.eclipse.as.runtime.32")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS32_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.40")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS40_LOGO;
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.42")) imageKey = JBossServerUISharedImages.WIZBAN_JBOSS42_LOGO;
		return JBossServerUISharedImages.getImageDescriptor(imageKey);
	}
	
	public String getVersion() {
		IRuntime rt = (IRuntime)getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		String id = rt.getRuntimeType().getId();
		if( id.equals("org.jboss.ide.eclipse.as.runtime.32")) return "3.2";
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.40")) return "4.0";
		else if( id.equals("org.jboss.ide.eclipse.as.runtime.42")) return "4.2";
		return ""; // default
	}

	private void createExplanationLabel(Composite main) {
		explanationLabel = new Label(main, SWT.NONE);
		FormData data = new FormData();
		data.top = new FormAttachment(0,5);
		data.left = new FormAttachment(0,5);
		data.right = new FormAttachment(100,-5);
		explanationLabel.setLayoutData(data);
		
		explanationLabel.setText(Messages.serverWizardFragmentExplanation);
	}

	private void createNameComposite(Composite main) {
		// Create our name composite
		nameComposite = new Composite(main, SWT.NONE);
		
		FormData cData = new FormData();
		cData.left = new FormAttachment(0,5);
		cData.right = new FormAttachment(100,-5);
		cData.top = new FormAttachment(explanationLabel, 10);
		nameComposite.setLayoutData(cData);
		
		nameComposite.setLayout(new FormLayout());

		
		// create internal widgets
		nameLabel = new Label(nameComposite, SWT.None);
		nameLabel.setText(Messages.wizardFragmentNameLabel);
		
		nameText = new Text(nameComposite, SWT.BORDER);
		nameText.setText(getDefaultNameText());
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
	
	private String getDefaultNameText() {
		String base = "JBoss " + getVersion() + " server";
		if( findServer(base) == null ) return base;
		int i = 1;
		while( ServerCore.findServer(base + " " + i) != null ) 
			i++;
		return base + " " + i;
	}
	private IServer findServer(String name) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			Server server = (Server) servers[i];
			if (name.equals(server.getName()))
				return server;
		}
		return null;
	}
	private void createHomeComposite(Composite main) {
		// Create our composite
		homeDirComposite = new Composite(main, SWT.NONE);
		
		FormData cData = new FormData();
		cData.left = new FormAttachment(0,5);
		cData.right = new FormAttachment(100,-5);
		cData.top = new FormAttachment(0, 5);
		homeDirComposite.setLayoutData(cData);

		homeDirComposite.setLayout(new FormLayout());
		
		
		// Create Internal Widgets
		homeDirLabel = new Label(homeDirComposite, SWT.NONE);
		homeDirLabel.setText(Messages.wizardFragmentHomeDirLabel);
		
		homeDirText = new Text(homeDirComposite, SWT.BORDER);
		homeDirText.setEnabled(false);
		
		// Set Layout Data
		FormData labelData = new FormData();
		FormData textData = new FormData();
		//FormData buttonData = new FormData();
		
		labelData.left = new FormAttachment(0,0);
		homeDirLabel.setLayoutData(labelData);
		
		
		textData.left = new FormAttachment(0, 5);
		textData.right = new FormAttachment(100, -5);
		textData.top = new FormAttachment(homeDirLabel, 5);
		homeDirText.setLayoutData(textData);
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
		jreCombo.setEnabled(false);
		
		
		// Set Layout Data
		FormData labelData = new FormData();
		FormData comboData = new FormData();
		
		labelData.left = new FormAttachment(0,5);
		labelData.top = new FormAttachment(0, 7);
		installedJRELabel.setLayoutData(labelData);
		
		
		comboData.left = new FormAttachment(installedJRELabel, 5);
		comboData.right = new FormAttachment(60, -5);
		comboData.top = new FormAttachment(homeDirComposite, 5);
		jreCombo.setLayoutData(comboData);
	}
	
	private void createConfigurationComposite(Composite main) {
		configComposite = new Composite(main, SWT.NONE);
		
		FormData cData = new FormData();
		cData.left = new FormAttachment(0,5);
		cData.right = new FormAttachment(100,-5);
		cData.top = new FormAttachment(jreComposite, 5);
		cData.bottom = new FormAttachment(100, -5);
		configComposite.setLayoutData(cData);

		configComposite.setLayout(new FormLayout());
		
		
		// Create Internal Widgets
		configLabel = new Label(configComposite, SWT.NONE);
		configLabel.setText("Configuration");
		
		configText = new Text(configComposite, SWT.BORDER);
		configText.setEnabled(false);
		
		// Set Layout Data
		FormData labelData = new FormData();
		FormData textData = new FormData();
		//FormData buttonData = new FormData();
		
		labelData.left = new FormAttachment(0,0);
		configLabel.setLayoutData(labelData);
		
		
		textData.left = new FormAttachment(0, 5);
		textData.right = new FormAttachment(100, -5);
		textData.top = new FormAttachment(configLabel, 5);
		configText.setLayoutData(textData);
	}

	private void updatePage(int changed) {
		switch( changed ) {
			case NAME_CHANGED:
				updateErrorMessage(SEVERITY_MAJOR);
				break;
			case HOME_CHANGED:
				updateErrorMessage(SEVERITY_MAJOR);

				break;
			case JRE_CHANGED:
				int sel = jreCombo.getSelectionIndex();
				if( sel != -1 )
					selectedVM = (IVMInstall) installedJREs.get(sel);
				break;
			case CONFIG_CHANGED:
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
		if( getJbossServerFolder(nameText.getText()) != null && getJbossServerFolder(nameText.getText()).exists() ) {
			return Messages.serverNameInUse;
		}
		
		if ( !new File(homeDirText.getText()).exists()) {
			return Messages.invalidDirectory;
		}
		
		if( severity == SEVERITY_MAJOR ) return null;
		
		// now give minor warnings
		if( nameText.getText().trim().equals("")) 
			return Messages.nameTextBlank;

		if( homeDirText.getText().trim().equals("")) 
			return Messages.homeDirBlank;


		return null;

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
		IRuntimeWorkingCopy wc;
		if( r instanceof IRuntimeWorkingCopy ) 
			wc = (IRuntimeWorkingCopy)r;
		else
			wc = r.createWorkingCopy();
		
		if( wc instanceof RuntimeWorkingCopy ) {
			RuntimeWorkingCopy rwc = (RuntimeWorkingCopy)wc;
			homeDirText.setText(rwc.getLocation().toOSString());
			configText.setText(rwc.getAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, ""));
			
			
			String[] vmNames = jreCombo.getItems();
			IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(rwc.getAttribute(IJBossServerRuntime.PROPERTY_VM_TYPE_ID, ""));
			String vmId = rwc.getAttribute(IJBossServerRuntime.PROPERTY_VM_ID, "");
			
			IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();
			
			int comboIndex = -1;
			for (int i = 0; i < vmNames.length && comboIndex == -1; i++) {
				for( int j = 0; j < vmInstalls.length && comboIndex == -1; j++ ) {
//					ASDebug.p("comparing " + vmNames[i] + " with " + vmInstalls[j].getName(), this);
					if (vmNames[i].equals(vmInstalls[j].getName()) && vmInstalls[j].getId().equals(vmId))
						comboIndex = i;
				}
			}

			jreCombo.select(comboIndex);

		}
	}

	public void exit() {
		name = nameText.getText();
	}

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IServerWorkingCopy serverWC = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
		IRuntime r = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		runtime = (IJBossServerRuntime) r.loadAdapter(IJBossServerRuntime.class, null);


		
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
	}

	public boolean isComplete() {
		String s = getErrorString(SEVERITY_ALL);
		return s == null ? true : false;
	}

	public boolean hasComposite() {
		return true;
	}


	
	private IFolder getJbossServerFolder(String serverName) {
		try {
			return ServerType.getServerProject().getFolder(serverName);
		} catch( Exception e) {
			return null;
		}
	}

}
